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
 * @author Andreas Poth
 */
public class SHPMultiPoint implements ISHPMultiPoint
{
  private final ISHPPoint[] m_points;

  private final SHPEnvelope m_envelope;

  static ISHPPoint[] readPoints( final byte[] recBuf, final int offset, final int numPoints )
  {
    final ISHPPoint[] points = new ISHPPoint[numPoints];

    for( int i = 0; i < numPoints; i++ )
    {
      final int pointOffset = offset + i * 16;

      final double x = ByteUtils.readLEDouble( recBuf, pointOffset );
      final double y = ByteUtils.readLEDouble( recBuf, pointOffset + 8 );

      points[i] = new SHPPoint( x, y );
    }

    return points;
  }

  public static SHPMultiPoint read( final byte[] recBuf )
  {
    final SHPEnvelope envelope = new SHPEnvelope( recBuf, 4 );
    final int numPoints = ByteUtils.readLEInt( recBuf, 36 );

    return read( recBuf, 40, envelope, numPoints );
  }

  public static SHPMultiPoint read( final byte[] recBuf, final int offset, final SHPEnvelope envelope, final int numPoints )
  {
    final ISHPPoint[] points = readPoints( recBuf, offset, numPoints );

    return new SHPMultiPoint( envelope, points );
  }

  public SHPMultiPoint( final SHPEnvelope envelope, final ISHPPoint[] points )
  {
    m_envelope = envelope;
    m_points = points;
  }

  @Override
  public void write( final DataOutput output ) throws IOException
  {
    m_envelope.writeLESHPEnvelope( output );

    DataUtils.writeLEInt( output, m_points.length );

    writePoints( output );
  }

  @Override
  public void writePoints( final DataOutput output ) throws IOException
  {
    for( final ISHPPoint point : m_points )
    {
      DataUtils.writeLEDouble( output, point.getX() );
      DataUtils.writeLEDouble( output, point.getY() );
    }
  }

  @Override
  public ShapeType getType( )
  {
    return ShapeType.MULTIPOINT;
  }

  /**
   * returns the size of the multipoint shape in bytes <BR>
   */
  @Override
  public int length( )
  {
    return 40 + m_points.length * 16;
  }

  @Override
  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
  }

  @Override
  public ISHPPoint[] getPoints( )
  {
    return m_points;
  }
  @Override
  public int getNumPoints( )
  {
    return m_points.length;
  }
}