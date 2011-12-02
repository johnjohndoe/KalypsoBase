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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.swt.awt.ImageConverter;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.LegendExporter;
import org.kalypso.ogc.gml.outline.nodes.NodeFactory;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.themes.ThemeUtilities;
import org.kalypso.util.themes.legend.LegendUtilities;
import org.kalypso.util.themes.position.PositionUtilities;

/**
 * The legend theme is able to display available legends for all or a subset of themes in a map.
 * 
 * @author Andreas Doemming (original)
 * @author Holger Albert (modifications)
 */
public class KalypsoLegendTheme extends AbstractImageTheme
{
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
   * The constructor
   * 
   * @param name
   *          The name of the theme.
   * @param mapModell
   *          The map modell to use.
   */
  public KalypsoLegendTheme( I10nString name, IMapModell mapModell )
  {
    super( name, "legend", mapModell ); //$NON-NLS-1$

    /* Initialize. */
    m_backgroundColor = null;
    m_insets = -1;
    m_themeIds = null;
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
   * @see org.kalypso.ogc.gml.map.themes.AbstractImageTheme#updateImage(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected Image updateImage( IProgressMonitor monitor )
  {
    /* If no monitor was given, take a null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( "Zeichne Legende...", 1000 );
      monitor.subTask( "Initialisiere Thema..." );

      /* Initialize properties. */
      initFromProperties();

      /* Create the nodes. */
      IThemeNode rootNode = NodeFactory.createRootNode( getMapModell(), null );
      final IThemeNode[] nodes = rootNode.getChildren();

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Erzeuge Legende..." );

      /* Create the legend. */
      final SubProgressMonitor subMonitor = new SubProgressMonitor( monitor, 250 );
      final org.eclipse.swt.graphics.Image[] image = new org.eclipse.swt.graphics.Image[1];
      final Display display = PlatformUI.getWorkbench().getDisplay();
      display.syncExec( new Runnable()
      {
        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run( )
        {
          try
          {
            if( m_backgroundColor == null )
            {
              image[0] = null;
              return;
            }

            /* Create the legend. */
            LegendExporter legendExporter = new LegendExporter();
            image[0] = legendExporter.exportLegends( m_themeIds, nodes, display, new Insets( m_insets, m_insets, m_insets, m_insets ), m_backgroundColor.getRGB(), -1, -1, true, subMonitor );
          }
          catch( CoreException ex )
          {
            image[0] = null;

            if( ex.getStatus().getSeverity() != IStatus.CANCEL )
              ex.printStackTrace();
          }
        }
      } );

      /* If something happend during the export of the legend. */
      if( image[0] == null )
        return null;

      /* Monitor. */
      monitor.subTask( "Konvertiere Legende..." );

      /* Convert to an AWT image. */
      BufferedImage awtImage = ImageConverter.convertToAWT( image[0].getImageData() );
      image[0].dispose();

      /* Monitor. */
      if( monitor.isCanceled() )
        return null;

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Zeichne Rahmen..." );

      /* Draw a border in the AWT image. */
      Graphics2D graphics = (Graphics2D) awtImage.getGraphics();
      graphics.setColor( Color.BLACK );
      graphics.setStroke( new BasicStroke( 2.0f ) );
      graphics.drawRect( 0, 0, awtImage.getWidth(), awtImage.getHeight() );
      graphics.dispose();

      /* Monitor. */
      monitor.worked( 250 );

      return awtImage;
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.themes.AbstractImageTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_backgroundColor != null )
      m_backgroundColor.dispose();

    m_backgroundColor = null;
    m_insets = -1;
    m_themeIds = null;

    super.dispose();
  }

  /**
   * This function initializes the legend theme from its own properties. For these not found, defaults will be set.
   */
  private void initFromProperties( )
  {
    /* Default values. */
    updatePosition( PositionUtilities.RIGHT, PositionUtilities.BOTTOM );
    m_backgroundColor = new org.eclipse.swt.graphics.Color( Display.getCurrent(), 255, 255, 255 );
    m_insets = 10;
    m_themeIds = new String[] {};

    /* Get the properties. */
    String horizontalProperty = getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, null );
    String verticalProperty = getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, null );
    String backgroundColorProperty = getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, null );
    String insetsProperty = getProperty( LegendUtilities.THEME_PROPERTY_INSETS, null );
    String themeIdsProperty = getProperty( LegendUtilities.THEME_PROPERTY_THEME_IDS, null );

    /* Check the horizontal and vertical position. */
    int horizontal = PositionUtilities.checkHorizontalPosition( horizontalProperty );
    int vertical = PositionUtilities.checkVerticalPosition( verticalProperty );
    if( horizontal != -1 && vertical != -1 )
      updatePosition( horizontal, vertical );

    /* Check the background color. */
    org.eclipse.swt.graphics.Color backgroundColor = ThemeUtilities.checkBackgroundColor( Display.getCurrent(), backgroundColorProperty );
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
}