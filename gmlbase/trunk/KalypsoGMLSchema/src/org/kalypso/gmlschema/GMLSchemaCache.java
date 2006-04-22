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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.kalypso.commons.cache.FileCache;
import org.kalypso.commons.cache.StringValidityKey;
import org.kalypso.commons.cache.StringValidityKeyFactory;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.commons.serializer.ISerializer;
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
  protected final static Logger LOGGER = Logger.getLogger( GMLSchemaCache.class.getName() );

  private final static int TIMEOUT = Integer.MAX_VALUE;

  private final static int SIZE = 30;

  private final Cache m_memCache;

  private final FileCache<StringValidityKey, GMLSchema> m_fileCache;

  private File m_baseTmpDir;

  public GMLSchemaCache( final File cacheDirectory, final File tmpDir )
  {
    m_baseTmpDir = tmpDir;
    
    m_memCache = new LfuCacheFactory().newInstance( "gml.schemas", TIMEOUT, SIZE );
    m_fileCache = new FileCache<StringValidityKey, GMLSchema>( new StringValidityKeyFactory(), StringValidityKey.createComparatorForStringCompareOnly(), new GMLSchemaSerializer( tmpDir ), cacheDirectory );
  }

  /**
   * Schreibt ein schema in diesen Cache.
   */
  public void addSchema( final String namespace, final GMLSchemaWrapper schemaWrapper )
  {
    m_memCache.addObject( namespace, schemaWrapper );
    m_fileCache.addObject( new StringValidityKey( namespace, schemaWrapper.getValidity() ), schemaWrapper.getSchema() );
  }

  /**
   * Lädt das Schmea aus dieser URL und nimmt diese id für den cache
   * 
   * @param namespace
   *          ID für den Cache, wenn null, wird die id anhand des geladenen schemas ermittelt
   */
  public GMLSchema getSchema( final String namespace, final URL schemaURL )
  {
    if( namespace == null )
      throw new NullPointerException( "Namespace darf nicht null sein" );

    Date validity = null;
    try
    {
      if( schemaURL != null )
      {
        final URLConnection connection = schemaURL.openConnection();
        connection.connect();
        validity = new Date( connection.getLastModified() );
      }
    }
    catch( final IOException e )
    {
      // ignorieren, dann immer die lokale Kopie nehmen
      // e.printStackTrace();
    }

    // if objekt already in memCache and is valid, just return it
    final GMLSchemaWrapper sw = (GMLSchemaWrapper) m_memCache.getObject( namespace );
    if( sw != null && (validity == null || validity.compareTo( sw.getValidity() ) <= 0) )
      return sw.getSchema();

    // else, try to get it from file cache
    GMLSchema schema = null;

    final StringValidityKey key = new StringValidityKey( namespace, validity );
    final StringValidityKey realKey = m_fileCache.getRealKey( key );

    if( validity != null && (realKey == null || realKey.getValidity().before( validity )) )
    {
      // cache is not not valid any more, load from url

      // if we have no url, we cant do anything
      if( schemaURL == null )
        return null;

      File tmpFile = null;
      try
      {
        tmpFile = File.createTempFile( "tempSchemaCacheFile", ".zip", m_baseTmpDir );
        GMLSchemaUtilities.createSchemaArchive( schemaURL, tmpFile );
        // put it into cache as file
        m_fileCache.addFile( key, tmpFile );
      }
      catch( final Exception e )
      {
        LOGGER.log( Level.WARNING, "Fehler beim Laden von Schema aus URL: " + schemaURL + "\nEs wird versucht die lokale Kopie zu laden.", e );
      }
      finally
      {
        if( tmpFile != null )
          tmpFile.delete();
      }
    }

    // falls noch valid oder laden hat nicht geklappt: aus dem File-Cache
    if( schema == null )
      schema = m_fileCache.getObject( key );

    if( schema != null )
      m_memCache.addObject( namespace, new GMLSchemaWrapper( schema, validity ) );

    return schema;
  }

  private static class GMLSchemaSerializer implements ISerializer<GMLSchema>
  {
    private final File m_baseTmpDir;
    
    public GMLSchemaSerializer( final File baseTmpDir )
    {
      m_baseTmpDir = baseTmpDir;
    }
    
    public GMLSchema read( final InputStream ins ) throws InvocationTargetException
    {
      OutputStream fos = null;
      File tmpDir = null;
      try
      {
        tmpDir = FileUtilities.createNewTempDir( "tmpSchemaCacheDir_unzipped", m_baseTmpDir );

        // we unzip because of the problem of deleting zip files, once accessed via a zip-url-stream
        ZipUtilities.unzip( ins, tmpDir );

        final URL url = tmpDir.toURL();
        final URL schemaURL = new URL( url, GMLSchemaUtilities.BASE_SCHEMA_IN_JAR );

        return GMLSchemaFactory.createGMLSchema( schemaURL );
      }
      catch( final Exception e )
      {
        e.printStackTrace();

        throw new InvocationTargetException( e );
      }
      finally
      {
        IOUtils.closeQuietly( ins );
        IOUtils.closeQuietly( fos );
        FileUtilities.deleteRecursive( tmpDir );
      }
    }

    public void write( final GMLSchema schema, final OutputStream os ) throws InvocationTargetException
    {
      // write schema into tmp dir
      // TODO: check if this really works with included schemata
      final File archiveDir = FileUtilities.createNewTempDir( "gmlSerializerTmpDir", m_baseTmpDir );
      try
      {
        GMLSchemaUtilities.createSchemaDir( schema.getContext(), schema.getSchema(), archiveDir );
        
        ZipUtilities.zip( new ZipOutputStream( os ), archiveDir );
      }
      catch( final XmlException e )
      {
        throw new InvocationTargetException( e );
      }
      catch( final IOException e )
      {
        throw new InvocationTargetException( e );
      }
    }
  }

  public static class GMLSchemaWrapper
  {
    private final GMLSchema m_schema;

    private final Date m_validity;

    public GMLSchemaWrapper( final GMLSchema schema, final Date validity )
    {
      m_schema = schema;
      m_validity = validity;
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
}