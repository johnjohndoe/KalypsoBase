/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.map.themes;

import java.awt.Graphics;
import java.awt.Image;

import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.viewers.ITooltipProvider;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.gml.AbstractKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.wms.provider.images.AbstractDeegreeImageProvider;
import org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider;
import org.kalypso.template.types.LayerType;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * This class implements the a theme, which loads images from a given provider.
 * 
 * @author Doemming, Kuepferle
 * @author Holger Albert
 */
public class KalypsoWMSTheme extends AbstractKalypsoTheme implements ITooltipProvider
{
  private static final IStatus INIT_STATUS = new Status( IStatus.INFO, KalypsoGisPlugin.PLUGIN_ID, Messages.getString( "KalypsoWMSTheme.0" ) ); //$NON-NLS-1$

  private final LayerType m_layer;

  /**
   * This variable stores the result of the loading.
   */
  private Image m_buffer;

  /**
   * This variable stores the image provider.
   */
  private final IKalypsoImageProvider m_provider;

  /**
   * This variable stores the max envelope of layer on WMS (local SRS).
   */
  protected GM_Envelope m_maxEnvLocalSRS;

  /**
   * This is the stored extent from the last time, a loader was started (call to {@link #setExtent(int, int, GM_Envelope)}).
   */
  protected GM_Envelope m_extent;

  /**
   * @param themeName
   *          The name of the theme.
   * @param imageProvider
   *          The image provider, which should be used. If it has also the type {@link ILegendProvider} also a legend
   *          can be shown.
   */
  public KalypsoWMSTheme( final String linktype, final I10nString themeName, final LayerType layerType, final IKalypsoImageProvider imageProvider, final IMapModell mapModel )
  {
    super( themeName, linktype.toUpperCase(), mapModel );

    m_layer = layerType;
    m_provider = imageProvider;
  }

  @Override
  public GM_Envelope getFullExtent( )
  {
    // should not block! If capabilities are not yet loaded, return null
    return m_maxEnvLocalSRS;
  }

  @Override
  public IStatus paint( final Graphics g, final GeoTransform p, final Boolean selected, final IProgressMonitor monitor )
  {
    /* The image can not be selected. */
    if( selected != null && selected )
      return Status.OK_STATUS;

    /* Initialize loader */
    setStatus( INIT_STATUS );
    final IStatus initStatus = initializeLoader();
    if( !initStatus.isOK() )
    {
      setStatus( initStatus );
      return initStatus;
    }

    /* Paint image */
    setStatus( AbstractKalypsoTheme.PAINT_STATUS );
    final IStatus imageStatus = paintImage( g, p, monitor );
    setStatus( imageStatus );

    return imageStatus;
  }

  @Override
  public void dispose( )
  {
    synchronized( this )
    {
      /* Dispose the legend. */
      if( m_buffer != null )
      {
        m_buffer.flush();
        m_buffer = null;
      }
    }

    super.dispose();
  }

  @Override
  public boolean isLoaded( )
  {
    return true;
  }

  @Override
  public String getTooltip( final Object element )
  {
    Assert.isTrue( element == this, "'Element' must be this" ); //$NON-NLS-1$

    if( getStatus().isOK() )
      return m_provider.getLabel();

    return getStatus().getMessage();
  }

  /**
   * This function returns the image provider of this theme.
   * 
   * @return The image provider of this theme.
   */
  public IKalypsoImageProvider getImageProvider( )
  {
    return m_provider;
  }

  public String getSource( )
  {
    return m_provider.getSource();
  }

  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_WMS );
  }

  /**
   * This function returns the last request or null.
   * 
   * @return The last request or null.
   */
  public String getLastRequest( )
  {
    if( m_provider == null )
      return null;

    if( !(m_provider instanceof AbstractDeegreeImageProvider) )
      return null;

    return ((AbstractDeegreeImageProvider)m_provider).getLastRequest();
  }

  public String getFeatureInfo( final double x, final double y ) throws OGCWebServiceException, OWSException
  {
    if( m_provider == null )
      return null;

    if( !(m_provider instanceof AbstractDeegreeImageProvider) )
      return null;

    return ((AbstractDeegreeImageProvider)m_provider).getFeatureInfo( x, y );
  }

  public ImageDescriptor getLegendGraphic( final String layer, final String style )
  {
    if( m_provider == null )
      return null;

    return m_provider.getLegendGraphic( layer, style );
  }

  public Style[] getStyles( )
  {
    if( m_layer == null )
      return new Style[] {};

    if( !(m_layer instanceof StyledLayerType) )
      return new Style[] {};

    final StyledLayerType styledLayer = (StyledLayerType)m_layer;
    return styledLayer.getStyle().toArray( new Style[] {} );
  }

  private IStatus initializeLoader( )
  {
    if( m_provider == null )
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "org.kalypso.ogc.gml.wms.loader.images.KalypsoImageLoader.1" ) ); //$NON-NLS-1$

    return m_provider.checkInitialize( new NullProgressMonitor() );
  }

  private synchronized IStatus paintImage( final Graphics g, final GeoTransform p, final IProgressMonitor monitor )
  {
    /* Start the task. */
    monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.wms.loader.images.KalypsoImageLoader.0" ), 1000 ); //$NON-NLS-1$

    try
    {
      // HACK: initialize the max-extend on first paint, because paint is the only method that is allowed to block
      // FIXME: we should refaktor the whole image provider: it should immediately start loading the capas in a
      // sepearate thread and should inform the theme when it has finished.
      if( m_maxEnvLocalSRS == null )
        m_maxEnvLocalSRS = m_provider.getFullExtent();

      /* Load the image. This could take a while. */
      final int width = (int)p.getDestWidth();
      final int height = (int)p.getDestHeight();
      final GM_Envelope extent = p.getSourceRect();

      // FIXME: can lead to dead lock like beahviour, if the image takes very long to be fetched. Should be callled outside of synchronized block
      m_buffer = m_provider.getImage( width, height, extent );

      final IStatus status = new Status( IStatus.OK, KalypsoGisPlugin.getId(), Messages.getString( "org.kalypso.ogc.gml.wms.loader.images.KalypsoImageLoader.2" ) ); //$NON-NLS-1$
      if( !status.isOK() )
        return status;

      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      // HINT: If the theme was switched invisible during these seconds, it will still be drawn, until the next repaint.
      // Hopefully this will avoid this. */
      if( isVisible() && m_buffer != null )
        g.drawImage( m_buffer, 0, 0, null );

      return status;
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }
    catch( final Throwable throwable )
    {
      return StatusUtilities.statusFromThrowable( throwable );
    }
    finally
    {
      monitor.done();
    }
  }

  public WMSCapabilities getCapabilities( )
  {
    if( m_provider == null )
      return null;

    return m_provider.getCapabilities();
  }

  public boolean isLayerVisible( final String name )
  {
    if( m_provider == null )
      return false;

    return m_provider.isLayerVisible( name );
  }

  public void setLayerVisible( final String[] names, final boolean visible )
  {
    if( m_provider == null )
      return;

    m_provider.setLayerVisible( names, visible );

    fireStatusChanged( this );
    fireRepaintRequested( getFullExtent() );
  }

  public String getStyle( final Layer layer )
  {
    if( m_provider == null )
      return null;

    return m_provider.getStyle( layer );
  }
}