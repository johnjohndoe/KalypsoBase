/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.grid.tiff.TIFFUtilities;

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
   * The tiled image.
   */
  private TiledImage m_image;

  /**
   * The minimum.
   */
  private BigDecimal m_min;

  /**
   * The maximum.
   */
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
  public TiffGeoGrid( Coordinate origin, Coordinate offsetX, Coordinate offsetY, String sourceCRS, File imageFile, int sizeX, int sizeY )
  {
    super( origin, offsetX, offsetY, sourceCRS );

    m_imageFile = imageFile;
    m_sizeX = sizeX;
    m_sizeY = sizeY;

    m_image = createTIFF( imageFile, sizeX, sizeY );
    m_min = null;
    m_max = null;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSizeX()
   */
  @Override
  public int getSizeX( )
  {
    if( m_sizeX < 1 )
      m_sizeX = m_image.getWidth();

    return m_sizeX;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSizeY()
   */
  @Override
  public int getSizeY( )
  {
    if( m_sizeY < 1 )
      m_sizeY = m_image.getHeight();

    return m_sizeY;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getValue(int, int)
   */
  @Override
  public double getValue( int x, int y )
  {
    if( m_image == null )
      return Double.NaN;

    int tileX = m_image.XToTileX( x );
    int tileY = m_image.YToTileY( y );

    Raster tile = m_image.getTile( tileX, tileY );

    return tile.getSampleDouble( x, y, 0 );
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMin()
   */
  @Override
  public BigDecimal getMin( )
  {
    if( m_min == null )
      calculateMinMax();

    return m_min;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMax()
   */
  @Override
  public BigDecimal getMax( )
  {
    if( m_max == null )
      calculateMinMax();

    return m_max;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#setMin(java.math.BigDecimal)
   */
  @Override
  public void setMin( BigDecimal minValue )
  {
    m_min = minValue;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#setMax(java.math.BigDecimal)
   */
  @Override
  public void setMax( BigDecimal maxValue )
  {
    m_max = maxValue;
  }

  /**
   * @see org.kalypso.grid.IWriteableGeoGrid#setValue(int, int, double)
   */
  @Override
  public void setValue( int x, int y, double value ) throws GeoGridException
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

  /**
   * @see org.kalypso.grid.IWriteableGeoGrid#setStatistically(java.math.BigDecimal, java.math.BigDecimal)
   */
  @Override
  public void setStatistically( BigDecimal min, BigDecimal max )
  {
  }

  /**
   * @see org.kalypso.grid.IWriteableGeoGrid#saveStatistically()
   */
  @Override
  public void saveStatistically( )
  {
  }

  /**
   * @see org.kalypso.grid.AbstractGeoGrid#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_image != null )
      m_image.dispose();

    m_imageFile = null;
    m_sizeX = -1;
    m_sizeY = -1;
    m_image = null;
    m_min = null;
    m_max = null;

    super.dispose();
  }

  private TiledImage createTIFF( File imageFile, int sizeX, int sizeY )
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

    /* Load the TIFF. */
    RenderedOp renderedOp = TIFFUtilities.loadTiff( imageFile );

    /* The dimensions should be automatically calculated. */
    m_sizeX = -1;
    m_sizeY = -1;

    return new TiledImage( renderedOp, false );
  }

  private void calculateMinMax( )
  {
    try
    {
      /* Calculate the min/max values. */
      IGeoWalkingStrategy walkingStrategy = getWalkingStrategy();
      MinMaxRasterWalker walker = new MinMaxRasterWalker();
      walkingStrategy.walk( this, walker, null, new NullProgressMonitor() );

      /* Set the min/max values. */
      m_min = BigDecimal.valueOf( walker.getMin() );
      m_max = BigDecimal.valueOf( walker.getMax() );
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
    }
  }

  public void save( )
  {
    TIFFUtilities.saveTiff( m_image, 100, 100, m_imageFile );
  }
}