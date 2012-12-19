/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.geotools.referencing.factory.custom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.geotools.Activator;
import org.geotools.GeoToolsExtensions;
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.epsg.FactoryUsingWKT;

/**
 * Provides common {@linkplain CoordinateReferenceSystem Coordinate Reference
 * Systems} not found in the standard EPSG database. Those CRS will be
 * registered in {@code "EPSG"} name space.
 * 
 * @author Holger Albert
 */
public class CustomCRSExtension extends FactoryUsingWKT
{
  /**
   * The definitions URL.
   */
  private URL m_definitionsURL;

  /**
   * Constructs an authority factory using the default set of factories.
   */
  public CustomCRSExtension( )
  {
    this( null );
  }

  /**
   * Constructs an authority factory using a set of factories created from the
   * specified hints. This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY
   * MATH_TRANSFORM} {@code FACTORY} hints.
   */
  public CustomCRSExtension( final Hints factoryHints )
  {
    super( factoryHints, DEFAULT_PRIORITY - 2 );

    m_definitionsURL = null;
  }

  /**
   * Returns the URL to the property file that contains CRS definitions. The
   * default implementation returns the URL to the {@value #FILENAME} file.
   * 
   * @return The URL, or {@code null} if none.
   */
  @Override
  protected URL getDefinitionsURL( )
  {
    try
    {
      if( m_definitionsURL != null )
        return m_definitionsURL;

      Properties mergedProperties = new Properties();

      ICustomCRSProvider[] allProvider = GeoToolsExtensions.createCustomCRSProvider();
      for( ICustomCRSProvider oneProvider : allProvider )
      {
        URL propertyURL = oneProvider.getURL();
        Properties loadedProperties = load( propertyURL );
        merge( mergedProperties, loadedProperties );
      }

      File stateLocation = Activator.getDefault().getStateLocation().toFile();
      File propertyFile = new File( stateLocation, "resources/crs/crs.properties" ); //$NON-NLS-1$
      propertyFile.getParentFile().mkdirs();

      save( propertyFile, mergedProperties );

      m_definitionsURL = propertyFile.toURI().toURL();

      return m_definitionsURL;
    }
    catch( CoreException | IOException ex )
    {
      ex.printStackTrace();
      return null;
    }
  }

  private Properties load( final URL propertyURL ) throws IOException
  {
    InputStream inputStream = null;

    try
    {
      final Properties properties = new Properties();

      inputStream = new BufferedInputStream( propertyURL.openStream() );
      properties.load( inputStream );

      return properties;
    }
    finally
    {
      closeStream( inputStream );
    }
  }

  private void save( final File propertyFile, final Properties properties ) throws IOException
  {
    OutputStream outputStream = null;

    try
    {
      outputStream = new BufferedOutputStream( new FileOutputStream( propertyFile ) );
      properties.store( outputStream, null );
    }
    finally
    {
      closeStream( outputStream );
    }
  }

  private void merge( final Properties base, final Properties extension )
  {
    final Set<Entry<Object, Object>> entries = extension.entrySet();
    for( final Entry<Object, Object> entry : entries )
    {
      final String property = base.getProperty( entry.getKey().toString() );
      if( property == null || property.length() == 0 )
        base.setProperty( entry.getKey().toString(), entry.getValue().toString() );
    }
  }

  private void closeStream( Closeable stream )
  {
    try
    {
      if( stream != null )
        stream.close();
    }
    catch( IOException ex )
    {
      ex.printStackTrace();
    }
  }
}