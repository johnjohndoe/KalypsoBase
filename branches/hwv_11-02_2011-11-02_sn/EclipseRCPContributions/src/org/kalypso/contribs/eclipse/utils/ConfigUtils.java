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
package org.kalypso.contribs.eclipse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;

/**
 * @author Holger Albert
 * @author Gernot Belger
 */
public final class ConfigUtils
{
  private static URL FALLBACK_LOCATION;

  private ConfigUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  public static URL findCentralConfigLocation( final String path ) throws IOException
  {
    final Location configurationLocation = Platform.getConfigurationLocation();

    try
    {
      return checkConfigLocation( configurationLocation, path );
    }
    catch( final IOException e )
    {
      try
      {
        // ignore exception for now, second try
        final Location baseConfigurationLocation = configurationLocation.getParentLocation();
        return checkConfigLocation( baseConfigurationLocation, path );
      }
      catch( final IOException e2 )
      {
        // ignore exception for now, third try
        try
        {
          final URL configResource = getFallbackConfigLocation();
          return checkConfigLocation( configResource, path );
        }
        catch( final IOException e1 )
        {
          // we throw the originial, first exception, as this is the primary location that should work
          throw e;
        }
      }
    }
  }

  private static URL checkConfigLocation( final Location configurationLocation, final String path ) throws IOException
  {
    if( configurationLocation == null )
      throw new IOException( "Config location not set" );

    final URL configurationURL = configurationLocation.getURL();
    return checkConfigLocation( configurationURL, path );
  }

  private static URL getFallbackConfigLocation( )
  {
    if( FALLBACK_LOCATION == null )
      FALLBACK_LOCATION = findFallbackConfigLocation();
    return FALLBACK_LOCATION;
  }

  private static URL findFallbackConfigLocation( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.contribs.eclipsercp.fallbackConfigLocation" );
    if( elements.length == 0 )
    {
      // TODO: complain
      return null;
    }

    if( elements.length > 1 )
    {
      // TODO: complain
    }

    for( final IConfigurationElement element : elements )
    {
      try
      {
        final ILocationProvider provider = (ILocationProvider) element.createExecutableExtension( "class" );
        return provider.getLocation();
      }
      catch( final CoreException e )
      {
        EclipseRCPContributionsPlugin.getDefault().getLog().log( e.getStatus() );
      }
    }

    return null;
  }

  public static URL checkConfigLocation( final URL basisURL, final String configurationPath ) throws IOException
  {
    final URL configLocation = new URL( basisURL, configurationPath );
    InputStream is = null;
    try
    {
      is = configLocation.openStream();
      return configLocation;
    }
    finally
    {
      try
      {
        if( is != null )
          is.close();
      }
      catch( final IOException e )
      {
        // ignore
      }
    }
  }
}