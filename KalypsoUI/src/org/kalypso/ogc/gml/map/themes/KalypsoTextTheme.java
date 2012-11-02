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
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
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
  protected RGB m_background;

  /**
   * The text, which should be shown.
   */
  protected String m_text;

  /**
   * The font size.
   */
  protected int m_fontSize;

  /**
   * True, if the transparency is switched on.
   */
  protected boolean m_transparency;

  /**
   * The constructor
   * 
   * @param name
   *          The name of the theme.
   * @param mapModell
   *          The map modell to use.
   */
  public KalypsoTextTheme( final I10nString name, final IMapModell mapModell )
  {
    super( name, "text", mapModell ); //$NON-NLS-1$

    /* Initialize. */
    m_background = null;
    m_text = null;
    m_fontSize = -1;
    m_transparency = false;
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
      monitor.beginTask( Messages.getString( "KalypsoTextTheme_1" ), 1000 ); //$NON-NLS-1$
      monitor.subTask( Messages.getString( "KalypsoTextTheme_2" ) ); //$NON-NLS-1$

      /* Initialize properties. */
      initFromProperties();

      /* Monitor. */
      monitor.worked( 250 );
      monitor.subTask( Messages.getString( "KalypsoTextTheme_3" ) ); //$NON-NLS-1$

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
      monitor.subTask( Messages.getString( "KalypsoTextTheme_4" ) ); //$NON-NLS-1$

      /* Convert to an AWT image. */
      final BufferedImage awtImage = ImageConverter.convertToAWT( image[0].getImageData() );
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
    m_fontSize = -1;
    m_transparency = false;

    super.dispose();
  }

  /**
   * This function initializes the legend theme from its own properties. For these not found, defaults will be set.
   */
  private void initFromProperties( )
  {
    /* Default values. */
    updatePosition( PositionUtilities.RIGHT, PositionUtilities.BOTTOM );
    m_background = new RGB( 255, 255, 255 );
    m_text = null;
    m_fontSize = 10;
    m_transparency = false;

    /* Get the properties. */
    final String horizontalProperty = getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, null );
    final String verticalProperty = getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, null );
    final String backgroundColorProperty = getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, null );
    final String textProperty = getProperty( TextUtilities.THEME_PROPERTY_TEXT, null );
    final String fontSizeProperty = getProperty( TextUtilities.THEME_PROPERTY_FONT_SIZE, null );
    final String transparencyProperty = getProperty( TextUtilities.THEME_PROPERTY_TRANSPARENCY, null );

    /* Check the horizontal and vertical position. */
    final int horizontal = PositionUtilities.checkHorizontalPosition( horizontalProperty );
    final int vertical = PositionUtilities.checkVerticalPosition( verticalProperty );
    if( horizontal != -1 && vertical != -1 )
      updatePosition( horizontal, vertical );

    /* Check the background color. */
    final RGB backgroundColor = ThemeUtilities.checkBackgroundColor( backgroundColorProperty );
    if( backgroundColor != null )
      m_background = backgroundColor;

    /* Check the text. */
    if( textProperty != null && textProperty.length() > 0 )
      m_text = TextUtilities.checkText( textProperty );

    /* Check the font size. */
    final int fontSize = TextUtilities.checkFontSize( fontSizeProperty );
    if( fontSize >= 1 && fontSize <= 35 )
      m_fontSize = fontSize;

    /* Check the transparency. */
    m_transparency = TextUtilities.checkTransparency( transparencyProperty );
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
    final Font smallFont = JFaceResources.getFont( JFaceResources.DIALOG_FONT );
    final Font bigFont = FontUtilities.changeHeightAndStyle( smallFont.getDevice(), smallFont, m_fontSize, SWT.BOLD );

    /* Create a helper image. */
    final org.eclipse.swt.graphics.Image helperImage = new org.eclipse.swt.graphics.Image( bigFont.getDevice(), 100, 100 );
    final GC helperGC = new GC( helperImage );
    helperGC.setFont( bigFont );

    /* Get the text extent. */
    final Point textExtent = helperGC.textExtent( m_text );
    final int width = textExtent.x;
    final int height = textExtent.y;

    /* Dispose the helper image. */
    helperImage.dispose();
    helperGC.dispose();

    /* Create the palette. */
    final Color white = bigFont.getDevice().getSystemColor( SWT.COLOR_WHITE );
    final Color black = bigFont.getDevice().getSystemColor( SWT.COLOR_BLACK );
    PaletteData palette = new PaletteData( new RGB[] { m_background, black.getRGB() } );
    if( m_transparency )
      palette = new PaletteData( new RGB[] { white.getRGB(), black.getRGB() } );

    /* Create a new image data. */
    final ImageData newImageData = new ImageData( width, height, 2, palette );
    if( m_transparency )
      newImageData.transparentPixel = 0;

    /* Create a new image. */
    final org.eclipse.swt.graphics.Image newImage = new org.eclipse.swt.graphics.Image( bigFont.getDevice(), newImageData );
    final GC newGC = new GC( newImage );
    newGC.setFont( bigFont );

    /* Draw the text. */
    final Color backgroundColor = new Color( newGC.getDevice(), m_background );
    if( !m_transparency )
      newGC.setBackground( backgroundColor );

    newGC.setForeground( black );
    newGC.drawText( m_text, 0, 0 );

    /* Dispose the new image. */
    newGC.dispose();
    backgroundColor.dispose();

    return newImage;
  }
}