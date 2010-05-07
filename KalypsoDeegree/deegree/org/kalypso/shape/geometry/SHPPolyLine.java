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
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;

/**
 * Class representig a two dimensional ESRI PolyLine <BR>
 * <B>Last changes <B>: <BR>
 * 12.01.2000 ap: constructor re-declared <BR>
 * 25.01.2000 ap: public variables numRings and numPoints declared <BR>
 * 21.03.2000 ap: parameter list of the second constructor modified <BR>
 * 14.08.2000 ap: constructor SHPPolyLine(GM_Point[][] gm_points) added <BR>
 * 14.08.2000 ap: method writeSHPPolyline(..) added <BR>
 * 14.08.2000 ap: method size() added <BR>
 * 16.08.2000 ap: constructor SHPPolyLine(GM_Point[][] gm_points) modified <BR>
 * <!---------------------------------------------------------------------------->
 * 
 * @version 16.08.2000
 * @author Andreas Poth
 */
public class SHPPolyLine implements ISHPParts
{
  private final int m_numPoints;

  private final ISHPPoint[][] m_parts;

  private final SHPEnvelope m_envelope;

  /**
   * @param parts
   *          REMARK: using class {@link SHPPoint} explicitely (not {@link ISHPPoint}, as we are not 3D.
   */
  public SHPPolyLine( final SHPPoint[][] parts )
  {
    SHPGeometryUtils.checkParts( parts );

    m_parts = parts;
    m_numPoints = SHPGeometryUtils.countPoints( parts );
    m_envelope = SHPGeometryUtils.createEnvelope( parts );
  }

  /**
   * constructor: gets a stream <BR>
   */
  public SHPPolyLine( final byte[] recBuf )
  {
    m_envelope = new SHPEnvelope( recBuf, 4 );

    final int numParts = ByteUtils.readLEInt( recBuf, 36 );
    m_numPoints = ByteUtils.readLEInt( recBuf, 40 );

    final int pointsStart = ShapeConst.PARTS_START + (numParts * 4);

    m_parts = new SHPPoint[numParts][];
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
        // ... usually out of ESRI shape Record
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
      m_parts[j] = new SHPPoint[lnumPoints];

      // create the points of the j-th part from the buffer
      for( int i = 0; i < lnumPoints; i++ )
      {
        m_parts[j][i] = new SHPPoint( recBuf, offset + (i * 16) );
      }
    }

    SHPGeometryUtils.checkParts( m_parts );
  }

  /**
   * constructor: receives a matrix of GM_Points <BR>
   */
  public SHPPolyLine( final GM_Curve[] curves )
  {
    final int numParts = curves.length;

    m_parts = new SHPPoint[numParts][];

    try
    {
      for( int i = 0; i < numParts; i++ )
      {
        final GM_LineString ls = curves[i].getAsLineString();
        m_parts[i] = new SHPPoint[ls.getNumberOfPoints()];
        for( int j = 0; j < ls.getNumberOfPoints(); j++ )
          m_parts[i][j] = new SHPPoint( ls.getPositionAt( j ) );
      }
    }
    catch( final GM_Exception e )
    {
      System.out.println( "SHPPolyLine:: " + e );
    }

    m_numPoints = SHPGeometryUtils.countPoints( m_parts );
    m_envelope = SHPGeometryUtils.createEnvelope( m_parts );

    SHPGeometryUtils.checkParts( m_parts );
  }

  @Override
  public void write( final DataOutput output ) throws IOException
  {
    writePart( output, m_envelope, m_numPoints, m_parts );
  }

  static void writePart( final DataOutput output, final SHPEnvelope envelope, final int numPoints, final ISHPPoint[][] points ) throws IOException
  {
    envelope.writeLESHPEnvelope( output );
    DataUtils.writeLEInt( output, points.length );
    DataUtils.writeLEInt( output, numPoints );

    int partIndex = 0;
    for( final ISHPPoint[] part : points )
    {
      DataUtils.writeLEInt( output, partIndex );
      partIndex += part.length;
    }

    for( final ISHPPoint[] part : points )
    {
      for( final ISHPPoint point : part )
        point.write( output );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPGeometry#getType()
   */
  @Override
  public int getType( )
  {
    return ShapeConst.SHAPE_TYPE_POLYLINE;
  }

  @Override
  public int length( )
  {
    return 40 + m_parts.length * 4 + m_numPoints * 16;
  }

  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
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
}
