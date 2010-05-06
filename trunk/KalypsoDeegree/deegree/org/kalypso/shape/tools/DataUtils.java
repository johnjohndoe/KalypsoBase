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
package org.kalypso.shape.tools;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * @author Gernot Belger
 */
public final class DataUtils
{
  private DataUtils( )
  {
    throw new UnsupportedOperationException();
  }

  public static int readLEShort( final DataInput raf ) throws IOException
  {
    final byte[] b = new byte[2];
    raf.readFully( b );
    return ByteUtils.readLEShort( b, 0 );
  }

  public static int readLEInt( final DataInput raf ) throws IOException
  {
    final byte[] b = new byte[4];
    raf.readFully( b );
    return ByteUtils.readLEInt( b, 0 );
  }

  public static void writeLEInt( final DataOutput output, final int val ) throws IOException
  {
    output.writeByte( (byte) ((val) & 0xff) );
    output.writeByte( (byte) ((val >> 8) & 0xff) );
    output.writeByte( (byte) ((val >> 16) & 0xff) );
    output.writeByte( (byte) ((val >> 24) & 0xff) );
  }

  public static void writeLEShort( final DataOutput output, final int val ) throws IOException
  {
    output.writeByte( (byte) ((val) & 0xff) );
    output.writeByte( (byte) ((val >> 8) & 0xff) );
  }

  /**
   * Writes the given double to the given buffer at the given location in little endian format.
   * 
   * @param val
   *          the double to write
   * @return the number of bytes written
   */
  public static void writeLEDouble( final DataOutput output, final double val ) throws IOException
  {
    writeLELong( output, Double.doubleToLongBits( val ) );
  }

  /**
   * Writes the given long to the given buffer at the given location in little endian format.
   * 
   * @param val
   *          the long to write
   * @return the number of bytes written
   */
  public static void writeLELong( final DataOutput output, final long val ) throws IOException
  {
    output.writeByte( (byte) ((val) & 0xff) );
    output.writeByte( (byte) ((val >> 8) & 0xff) );
    output.writeByte( (byte) ((val >> 16) & 0xff) );
    output.writeByte( (byte) ((val >> 24) & 0xff) );
    output.writeByte( (byte) ((val >> 32) & 0xff) );
    output.writeByte( (byte) ((val >> 40) & 0xff) );
    output.writeByte( (byte) ((val >> 48) & 0xff) );
    output.writeByte( (byte) ((val >> 56) & 0xff) );
  }

  /**
   * method: readLELong(byte[] b, int off) <BR>
   * Reads a little endian 8 byte integer.
   * 
   * @param b
   *          the raw data buffer
   * @param off
   *          the offset into the buffer where the long resides
   * @return the long read from the buffer at the offset location
   */
  public static long readLELong( final DataInput input ) throws IOException
  {
    final byte[] b = new byte[8];
    input.readFully( b );
    return ByteUtils.readLELong( b, 0 );
  }

  /**
   * method: readLEDouble(byte[] b, int off) <BR>
   * Reads a little endian double.
   * 
   * @param b
   *          the raw data buffer
   * @param off
   *          the offset into the buffer where the double resides
   * @return the double read from the buffer at the offset location
   */
  public static double readLEDouble( final DataInput input ) throws IOException
  {
    return Double.longBitsToDouble( readLELong( input ) );
  }
}
