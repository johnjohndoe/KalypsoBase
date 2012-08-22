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

import org.kalypso.shape.ShapeConst;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.tools.DataUtils;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * @author Gernot Belger
 */
abstract class AbstractSHPPolyLine implements ISHPPolyLine
{
  private final int[] m_parts;

  private final ISHPMultiPoint m_multiPoint;

  private final ShapeType m_type;

  public AbstractSHPPolyLine( final ISHPMultiPoint points, final int[] parts, final ShapeType type )
  {
    SHPGeometryUtils.checkParts( points, parts );

    m_type = type;
    m_multiPoint = points;
    m_parts = parts;
  }

  public AbstractSHPPolyLine( final byte[] recBuf, final ShapeType type )
  {
    m_type = type;

    final SHPEnvelope envelope = new SHPEnvelope( recBuf, 4 );

    final int numParts = ByteUtils.readLEInt( recBuf, 36 );
    final int numPoints = ByteUtils.readLEInt( recBuf, 40 );

    /* Read points */
    m_parts = new int[numParts];
    for( int j = 0; j < numParts; j++ )
      m_parts[j] = ByteUtils.readLEInt( recBuf, ShapeConst.PARTS_START + j * 4 );

    m_multiPoint = readPoints( recBuf, envelope, numParts, numPoints );

    // REMARK: this check is controproductive, because there are many invalid 8according to shape whitepaper) shapes otu
// there...
    // The nice thing: even ArcGIS produces invalid shape files ;-(
    // SHPGeometryUtils.checkParts( m_multiPoint, m_parts );
  }

  protected abstract ISHPMultiPoint readPoints( byte[] recBuf, SHPEnvelope envelope, final int numParts, int numPoints );

  @Override
  public void write( final DataOutput output ) throws IOException
  {
    writePart( output, m_multiPoint, m_parts );
  }

  static void writePart( final DataOutput output, final ISHPMultiPoint multiPoint, final int[] parts ) throws IOException
  {
    final SHPEnvelope envelope = multiPoint.getEnvelope();

    envelope.writeLESHPEnvelope( output );

    DataUtils.writeLEInt( output, parts.length );

    DataUtils.writeLEInt( output, multiPoint.getNumPoints() );

    for( final int part : parts )
      DataUtils.writeLEInt( output, part );

    multiPoint.writePoints( output );
  }

  @Override
  public ShapeType getType( )
  {
    return m_type;
  }

  @Override
  public SHPEnvelope getEnvelope( )
  {
    return m_multiPoint.getEnvelope();
  }

  @Override
  public int getNumParts( )
  {
    return m_parts.length;
  }

  @Override
  public int getNumPoints( )
  {
    return m_multiPoint.getNumPoints();
  }

  @Override
  public ISHPPoint[] getPoints( )
  {
    return m_multiPoint.getPoints();
  }

  @Override
  public int[] getParts( )
  {
    return m_parts;
  }
}