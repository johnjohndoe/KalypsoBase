/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
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
package org.kalypso.contribs.java.i18n;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class for I81N stuff.
 *
 * @author Gernot Belger
 */
public final class I18nUtils
{
  private I18nUtils( )
  {
    throw new IllegalStateException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * Searches a file based on the current locale.<br>
   * The first of the following files that exist will be taken:
   * <ul>
   * <li><code>baseName_language_country_variantsuffix</code></li>
   * <li><code>baseName_language_countrysuffix</code></li>
   * <li><code>baseName_languagesuffix</code></li>
   * <li><code>baseNamesuffixSuffix</code></li>
   * </ul>
   * Where lanugage, country and variant correspond to the current language settings.
   *
   * @return The first suitable file, or <code>null</code>, if nothing could be found.
   */
  public static File getLocaleFile( final File dir, final String baseName, final String suffix )
  {
    final Locale locale = Locale.getDefault();
    final String[] keys = generateKeys( locale, baseName );
    for( final String key : keys )
    {
      final String filename = key + suffix;
      final File file = new File( dir, filename );
      if( file.isFile() )
        return file;

    }

    return null;
  }

  /**
   * Similar as {@link Class#getResource(String)}, but tries different versions depending on the current locale.<br>
   * Tries to access
   * <ul>
   * <li><code>resourceBaseName_language_country_variantresourceSuffix</code></li>
   * <li><code>resourceBaseName_language_countryresourceSuffix</code></li>
   * <li><code>resourceBaseName_languageresourceSuffix</code></li>
   * <li><code>resourceBaseNameresourceSuffix</code></li>
   * </ul>
   * Where lanugage, country and variant correspond to the current language settings.
   *
   * @see Class#getResource(String)
   */
  public static URL getLocaleResource( final Class< ? > clazz, final String resourceBaseName, final String resourceSuffix )
  {
    final Locale locale = Locale.getDefault();

    final String[] names = generateKeys( locale, resourceBaseName );
    for( final String name : names )
    {
      final URL location = clazz.getResource( name + resourceSuffix );
      if( location != null )
        return location;
    }

    return null;
  }

  /**
   * Similar as {@link Class#getResourceAsStream(String)}, but tries different versions depending on the current locale.<br>
   * Tries to access
   * <ul>
   * <li><code>resourceBaseName_language_country_variantresourceSuffix</code></li>
   * <li><code>resourceBaseName_language_countryresourceSuffix</code></li>
   * <li><code>resourceBaseName_languageresourceSuffix</code></li>
   * <li><code>resourceBaseNameresourceSuffix</code></li>
   * </ul>
   * Where lanugage, country and variant correspond to the current language settings.<br>
   *
   * @return An open stream. Must be closed by the caller.
   * @see Class#getResourceAsStream(String)
   */
  public static InputStream getLocaleResourceAsStream( final Class< ? > clazz, final String resourceBaseName, final String resourceSuffix )
  {
    final Locale locale = Locale.getDefault();

    final String[] names = generateKeys( locale, resourceBaseName );
    for( final String name : names )
    {
      final InputStream is = clazz.getResourceAsStream( name + resourceSuffix );
      if( is != null )
        return is;
    }

    return null;
  }

  public static String[] generateKeys( final Locale locale, final String baseName )
  {
    final List<String> keys = new ArrayList<String>( 4 );

    final String language = locale.getLanguage();
    final String country = locale.getCountry();
    final String variant = locale.getVariant();

    keys.add( baseName );
    if( language.length() > 0 )
    {
      keys.add( String.format( "%s_%s", baseName, language ) ); //$NON-NLS-1$
      if( country.length() > 0 )
      {
        keys.add( String.format( "%s_%s_%s", baseName, language, country ) ); //$NON-NLS-1$
        if( variant.length() > 0 )
          keys.add( String.format( "%s_%s_%s_%s", baseName, language, country, variant ) ); //$NON-NLS-1$
      }
    }

    Collections.reverse( keys );

    return keys.toArray( new String[keys.size()] );
  }

  /**
   * Formats a message from a resource bundle. for the given key.<br/>
   * This is the common method that should be used for all Message-classes.
   */
  public static String formatMessage( final ResourceBundle resourceBundle, final String key, final Object[] args )
  {
    try
    {
      final String formatStr = resourceBundle.getString( key );
      if( args.length == 0 )
        return formatStr;

      try
      {
        return String.format( formatStr, args );
      }
      catch( final IllegalFormatException e )
      {
        e.printStackTrace();
        return '!' + formatStr + '!';
      }
    }
    catch( final MissingResourceException e )
    {
      return '!' + key + '!';
    }
  }
}
