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

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.Assert;

/**
 * Represents the header information of a .bin grid.
 *
 * @author Gernot Belger
 */
public class BinaryGeoGridHeader
{
  static final int HEADER_SIZE = 4 * 4;

  private final int m_sizeX;

  private final int m_sizeY;

  private final int m_scale;

  private final double m_scalePower;

  public static BinaryGeoGridHeader read( final DataInput input ) throws IOException
  {
    final int version = input.readInt();

    Assert.isTrue( version == 0, "Unknown binary file format version: " + version ); //$NON-NLS-1$

    final int sizeX = input.readInt();
    final int sizeY = input.readInt();
    final int scale = input.readInt();

    return new BinaryGeoGridHeader( sizeX, sizeY, scale );
  }

  public static BinaryGeoGridHeader read( final FileChannel input ) throws IOException
  {
    final ByteBuffer buffer = ByteBuffer.allocate( HEADER_SIZE );

    input.read( buffer );

    final int version = buffer.getInt( 0 );

    Assert.isTrue( version == 0, "Unknown binary file format version: " + version ); //$NON-NLS-1$

    final int sizeX = buffer.getInt( 4 );
    final int sizeY = buffer.getInt( 8 );
    final int scale = buffer.getInt( 12 );

    return new BinaryGeoGridHeader( sizeX, sizeY, scale );
  }

  public BinaryGeoGridHeader( final int sizeX, final int sizeY, final int scale )
  {
    m_sizeX = sizeX;
    m_sizeY = sizeY;
    m_scale = scale;
    m_scalePower = Math.pow( 10, scale );
  }

  public int getSizeX( )
  {
    return m_sizeX;
  }

  public int getSizeY( )
  {
    return m_sizeY;
  }

  public int getScale( )
  {
    return m_scale;
  }

  public double getScaleFactor( )
  {
    return m_scalePower;
  }

  public void write( final FileChannel channel ) throws IOException
  {
    final ByteBuffer buffer = ByteBuffer.allocate( HEADER_SIZE );

    buffer.putInt( 0 ); // Version number
    buffer.putInt( m_sizeX );
    buffer.putInt( m_sizeY );
    buffer.putInt( m_scale );

    buffer.rewind();
    channel.write( buffer );
  }
}
