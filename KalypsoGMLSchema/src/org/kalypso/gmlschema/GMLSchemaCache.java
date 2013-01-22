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

import java.net.URL;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.gmlschema.i18n.Messages;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Cached GMLSchemata zweistufig. Zuerst wird das eigentliche Schema (aus einer URL) lokal in einem File-Cache
 * gespeichert und dann zusätzlich noch im Speicher gehalten.
 * 
 * @author schlienger
 */
public class GMLSchemaCache
{
  private final static int CHECK_MODIFIED_INTERVAL = 30; // 30 seconds

  private final LoadingCache<URL, Date> m_lastModifiedCache;

  private Cache<String, GMLSchemaWrapper> m_schemaCache;

  public GMLSchemaCache( )
  {
    /* avoids too many (costly) checks for 'lastModifed' of a URL. */

    // REMARK: Only really check the lastModifiedStamp, if some time has passed
    // This is a MAJOR performance bugfix for the GML-Parser, as for every feature, the GML-Schema
    // was looked-up, causing MANY file accesses...

    final CacheLoader<URL, Date> lastModifedLoader = new CacheLoader<URL, Date>()
    {
      @Override
      public Date load( final URL resource ) throws Exception
      {
        Debug.CACHE.printf( "Fetching lastModified for %s%n", resource ); //$NON-NLS-1$
        final Date lastModified = UrlUtilities.lastModified( resource );
        if( lastModified == null )
        {
          // REMARK: using fixed date here, if we cannot determine the real last modified date in order to avoid to many reloads.
          // FIXME: improve UrlUtilities.lastModified
          return new Date( 0 );
        }

//        Debug.CACHE.printf( "Last modified is %s%n", lastModified ); //$NON-NLS-1$
        return lastModified;
      }
    };
    m_lastModifiedCache = CacheBuilder.newBuilder().concurrencyLevel( 1 ).expireAfterWrite( CHECK_MODIFIED_INTERVAL, TimeUnit.SECONDS ).build( lastModifedLoader );

    /* cache for schemas */
    m_schemaCache = CacheBuilder.newBuilder().concurrencyLevel( 1 ).build();
//    m_schemaCache = CacheBuilder.newBuilder().concurrencyLevel( 1 ).weakValues().build();

    Debug.CACHE.printf( "Schema cache initialized%n" ); //$NON-NLS-1$
  }

  /**
   * Schreibt ein schema in diesen Cache.
   */
  public synchronized void addSchema( final String namespace, final GMLSchema schema, final Date validity, final String gmlVersion )
  {
    final String publicId = namespace + "#" + gmlVersion; //$NON-NLS-1$

    Debug.CACHE.printf( "Manually adding schema to cache: %s%n", publicId ); //$NON-NLS-1$

    m_schemaCache.put( publicId, new GMLSchemaWrapper( schema, validity ) );
  }

  /**
   * Lädt das Schmea aus dieser URL und nimmt diese id für den cache
   * 
   * @param namespace
   *          ID für den Cache, wenn null, wird die id anhand des geladenen schemas ermittelt
   */
  public synchronized GMLSchema getSchema( final String namespace, final String gmlVersion, final URL schemaURL ) throws ExecutionException
  {
//    Debug.CACHE.printf( "GML-Schema cache lookup: %s, %s, %s%n", namespace, gmlVersion, schemaURL ); //$NON-NLS-1$

    Assert.isNotNull( namespace );

    final String publicId = namespace + "#" + gmlVersion; //$NON-NLS-1$

    if( gmlVersion != null && !"3.1.1".equals( gmlVersion ) && !"2.1.2".equals( gmlVersion ) ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      // TODO: put this into a tracing option or log to plug-in log.
      System.out.println( "Unknown gml version: " + gmlVersion + " for namespace: " + namespace ); //$NON-NLS-1$ //$NON-NLS-2$
      System.out.println( "Use appinfo and use one of the known (3.1.1 or 2.1.2) in order to avoid multiple schema parsing." ); //$NON-NLS-1$
    }

    // PROBLEM: we have a problem with imported schematas here.
    // If an imported schema is updated, it will not be reloaded if
    // the importing schema is still in memory.
    // TODO: maybe create a validity from all imported schematas as well?
    final Date validity = m_lastModifiedCache.get( schemaURL );

    /* fetch schema from cache */
    final Callable<GMLSchemaWrapper> loader = new Callable<GMLSchemaWrapper>()
    {
      @Override
      public GMLSchemaWrapper call( ) throws Exception
      {
        if( schemaURL == null )
          throw new GMLSchemaException( Messages.getString( "org.kalypso.gmlschema.GMLSchemaCache.0", namespace ) ); //$NON-NLS-1$

        Debug.CACHE.printf( "Loading schema: %s from %s%n", publicId, schemaURL );

        final GMLSchema schema = GMLSchemaFactory.createGMLSchema( gmlVersion, schemaURL );
        return new GMLSchemaWrapper( schema, validity );
      }
    };

    final GMLSchemaWrapper sw = m_schemaCache.get( publicId, loader );

    // We have an already loaded schema; use it, if the current validity is same (or older) the the last one
    // or if either the old or the current validity could not be determined
//    Debug.CACHE.printf( "Checking for modification of: %s%n", schemaURL ); //$NON-NLS-1$
    final Date lastValidity = sw.getValidity();
    if( validity == null || lastValidity != null && validity.compareTo( lastValidity ) <= 0 )
    {
//      Debug.CACHE.printf( "Schema found in mem-cache.%n" ); //$NON-NLS-1$
      return sw.getSchema();
    }
    else
    {
      // reload
      Debug.CACHE.printf( "Invalidating schema %s%n", publicId ); //$NON-NLS-1$
      m_schemaCache.invalidate( publicId );
      final GMLSchemaWrapper newWrapper = m_schemaCache.get( publicId, loader );
      return newWrapper.getSchema();
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
    m_schemaCache.invalidateAll();
  }
}