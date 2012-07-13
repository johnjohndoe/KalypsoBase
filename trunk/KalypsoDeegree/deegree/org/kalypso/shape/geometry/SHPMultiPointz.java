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
public class SHPMultiPointz implements ISHPMultiPoint
{
  private final ISHPPoint[] m_points;

  private final SHPRange m_zrange;

  private final SHPRange m_mrange;

  private final SHPEnvelope m_envelope;

  private static ISHPPoint[] readPoints( final byte[] recBuf, final int offset, final int numPoints )
  {
    final ISHPPoint[] points = new ISHPPoint[numPoints];

    final int zOffset = offset + numPoints * 16;
    final int mOffset = offset + numPoints * 16 + 16 + 8 * numPoints;

    for( int i = 0; i < numPoints; i++ )
    {
      final double x = ByteUtils.readLEDouble( recBuf, offset + i * 16 );
      final double y = ByteUtils.readLEDouble( recBuf, offset + i * 16 + 8 );

      final int zPos = zOffset + 16 + 8 * i;
      final double z = ByteUtils.readLEDouble( recBuf, zPos );

      // REMARK: m values seem to be optional, although this is not really described in the white paper
      final double m;
      final int mPos = mOffset + 16 + 8 * i;
      if( mPos < recBuf.length )
        m = ByteUtils.readLEDouble( recBuf, mPos );
      else
        m = Double.NaN;

      points[i] = new SHPPointz( x, y, z, m );
    }

    return points;
  }

  public static SHPMultiPointz read( final byte[] recBuf )
  {
    final SHPEnvelope envelope = new SHPEnvelope( recBuf, 4 );
    final int numPoints = ByteUtils.readLEInt( recBuf, 36 );

    return read( recBuf, 40, envelope, numPoints );
  }

  public static SHPMultiPointz read( final byte[] recBuf, final int offset, final SHPEnvelope envelope, final int numPoints )
  {
    final ISHPPoint[] points = readPoints( recBuf, offset, numPoints );

    final int zOffset = offset + numPoints * 16;
    final SHPRange zrange = new SHPRange( recBuf, zOffset );

    // REMARK: m values seem to be optional, although this is not really described in the white paper
    final SHPRange mrange;
    final int mOffset = offset + numPoints * 16 + 16 + 8 * numPoints;
    if( mOffset < recBuf.length )
      mrange = new SHPRange( recBuf, mOffset );
    else
      mrange = null;

    return new SHPMultiPointz( envelope, points, zrange, mrange );
  }

  public SHPMultiPointz( final SHPEnvelope envelope, final ISHPPoint[] points, final SHPRange zRange, final SHPRange mRange )
  {
    m_envelope = envelope;
    m_points = points;
    m_zrange = zRange;
    m_mrange = mRange;
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
    for( final ISHPPoint element : m_points )
    {
      DataUtils.writeLEDouble( output, element.getX() );
      DataUtils.writeLEDouble( output, element.getY() );
    }

    /* write z */
    m_zrange.write( output );

    for( final ISHPPoint element : m_points )
      DataUtils.writeLEDouble( output, element.getZ() );

    /* write m */
    m_mrange.write( output );

    for( final ISHPPoint element : m_points )
      DataUtils.writeLEDouble( output, element.getM() );
  }

  @Override
  public ShapeType getType( )
  {
    return ShapeType.MULTIPOINTZ;
  }

  @Override
  public int length( )
  {
    return 36 + 8 + m_points.length * 16 + 16 + 8 * m_points.length + 16 + 8 * m_points.length;
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