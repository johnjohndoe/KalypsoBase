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
package org.kalypso.ui.addlayer.internal.wms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.owscommon_new.ServiceIdentification;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.addlayer.internal.ImageProviderExtensions;
import org.kalypso.ui.i18n.Messages;

/**
 * Wrapper for capabilities used in wms page for easier access.
 *
 * @author Gernot Belger
 */
public class CapabilitiesInfo
{
  private static final String STR_INVALID_SERVER_ADDRESS = Messages.getString("CapabilitiesInfo_0"); //$NON-NLS-1$

  private static final String STR_NOT_LOADED = Messages.getString("CapabilitiesInfo_1"); //$NON-NLS-1$

  private IStatus m_status = new Status( IStatus.INFO, KalypsoAddLayerPlugin.getId(), STR_NOT_LOADED );

  /** The server address of the server this info represents */
  private final String m_address;

  /** Server address, parsed as URL. As address might not always be a valid {@link URL}, we keep this separated. */
  private URL m_url;

  private String m_providerID;

  private WMSCapabilities m_capabilities;

  public CapabilitiesInfo( final String address )
  {
    Assert.isNotNull( address );

    m_address = address;

    final Map<String, String> imageProviders = ImageProviderExtensions.getImageProviders();
    /* If only one image provider exists, we us it as default (avoids empty combo box) */
    if( imageProviders.size() == 1 )
      m_providerID = imageProviders.keySet().iterator().next();

    parseAddress();
  }

  private void parseAddress( )
  {
    try
    {
      m_url = new URL( m_address );

      if( !getValidAddress() )
        m_status = new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), STR_INVALID_SERVER_ADDRESS, null );
    }
    catch( final MalformedURLException e )
    {
      m_status = new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), STR_INVALID_SERVER_ADDRESS, e );
    }
  }

  public void setProviderID( final String providerID )
  {
    m_providerID = providerID;
  }

  public String getAddress( )
  {
    return m_address;
  }

  @Override
  public int hashCode( )
  {
    return m_address.hashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj == null )
      return false;

    if( obj == this )
      return true;

    if( !(obj instanceof CapabilitiesInfo) )
      return false;

    final CapabilitiesInfo other = (CapabilitiesInfo) obj;
    return m_address.equals( other.m_address );
  }

  @Override
  public String toString( )
  {
    return m_address;
  }

  public String getImageProvider( )
  {
    return m_providerID;
  }

  public boolean isLoaded( )
  {
    return m_capabilities != null;
  }

  public IStatus getStatus( )
  {
    return m_status;
  }

  public URL getURL( )
  {
    return m_url;
  }

  public WMSCapabilities getCapabilities( )
  {
    return m_capabilities;
  }

  public void setCapabilities( final WMSCapabilities capabilities, final IStatus status )
  {
    m_capabilities = capabilities;

    m_status = status;
  }

  public String getTitle( )
  {
    if( m_capabilities == null )
      return STR_NOT_LOADED;

    final ServiceIdentification identification = m_capabilities.getServiceIdentification();
    return identification.getTitle();
  }

  public String getAbstract( )
  {
    if( m_capabilities == null )
      return STR_NOT_LOADED;

    final ServiceIdentification identification = m_capabilities.getServiceIdentification();
    return identification.getAbstractString();
  }

  public boolean getValidAddress( )
  {
    final URL url = getURL();
    if( url == null )
      return false;

    final String host = url.getHost();
    if( StringUtils.isBlank( host ) )
      return false;

    return true;
  }
}