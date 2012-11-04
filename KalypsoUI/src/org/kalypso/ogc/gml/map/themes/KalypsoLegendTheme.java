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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.swt.awt.ImageConverter;
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.NodeFactory;
import org.kalypso.ogc.gml.outline.nodes.NodeLegendBuilder;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
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
  private RGB m_backgroundColor;

  private int m_insets = -1;

  /**
   * The ids of the selected themes.
   */
  private String[] m_themeIds;

  private int m_fontSize = -1;

  /**
   * @param name
   *          The name of the theme.
   * @param mapModel
   *          The map model to use.
   */
  public KalypsoLegendTheme( final I10nString name, final IMapModell mapModel )
  {
    super( name, "legend", mapModel ); //$NON-NLS-1$
  }

  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_LEGEND );
  }

  @Override
  protected Image updateImage( IProgressMonitor monitor )
  {
    /* If no monitor was given, take a null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( Messages.getString( "KalypsoLegendTheme_0" ), 1000 ); //$NON-NLS-1$
      monitor.subTask( Messages.getString( "KalypsoLegendTheme_1" ) ); //$NON-NLS-1$

      /* Initialize properties. */
      initFromProperties();

      /* Create the nodes. */
      final IThemeNode rootNode = NodeFactory.createRootNode( getMapModell(), null );
      final IThemeNode[] nodes = rootNode.getChildren();

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( Messages.getString( "KalypsoLegendTheme_2" ) ); //$NON-NLS-1$

      /* Create the legend. */
      final SubProgressMonitor subMonitor = new SubProgressMonitor( monitor, 250 );
      final org.eclipse.swt.graphics.Image[] image = new org.eclipse.swt.graphics.Image[1];
      final Display display = PlatformUI.getWorkbench().getDisplay();
      display.syncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          image[0] = doUpdateImage( nodes, display, subMonitor );
        }
      } );

      rootNode.dispose();

      /* If something happend during the export of the legend. */
      if( image[0] == null )
        return null;

      /* Monitor. */
      monitor.subTask( Messages.getString( "KalypsoLegendTheme_3" ) ); //$NON-NLS-1$

      /* Convert to an AWT image. */
      final BufferedImage awtImage = ImageConverter.convertToAWT( image[0].getImageData() );
      image[0].dispose();

      /* Monitor. */
      if( monitor.isCanceled() )
        return null;

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( Messages.getString( "KalypsoLegendTheme_4" ) ); //$NON-NLS-1$

      /* Draw a border in the AWT image. */
      final Graphics2D graphics = (Graphics2D)awtImage.getGraphics();
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

  protected org.eclipse.swt.graphics.Image doUpdateImage( final IThemeNode[] nodes, final Display display, final SubProgressMonitor subMonitor )
  {
    try
    {
      /* Create the legend. */
      final Insets insets = new Insets( m_insets, m_insets, m_insets, m_insets );

      final NodeLegendBuilder legendBuilder = new NodeLegendBuilder( m_themeIds, true );
      legendBuilder.setBackground( m_backgroundColor );
      legendBuilder.setInsets( insets );
      legendBuilder.setFontSize( m_fontSize );

      return legendBuilder.createLegend( nodes, display, subMonitor );
    }
    catch( final OperationCanceledException e )
    {
      // ignored
      return null;
    }
  }

  /**
   * This function initializes the legend theme from its own properties. For these not found, defaults will be set.
   */
  private void initFromProperties( )
  {
    /* Default values. */
    updatePosition( PositionUtilities.RIGHT, PositionUtilities.BOTTOM );
    m_backgroundColor = new RGB( 255, 255, 255 );
    m_insets = 10;
    m_themeIds = new String[] {};
    m_fontSize = 10;

    /* Get the properties. */
    final String horizontalProperty = getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, null );
    final String verticalProperty = getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, null );
    final String backgroundColorProperty = getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, null );
    final String insetsProperty = getProperty( LegendUtilities.THEME_PROPERTY_INSETS, null );
    final String themeIdsProperty = getProperty( LegendUtilities.THEME_PROPERTY_THEME_IDS, null );
    final String fontSizeProperty = getProperty( LegendUtilities.THEME_PROPERTY_FONT_SIZE, null );

    /* Check the horizontal and vertical position. */
    final int horizontal = PositionUtilities.checkHorizontalPosition( horizontalProperty );
    final int vertical = PositionUtilities.checkVerticalPosition( verticalProperty );
    if( horizontal != -1 && vertical != -1 )
      updatePosition( horizontal, vertical );

    /* Check the background color. */
    final RGB backgroundColor = ThemeUtilities.checkBackgroundColor( backgroundColorProperty );
    if( backgroundColor != null )
      m_backgroundColor = backgroundColor;

    /* Check the insets. */
    final int insets = LegendUtilities.checkInsets( insetsProperty );
    if( insets >= 1 && insets <= 25 )
      m_insets = insets;

    /* Check the theme ids. */
    final List<String> themeIds = LegendUtilities.verifyThemeIds( getMapModell(), themeIdsProperty );
    if( themeIds != null && themeIds.size() > 0 )
      m_themeIds = themeIds.toArray( new String[] {} );

    /* Check the font size. */
    final int fontSize = LegendUtilities.checkFontSize( fontSizeProperty );
    if( fontSize >= 1 && fontSize <= 25 )
      m_fontSize = fontSize;
  }
}