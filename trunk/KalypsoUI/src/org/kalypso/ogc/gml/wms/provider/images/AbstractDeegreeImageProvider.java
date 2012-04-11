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
package org.kalypso.ogc.gml.wms.provider.images;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wms.RemoteWMService;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.owscommon_new.DCP;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.core.variables.VariableUtils;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.wms.deegree.DeegreeWMSUtilities;
import org.kalypso.ogc.gml.wms.loader.ICapabilitiesLoader;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Holger Albert
 */
public abstract class AbstractDeegreeImageProvider implements IKalypsoImageProvider
{
  /**
   * This variable stores the name of the theme.
   */
  private String m_themeName;

  /**
   * The LAYERS property of the source.
   */
  private String[] m_layers;

  /**
   * The STYLES property of the source.
   */
  private String[] m_styles;

  /**
   * The service.
   */
  private String m_service;

  /**
   * This variable stores the client coordinate system.
   */
  private String m_localSRS;

  /**
   * This is the content of a SLD, if we want the server to render the image with a specific style.
   */
  private String m_sldBody;

  /**
   * This variable stores the WMS service or is null.
   */
  private RemoteWMService m_wms;

  /**
   * The negotiated coordinate system.
   */
  private String m_negotiatedSRS;

  private URL m_getMapUrl;

  private String m_lastRequest;

  private WMSCapabilities m_capabilities;

  private String m_providerID;

  @Override
  public void init( final String providerID, final String themeName, final String[] layers, final String[] styles, final String service, final String localSRS, final String sldBody )
  {
    m_providerID = providerID;
    m_themeName = themeName;
    m_layers = layers;
    m_styles = styles;
    m_service = service;
    m_localSRS = localSRS;
    m_sldBody = sldBody;

    m_wms = null;
    m_negotiatedSRS = null;
    m_getMapUrl = null;
    m_lastRequest = null;
  }

