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
package org.kalypso.grid;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.math.BigDecimal;

import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.grid.tiff.TIFFUtilities;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * A writable tiff geo grid.
 * 
 * @author Holger Albert
 */
public class TiffGeoGrid extends AbstractGeoGrid implements IWriteableGeoGrid
{
  /**
   * The file of the TIFF.
   */
  private File m_imageFile;

  /**
   * The x dimension.
   */
  private int m_sizeX;

  /**
   * The y dimension.
   */
  private int m_sizeY;

  /**
   * The input stream of the TIFF.
   */
  private SeekableStream m_inputStream;

  /**
   * The tiled image.
   */
  private TiledImage m_image;

  private BigDecimal m_min;

  private BigDecimal m_max;

  /**
   * The constructor.
   * 
   * @param origin
   *          The origin of the raster.
   * @param offsetX
   *          The x offset.
   * @param offsetY
   *          The y offset.
   * @param sourceCRS
   *          The coordinate system.
   * @param imageFile
   *          The file of the TIFF.
   * @param sizeX
   *          The x dimension.
   * @param sizeY
   *          The y dimension.
   */
  public TiffGeoGrid( final Coordinate origin, final Coordinate offsetX, final Coordinate offsetY, final String sourceCRS, final File imageFile, final int sizeX, final int sizeY ) throws GeoGridException
  {
    super( origin, offsetX, offsetY, sourceCRS );

    m_imageFile = imageFile;
    m_sizeX = sizeX;
    m_sizeY = sizeY;

    m_inputStream = null;
    m_image = createTIFF( imageFile, sizeX, sizeY );
    m_min = null;
    m_max = null;
  }

  @Override
  public int getSizeX( )
  {
    if( m_sizeX < 1 )
      m_sizeX = m_image.getWidth();

    return m_sizeX;
  }

  @Override
  public int getSizeY( )
  {
    if( m_sizeY < 1 )
      m_sizeY = m_image.getHeight();

    return m_sizeY;
  }

  @Override
  public double getValue( final int x, final int y )
  {
    if( m_image == null )
      return Double.NaN;

    final int tileX = m_image.XToTileX( x );
    final int tileY = m_image.YToTileY( y );

    final Raster tile = m_image.getTile( tileX, tileY );

    return tile.getSampleDouble( x, y, 0 );
  }

  @Override
  public BigDecimal getMin( )
  {
    if( m_min == null )
      calculateMinMax();

    return m_min;
  }

  @Override
  public BigDecimal getMax( )
  {
    if( m_max == null )
      calculateMinMax();

    return m_max;
  }

  @Override
  public void setValue( final int x, final int y, final double value ) throws GeoGridException
  {
    /* Was the TIFF created/loaded? */
    if( m_image == null )
      throw new GeoGridException( "The TIFF was not created/loaded...", null );

    /* The min/max values needs to be recalculated. */
    m_min = null;
    m_max = null;

    /* Set the value. */
    m_image.setSample( x, y, 0, value );
  }

  @Override
  public void dispose( )
  {
    /* Close the input Stream. */
    IOUtils.closeQuietly( m_inputStream );

    /* Dispose the image. */
    if( m_image != null )
      m_image.dispose();

    /* Discard the references. */
    m_imageFile = null;
    m_sizeX = -1;
    m_sizeY = -1;
    m_inputStream = null;
    m_image = null;
    m_min = null;
    m_max = null;

    super.dispose();
  }

  private TiledImage createTIFF( final File imageFile, final int sizeX, final int sizeY ) throws GeoGridException
  {
    try
    {
      /* Store the image file. */
      m_imageFile = imageFile;

      /* Create the TIFF, using the given dimensions. */
      if( !imageFile.exists() )
      {
        m_sizeX = sizeX;
        m_sizeY = sizeY;

        return TIFFUtilities.createTiff( DataBuffer.TYPE_FLOAT, sizeX, sizeY );
      }

      /* Create the input stream. */
      m_inputStream = new FileSeekableStream( imageFile );

      /* Load the TIFF. */
      final RenderedOp renderedOp = TIFFUtilities.loadTiff( m_inputStream );

      /* The dimensions should be automatically calculated. */
      m_sizeX = -1;
      m_sizeY = -1;

      return new TiledImage( renderedOp, false );
    }
    catch( final Exception ex )
    {
      throw new GeoGridException( "Error while creating/loading the tiff image...", ex );
    }
  }

  private void calculateMinMax( )
  {
    try
    {
      /* Calculate the min/max values. */
      final IGeoWalkingStrategy walkingStrategy = getWalkingStrategy();
      final MinMaxRasterWalker walker = new MinMaxRasterWalker();
      walkingStrategy.walk( this, walker, null, new NullProgressMonitor() );

      /* Set the min/max values. */
      m_min = BigDecimal.valueOf( walker.getMin() );
      m_max = BigDecimal.valueOf( walker.getMax() );
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  public void save( )
  {
    TIFFUtilities.saveTiff( m_image, 100, 100, m_imageFile );
  }
}