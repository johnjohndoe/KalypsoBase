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
package org.kalypso.ogc.gml.wms.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.wms.provider.images.AbstractDeegreeImageProvider;
import org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider;
import org.kalypso.ogc.gml.wms.provider.images.WMSImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * This is a helper class, providing functions for dealing with a WMS.
 * 
 * @author Holger Albert
 */
public class KalypsoWMSUtilities
{
  private static final String URL_PARAM_SERVICE = "SERVICE"; //$NON-NLS-1$

  private static final String SERVICE_WMS = "WMS"; //$NON-NLS-1$

  private static final String URL_PARAM_REQUEST = "REQUEST"; //$NON-NLS-1$ 

  private static final String REQUEST_GET_CAPABILITIES = "GetCapabilities"; //$NON-NLS-1$

  private static final String URL_PARAM_VERSION = "VERSION"; //$NON-NLS-1$

  private static final String VERSION_1_1_1 = "1.1.1"; //$NON-NLS-1$

  private KalypsoWMSUtilities( )
  {
  }

  /**
   * This function should return an image provider for the given source. It chooses with the PROVIDER attribute and
   * returns the specific provider. If none could be found using that source of information, an default one (
   * {@link org.kalypso.ogc.gml.map.themes.provider.KalypsoWMSImageProvider} will be returned.
   * 
   * @param themeName
   *          The name of the theme. Will be used to initialize the image provider.
   * @param layers
   *          The layers. Will be used to initialize the image provider.
   * @param styles
   *          The styles. Will be used to initialize the image provider.
   * @param service
   *          The service. Will be used to initialize the image provider.
   * @param providerID
   *          The ID of the image provider.
   * @param localSRS
   *          The client coordinate system. Will be used to initialize the image provider.
   * @return An image provider. Should never be null.
   */
  public static IKalypsoImageProvider getImageProvider( final String themeName, final String[] layers, final String[] styles, final String service, final String providerID )
  {
    final String localSRS = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    /* If it is missing or null, return the default provider. */
    if( providerID == null )
      return getDefaultImageProvider( themeName, layers, styles, service, localSRS );

    /* Get the extension registry. */
    final IExtensionRegistry er = Platform.getExtensionRegistry();

    /* The registry must exist. */
    if( er == null )
      return getDefaultImageProvider( themeName, layers, styles, service, localSRS );

    final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.ui.addlayer.WMSImageProvider" ); //$NON-NLS-1$
    for( final IConfigurationElement element : configurationElementsFor )
    {
      /* Get some attributes. */
      final String id = element.getAttribute( "id" ); //$NON-NLS-1$
      if( id.equals( providerID ) )
      {
        try
        {
          /* This is the wanted provider. */
          final IKalypsoImageProvider provider = (IKalypsoImageProvider) element.createExecutableExtension( "class" ); //$NON-NLS-1$
          provider.init( themeName, layers, styles, service, localSRS );
          return provider;
        }
        catch( final CoreException e )
        {
          KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );

          return getDefaultImageProvider( themeName, layers, styles, service, localSRS );
        }
        catch( final Throwable t )
        {
          // protect against bad extensions
          final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.ogc.gml.wms.utils.KalypsoWMSUtilities.3" ) + id, t ); //$NON-NLS-1$
          KalypsoGisPlugin.getDefault().getLog().log( status );

          return getDefaultImageProvider( themeName, layers, styles, service, localSRS );
        }
      }
    }

    return getDefaultImageProvider( themeName, layers, styles, service, localSRS );
  }

  /**
   * This function returns the default wms image provider. It initializes it, too.
   * 
   * @param themeName
   *          The name of the theme.
   * @param layers
   *          The layers.
   * @param styles
   *          The styles.
   * @param service
   *          The service.
   * @param localSRS
   *          The client coordinate system.
   * @return The default wms image provider.
   */
  public static IKalypsoImageProvider getDefaultImageProvider( final String themeName, final String[] layers, final String[] styles, final String service, final String localSRS )
  {
    final AbstractDeegreeImageProvider provider = new WMSImageProvider();
    provider.init( themeName, layers, styles, service, localSRS );
    return provider;
  }

  /**
   * This function creates an URL for a get capabilities request from the given base URL.
   * 
   * @param baseURL
   *          The base URL.
   * @return The get capabilities request URL.
   */
  public static URL createCapabilitiesRequest( final URL baseURL ) throws MalformedURLException
  {
    final Map<String, String> params = UrlUtilities.parseQuery( baseURL );

// /* Transform all keys to upper-case, else this wont work. */
// // TODO: it would be nice to preserve the original keys instead; what is the easiest way to do it?
// final Transformer capitalizeTransformer = new Transformer()
// {
// @Override
// public Object transform( final Object input )
// {
// return ((String) input).toUpperCase();
// }
// };
// @SuppressWarnings("unchecked")
// final Map<String, String> paramsCapitalized = TransformedMap.decorateTransform( params, capitalizeTransformer, null
// );

    /* Check if necessary parameters already exists; if yes they must fit else we have a problem */
    checkParameter( params, URL_PARAM_SERVICE, SERVICE_WMS );
    checkParameter( params, URL_PARAM_VERSION, VERSION_1_1_1 );
    checkParameter( params, URL_PARAM_REQUEST, REQUEST_GET_CAPABILITIES );

    return UrlUtilities.addQuery( baseURL, params );
  }

  /**
   * Checks if a parameter already exists and has a certeain value.<br/>
   * If the parameter is not set, sets it.<br>
   * If the parameter is set to a wrong value, an error is thrown.
   */
  private static void checkParameter( final Map<String, String> params, final String paramKey, final String paramValue )
  {
    final Entry<String, String> existingEntry = searchIgnoreCase( params, paramKey );

    if( existingEntry == null )
      params.put( paramKey, paramValue );
    else
    {
      final String existingValue = existingEntry.getValue();
      if( !paramValue.equals( existingValue ) )
        throw new IllegalArgumentException( String.format( "WMS-URL contains wrong parameter '%s': %s", paramKey, existingValue ) );
    }
  }

  // TODO: move into MapUtils
  private static Entry<String, String> searchIgnoreCase( final Map<String, String> map, final String key )
  {
    final Set<Entry<String, String>> entries = map.entrySet();
    for( final Entry<String, String> entry : entries )
    {
      if( entry.getKey().equalsIgnoreCase( key ) )
        return entry;
    }

    return null;
  }
}