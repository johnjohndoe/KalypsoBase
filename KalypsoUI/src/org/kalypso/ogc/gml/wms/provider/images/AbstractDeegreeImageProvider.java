/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.deegree.datatypes.QualifiedName;
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.outline.nodes.ILegendProvider;
import org.kalypso.ogc.gml.wms.deegree.DeegreeWMSUtilities;
import org.kalypso.ogc.gml.wms.loader.ICapabilitiesLoader;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * The base implementation of the deegree WMS client.
 * 
 * @author Holger Albert
 */
public abstract class AbstractDeegreeImageProvider implements IKalypsoImageProvider, ILegendProvider
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
   * This variable stores the loader for the capabilities.
   */
  private final ICapabilitiesLoader m_loader;

  /**
   * This variable stores the WMS service or is null.
   */
  private RemoteWMService m_wms;

  /**
   * The negotiated coordinate system.
   */
  private String m_negotiatedSRS;

  private URL m_getMapUrl;

  /**
   * The last request.
   */
  private String m_lastRequest;

  /**
   * The constructor.
   * 
   * @param loader
   *          The loader for loading the capabilities.
   */
  public AbstractDeegreeImageProvider( final ICapabilitiesLoader loader )
  {
    m_themeName = null;
    m_layers = null;
    m_styles = null;
    m_service = null;
    m_localSRS = null;
    m_sldBody = null;

    m_loader = loader;
    m_wms = null;
    m_negotiatedSRS = null;
    m_getMapUrl = null;
    m_lastRequest = null;
  }

  /**
   * @see org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider#init(java.lang.String, java.lang.String[],
   *      java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void init( final String themeName, final String[] layers, final String[] styles, final String service, final String localSRS, final String sldBody )
  {
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

  /**
   * @see org.kalypso.ogc.gml.wms.provider.IKalypsoImageProvider#getImage(int, int,
   *      org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  @Override
  public Image getImage( final int width, final int height, final GM_Envelope bbox ) throws CoreException
  {
    /* Initialize the remote WMS, if it is not already done. */
    initializeRemoteWMS();

    return loadImage( width, height, bbox );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.ILegendProvider#getLegendGraphic(java.lang.String[], boolean,
   *      org.eclipse.swt.graphics.Font)
   */
  @Override
  public org.eclipse.swt.graphics.Image getLegendGraphic( final String[] whiteList,final boolean onlyVisible, final Font font ) throws CoreException
  {
    /* Initialize the remote WMS, if it is not already done. */
    initializeRemoteWMS();

    return loadLegendGraphic( font.getDevice() );
  }

  /**
   * This function initialises the WMS and loads the Capabilities.
   */
  private void initializeRemoteWMS( ) throws CoreException
  {
    /* Cache the WMS, so that its capabilities are only initialized once. */
    if( m_wms == null )
    {
      if( m_service == null )
        throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "org.kalypso.ogc.gml.wms.provider.images.AbstractDeegreeImageProvider.1" ) ) ); //$NON-NLS-1$

      /* Create the service URL. */
      final URL serviceURL = parseServiceUrl( m_service );

      /* Init the loader. */
      m_loader.init( serviceURL );

      /* Create the capabilities. */
      final WMSCapabilities wmsCaps = DeegreeWMSUtilities.loadCapabilities( m_loader, new NullProgressMonitor() );

      /* Ask for the srs. */
      m_negotiatedSRS = negotiateCRS( m_localSRS, wmsCaps, m_layers );
      final Operation operation = wmsCaps.getOperationMetadata().getOperation( new QualifiedName( "GetMap" ) );
      HTTP http = null;
      final List<DCP> dcps = operation.getDCP();
      for( final DCP dcp : dcps )
        if( dcp instanceof HTTP )
          http = (HTTP) dcp;
      if( http != null )
        m_getMapUrl = http.getLinks().get( 0 ).getLinkage().getHref();

      /* Initialize the WMS. */
      m_wms = getRemoteService( wmsCaps );
    }
  }

  /**
   * This function creates the remote service and returns it.
   * 
   * @param capabilities
   *          The capabilites for the remote service.
   * @return The remote service.
   */
  protected abstract RemoteWMService getRemoteService( WMSCapabilities capabilities );

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
      return new URL( service );
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

  /**
   * @see org.kalypso.ogc.gml.map.themes.provider.IKalypsoImageProvider#getFullExtent()
   */
  @Override
  public GM_Envelope getFullExtent( )
  {
    try
    {
      /* Initialize the remote WMS, if it is not already done. */
      initializeRemoteWMS();

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

  protected String getThemeName( )
  {
    return m_themeName;
  }

  /**
   * Needed for some childs that do implement an own load image function.
   */
  protected String[] getLayers( )
  {
    return m_layers;
  }

  /**
   * Needed for some childs that do implement an own load image function.
   */
  protected String[] getStyles( )
  {
    return m_styles;
  }

  protected String getService( )
  {
    return m_service;
  }

  protected String getLocalSRS( )
  {
    return m_localSRS;
  }

  /**
   * @see org.kalypso.ogc.gml.wms.provider.IKalypsoImageProvider#getLoader()
   */
  @Override
  public ICapabilitiesLoader getLoader( )
  {
    return m_loader;
  }

  protected RemoteWMService getWms( )
  {
    return m_wms;
  }

  protected String getNegotiatedSRS( )
  {
    return m_negotiatedSRS;
  }

  protected Image loadImage( final int width, final int height, final GM_Envelope bbox ) throws CoreException
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

  protected org.eclipse.swt.graphics.Image loadLegendGraphic( final Device device ) throws CoreException
  {
    /* We need a remote WMS. */
    final RemoteWMService wms = getWms();
    if( wms == null )
      return null;

    /* Check, if there is a legend graphic available. */
    final WMSCapabilities capabilities = (WMSCapabilities) wms.getCapabilities();

    /* Layers. */
    final Layer[] layers = findLayer( capabilities );

    /* No layers, no legend graphic. */
    if( layers == null || layers.length == 0 )
      return null;

    /* Get the URL of the legend. */
    final LegendURL legendURL = findLegendURL( layers );
    if( legendURL == null )
      return null;

    /* Get the real URL. */
    final URL onlineResource = legendURL.getOnlineResource();

    /* The input stream. */
    InputStream inputStream = null;

    try
    {
      /* Open the stream. */
      inputStream = onlineResource.openStream();

      /* The result image. */
      final org.eclipse.swt.graphics.Image result = new org.eclipse.swt.graphics.Image( device, inputStream );

      /* Close the stream. */
      inputStream.close();

      return result;
    }
    catch( final IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    finally
    {
      IOUtils.closeQuietly( inputStream );
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
  public String getLastRequest( )
  {
    return m_lastRequest;
  }
}