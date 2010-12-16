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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.swt.awt.ImageConverter;
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
import org.kalypso.util.themes.legend.LegendCoordinate;
import org.kalypso.util.themes.legend.LegendUtilities;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * The legend theme is able to display available legends for all or a subset of themes in a map.
 * 
 * @author Andreas Doemming (original)
 * @author Holger Albert (modifications)
 */
public class KalypsoLegendTheme extends AbstractKalypsoTheme
{
  /**
   * This listener invalidates the displayed legends.
   */
  private IMapModellListener m_modellListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeAdded(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeAdded( IMapModell source, IKalypsoTheme theme )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeRemoved(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeRemoved( IMapModell source, IKalypsoTheme theme, boolean lastVisibility )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeOrderChanged(org.kalypso.ogc.gml.mapmodel.IMapModell)
     */
    @Override
    public void themeOrderChanged( IMapModell source )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeVisibilityChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeVisibilityChanged( IMapModell source, IKalypsoTheme theme, boolean visibility )
    {
      invalidateLegend();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeStatusChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeStatusChanged( IMapModell source, IKalypsoTheme theme )
    {
      invalidateLegend();
    }
  };

  /**
   * Responsible for updating the legend.
   */
  private Job m_legendJob = new UIJob( Messages.getString( "org.kalypso.ogc.gml.KalypsoLegendTheme.0" ) ) //$NON-NLS-1$
  {
    /**
     * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInUIThread( IProgressMonitor monitor )
    {
      /* If no monitor was given, take a null progress monitor. */
      if( monitor == null )
        monitor = new NullProgressMonitor();

      try
      {
        /* Monitor. */
        monitor.beginTask( "Zeichne Legende...", 1000 );
        monitor.subTask( "Zeichne Legende..." );

        /* Update the legend. */
        updateLegend( new SubProgressMonitor( monitor, 1000 ) );

        return Status.OK_STATUS;
      }
      catch( CoreException ex )
      {
        if( !ex.getStatus().matches( IStatus.CANCEL ) )
          ex.printStackTrace();

        return ex.getStatus();
      }
      finally
      {
        /* Monitor. */
        monitor.done();
      }
    }
  };

  /**
   * The horizontal position.
   */
  protected int m_horizontal;

  /**
   * The vertical position.
   */
  protected int m_vertical;

  /**
   * The background color.
   */
  protected org.eclipse.swt.graphics.Color m_backgroundColor;

  /**
   * The insets.
   */
  protected int m_insets;

  /**
   * The ids of the selected themes.
   */
  protected String[] m_themeIds;

  /**
   * The drawn image.
   */
  private Image m_image;

  /**
   * The constructor
   * 
   * @param name
   *          The name of the theme.
   * @param mapModel
   *          The map model to use.
   */
  public KalypsoLegendTheme( I10nString name, IMapModell mapModell )
  {
    super( name, "legend", mapModell ); //$NON-NLS-1$

    m_image = null;
    m_horizontal = -1;
    m_vertical = -1;
    m_backgroundColor = null;
    m_insets = -1;
    m_themeIds = null;

    mapModell.addMapModelListener( m_modellListener );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    getMapModell().removeMapModelListener( m_modellListener );

    if( m_backgroundColor != null )
      m_backgroundColor.dispose();

    m_image = null;
    m_horizontal = -1;
    m_vertical = -1;
    m_backgroundColor = null;
    m_insets = -1;
    m_themeIds = null;

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getDefaultIcon()
   */
  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_LEGEND );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform, java.lang.Boolean,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus paint( Graphics g, GeoTransform p, Boolean selected, IProgressMonitor monitor )
  {
    if( selected != null && selected )
      return Status.OK_STATUS;

    if( m_image != null )
    {
      /* Determine the position. */
      LegendCoordinate position = PositionUtilities.determinePosition( g, m_image, m_horizontal, m_vertical );

      /* Draw the image. */
      g.setPaintMode();
      g.drawImage( m_image, position.getX(), position.getY(), m_image.getWidth( null ), m_image.getHeight( null ), null );
    }

    return Status.OK_STATUS;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoTheme#getFullExtent()
   */
  @Override
  public GM_Envelope getFullExtent( )
  {
    return null;
  }

  /**
   * This function initializes the legend theme from its own properties. For these not found, defaults will be set.
   */
  private void initFromProperties( )
  {
    /* Default values. */
    m_horizontal = PositionUtilities.RIGHT;
    m_vertical = PositionUtilities.BOTTOM;
    m_backgroundColor = new org.eclipse.swt.graphics.Color( Display.getCurrent(), 255, 255, 255 );
    m_insets = 10;
    m_themeIds = new String[] {};

    /* Get the properties. */
    String horizontalProperty = getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, null );
    String verticalProperty = getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, null );
    String backgroundColorProperty = getProperty( LegendUtilities.THEME_PROPERTY_BACKGROUND_COLOR, null );
    String insetsProperty = getProperty( LegendUtilities.THEME_PROPERTY_INSETS, null );
    String themeIdsProperty = getProperty( LegendUtilities.THEME_PROPERTY_THEME_IDS, null );

    /* Check the horizontal position. */
    int horizontal = LegendUtilities.checkHorizontalPosition( horizontalProperty );
    if( horizontal != -1 )
      m_horizontal = horizontal;

    /* Check the vertical position. */
    int vertical = LegendUtilities.checkVerticalPosition( verticalProperty );
    if( vertical != -1 )
      m_vertical = vertical;

    /* Check the background color. */
    org.eclipse.swt.graphics.Color backgroundColor = LegendUtilities.checkBackgroundColor( Display.getCurrent(), backgroundColorProperty );
    if( backgroundColor != null )
    {
      m_backgroundColor.dispose();
      m_backgroundColor = backgroundColor;
    }

    /* Check the insets. */
    int insets = LegendUtilities.checkInsets( insetsProperty );
    if( insets >= 1 && insets <= 25 )
      m_insets = insets;

    /* Check the theme ids. */
    List<String> themeIds = LegendUtilities.verifyThemeIds( getMapModell(), themeIdsProperty );
    if( themeIds != null && themeIds.size() > 0 )
      m_themeIds = themeIds.toArray( new String[] {} );
  }

