/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of
 * the License.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.kalypso.contribs.eclipse.swt.udig;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import javax.swing.Icon;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;

/**
 * Provides a bunch of Utility methods for converting between AWT and SWT
 * 
 * @author jesse
 * @since 1.1.0
 */
public final class AWTSWTImageUtils
{
  public static Path createPath( final PathIterator p, final Device device )
  {
    if( p.isDone() )
    {
      return null;
    }

    final float[] current = new float[6];
    final Path path = new Path( device );
    while( !p.isDone() )
    {
      final int result = p.currentSegment( current );
      switch( result )
      {
        case PathIterator.SEG_CLOSE:
          path.close();
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo( current[0], current[1] );
          break;
        case PathIterator.SEG_MOVETO:
          path.moveTo( current[0], current[1] );
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo( current[0], current[1], current[2], current[3] );
          break;
        case PathIterator.SEG_CUBICTO:
          path.cubicTo( current[0], current[1], current[2], current[3], current[4], current[5] );
          break;
        default:
      }
      p.next();
    }
    return path;
  }

  /**
   * Creates an image with a depth of 24 and has a transparency channel.
   * 
   * @param device
   *          device to use for creating the image
   * @param width
   *          the width of the final image
   * @param height
   *          the height of the final image
   * @return an image with a depth of 24 and has a transparency channel.
   */
  public static Image createDefaultImage( final Device device, final int width, final int height )
  {
    AWTSWTImageUtils.checkAccess();
    ImageData swtdata = null;
    PaletteData palette;
    int depth;

    depth = 24;
    palette = new PaletteData( 0xFF0000, 0xFF00, 0xFF );
    swtdata = new ImageData( width, height, depth, palette );
    swtdata.transparentPixel = -1;
    swtdata.alpha = -1;
    swtdata.alphaData = new byte[swtdata.data.length];
    for( int i = 0; i < swtdata.alphaData.length; i++ )
    {
      swtdata.alphaData[i] = 0;
    }
    return new Image( device, swtdata );

  }

  public static Image createDefaultImage( final Display display, final int width, final int height )
  {
    AWTSWTImageUtils.checkAccess();
    ImageData swtdata = null;
    PaletteData palette;
    int depth;

    depth = 24;
    palette = new PaletteData( 0xFF0000, 0xFF00, 0xFF );
    swtdata = new ImageData( width, height, depth, palette );
    swtdata.transparentPixel = 0;
    // swtdata.transparentPixel = -1;
    swtdata.alpha = -1;
    swtdata.alphaData = new byte[swtdata.data.length];
    for( int i = 0; i < swtdata.alphaData.length; i++ )
    {
      swtdata.alphaData[i] = (byte) 255;
    }

    return new Image( display, swtdata );
  }

  /** Create a buffered image that can be be converted to SWTland later */
  public static BufferedImage createBufferedImage( final int w, final int h )
  {
    // AWTSWTImageUtils.checkAccess();
    return new BufferedImage( w, h, BufferedImage.TYPE_4BYTE_ABGR_PRE );
  }

  public static Image createSWTImage( final RenderedImage image, final boolean transparent )
  {
    AWTSWTImageUtils.checkAccess();

    ImageData data;
    if( image instanceof BufferedImage )
    {
      data = AWTSWTImageUtils.createImageData( (BufferedImage) image );
    }
    else
    {
      data = AWTSWTImageUtils.createImageData( image, transparent );
    }

    return new org.eclipse.swt.graphics.Image( Display.getDefault(), data );
  }

