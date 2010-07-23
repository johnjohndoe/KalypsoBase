package org.kalypso.referencing.factory.dwd;

import java.net.URL;

import org.geotools.factory.Hints;
import org.geotools.referencing.factory.epsg.FactoryUsingWKT;

public class DwdExtension extends FactoryUsingWKT
{
  /**
   * The default filename to read. This file will be searched in the {@code org/kalypso/referencing/factory/dwd}
   * directory in the classpath or in a JAR file.
   * 
   * @see #getDefinitionsURL
   */
  public static final String FILENAME = "dwd.properties";

  /**
   * Constructs an authority factory using the default set of factories.
   */
  public DwdExtension( )
  {
    this( null );
  }

  /**
   * Constructs an authority factory using a set of factories created from the specified hints. This constructor
   * recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM} and
   * {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM} {@code FACTORY} hints.
   */
  public DwdExtension( final Hints hints )
  {
    super( hints, DEFAULT_PRIORITY - 2 );
  }

  /**
   * Returns the URL to the property file that contains CRS definitions. The default implementation returns the URL to
   * the {@value #FILENAME} file.
   * 
   * @return The URL, or {@code null} if none.
   */
  @Override
  protected URL getDefinitionsURL( )
  {
    return DwdExtension.class.getResource( FILENAME );
  }
}