  /**
   * This function invaludates the legend.
   */
  protected void invalidateLegend( )
  {
    m_legendJob.cancel();
    m_image = null;
    m_legendJob.schedule( 100 );
  }

  /**
   * This function updates the legend.
   * 
   * @param monitor
   *          A progress monitor.
   */
  protected void updateLegend( IProgressMonitor monitor ) throws CoreException
  {
    /* If no monitor was given, take a null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( "Zeichne Legende...", 1000 );
      monitor.subTask( "Erzeuge Knoten..." );

      /* It is not initialized, if the background color is still null. */
      initFromProperties();

      /* Create the nodes. */
      IThemeNode rootNode = NodeFactory.createRootNode( getMapModell(), null );
      IThemeNode[] nodes = rootNode.getChildren();

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Erzeuge Legende..." );

      /* Create the legend. */
      LegendExporter legendExporter = new LegendExporter();
      org.eclipse.swt.graphics.Image image = legendExporter.exportLegends( m_themeIds, nodes, Display.getCurrent(), new Insets( m_insets, m_insets, m_insets, m_insets ), m_backgroundColor.getRGB(), -1, -1, new SubProgressMonitor( monitor, 250 ) );

      /* Monitor. */
      monitor.subTask( "Konvertiere Legende..." );

      /* Convert to an AWT image. */
      BufferedImage awtImage = ImageConverter.convertToAWT( image.getImageData() );
      image.dispose();

      /* Monitor. */
      if( monitor.isCanceled() )
        return;

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Zeichne Legende..." );

      /* Draw the AWT image. */
      Graphics2D graphics = (Graphics2D) awtImage.getGraphics();
      graphics.setColor( Color.BLACK );
      graphics.setStroke( new BasicStroke( 2.0f ) );
      graphics.drawRect( 0, 0, awtImage.getWidth(), awtImage.getHeight() );
      graphics.dispose();

      /* Store the AWT image. */
      m_image = awtImage;

      /* Fire a repaint request. */
      fireRepaintRequested( null );

      /* Monitor. */
      monitor.worked( 250 );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }
}