  public static ImageData createImageData( final RenderedImage image, final boolean transparent )
  {
    // AWTSWTImageUtils.checkAccess();

    ImageData swtdata = null;
    final int width = image.getWidth();
    final int height = image.getHeight();
    PaletteData palette;
    int depth;

    depth = 24;
    palette = new PaletteData( 0xFF, 0xFF00, 0xFF0000 );
    swtdata = new ImageData( width, height, depth, palette );
    final Raster raster = image.getData();
    final int numbands = raster.getNumBands();
    final int[] awtdata = raster.getPixels( 0, 0, width, height, new int[width * height * numbands] );
    final int step = swtdata.depth / 8;

    final byte[] data = swtdata.data;
    swtdata.transparentPixel = -1;
    int baseindex = 0;
    for( int y = 0; y < height; y++ )
    {
      int idx = (0 + y) * swtdata.bytesPerLine + 0 * step;

      for( int x = 0; x < width; x++ )
      {
        final int pixel = x + y * width;
        baseindex = pixel * numbands;

        data[idx++] = (byte) awtdata[baseindex + 2];
        data[idx++] = (byte) awtdata[baseindex + 1];
        data[idx++] = (byte) awtdata[baseindex];
        if( numbands == 4 && transparent )
        {
          swtdata.setAlpha( x, y, awtdata[baseindex + 3] );
        }
      }
    }
    return swtdata;
  }

  public static ImageDescriptor createImageDescriptor( final RenderedImage image, final boolean transparent )
  {
    AWTSWTImageUtils.checkAccess();
    return new ImageDescriptor()
    {

      @Override
      public ImageData getImageData( )
      {
        return createImageData( image, transparent );
      }
    };
  }

  /**
   * Creates an image descriptor that from the source image.
   * 
   * @param image
   *          source image
   * @return an image descriptor that from the source image.
   */
  public static ImageDescriptor createImageDescriptor( final BufferedImage image )
  {
    AWTSWTImageUtils.checkAccess();
    return new ImageDescriptor()
    {

      @Override
      public ImageData getImageData( )
      {
        return AWTSWTImageUtils.createImageData( image );
      }
    };
  }

  /**
   * Converts a BufferedImage to an SWT Image. You are responsible for disposing the created image. This method is
   * faster than creating a SWT image from a RenderedImage so use this method if possible.
   * 
   * @param image
   *          source image.
   * @return a swtimage showing the source image.
   */
  public static Image convertToSWTImage( final BufferedImage image )
  {
    AWTSWTImageUtils.checkAccess();
    ImageData data;
    data = AWTSWTImageUtils.createImageData( image );

    return new org.eclipse.swt.graphics.Image( Display.getDefault(), data );
  }

  /**
   * Creates an ImageData from the 0,0,width,height section of the source BufferedImage.
   * <p>
   * This method is faster than creating the ImageData from a RenderedImage so use this method if possible.
   * </p>
   * 
   * @param image
   *          source image.
   * @return an ImageData from the 0,0,width,height section of the source BufferedImage
   */
  public static ImageData createImageData( final BufferedImage image )
  {
    // AWTSWTImageUtils.checkAccess();

    if( image.getType() != BufferedImage.TYPE_3BYTE_BGR )
    {
      return createImageData( image, image.getTransparency() != Transparency.OPAQUE );
    }

    final int width = image.getWidth();
    final int height = image.getHeight();
    final int bands = image.getColorModel().getColorSpace().getNumComponents();
    final int depth = 24;
    final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    final ImageData data = new ImageData( width, height, depth, new PaletteData( 0x0000ff, 0x00ff00, 0xff0000 ), width * bands, pixels );
    return data;
  }

  /**
   * Converts a RenderedImage to an SWT Image. You are responsible for disposing the created image. This method is
   * slower than calling {@link SWTGraphics#createSWTImage(BufferedImage, int, int)}.
   * 
   * @param image
   *          source image.
   * @param width
   *          the width of the final image
   * @param height
   *          the height of the final image
   * @return a swtimage showing the 0,0,width,height rectangle of the source image.
   */
  public static Image createSWTImage( final RenderedImage image )
  {
    AWTSWTImageUtils.checkAccess();
    final ImageData data = AWTSWTImageUtils.createImageData( image );

    return new org.eclipse.swt.graphics.Image( Display.getDefault(), data );
  }

