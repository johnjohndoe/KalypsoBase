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
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.common.JarHelper;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.cache.FileCache;
import org.kalypso.commons.cache.StringValidityKey;
import org.kalypso.commons.cache.StringValidityKeyFactory;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.commons.serializer.ISerializer;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
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
  private final static int TIMEOUT = Integer.MAX_VALUE;

  private final static int SIZE = 30;

  private final Cache m_memCache;

  private final FileCache<StringValidityKey, GMLSchema> m_fileCache;

  public GMLSchemaCache( final File cacheDirectory )
  {
    Debug.CATALOG.printf( "Schema cache initialized" );

    m_memCache = new LfuCacheFactory().newInstance( "gml.schemas", TIMEOUT, SIZE );
    m_fileCache = new FileCache<StringValidityKey, GMLSchema>( new StringValidityKeyFactory(), StringValidityKey.createComparatorForStringCompareOnly(), new GMLSchemaSerializer(), cacheDirectory );
  }

  /**
   * Schreibt ein schema in diesen Cache.
   */
  public void addSchema( final String namespace, final GMLSchemaWrapper schemaWrapper )
  {
    final String version = schemaWrapper.getSchema().getGMLVersion();
    final String publicId = namespace + "#" + version;

    Debug.CATALOG.printf( "Adding schema to cache: %s", publicId );

    m_memCache.addObject( publicId, schemaWrapper );
    m_fileCache.addObject( new StringValidityKey( publicId, schemaWrapper.getValidity() ), schemaWrapper.getSchema() );
  }

  /**
   * Lädt das Schmea aus dieser URL und nimmt diese id für den cache
   * 
   * @param namespace
   *            ID für den Cache, wenn null, wird die id anhand des geladenen schemas ermittelt
   */
  public GMLSchema getSchema( final String namespace, final String gmlVersion, final URL schemaURL ) throws InvocationTargetException
  {
    Debug.CATALOG.printf( "GML-Schema cache lookup: %s, %s, %s%n", namespace, gmlVersion, schemaURL );

    Assert.isNotNull( namespace );

    final String publicId = namespace + "#" + gmlVersion;

    //
    if( gmlVersion != null && !"3.1.1".equals( gmlVersion ) && !"2.1.2".equals( gmlVersion ) )
    {
    // TODO: put this into a tracing optio ord log to plug-in log.
      System.out.println( "Unknown gml version: " + gmlVersion + " for namespace: " + namespace );
      System.out.println( "Use appinfo and use one of the known (3.1.1 or 2.1.2) in order to avoid multiple schema parsing." );
    }
    //

    Date validity = null;
    try
    {
      if( schemaURL != null )
        validity = new Date( lastModified( schemaURL ) );
    }
    catch( final IOException e )
    {
      // ignorieren, dann immer die lokale Kopie nehmen
      // e.printStackTrace();
    }

    // PROBLEM: we have a problem with imported schematas here.
    // If an imported schema is updated, it will not be reloaded if
    // the importing schema is still in memory.
    // TODO: maybe create a validity from all imported schematas as well?

    // if objekt already in memCache and is valid, just return it
    final GMLSchemaWrapper sw = (GMLSchemaWrapper) m_memCache.getObject( publicId );
    if( sw != null && sw.getValidity() != null && (validity == null || validity.compareTo( sw.getValidity() ) <= 0) )
    {
      Debug.CATALOG.printf( "Schema found in mem-cache.%n" );

      return sw.getSchema();
    }

    // else, try to get it from file cache
    GMLSchema schema = null;

    final StringValidityKey key = new StringValidityKey( publicId, validity );
    final StringValidityKey realKey = m_fileCache.getRealKey( key );
    final Date realValidity = realKey == null ? null : realKey.getValidity();

    if( validity != null && (realKey == null || realValidity.before( validity )) )
    {
      if( realValidity == null )
        Debug.CATALOG.printf( "Schema not found in file cache, loading it from: %s%n", schemaURL );
      else
        Debug.CATALOG.printf( "Schema no more valid. Schema: %s - Cache: %s", validity, realValidity );

      // cache is not not valid any more, load from url

      // if we have no url, we cant do anything
      if( schemaURL == null )
        return null;

      File tmpFile = null;
      File archiveDir = null;
      try
      {
        tmpFile = File.createTempFile( "tempSchemaCacheFile", ".zip" );

        archiveDir = FileUtilities.createNewTempDir( "kalypsoSchemaZip" );

        final SchemaDocument schemaDocument = SchemaDocument.Factory.parse( schemaURL );

        GMLSchemaUtilities.createSchemaDir( schemaURL, schemaDocument, archiveDir );
        FileUtils.writeStringToFile( new File( archiveDir, ".version" ), gmlVersion, "UTF-8" );

        final JarHelper helper = new JarHelper();
        helper.jarDir( archiveDir, tmpFile );

        FileUtilities.deleteRecursive( archiveDir );

        // put it into cache as file
        m_fileCache.addFile( key, tmpFile );

        Debug.CATALOG.printf( "Schema was put into file cache.%n" );
      }
      catch( final Exception e )
      {
        Debug.CATALOG.printf( IStatus.WARNING, "Fehler beim Laden von Schema aus URL: %s%nEs wird versucht die lokale Kopie zu laden.%n", schemaURL );
        Debug.CATALOG.printStackTrace( IStatus.WARNING, e );
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoGMLSchemaPlugin.getDefault().getLog().log( status );
      }
      finally
      {
        if( tmpFile != null )
          tmpFile.delete();

        FileUtilities.deleteRecursive( archiveDir );
      }
    }
    else
    {
      Debug.CATALOG.printf( "Schema found in file cache, start loading%n" );
    }

    // falls noch valid oder laden hat nicht geklappt: aus dem File-Cache
    if( schema == null )
      schema = m_fileCache.getObject( key );

    if( schema != null )
    {
      m_memCache.addObject( publicId, new GMLSchemaWrapper( schema, validity ) );

      Debug.CATALOG.printf( "Schema successfully looked-up: %s%n%n", namespace );
    }
    else
      Debug.CATALOG.printf( "Schema was not loaded/looked-up: %s%n%n", namespace );

    return schema;
  }

  private long lastModified( final URL schemaURL ) throws IOException
  {
    final URLConnection connection = schemaURL.openConnection();
    connection.connect();

    final long lastModified = connection.getLastModified();
    // BUGFIX: some URLConnection implementations (such as eclipse resource-protokoll)
    // do not return lastModified correctly. If we have such a case, we try some more...
    if( lastModified != 0 )
      return lastModified;
    else
    {
      final IPath path = ResourceUtilities.findPathFromURL( schemaURL );
      if( path == null )
        return 0;

      final File file = ResourceUtilities.makeFileFromPath( path );
      return file.lastModified();
    }
  }

  protected static class GMLSchemaSerializer implements ISerializer<GMLSchema>
  {
    public GMLSchema read( final InputStream ins ) throws InvocationTargetException, IOException
    {
      final OutputStream fos = null;
      File tmpDir = null;
      try
      {
        tmpDir = FileUtilities.createNewTempDir( "tmpSchemaCacheDir_unzipped" );

        // we unzip because of the problem of deleting zip files, once accessed via a zip-url-stream
        ZipUtilities.unzip( ins, tmpDir );

        final File versionFile = new File( tmpDir, ".version" );
        final String gmlVersion;
        if( versionFile.exists() )
        {
          final String content = FileUtils.readFileToString( versionFile, "UTF-8" );
          gmlVersion = content.trim().length() == 0 ? null : content.trim();
        }
        else
          gmlVersion = null;

        final URL url = tmpDir.toURL();
        final URL schemaURL = new URL( url, GMLSchemaUtilities.BASE_SCHEMA_IN_JAR );

        return GMLSchemaFactory.createGMLSchema( gmlVersion, schemaURL );
      }
      catch( final GMLSchemaException e )
      {
        throw new InvocationTargetException( e );
      }
      finally
      {
        IOUtils.closeQuietly( ins );
        IOUtils.closeQuietly( fos );
        FileUtilities.deleteRecursive( tmpDir );
      }
    }

    public void write( final GMLSchema schema, final OutputStream os ) throws InvocationTargetException, IOException
    {
      // write schema into tmp dir
      // TODO: check if this really works with included schemata
      final File archiveDir = FileUtilities.createNewTempDir( "gmlSerializerTmpDir" );
      try
      {
        GMLSchemaUtilities.createSchemaDir( schema.getContext(), schema.getSchema(), archiveDir );
        FileUtils.writeStringToFile( new File( archiveDir, ".version" ), schema.getGMLVersion(), "UTF-8" );
        ZipUtilities.zip( new ZipOutputStream( os ), archiveDir );
      }
      catch( final XmlException e )
      {
        throw new InvocationTargetException( e );
      }
      finally
      {
        FileUtilities.deleteRecursive( archiveDir );
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

  /**
   * Clears the cache. Schematas will be reloaded after this operation.
   * 
   * @param onlyMemoryCache
   *            If true, only the memory cache is cleared. Else, file and memory cache are cleared.
   */
  public void clearCache( final boolean onlyMemoryCache )
  {
    m_memCache.clear();
    if( !onlyMemoryCache )
      m_fileCache.clear();
  }
}