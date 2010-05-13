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
import org.kalypso.shape.tools.DataUtils;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * Class representig a two dimensional ESRI PolyLine <BR>
 * <B>Last changes <B>: <BR>
 * <!----------------------------------------------------------------------------><br>
 * TODO: support the (optional) m-part
 * 
 * @version 19.01.2007
 * @author Thomas Jung
 */
public class SHPPolyLinez implements ISHPParts
{
  private final int m_numPoints;

  private final SHPZRange m_zrange;

  private final SHPEnvelope m_envelope;

  private final ISHPPoint[][] m_parts;

  public SHPPolyLinez( final SHPPointz[][] parts )
  {
    SHPGeometryUtils.checkParts( parts );

    m_parts = parts;
    m_numPoints = SHPGeometryUtils.countPoints( m_parts );
    m_envelope = SHPGeometryUtils.createEnvelope( m_parts );
    m_zrange = SHPGeometryUtils.createZRange( m_parts );
  }

  /**
   * constructor: gets a stream <BR>
   */
  public SHPPolyLinez( final byte[] recBuf )
  {
    m_envelope = new SHPEnvelope( recBuf, 4 );

    final int numParts = ByteUtils.readLEInt( recBuf, 36 );
    m_numPoints = ByteUtils.readLEInt( recBuf, 40 );

    // index of the first point in part
    final int pointsStart = ShapeConst.PARTS_START + (numParts * 4);

    // array of points for all parts
    m_parts = new SHPPointz[numParts][];

    int count = 0;

    // get the index for the first point of each part
    for( int j = 0; j < numParts; j++ )
    {
      // get number of first point of current part out of ESRI shape Record:
      final int firstPointNo = ByteUtils.readLEInt( recBuf, ShapeConst.PARTS_START + (j * 4) );

      // calculate offset of part in bytes, count from the beginning of
      // recordbuffer
      final int offset = pointsStart + (firstPointNo * 16);

      // get number of first point of next part ...
      int nextFirstPointNo = 0;
      if( j < numParts - 1 )
      {
        // ... usually from ESRI shape Record
        nextFirstPointNo = ByteUtils.readLEInt( recBuf, ShapeConst.PARTS_START + ((j + 1) * 4) );
      }
      // ... for the last part as total number of points
      else if( j == numParts - 1 )
      {
        nextFirstPointNo = m_numPoints;
      }

      // calculate number of points per part due to distance and
      // calculate some checksum for the total number of points to be worked
      final int lnumPoints = nextFirstPointNo - firstPointNo;

      // allocate memory for the j-th part
      m_parts[j] = new SHPPointz[lnumPoints];

      // create the points of the j-th part from the buffer
      for( int i = 0; i < lnumPoints; i++ )
      {
        // number of the current point
        count++;

        // allocate memory for the points of the j-th part
        final double x = ByteUtils.readLEDouble( recBuf, offset + (i * 16) );
        final double y = ByteUtils.readLEDouble( recBuf, offset + (i * 16) + 8 );

        // jump to the z-values of the points
        final int byteposition = 44 + (4 * numParts) + (m_numPoints * 16) + 16 + ((count - 1) * 8);
        final double z = ByteUtils.readLEDouble( recBuf, byteposition );

        m_parts[j][i] = new SHPPointz( x, y, z, 0.0 );
      }
    }

    // next the z-range of the pointsz...
    final int byteposition = 44 + (4 * numParts) + (m_numPoints * 16);
    m_zrange = new SHPZRange( recBuf, byteposition );

    SHPGeometryUtils.checkParts( m_parts );
  }

  @Override
  public void write( final DataOutput output ) throws IOException
  {
    SHPPolyLine.writePart( output, m_envelope, getNumPoints(), getPoints() );

    m_zrange.writeLESHPRange( output );
    for( final ISHPPoint[] part : m_parts )
    {
      for( final ISHPPoint point : part )
        DataUtils.writeLEDouble( output, point.getZ() );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPGeometry#getType()
   */
  @Override
  public int getType( )
  {
    return ShapeConst.SHAPE_TYPE_POLYLINEZ;
  }

  @Override
  public int length( )
  {
    return 40 + m_parts.length * 4 + m_numPoints * 16 + 16 + (8 * m_numPoints);
  }

  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
  }

  public SHPZRange getZRange( )
  {
    return m_zrange;
  }

  public int getNumParts( )
  {
    return m_parts.length;
  }

  public int getNumPoints( )
  {
    return m_numPoints;
  }

  public ISHPPoint[][] getPoints( )
  {
    return m_parts;
  }

  public SHPZRange getZrange( )
  {
    return m_zrange;
  }
}
