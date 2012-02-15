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
import org.kalypso.grid.AbstractDelegatingGeoGrid;
import org.kalypso.grid.BinaryGeoGrid;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.IGeoGrid;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author barbarins
 */
public abstract class SequentialBinaryGeoGridReader extends AbstractDelegatingGeoGrid implements Closeable
{
  /* Number of values per block */
  private final Integer m_blockSize = 1024 * 2;

  private final DataInputStream m_gridStream;

  private final int m_scale;

  private long m_currentPosition = 0;

  private final long m_blockAmount;

  public SequentialBinaryGeoGridReader( final IGeoGrid inputGrid, final URL pUrl ) throws IOException, GeoGridException
  {
    // FIXME: why the input grid here?! it is never accessed, but the value are read from the url!
    // TODO: we should not inherit form iGeoGrid at all, this is a different API completely
    super( inputGrid );

    /* Tries to find a file from the given url. */
    File gridFile = ResourceUtilities.findJavaFileFromURL( pUrl );
    if( gridFile == null )
      gridFile = FileUtils.toFile( pUrl );

    m_gridStream = new DataInputStream( new BufferedInputStream( new FileInputStream( gridFile ) ) );

    // skip header
    m_gridStream.skipBytes( 12 );

    /* Read header */
    m_scale = m_gridStream.readInt();

    final long length = getSizeX() * getSizeY();
    m_blockAmount = (length / m_blockSize) + 1;
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

    return new BigDecimal( BigInteger.valueOf( rawValue ), m_scale ).doubleValue();
  }

  public int getScale( )
  {
    return m_scale;
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

    super.dispose();
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

    final ParallelBinaryGridProcessorBean bean = createNewBean( data, m_currentPosition );

    m_currentPosition += data.length;

    return bean;
  }

  protected ParallelBinaryGridProcessorBean createNewBean( final Double[] data, final long startPosition )
  {
    return new ParallelBinaryGridProcessorBean( data, startPosition );
  }

  double getValue( final int k, final ParallelBinaryGridProcessorBean bean ) throws GeoGridException
  {
    final long globalPosition = k + bean.getStartPosition();

    final int sizeX = getDelegate().getSizeX();

    final int x = (int) (globalPosition % sizeX);
    final int y = (int) (globalPosition / sizeX);

    if( y > 0 )
    {
      System.out.println();
    }

    final Coordinate origin = getOrigin();
    final Coordinate offsetX = getOffsetX();
    final Coordinate offsetY = getOffsetY();

    final double cx = origin.x + x * offsetX.x + y * offsetY.x;
    final double cy = origin.y + x * offsetX.y + y * offsetY.y;
    final double z = bean.getValue( k );

    final Coordinate crd = new Coordinate( cx, cy, z );

    return getValue( x, y, crd, bean );
  }

  // FIXME: ugly, we should not give the bean to outsiders
  protected abstract double getValue( int x, int y, Coordinate crd, ParallelBinaryGridProcessorBean bean ) throws GeoGridException;
}
