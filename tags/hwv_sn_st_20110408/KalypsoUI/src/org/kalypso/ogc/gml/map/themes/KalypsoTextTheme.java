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
package org.kalypso.ogc.gml.map.themes;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.contribs.eclipse.swt.awt.ImageConverter;
import org.kalypso.contribs.eclipse.swt.graphics.FontUtilities;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.themes.ThemeUtilities;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypso.util.themes.text.TextUtilities;

/**
 * This theme displays a text on the map.
 * 
 * @author Holger Albert
 */
public class KalypsoTextTheme extends AbstractImageTheme
{
  /**
   * The background color.
   */
  protected org.eclipse.swt.graphics.Color m_backgroundColor;

  /**
   * The text, which should be shown.
   */
  protected String m_text;

  /**
   * The constructor
   * 
   * @param name
   *          The name of the theme.
   * @param mapModell
   *          The map modell to use.
   */
  public KalypsoTextTheme( I10nString name, IMapModell mapModell )
  {
    super( name, "text", mapModell );

    /* Initialize. */
    m_backgroundColor = null;
    m_text = null;
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractKalypsoTheme#getDefaultIcon()
   */
  @Override
  public ImageDescriptor getDefaultIcon( )
  {
    return KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.IMAGE_THEME_TEXT );
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
      monitor.beginTask( "Zeichne Text...", 1000 );
      monitor.subTask( "Initialisiere Thema..." );

      /* Initialize properties. */
      initFromProperties();

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( "Erzeuge Text..." );

      /* Create the text. */
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
          /* Create the text. */
          image[0] = createSwtImage();
        }
      } );

      /* Monitor. */
      monitor.worked( 500 );
      monitor.subTask( "Konvertiere Text..." );

      /* Convert to an AWT image. */
      BufferedImage awtImage = ImageConverter.convertToAWT( image[0].getImageData() );
      image[0].dispose();

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
   * @see org.kalypso.ogc.gml.AbstractImageTheme#dispose()
   */
  @Override
  public void dispose( )
  {
    m_text = null;

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
    m_text = null;

    /* Get the properties. */
    String horizontalProperty = getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, null );
    String verticalProperty = getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, null );
    String backgroundColorProperty = getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, null );
    String textProperty = getProperty( TextUtilities.THEME_PROPERTY_TEXT, null );

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

    /* Check the text. */
    if( textProperty != null && textProperty.length() > 0 )
      m_text = TextUtilities.checkText( textProperty );
  }

  /**
   * This function creates an SWT image.
   * 
   * @return The SWT image.
   */
  protected org.eclipse.swt.graphics.Image createSwtImage( )
  {
    /* Is a text available? */
    if( m_text == null || m_text.length() == 0 )
      return null;

    /* Get the font. */
    Font smallFont = JFaceResources.getFont( JFaceResources.DIALOG_FONT );
    Font bigFont = FontUtilities.changeHeightAndStyle( smallFont.getDevice(), smallFont, 35, SWT.BOLD );

    /* Create a helper image. */
    org.eclipse.swt.graphics.Image helperImage = new org.eclipse.swt.graphics.Image( bigFont.getDevice(), 100, 100 );
    GC helperGC = new GC( helperImage );
    helperGC.setFont( bigFont );

    /* Get the text extent. */
    Point textExtent = helperGC.textExtent( m_text );
    int width = textExtent.x;
    int height = textExtent.y;

    /* Dispose the helper image. */
    helperImage.dispose();
    helperGC.dispose();

    Color white = bigFont.getDevice().getSystemColor( SWT.COLOR_WHITE );
    Color black = bigFont.getDevice().getSystemColor( SWT.COLOR_BLACK );
    PaletteData palette = new PaletteData( new RGB[] { white.getRGB(), m_backgroundColor.getRGB(), black.getRGB() } );

    /* Create a new image. */
    ImageData newImageData = new ImageData( width, height, 32, palette );
    newImageData.transparentPixel = 0;
    org.eclipse.swt.graphics.Image newImage = new org.eclipse.swt.graphics.Image( bigFont.getDevice(), newImageData );
    GC newGC = new GC( newImage );
    newGC.setFont( bigFont );

    /* Draw the text. */
    newGC.setBackground( m_backgroundColor );
    newGC.setForeground( black );
    newGC.drawText( m_text, 0, 0 );

    /* Dispose the new image. */
    newGC.dispose();

    return newImage;
  }
}