  @Override
  public synchronized IStatus checkInitialize( final IProgressMonitor monitor )
  {
    if( m_service == null )
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "org.kalypso.ogc.gml.wms.provider.images.AbstractDeegreeImageProvider.1" ) ); //$NON-NLS-1$

    try
    {
      final WMSCapabilities wmsCaps = loadCapabilities();

      /* Initialize the remote WMS, if it is not already done. */
      return initializeRemoteWMS( wmsCaps );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, "Failed to load service capabilities", e );
    }
  }

  private WMSCapabilities loadCapabilities( ) throws CoreException
  {
    if( m_capabilities != null )
      return m_capabilities;

    /* Create the service URL. */
    final URL serviceURL = parseServiceUrl( m_service );

    /* Init the loader. */
    final ICapabilitiesLoader loader = createCapabilitiesLoader();
    m_capabilities = loader.load( serviceURL, new NullProgressMonitor() );
    return m_capabilities;
  }

  @Override
  public Image getImage( final int width, final int height, final GM_Envelope bbox ) throws CoreException
  {
    if( m_wms == null )
      return null;

    return loadImage( width, height, bbox );
  }

  @Override
  public synchronized org.eclipse.swt.graphics.Image getLegendGraphic( final Font font )
  {
    if( m_capabilities == null )
      return null;

    /* Layers. */
    final Layer[] layers = findLayer( m_capabilities );

    /* No layers, no legend graphic. */
    if( layers == null || layers.length == 0 )
      return null;

    /* Get the URL of the legend. */
    final LegendURL legendURL = findLegendURL( layers );
    if( legendURL == null )
      return null;

    /* Get the real URL. */
    final URL onlineResource = legendURL.getOnlineResource();

    final ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL( onlineResource );
    return imageDescriptor.createImage( font.getDevice() );
  }

  /**
   * This function initializes the WMS and loads the Capabilities.
   */
  private IStatus initializeRemoteWMS( final WMSCapabilities wmsCaps )
  {
    if( m_wms != null )
      return Status.OK_STATUS;

    /* Ask for the srs. */
    m_negotiatedSRS = negotiateCRS( m_localSRS, wmsCaps, m_layers );

    m_getMapUrl = findGetMapGetURL( wmsCaps );

    /* Initialize the WMS. */
    m_wms = createRemoteService( wmsCaps );

    return Status.OK_STATUS;
  }

  private URL findGetMapGetURL( final WMSCapabilities wmsCaps )
  {
    final Operation operation = wmsCaps.getOperationMetadata().getOperation( new QualifiedName( "GetMap" ) ); //$NON-NLS-1$

    final List<DCP> dcps = operation.getDCP();
    for( final DCP dcp : dcps )
    {
      if( dcp instanceof HTTP )
      {
        final HTTP http = (HTTP) dcp;
        final List<OnlineResource> links = http.getLinks();
        if( links.size() > 0 )
          return links.get( 0 ).getLinkage().getHref();
      }
    }

    return null;
  }

  /**
   * This function creates the remote service and returns it.
   *
   * @param capabilities
   *          The capabilites for the remote service.
   * @return The remote service.
   */
  protected abstract RemoteWMService createRemoteService( WMSCapabilities capabilities );

  /**
   * This function parses a String into an URL to the WMS service.
   *
   * @param service
   *          The String representation of the URL to the WMS service.
   * @return The URL to the WMS service.
   */
  private URL parseServiceUrl( final String service ) throws CoreException
  {
    try
    {
      final String resolvedService = VariableUtils.resolveVariablesQuietly( service );
      return new URL( resolvedService );
    }
    catch( final MalformedURLException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ogc.gml.wms.provider.images.AbstractDeegreeImageProvider.3", service, e.getLocalizedMessage() ) ) ); //$NON-NLS-1$
    }
  }

  /**
   * This method tries to find a common spatial reference system (srs) for a given set of layers. If all layers
   * coorespond to the local crs the local crs is returned, otherwise the srs of the top layer is returned and the
   * client must choose one to transform it to the local coordinate system
   *
   * @param localCRS
   *          The local spatial reference system.
   * @param capabilities
   *          The capabilites document of the web map service.
   * @param layerNames
   *          The layers that have to be matched to the local srs.
   * @return An array of possible coordiante systems.
   */
  private String negotiateCRS( final String localSRS, final WMSCapabilities wmsCapabilities, final String[] layers )
  {
    /* Match the local with the remote coordinate system. */
    final String[] crs = DeegreeWMSUtilities.negotiateCRS( localSRS, wmsCapabilities, layers );
    if( crs.length > 0 )
      return crs[0];

    return localSRS;
  }

  @Override
  public synchronized GM_Envelope getFullExtent( )
  {
    try
    {
      if( m_wms == null || m_layers == null )
        return null;

      return DeegreeWMSUtilities.getMaxExtent( m_layers, (WMSCapabilities) m_wms.getCapabilities(), m_negotiatedSRS );
    }
    catch( final Exception ex )
    {
      KalypsoGisPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex, Messages.getString( "org.kalypso.ogc.gml.wms.provider.images.AbstractDeegreeImageProvider.5" ) ) ); //$NON-NLS-1$
    }

    return null;
  }

  private String getThemeName( )
  {
    return m_themeName;
  }

  protected synchronized String getService( )
  {
    return m_service;
  }

  private String getLocalSRS( )
  {
    return m_localSRS;
  }

  private RemoteWMService getWms( )
  {
    return m_wms;
  }

  private String getNegotiatedSRS( )
  {
    return m_negotiatedSRS;
  }

  private final Image loadImage( final int width, final int height, final GM_Envelope bbox ) throws CoreException
  {
    try
    {
      /* Reset the request. */
      m_lastRequest = null;

      /* Check if nothing to request. */
      if( bbox == null )
        return null;

      /* If the some of the parameters has the wrong values, return null. */
      if( width == 0 || height == 0 || bbox.getWidth() == 0 || bbox.getHeight() == 0 )
        return null;

      /* Work locally against a copy of the reference, because it may change any time... */
      final RemoteWMService remoteWMS = getWms();
      if( remoteWMS == null )
        return null;

      /* Create the GetMap request. */
      final GetMap request = DeegreeWMSUtilities.createGetMapRequest( (WMSCapabilities) remoteWMS.getCapabilities(), getNegotiatedSRS(), getThemeName(), m_layers, m_styles, width, height, bbox, getLocalSRS(), m_sldBody );

      /* Store the request, before actually asking the WMS for a response. */
      m_lastRequest = URLDecoder.decode( String.format( "%s%s", m_getMapUrl, request.toString() ), "UTF-8" );

      /* Do the request and wait, until the result is there. */
      final Object result = remoteWMS.doService( request );
      if( result == null )
        return null;

      /* Wrong result, no image is returned. */
      if( !(result instanceof GetMapResult) )
        return null;

      /* Cast. */
      final GetMapResult mapResponse = (GetMapResult) result;

      /* Get the image. */
      final Image resultImage = (Image) mapResponse.getMap();
      if( resultImage == null )
      {
        /* Handle service-exception: convert to status and set it. */
        final OGCWebServiceRequest mapRequest = mapResponse.getRequest();
        final OGCWebServiceException exception = mapResponse.getException();

        final MultiStatus status = new MultiStatus( KalypsoGisPlugin.getId(), 0, Messages.getString( "org.kalypso.ogc.gml.wms.loader.images.WMSImageProvider.1" ), null ); //$NON-NLS-1$
        status.add( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "org.kalypso.ogc.gml.wms.loader.images.WMSImageProvider.2" ) + mapRequest + "'" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        status.add( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "org.kalypso.ogc.gml.wms.loader.images.WMSImageProvider.4" ) ) ); //$NON-NLS-1$
        status.add( StatusUtilities.createMultiStatusFromMessage( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, exception.toString(), "\n", null ) ); //$NON-NLS-1$

        throw new CoreException( status );
      }

      return resultImage;
    }
    catch( final Throwable t )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( t ) );
    }
  }

  /**
   * Finds the layers this image provider represents.
   */
  private Layer[] findLayer( final WMSCapabilities capabilities )
  {
    if( m_layers == null )
      return null;

    final List<Layer> result = new ArrayList<Layer>();
    for( final String name : m_layers )
    {
      final Layer layer = capabilities.getLayer( name );
      if( layer != null )
        result.add( layer );
    }

    return result.toArray( new Layer[result.size()] );
  }

  private LegendURL findLegendURL( final Layer[] layers )
  {
    /* End recursion. */
    if( layers == null || layers.length == 0 )
      return null;

    // TODO: we should show all styles of all layers as a tree in the outline
    // TODO: Instead, we show the graphic of the first style of the first layer
    final Style[] styles = layers[0].getStyles();
    if( styles.length == 0 )
      return findLegendURL( layers[0].getLayer() );

    /* TODO Only the first style will be used for now. */
    final Style style = styles[0];

    /* Only use styles, denoted by m_styles. */
    final List<String> styleList = Arrays.asList( m_styles );
    if( !styleList.contains( style.getName() ) )
      return null;

    /* Get the URLs for the legend. */
    final LegendURL[] legendURLs = style.getLegendURL();
    if( legendURLs.length == 0 )
      return null;

    // TODO: Only the first legend URL will be used for now.
    return legendURLs[0];
  }

  /**
   * This function returns the last request or null.
   *
   * @return The last request or null.
   */
  public synchronized String getLastRequest( )
  {
    return m_lastRequest;
  }

  public synchronized void setLayers( final String[] layers, final String[] styles )
  {
    Assert.isTrue( layers.length == styles.length );

    m_layers = layers;
    m_styles = styles;

    /* Reset cached properties */
    m_wms = null;
    m_negotiatedSRS = null;
    m_getMapUrl = null;
    m_lastRequest = null;
  }

  @Override
  public WMSCapabilities getCapabilities( )
  {
    return m_capabilities;
  }

  @Override
  public boolean isLayerVisible( final String name )
  {
    return ArrayUtils.contains( m_layers, name );
  }

  @Override
  public void setLayerVisible( final String name, final boolean visible )
  {
    if( m_capabilities == null )
      return;

    /* Prepare which-list */
    final Map<String, String> layersAndStyles = new LinkedHashMap<>();
    for( int i = 0; i < m_layers.length; i++ )
      layersAndStyles.put( m_layers[i], m_styles[i] );

    /* Change visibility of that one layer */
    if( visible )
      layersAndStyles.put( name, null );
    else
      layersAndStyles.remove( name );

    final WMSLayerConfigurator configurator = new WMSLayerConfigurator( m_capabilities, layersAndStyles );

    final String[] layers = configurator.getLayers();
    final String[] styles = configurator.getStyles();

    setLayers( layers, styles );
  }

  @Override
  public synchronized String getStyle( final Layer layer )
  {
    final String name = layer.getName();
    for( int i = 0; i < m_layers.length; i++ )
    {
      if( m_layers[i].equals( name ) )
        return m_styles[i];
    }

    return null;
  }

  @Override
  public String getSource( )
  {
    final String layers = StringUtils.join( m_layers, "," );
    final String styles = StringUtils.join( m_styles, "," );

    return String.format( "%s=%s#%s=%s#%s=%s#%s=%s", IKalypsoImageProvider.KEY_URL, m_service, IKalypsoImageProvider.KEY_PROVIDER, m_providerID, IKalypsoImageProvider.KEY_LAYERS, layers, IKalypsoImageProvider.KEY_STYLES, styles );
  }
}