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

import java.awt.image.Raster;
import java.math.BigDecimal;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A {@link IGeoGrid} implementations based on the JavaAdvancedImaging API (JAI).<br>
 * Provides a {@link IGeoGrid} for various image file formats (jpg, tiff, png, gif, ...).<br>
 * The underlying image must be a greyscale'd image whose greyvalues represent the grid values.
 * <p>
 * The values of this grid are accessed on demand, which results in the following behaviour: the first access is quite
 * slow (several seconds), but succeeding access is accpetable fast. However, no remarkable memory consumption is
 * noticed.
 * </p>
 * 
 * @author Dejan
 */
public class ImageGeoGrid extends AbstractGeoGrid implements IGeoGrid
{
  private int m_sizeX;

  private int m_sizeY;

  private RenderedOp m_image;

  private BigDecimal m_min;

  private BigDecimal m_max;

  public ImageGeoGrid( final URL imageURL, final Coordinate origin, final Coordinate offsetX, final Coordinate offsetY, final String sourceCRS )
  {
    super( origin, offsetX, offsetY, sourceCRS );

    m_image = JAI.create( "url", imageURL );

    m_sizeX = -1;
    m_sizeY = -1;
    m_min = null;
    m_max = null;

    // initialize();
  }

  /**
   * @see org.kalypso.gis.doubleraster.grid.DoubleGrid#getValue(int, int)
   */
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

  /**
   * @see org.kalypso.gis.doubleraster.grid.DoubleGrid#getSizeX()
   */
  @Override
  public int getSizeX( )
  {
    if( m_sizeX < 1 )
      m_sizeX = m_image.getWidth();

    return m_sizeX;
  }

  /**
   * @see org.kalypso.gis.doubleraster.grid.DoubleGrid#getSizeY()
   */
  @Override
  public int getSizeY( )
  {
    if( m_sizeY < 1 )
      m_sizeY = m_image.getHeight();

    return m_sizeY;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMin()
   */
  @Override
  public BigDecimal getMin( )
  {
    if( m_min == null )
      initialize();

    return m_min;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMax()
   */
  @Override
  public BigDecimal getMax( )
  {
    if( m_max == null )
      initialize();

    return m_max;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#setMin(java.math.BigDecimal)
   */
  @Override
  public void setMin( final BigDecimal minValue )
  {
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#setMax(java.math.BigDecimal)
   */
  @Override
  public void setMax( final BigDecimal maxValue )
  {
  }

  /**
   * @see org.kalypso.gis.doubleraster.grid.DoubleGrid#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_image != null )
      m_image.dispose();

    m_image = null;
    m_min = null;
    m_max = null;
  }

  private void initialize( )
  {
    try
    {
      final IGeoWalkingStrategy walkingStrategy = getWalkingStrategy();
      final MinMaxRasterWalker walker = new MinMaxRasterWalker();
      walkingStrategy.walk( this, walker, null, new NullProgressMonitor() );

      m_min = BigDecimal.valueOf( walker.getMin() );
      m_max = BigDecimal.valueOf( walker.getMax() );
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }
}