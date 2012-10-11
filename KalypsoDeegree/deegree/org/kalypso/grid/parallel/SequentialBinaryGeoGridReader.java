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
package org.kalypso.grid.parallel;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.grid.AbstractGeoGrid;
import org.kalypso.grid.BinaryGeoGrid;
import org.kalypso.grid.BinaryGeoGridHeader;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.grid.IGeoWalkingStrategy;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author barbarins
 */
public abstract class SequentialBinaryGeoGridReader extends AbstractGeoGrid implements Closeable
{
  private static final String ERROR_RANDOM_ACCESS = "Random access not supported by this grid implementation"; //$NON-NLS-1$;

  /* Number of values per block */
  private final Integer m_blockSize = 1024 * 1024;

  private final DataInputStream m_gridStream;

  private long m_currentPosition = 0;

  private final long m_blockAmount;

  private final BinaryGeoGridHeader m_header;

  // TODO: replace templateGrid with something that only provides origin etc; just like RectifiedGridDomain...
  public SequentialBinaryGeoGridReader( final IGeoGrid templateGrid, final URL pUrl ) throws IOException, GeoGridException
  {
    super( templateGrid.getOrigin(), templateGrid.getOffsetX(), templateGrid.getOffsetY(), templateGrid.getSourceCRS() );

    /* Tries to find a file from the given url. */
    File gridFile = ResourceUtilities.findJavaFileFromURL( pUrl );
    if( gridFile == null )
      gridFile = FileUtils.toFile( pUrl );

    m_gridStream = new DataInputStream( new BufferedInputStream( new FileInputStream( gridFile ) ) );

    m_header = BinaryGeoGridHeader.read( m_gridStream );

    final long length = getSizeX() * getSizeY();
    m_blockAmount = (length / m_blockSize) + 1;
  }

  @Override
  public int getSizeX( )
  {
    return m_header.getSizeX();
  }

  @Override
  public int getSizeY( )
  {
    return m_header.getSizeY();
  }

  int getBlocksAmount( )
  {
    return (int) m_blockAmount;
  }

  private Double[] read( final int items ) throws IOException
  {
    final Double[] data = new Double[items];
    for( int i = 0; i < data.length; i++ )
    {
      try
      {
        final int rawValue = m_gridStream.readInt();
        data[i] = unscaleValue( rawValue );
      }
      catch( final EOFException e )
      {
        /* EOF: shorten block to real size */
        if( i == 0 )
          return null;

        final Double[] lastBlock = new Double[i];
        System.arraycopy( data, 0, lastBlock, 0, i );
        return lastBlock;
      }
    }
    return data;
  }

  private Double unscaleValue( final int rawValue )
  {
    /* NO_DATA */
    if( rawValue == BinaryGeoGrid.NO_DATA )
      return null;

    return new BigDecimal( BigInteger.valueOf( rawValue ), getScale() ).doubleValue();
  }

  public int getScale( )
  {
    return m_header.getScale();
  }

  @Override
  public void dispose( )
  {
    try
    {
      close();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void close( ) throws IOException
  {
    m_gridStream.close();
  }

  ParallelBinaryGridProcessorBean getNextBlock( ) throws IOException
  {
    final Double[] data = read( m_blockSize );
    if( data == null )
      return null;

    final ParallelBinaryGridProcessorBean bean = new ParallelBinaryGridProcessorBean( data, m_currentPosition );

    m_currentPosition += data.length;

    return bean;
  }

  double getValue( final int k, final ParallelBinaryGridProcessorBean bean ) throws GeoGridException
  {
    final long globalPosition = k + bean.getStartPosition();

    final int sizeX = getSizeX();

    final int x = (int) (globalPosition % sizeX);
    final int y = (int) (globalPosition / sizeX);

    final Coordinate origin = getOrigin();
    final Coordinate offsetX = getOffsetX();
    final Coordinate offsetY = getOffsetY();

    final double cx = origin.x + x * offsetX.x + y * offsetY.x;
    final double cy = origin.y + x * offsetX.y + y * offsetY.y;
    final double z = bean.getValue( k );

    final Coordinate crd = new Coordinate( cx, cy, z );

    return getValue( x, y, crd );
  }

  protected abstract double getValue( int x, int y, Coordinate crd ) throws GeoGridException;

  @Override
  public double getValue( final int x, final int y )
  {
    throw new UnsupportedOperationException( ERROR_RANDOM_ACCESS );
  }

  @Override
  public IGeoWalkingStrategy getWalkingStrategy( )
  {
    return new SequentialBinaryGeoGridWalkingStrategy( this );
  }

  @Override
  public BigDecimal getMin( )
  {
    throw new UnsupportedOperationException( ERROR_RANDOM_ACCESS );
  }

  @Override
  public BigDecimal getMax( )
  {
    throw new UnsupportedOperationException( ERROR_RANDOM_ACCESS );
  }

  @Override
  public void setMin( final BigDecimal minValue )
  {
    throw new UnsupportedOperationException( ERROR_RANDOM_ACCESS );
  }

  @Override
  public void setMax( final BigDecimal maxValue )
  {
    throw new UnsupportedOperationException( ERROR_RANDOM_ACCESS );
  }
}