  /**
   * Creates an ImageData from the source RenderedImage.
   * <p>
   * This method is slower than using {@link createImageData}.
   * </p>
   * 
   * @param image
   *          source image.
   * @return an ImageData from the source RenderedImage.
   */
  public static ImageData createImageData( final RenderedImage image )
  {
    AWTSWTImageUtils.checkAccess();

    if( image instanceof BufferedImage )
    {
      return createImageData( (BufferedImage) image );
    }
    final int depth = 24;
    final int width = image.getWidth();
    final int height = image.getHeight();
    final byte[] pixels = ((DataBufferByte) image.getTile( 0, 0 ).getDataBuffer()).getData();
    final ImageData data = new ImageData( width, height, depth, new PaletteData( 0xff0000, 0x00ff00, 0x0000ff ), width, pixels );
    return data;
  }

  public static java.awt.Color swtColor2awtColor( final GC gc, final Color swt )
  {
    final java.awt.Color awt = new java.awt.Color( swt.getRed(), swt.getGreen(), swt.getBlue(), gc.getAlpha() );
    return awt;
  }

  static void checkAccess( )
  {
    if( Display.getCurrent() == null )
    {
      SWT.error( SWT.ERROR_THREAD_INVALID_ACCESS );
    }
  }

  /**
   * Converts SWT FontData to a AWT Font
   * 
   * @param fontData
   *          the font data
   * @return the equivalent AWT font
   */
  public static java.awt.Font swtFontToAwt( final FontData fontData )
  {
    int style = java.awt.Font.PLAIN;
    if( (fontData.getStyle() & SWT.BOLD) == SWT.BOLD )
    {
      style = java.awt.Font.BOLD;
    }
    if( (fontData.getStyle() & SWT.ITALIC) == SWT.ITALIC )
    {
      style |= java.awt.Font.ITALIC;
    }

    final java.awt.Font font = new java.awt.Font( fontData.getName(), style, fontData.getHeight() );
    return font;
  }

  /**
   * Converts an AWTFont to a SWT Font
   * 
   * @param font
   *          and AWT Font
   * @param fontRegistry
   * @return the equivalent SWT Font
   */
  public static org.eclipse.swt.graphics.Font awtFontToSwt( final java.awt.Font font, final FontRegistry fontRegistry )
  {
    final String fontName = font.getFontName();
    if( fontRegistry.hasValueFor( fontName ) )
    {
      return fontRegistry.get( fontName );
    }

    int style = 0;
    if( (font.getStyle() & java.awt.Font.BOLD) == java.awt.Font.BOLD )
    {
      style = SWT.BOLD;
    }
    if( (font.getStyle() & java.awt.Font.ITALIC) == java.awt.Font.ITALIC )
    {
      style |= SWT.ITALIC;
    }
    final FontData data = new FontData( fontName, font.getSize(), style );
    fontRegistry.put( fontName, new FontData[] { data } );
    return fontRegistry.get( fontName );
  }

  /**
   * Takes an AWT Font.
   * 
   * @param style
   * @return
   */
  public static int toFontStyle( final java.awt.Font f )
  {
    int s = SWT.NORMAL;

    if( f.isItalic() )
    {
      s = s | SWT.ITALIC;
    }
    if( f.isBold() )
    {
      s = s | SWT.BOLD;
    }
    return s;
  }

  /**
   * Converts a Swing {@link Icon} to an {@link ImageDescriptor}
   * 
   * @param icon
   *          icon to convert
   * @return an ImageDescriptor
   */
  public static ImageDescriptor awtIcon2ImageDescriptor( final Icon icon )
  {
    final ImageDescriptor descriptor = new ImageDescriptor()
    {

      @Override
      public ImageData getImageData( )
      {
        final BufferedImage image = createBufferedImage( icon.getIconWidth(), icon.getIconHeight() );
        final Graphics2D g = image.createGraphics();
        try
        {
          icon.paintIcon( null, g, 0, 0 );
        }
        finally
        {
          g.dispose();
        }
        final ImageData data = createImageData( image );
        return data;
      }

    };
    return descriptor;
  }

}
