/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.contribs.java.i18n;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for I81N stuff.
 * 
 * @author Gernot Belger
 */
public class I18nUtils
{
  private I18nUtils( )
  {
    throw new IllegalStateException( "Helper class, do not instantiate" ); //$NON-NLS-1$
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
    for( String name : names )
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
    for( String name : names )
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

}
