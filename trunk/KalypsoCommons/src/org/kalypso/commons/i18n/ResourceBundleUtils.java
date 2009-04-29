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
package org.kalypso.commons.i18n;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.KalypsoCommonsDebug;

/**
 * Helper class for {@link java.util.ResourceBundle}
 * 
 * @author Gernot Belger
 */
public final class ResourceBundleUtils
{
  private ResourceBundleUtils( )
  {
    throw new UnsupportedOperationException( "Do not instantiate this helper class" );
  }

  /**
   * @param baseURL
   *          Base name for which to try to load the resource bundle.<br>
   *          If <code>baseUrl</code> is something like <code>http://somehost/myfile.txt</code>, we try to load
   *          properties like <code>http://somehost/myfile.properties</code>.<br>
   *          Urls with query part or anchor are not supported.
   */
  public static ResourceBundle loadResourceBundle( final URL baseURL )
  {
    try
    {
      // Unfinished: this does probably does not cover all cases...
      final URL _baseURL = extractBaseUrl( baseURL );
      final String path = baseURL.getPath();
      final String baseName = FilenameUtils.getBaseName( path );
      
      // REMARK: the trick here is to use the special class loader, that just links back to the given url.
      // This allows us to use the full functionality of the ResourceBundle#getBundle implementation.
      final ClassLoader loader = new ClassLoader()
      {
        /**
         * @see java.lang.ClassLoader#findResource(java.lang.String)
         */
        @Override
        protected URL findResource( final String name )
        {
          try
          {
            return new URL( _baseURL, name );
          }
          catch( final MalformedURLException e )
          {
            e.printStackTrace();
            return null;
          }
        }
      };
      return ResourceBundle.getBundle( baseName, Locale.getDefault(), loader );
    }
    catch( final MissingResourceException e )
    {
      KalypsoCommonsDebug.DEBUG_I18N.printf( IStatus.WARNING, "No resource bundle found for: %s%n", baseURL );
      return null;
    }
    catch( final MalformedURLException e )
    {
      KalypsoCommonsDebug.DEBUG_I18N.printf( IStatus.WARNING, "Could not load resource bundle found for: %s (%s)%n", baseURL, e.toString() );
      return null;
    }
  }
  
  private static URL extractBaseUrl( final URL location ) throws MalformedURLException
  {
    final String externalForm = location.toExternalForm();
    final int index = externalForm.lastIndexOf( '/' );
    if( index == -1 )
      return null;

    return new URL( externalForm.substring( 0, index + 1 ) );
  }

  /**
   * Returns an translated string for a given key, similar to i18n of the plugin.xml file.<br>
   * The string is translated according to the given properties, if it start with '%'. If not, the original value is
   * returned.<br>
   * If the given string starts with '%', but the bundle does not contain a corresponding entry, the original string is
   * returned.
   * 
   * @param translatableString
   *          If <code>null</code>, <code>null</code> is returned.
   * @param resourceBundle
   *          If <code>null</code>, we always return <code>translatableString</code>.
   * @return If <code>translatableString</code> starts with '%',
   *         <code>resourceBundle.getString( translatableString.substring(1) )</code> is returned. Else return
   *         <code>translatableString</code>.
   */
  public static String getI18NString( final String translatableString, final ResourceBundle resourceBundle )
  {
    if( translatableString == null )
      return null;

    if( translatableString.isEmpty() )
      return translatableString;

    if( resourceBundle == null )
      return translatableString;

    if( translatableString.charAt( 0 ) == '%' )
    {
      try
      {
        final String key = translatableString.substring( 1 );
        final String string = resourceBundle.getString( key );
        if( string != null && !string.isEmpty() )
          return string;
      }
      catch( final MissingResourceException e )
      {
        KalypsoCommonsDebug.DEBUG_I18N.printf( IStatus.WARNING, "No translation found for: %s%n", translatableString );
      }
    }

    return translatableString;
  }

  /**
   * Returns <code>bundle.getString(key)</code>, silently returning <code>null</code>, if key is not known.
   */
  public static String getStringQuiet( final ResourceBundle bundle, final String key )
  {
    try
    {
      if( bundle == null )
        return null;
      
      return bundle.getString( key );
    }
    catch( final MissingResourceException e )
    {
      // ignore
      return null;
    }
  }

}
