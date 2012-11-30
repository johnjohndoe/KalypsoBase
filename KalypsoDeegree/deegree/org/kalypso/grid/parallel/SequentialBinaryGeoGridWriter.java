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
package org.kalypso.grid.parallel;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.kalypso.grid.BinaryGeoGrid;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * @author barbarins
 */
public class SequentialBinaryGeoGridWriter implements Closeable
{
  private final byte[] m_writeBuffer = new byte[4];

  private final OutputStream m_gridStream;

  private Integer m_unscaledMin = Integer.MAX_VALUE;

  private Integer m_unscaledMax = -Integer.MAX_VALUE;

  private final double m_scalePotence;

  public SequentialBinaryGeoGridWriter( final String outputCoverageFileName, final int sizeX, final int sizeY, final int scale ) throws IOException
  {
    // init values
    m_scalePotence = Math.pow( 10, scale );

    // .. init file
    m_gridStream = new BufferedOutputStream( new FileOutputStream( outputCoverageFileName ), SequentialBinaryGeoGridReader.BLOCK_SIZE );

    // ...
    writeInt( 0 ); // version
    writeInt( sizeX );
    writeInt( sizeY );
    writeInt( scale );
  }

  public void write( final ParallelBinaryGridProcessorBean bean ) throws IOException
  {
    final double[] data = bean.getData();
    for( final double value : data )
    {
      final int rawValue = rescaleValue( value );
      writeInt( rawValue );
    }
  }

  private int rescaleValue( final double value )
  {
    if( Double.isNaN( value ) )
      return BinaryGeoGrid.NO_DATA;

    final double scaledValue = value * m_scalePotence;
    return (int)Math.round( scaledValue );
  }

  private final void writeInt( final int v ) throws IOException
  {
    ByteUtils.writeBEInt( m_writeBuffer, 0, v );
    m_gridStream.write( m_writeBuffer, 0, 4 ); // Version number

    /* update min/max */
    if( v != BinaryGeoGrid.NO_DATA )
    {
      m_unscaledMin = Math.min( m_unscaledMin, v );
      m_unscaledMax = Math.max( m_unscaledMax, v );
    }
  }

  @Override
  public void close( ) throws IOException
  {
    writeInt( m_unscaledMin );
    writeInt( m_unscaledMax );

    m_gridStream.close();
  }
}