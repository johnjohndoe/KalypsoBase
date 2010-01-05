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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.swt.awt.ImageConverter;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.AbstractKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.LegendExporter;
import org.kalypso.ogc.gml.outline.nodes.NodeFactory;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author doemming
 */
public class KalypsoLegendTheme extends AbstractKalypsoTheme
{
  private final IMapModellListener m_modellListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeAdded(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeAdded( final IMapModell source, final IKalypsoTheme theme )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeRemoved(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeRemoved( final IMapModell source, final IKalypsoTheme theme, final boolean lastVisibility )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeOrderChanged(org.kalypso.ogc.gml.mapmodel.IMapModell)
     */
    @Override
    public void themeOrderChanged( final IMapModell source )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeVisibilityChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeVisibilityChanged( final IMapModell source, final IKalypsoTheme theme, final boolean visibility )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeStatusChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeStatusChanged( final IMapModell source, final IKalypsoTheme theme )
    {
      invalidateLegend();
    }
  };

  private final Job m_legendJob = new UIJob( Messages.getString("org.kalypso.ogc.gml.KalypsoLegendTheme.0") ) //$NON-NLS-1$
  {
    /**
     * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      try
      {
        updateLegend( monitor );
        return Status.OK_STATUS;
      }
      catch( final CoreException e )
      {
        if( !e.getStatus().matches( IStatus.CANCEL ) )
          e.printStackTrace();

        return e.getStatus();
      }
    }
  };

  private Image m_image = null;

  // TODO: get from properties
  private final int m_borderWidth = 10;

  // TODO: get from properties
  // Default: just a shade of gray for a little contrast
  private final RGB m_backgroundColor = new RGB( 245, 245, 245 );

  public KalypsoLegendTheme( final I10nString name, final IMapModell mapModell )
  {
    super( name, "legend", mapModell ); //$NON-NLS-1$

    mapModell.addMapModelListener( m_modellListener );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    getMapModell().removeMapModelListener( m_modellListener );

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.lang.Boolean,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus paint( final Graphics g, final GeoTransform p, final Boolean selected, final IProgressMonitor monitor )
  {
    if( selected != null && selected )
      return Status.OK_STATUS;

    final int wMax = g.getClipBounds().width;
    final int hMax = g.getClipBounds().height;
    if( m_image != null )
    {
      g.setPaintMode();
      final int widthIamge = m_image.getWidth( null );
      final int heightImage = m_image.getHeight( null );
      g.drawImage( m_image, wMax - widthIamge, hMax - heightImage, widthIamge, heightImage, null );
    }
    return Status.OK_STATUS;
  }

  protected final void invalidateLegend( )
  {
    m_legendJob.cancel();

    m_image = null;

    m_legendJob.schedule( 100 );
  }

  protected void updateLegend( final IProgressMonitor monitor ) throws CoreException
  {
    final IMapModell mapModell = getMapModell();
    if( mapModell == null )
      return;

    final IThemeNode rootNode = NodeFactory.createRootNode( mapModell, null );
    final IThemeNode[] nodes = rootNode.getChildrenCompact();

    final Display display = Display.getCurrent();
    final Insets insets = new Insets( m_borderWidth, m_borderWidth, m_borderWidth, m_borderWidth );
    final LegendExporter legendExporter = new LegendExporter();
    final org.eclipse.swt.graphics.Image image = legendExporter.exportLegends( nodes, display, insets, m_backgroundColor, -1, -1, monitor );

    final BufferedImage awtImage = ImageConverter.convertToAWT( image.getImageData() );
    image.dispose();
    ProgressUtilities.worked( monitor, 0 ); // cancel check

    final Graphics2D graphics = (Graphics2D) awtImage.getGraphics();
    graphics.setColor( Color.BLACK );
    graphics.setStroke( new BasicStroke( 2.0f ) );
    graphics.drawRect( 0, 0, awtImage.getWidth(), awtImage.getHeight() );
    graphics.dispose();

    m_image = awtImage;

    fireRepaintRequested( null );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getBoundingBox()
   */
  public GM_Envelope getFullExtent( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getDefaultIcon()
   */
  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_LEGEND );
  }
}