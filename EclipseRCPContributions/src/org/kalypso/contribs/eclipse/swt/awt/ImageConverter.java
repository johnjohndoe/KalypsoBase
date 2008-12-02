/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * Code taken from: http://www.eclipse.org/swt/snippets/
 */
package org.kalypso.contribs.eclipse.swt.awt;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Gernot Belger
 */
public class ImageConverter
{
  public static BufferedImage convertToAWT( final ImageData data )
  {
    ColorModel colorModel = null;
    final PaletteData palette = data.palette;
    if( palette.isDirect )
    {
      colorModel = new DirectColorModel( data.depth, palette.redMask, palette.greenMask, palette.blueMask );
      final BufferedImage bufferedImage = new BufferedImage( colorModel, colorModel.createCompatibleWritableRaster( data.width, data.height ), false, null );
// final BufferedImage bufferedImage = new BufferedImage( data.width, data.height, BufferedImage.TYPE_INT_ARGB );
      colorModel = bufferedImage.getColorModel();

      final WritableRaster raster = bufferedImage.getRaster();
      final int[] pixelArray = new int[3];
      for( int y = 0; y < data.height; y++ )
      {
        for( int x = 0; x < data.width; x++ )
        {
          final int pixel = data.getPixel( x, y );
          final RGB rgb = palette.getRGB( pixel );
          pixelArray[0] = rgb.red;
          pixelArray[1] = rgb.green;
          pixelArray[2] = rgb.blue;
          raster.setPixels( x, y, 1, 1, pixelArray );
        }
      }
      return bufferedImage;
    }
    else
    {
      final RGB[] rgbs = palette.getRGBs();
      final byte[] red = new byte[rgbs.length];
      final byte[] green = new byte[rgbs.length];
      final byte[] blue = new byte[rgbs.length];
      for( int i = 0; i < rgbs.length; i++ )
      {
        final RGB rgb = rgbs[i];
        red[i] = (byte) rgb.red;
        green[i] = (byte) rgb.green;
        blue[i] = (byte) rgb.blue;
      }
      if( data.transparentPixel != -1 )
      {
        colorModel = new IndexColorModel( data.depth, rgbs.length, red, green, blue, data.transparentPixel );
      }
      else
      {
        colorModel = new IndexColorModel( data.depth, rgbs.length, red, green, blue );
      }
      final BufferedImage bufferedImage = new BufferedImage( colorModel, colorModel.createCompatibleWritableRaster( data.width, data.height ), false, null );
      final WritableRaster raster = bufferedImage.getRaster();
      final int[] pixelArray = new int[1];
      for( int y = 0; y < data.height; y++ )
      {
        for( int x = 0; x < data.width; x++ )
        {
          final int pixel = data.getPixel( x, y );
          pixelArray[0] = pixel;
          raster.setPixel( x, y, pixelArray );
        }
      }
      return bufferedImage;
    }
  }

  public static ImageData convertToSWT( final BufferedImage bufferedImage )
  {
    if( bufferedImage.getColorModel() instanceof DirectColorModel )
    {
      final DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
      final PaletteData palette = new PaletteData( colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask() );
      final ImageData data = new ImageData( bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette );
      final WritableRaster raster = bufferedImage.getRaster();
      final int[] pixelArray = new int[3];
      for( int y = 0; y < data.height; y++ )
      {
        for( int x = 0; x < data.width; x++ )
        {
          raster.getPixel( x, y, pixelArray );
          final int pixel = palette.getPixel( new RGB( pixelArray[0], pixelArray[1], pixelArray[2] ) );
          data.setPixel( x, y, pixel );
        }
      }
      return data;
    }
    else if( bufferedImage.getColorModel() instanceof IndexColorModel )
    {
      final IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
      final int size = colorModel.getMapSize();
      final byte[] reds = new byte[size];
      final byte[] greens = new byte[size];
      final byte[] blues = new byte[size];
      colorModel.getReds( reds );
      colorModel.getGreens( greens );
      colorModel.getBlues( blues );
      final RGB[] rgbs = new RGB[size];
      for( int i = 0; i < rgbs.length; i++ )
      {
        rgbs[i] = new RGB( reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF );
      }
      final PaletteData palette = new PaletteData( rgbs );
      final ImageData data = new ImageData( bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette );
      data.transparentPixel = colorModel.getTransparentPixel();
      final WritableRaster raster = bufferedImage.getRaster();
      final int[] pixelArray = new int[1];
      for( int y = 0; y < data.height; y++ )
      {
        for( int x = 0; x < data.width; x++ )
        {
          raster.getPixel( x, y, pixelArray );
          data.setPixel( x, y, pixelArray[0] );
        }
      }
      return data;
    }
    return null;
  }
}
