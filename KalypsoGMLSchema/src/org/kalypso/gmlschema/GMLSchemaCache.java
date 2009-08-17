/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.gmlschema;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.shiftone.cache.Cache;
import org.shiftone.cache.policy.lfu.LfuCacheFactory;

/**
 * Cached GMLSchemata zweistufig. Zuerst wird das eigentliche Schema (aus einer URL) lokal in einem File-Cache
 * gespeichert und dann zusätzlich noch im Speicher gehalten.
 *
 * @author schlienger
 */
public class GMLSchemaCache
{
  /** Timespan that must pass in order to check again for modification of the schema resource */
  private final static int CHECK_MODIFIED_INTERVAL = 1000 * 30; // 30 seconds

  private final static int TIMEOUT = Integer.MAX_VALUE;

  private final static int SIZE = 30;

  private final Cache m_memCache;

  public GMLSchemaCache( )
  {
    Debug.CATALOG.printf( "Schema cache initialized" );
    m_memCache = new LfuCacheFactory().newInstance( "gml.schemas", TIMEOUT, SIZE );
  }

  /**
   * Schreibt ein schema in diesen Cache.
   */
  public synchronized void addSchema( final String namespace, final GMLSchema schema, final Date validity, final long lastModifiedCheck )
  {
    final String version = schema.getGMLVersion();
    final String publicId = namespace + "#" + version;

    Debug.CATALOG.printf( "Adding schema to cache: %s", publicId );

    m_memCache.addObject( publicId, new GMLSchemaWrapper( schema, validity, lastModifiedCheck ) );
  }

  /**
   * Lädt das Schmea aus dieser URL und nimmt diese id für den cache
   *
   * @param namespace
   *          ID für den Cache, wenn null, wird die id anhand des geladenen schemas ermittelt
   */
  public synchronized GMLSchema getSchema( final String namespace, final String gmlVersion, final URL schemaURL ) throws InvocationTargetException
  {
    if( schemaURL == null )
      throw new InvocationTargetException( new GMLSchemaException( "Unable to load schema, unknown namespace: " + namespace ) );

    Debug.CATALOG.printf( "GML-Schema cache lookup: %s, %s, %s%n", namespace, gmlVersion, schemaURL );

    Assert.isNotNull( namespace );

    final String publicId = namespace + "#" + gmlVersion;

    if( gmlVersion != null && !"3.1.1".equals( gmlVersion ) && !"2.1.2".equals( gmlVersion ) )
    {
      // TODO: put this into a tracing option or log to plug-in log.
      System.out.println( "Unknown gml version: " + gmlVersion + " for namespace: " + namespace );
      System.out.println( "Use appinfo and use one of the known (3.1.1 or 2.1.2) in order to avoid multiple schema parsing." );
    }

    // PROBLEM: we have a problem with imported schematas here.
    // If an imported schema is updated, it will not be reloaded if
    // the importing schema is still in memory.
    // TODO: maybe create a validity from all imported schematas as well?

    // if object already in memCache and is valid, just return it
    final GMLSchemaWrapper sw = (GMLSchemaWrapper) m_memCache.getObject( publicId );

    final long currentMillis = System.currentTimeMillis();

    if( sw != null )
    {
      // REMARK: Only really check the lastModifiedStamp, if some time has passed
      // This is a MAJOR performance bugfix for the GML-Parser, as for every feature, the GML-Schema
      // was looked-up, causing MANY file accesses...
      final boolean doCheckModified = currentMillis - sw.getLastModifiedCheck() > CHECK_MODIFIED_INTERVAL;

      final Date validity;
      if( doCheckModified )
      {
        Debug.CATALOG.printf( "Check for modification of: %s%n", schemaURL );
        validity = lastModified( schemaURL );
        sw.setLastModifiedCheck( currentMillis );
      }
      else
      {
        validity = null;
      }

      // We have an already loaded schema; use it, if the current validity is same (or older) the the last one
      // or if either the old or the current validity could not be determined
      final Date lastValidity = sw.getValidity();
      if( validity == null || (lastValidity != null && validity.compareTo( lastValidity ) <= 0) )
      {
        Debug.CATALOG.printf( "Schema found in mem-cache.%n" );
        return sw.getSchema();
      }
    }

    try
    {
      final GMLSchema schema = GMLSchemaFactory.createGMLSchema( gmlVersion, schemaURL );
      final Date validity = lastModified( schemaURL );
      m_memCache.addObject( publicId, new GMLSchemaWrapper( schema, validity, currentMillis ) );

      Debug.CATALOG.printf( "Schema successfully looked-up: %s%n%n", namespace );
      return schema;
    }
    catch( final GMLSchemaException e )
    {
      Debug.CATALOG.printf( "Schema was not loaded/looked-up: %s%n%n", namespace );
      throw new InvocationTargetException( e );
    }

  }

  private Date lastModified( final URL schemaURL )
  {
    if( schemaURL == null )
      return null;

    try
    {
      final URLConnection connection = schemaURL.openConnection();
      connection.connect();

      final long lastModified = connection.getLastModified();
      // BUGFIX: some URLConnection implementations (such as eclipse resource-protokoll)
      // do not return lastModified correctly. If we have such a case, we try some more...
      if( lastModified != 0 )
        return new Date( lastModified );

      final IPath path = ResourceUtilities.findPathFromURL( schemaURL );
      if( path == null )
        return null;

      final File file = ResourceUtilities.makeFileFromPath( path );
      return new Date( file.lastModified() );
    }
    catch( final IOException e )
    {
      // ignore, some resources cannot be checked at all
    }

    return null;
  }

  private static class GMLSchemaWrapper
  {
    private final GMLSchema m_schema;

    private final Date m_validity;

    /**
     * Timestamp (as from System.currentMillis), when the last check for modification of the underlying resource was
     * made.
     */
    private long m_lastModifiedCheck;

    public GMLSchemaWrapper( final GMLSchema schema, final Date validity, final long lastModifiedCheck )
    {
      m_schema = schema;
      m_validity = validity;
      m_lastModifiedCheck = lastModifiedCheck;
    }

    public long getLastModifiedCheck( )
    {
      return m_lastModifiedCheck;
    }

    public void setLastModifiedCheck( final long lastModifiedCheck )
    {
      m_lastModifiedCheck = lastModifiedCheck;
    }

    public GMLSchema getSchema( )
    {
      return m_schema;
    }

    public Date getValidity( )
    {
      return m_validity;
    }
  }

  /**
   * Clears the cache. Schematas will be reloaded after this operation.
   *
   * @param onlyMemoryCache
   *          If true, only the memory cache is cleared. Else, file and memory cache are cleared.
   */
  public synchronized void clearCache( )
  {
    m_memCache.clear();
  }
}