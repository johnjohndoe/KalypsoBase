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
 * Class representig a collection of points <BR>
 * <B>Last changes <B>: <BR>
 * 21.03.2000 ap: constructor declared and implemented <BR>
 * 14.08.2000 ap: constructor SHPMultiPoint(GM_Point[] gm_points) added <BR>
 * 14.08.2000 ap: method writeSHPMultiPoint(..) added <BR>
 * 14.08.2000 ap: import clause added <BR>
 * 16.08.2000 ap: constructor SHPMultiPoint(GM_Point[] gm_points) modified <BR>
 * <!---------------------------------------------------------------------------->
 * 
 * @version 16.08.2000
 * @author Andreas Poth
 */

public class SHPMultiPoint implements ISHPGeometry
{
  private final SHPPoint[] m_points;

  private final SHPEnvelope m_envelope;

  /**
   * constructor: recieves a stream <BR>
   */
  public SHPMultiPoint( final byte[] recBuf )
  {
    m_envelope = new SHPEnvelope( recBuf, 4 );

    final int numPoints = ByteUtils.readLEInt( recBuf, 36 );

    m_points = new SHPPoint[numPoints];

    for( int i = 0; i < numPoints; i++ )
      m_points[i] = new SHPPoint( recBuf, 40 + i * 16 );
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.AbstractShape#writeContent(java.io.DataOutput)
   */
  @Override
  public void write( final DataOutput output ) throws IOException
  {
    m_envelope.writeLESHPEnvelope( output );

    DataUtils.writeLEInt( output, m_points.length );

    for( final SHPPoint point : m_points )
    {
      DataUtils.writeLEDouble( output, point.getX() );
      DataUtils.writeLEDouble( output, point.getY() );
    }
  }

  public SHPPoint[] getPoints( )
  {
    return m_points;
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPGeometry#getType()
   */
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
    return 36 + m_points.length * 16;
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.SHPGeometry#getEnvelope()
   */
  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
  }

}