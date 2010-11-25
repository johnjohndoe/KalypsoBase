/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.grid.tiff;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.File;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.IGeoGrid;

import com.sun.media.jai.codec.TIFFEncodeParam;

/**
 * This class contains functions for handling with TIFFs.
 * 
 * @author Holger Albert
 */
public class TIFFUtilities
{
  /**
   * The constructor.
   */
  private TIFFUtilities( )
  {
  }

  /**
   * This function creates a empty TIFF.
   * 
   * @param dataType
   *          The data type. @see {@link java.awt.image.DataBuffer}.
   * @param width
   *          The width of the TIFF.
   * @param height
   *          The height of the TIFF.
   * @return The empty TIFF.
   */
  public static TiledImage createTiff( int dataType, int width, int height )
  {
    /* Create a float data sample model. */
    SampleModel sampleModel = RasterFactory.createBandedSampleModel( dataType, width, height, 1 );

    /* Create a compatible color model. */
    ColorModel colorModel = PlanarImage.createColorModel( sampleModel );

    /* Create a writable raster. */
    Raster raster = RasterFactory.createWritableRaster( sampleModel, new Point( 0, 0 ) );

    /* Create a tiled image. */
    TiledImage tiledImage = new TiledImage( 0, 0, width, height, 0, 0, sampleModel, colorModel );
    tiledImage.setData( raster );

    return tiledImage;
  }

  /**
   * This function creates a TIFF with data within the given data buffer.
   * 
   * @param dataBuffer
   *          The data buffer with the data for the TIFF. @see {@link java.awt.image.DataBuffer}.
   * @param width
   *          The width of the TIFF.
   * @param height
   *          The height of the TIFF.
   * @return The TIFF.
   */
  public static TiledImage createTiff( DataBuffer dataBuffer, int width, int height )
  {
    /* Create a float data sample model. */
    SampleModel sampleModel = RasterFactory.createBandedSampleModel( dataBuffer.getDataType(), width, height, 1 );

    /* Create a compatible color model. */
    ColorModel colorModel = PlanarImage.createColorModel( sampleModel );

    /* Create a writable raster. */
    Raster raster = RasterFactory.createWritableRaster( sampleModel, dataBuffer, new Point( 0, 0 ) );

    /* Create a tiled image. */
    TiledImage tiledImage = new TiledImage( 0, 0, width, height, 0, 0, sampleModel, colorModel );
    tiledImage.setData( raster );

    return tiledImage;
  }

  public static RenderedOp loadTiff( File file )
  {
    return JAI.create( "fileload", file.getAbsolutePath() );
  }

  /**
   * This function simply copies all values from the geo grid to the TIFF. It may not be very perfomant. The TIFF must
   * have the same dimensions as the geo grid.
   * 
   * @param grid
   *          The geo grid. From it the values will be retrieved.
   * @param image
   *          The TIFF. To it the values will be copied.
   */
  public static void copyGeoGridToTiff( IGeoGrid grid, TiledImage image ) throws GeoGridException
  {
    int sizeY = grid.getSizeY();
    int sizeX = grid.getSizeX();
    if( sizeX != image.getMaxX() || sizeY != image.getMaxY() )
      throw new GeoGridException( "The size of the geo grid does not match the size of the TIFF...", null );

    for( int y = 0; y < sizeY; y++ )
    {
      for( int x = 0; x < sizeX; x++ )
        image.setSample( x, y, 0, grid.getValue( x, y ) );
    }
  }

  /**
   * This function saves the TIFF.
   * 
   * @param image
   *          The TIFF.
   * @param tileWidth
   *          The width of the tile. If width and height are valid (>0), the TIFF will be saved with tiles.
   * @param tileHeight
   *          The height of the tile. If width and height are valid (>0), the TIFF will be saved with tiles.
   * @param file
   *          The path of the target file.
   */
  public static void saveTiff( TiledImage image, int tileWidth, int tileHeight, File file )
  {
    /* Save the image on a file. */
    TIFFEncodeParam tep = new TIFFEncodeParam();
    tep.setCompression( TIFFEncodeParam.COMPRESSION_PACKBITS );

    /* Set tile options, if wanted. */
    if( tileWidth > 0 && tileHeight > 0 )
    {
      tep.setTileSize( tileWidth, tileHeight );
      tep.setWriteTiled( true );
    }

    /* Save the file. */
    JAI.create( "filestore", image, file.getAbsolutePath(), "TIFF", tep );
  }
}