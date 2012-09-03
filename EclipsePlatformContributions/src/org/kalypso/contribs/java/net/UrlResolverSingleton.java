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
package org.kalypso.contribs.java.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * UrlResolverSingleton
 * <p>
 * Helper class that simply holds a static reference to a UrlResolver. It additionally provides a static resolveUrl
 * method that forwards the call to the singleton instance in addition to checking for null-argument.
 * 
 * @author schlienger (24.05.2005)
 */
public final class UrlResolverSingleton
{
  private static IUrlResolver RESOLVER = null;

  private UrlResolverSingleton( )
  {
    // no instantiation
  }

  public static IUrlResolver getDefault( )
  {
    if( RESOLVER == null )
      RESOLVER = new UrlResolver();

    return RESOLVER;
  }

  public static URL resolveUrl( final URL baseURL, final String relativeURL ) throws MalformedURLException
  {
    if( baseURL == null )
      return new URL( relativeURL );

    return getDefault().resolveURL( baseURL, relativeURL );
  }

  /**
   * Goes through a property-list and resolves every key ending with 'URL' against the given context.
   * 
   * @param confUrl
   *          Context for resolving the urls
   * @param props
   *          Every property ending with 'URL' will be changed = URL( context, prop )
   * @throws MalformedURLException
   *           if any url is malformed
   */
  public static void resolveUrlProperties( final URL confUrl, final Properties props ) throws MalformedURLException
  {
    for( final Object object : props.keySet() )
    {
      final String key = (String) object;
      if( key.endsWith( "URL" ) ) //$NON-NLS-1$
      {
        final String path = props.getProperty( key );
        final URL resolved = resolveUrl( confUrl, path );
        final String fullPath = resolved.toExternalForm();

        props.setProperty( key, fullPath );
      }
    }
  }
}
