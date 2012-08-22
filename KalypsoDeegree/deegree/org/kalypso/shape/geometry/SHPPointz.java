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

package org.kalypso.shape.geometry;

import java.io.DataOutput;
import java.io.IOException;

import org.kalypso.shape.ShapeType;
import org.kalypso.shape.tools.DataUtils;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * @author Thomas Jung
 */
public class SHPPointz implements ISHPPoint
{
  private final double m_x;

  private final double m_y;

  private final double m_z;

  private final double m_m;

  private final SHPEnvelope m_envelope;

  public SHPPointz( final byte[] recBuf )
  {
    this( recBuf, 4 );
  }

  private SHPPointz( final byte[] recBuf, final int off )
  {
    m_x = ByteUtils.readLEDouble( recBuf, off );
    m_y = ByteUtils.readLEDouble( recBuf, off + 8 );
    m_z = ByteUtils.readLEDouble( recBuf, off + 16 );

    if( off + 24 < recBuf.length )
      m_m = ByteUtils.readLEDouble( recBuf, off + 24 );
    else
      m_m = Double.NaN;

    m_envelope = new SHPEnvelope( m_x, m_x, m_y, m_y );
  }

  public SHPPointz( final double x, final double y, final double z, final double m )
  {
    m_x = x;
    m_y = y;
    m_z = z;
    m_m = m;

    m_envelope = new SHPEnvelope( x, x, y, y );
  }

  @Override
  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
  }

  @Override
  public void write( final DataOutput output ) throws IOException
  {
    DataUtils.writeLEDouble( output, m_x );
    DataUtils.writeLEDouble( output, m_y );
    DataUtils.writeLEDouble( output, m_z );

    if( !Double.isNaN( m_m ) )
      DataUtils.writeLEDouble( output, m_m );
  }

  @Override
  public ShapeType getType( )
  {
    return ShapeType.POINTZ;
  }

  /**
   * returns the size of the point shape in bytes <BR>
   */
  @Override
  public int length( )
  {
    if( Double.isNaN( m_m ) )
      return 24;
    else
      return 32;
  }

  @Override
  public String toString( )
  {
    return "SHPPOINTZ" + "[" + m_x + "; " + m_y + "; " + m_z + "; " + m_m + "]";
  }

  @Override
  public double getX( )
  {
    return m_x;
  }

  @Override
  public double getY( )
  {
    return m_y;
  }

  @Override
  public double getZ( )
  {
    return m_z;
  }

  @Override
  public double getM( )
  {
    return m_m;
  }
}