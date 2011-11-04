/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 * 
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always. 
 * 
 * If you intend to use this software in other ways than in kalypso 
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree, 
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */

package org.kalypsodeegree_impl.io.shpapi;

import org.kalypsodeegree.model.geometry.ByteUtils;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * Class representig a three dimensional point <BR>
 * <B>Last changes <B>: <BR>
 * 16.01.07 Jung: class createtd <BR>
 * <!---------------------------------------------------------------------------->
 * 
 * @version 16.01.07
 * @author Thomas Jung
 */

public class SHPPointz implements ISHPPoint
{
  private final double x;

  private final double y;

  private final double z;

  private final double m;

  private final SHPEnvelope m_envelope;

  /**
   * constructor: gets a stream and the start index <BR>
   * of point on it <BR>
   */
  public SHPPointz( final byte[] recBuf )
  {
    this( recBuf, 4 );
  }

  /**
   * constructor: gets a stream and the start index <BR>
   * of point on it <BR>
   */
  public SHPPointz( final byte[] recBuf, final int off )
  {
    // get x out of recordbuffer
    x = ByteUtils.readLEDouble( recBuf, off );
    // get y out of recordbuffer
    y = ByteUtils.readLEDouble( recBuf, off + 8 );
    // get z out of recordbuffer
    z = ByteUtils.readLEDouble( recBuf, off + 16 );
    // get measure m out of recordbuffer
    m = ByteUtils.readLEDouble( recBuf, off + 24 );

    m_envelope = new SHPEnvelope( x, x, y, y );
  }

  /**
   * constructor: creates a SHPPoint from a WKS Geometrie <BR>
   */
  public SHPPointz( final GM_Position position )
  {
    this( position.getX(), position.getY(), position.getZ(), 0.0 );
  }

  /**
   * constructor: creates a SHPPoint from a point <BR>
   */
  public SHPPointz( final GM_Point point )
  {
    this( point.getX(), point.getY(), point.getZ(), 0.0 );
  }

  @SuppressWarnings("hiding")
  public SHPPointz( final double x, final double y, final double z, final double m )
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = m;

    m_envelope = new SHPEnvelope( x, x, y, y );
  }

  /**
   * method: writeSHPPoint: writes a SHPPoint Objekt to a recBuffer <BR>
   */
  @Override
  public byte[] writeShape( )
  {
    int offset = ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH;
    final byte[] byteArray = new byte[offset + size()];

    // write shape type identifier ( 11 = pointz )
    ByteUtils.writeLEInt( byteArray, offset, 11 );

    offset += 4;

    // write x into the recbuffer
    ByteUtils.writeLEDouble( byteArray, offset, x );

    offset += 8;

    // write y into the recbuffer
    ByteUtils.writeLEDouble( byteArray, offset, y );

    offset += 8;

    // write z into the recbuffer
    ByteUtils.writeLEDouble( byteArray, offset, z );

    offset += 8;

    // write m into the recbuffer
    ByteUtils.writeLEDouble( byteArray, offset, m );

    return byteArray;
  }

  /**
   * returns the size of the point shape in bytes <BR>
   */
  @Override
  public int size( )
  {
    return 36;
  }

  @Override
  public String toString( )
  {
    return "SHPPOINTZ" + "[" + x + "; " + y + "; " + z + "; " + m + "]";
  }

  @Override
  public double getX( )
  {
    return x;
  }

  @Override
  public double getY( )
  {
    return y;
  }

  @Override
  public double getZ( )
  {
    return z;
  }

  @Override
  public double getM( )
  {
    return m;
  }

  @Override
  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
  }

}