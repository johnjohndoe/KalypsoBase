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
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * Class representing a two dimensional point <BR>
 * <B>Last changes <B>: <BR>
 * 25.05.00 chm: method writeSHPPoint implemented <BR>
 * 14.08.00 ap: import clause added <BR>
 * <!---------------------------------------------------------------------------->
 * 
 * @version 14.08.2000
 * @author Andreas Poth
 */

public class SHPPoint implements ISHPPoint
{
  private final double m_x;

  private final double m_y;

  private final SHPEnvelope m_envelope;

  /**
   * constructor: gets a stream and the start index <BR>
   * of point on it <BR>
   */
  public SHPPoint( final byte[] recBuf )
  {
    this( recBuf, 4 );
  }

  public SHPPoint( final double x, final double y )
  {
    m_x = x;
    m_y = y;

    m_envelope = new SHPEnvelope( x, x, y, y );
  }

  /**
   * constructor: gets a stream and the start index <BR>
   * of point on it <BR>
   */
  public SHPPoint( final byte[] recBuf, final int xStart )
  {
    this( ByteUtils.readLEDouble( recBuf, xStart ), ByteUtils.readLEDouble( recBuf, xStart + 8 ) );
  }

  /**
   * constructor: creates a SHPPoint from a WKS Geometrie <BR>
   */
  public SHPPoint( final GM_Position position )
  {
    this( position.getX(), position.getY() );
  }

  /**
   * constructor: creates a SHPPoint from a GM_Point <BR>
   */
  public SHPPoint( final GM_Point point )
  {
    this( point.getX(), point.getY() );
  }

  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
  }

  @Override
  public void write( final DataOutput output ) throws IOException
  {
    DataUtils.writeLEDouble( output, m_x );
    DataUtils.writeLEDouble( output, m_y );
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPGeometry#getType()
   */
  @Override
  public ShapeType getType( )
  {
    return ShapeType.POINT;
  }

  @Override
  public int length( )
  {
    return 16;
  }

  @Override
  public String toString( )
  {
    return "SHPPOINT" + "[" + this.m_x + "; " + this.m_y + "]";
  }

  public double getX( )
  {
    return m_x;
  }

  public double getY( )
  {
    return m_y;
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPPoint#getZ()
   */
  @Override
  public double getZ( )
  {
    return Double.NaN;
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPPoint#getM()
   */
  @Override
  public double getM( )
  {
    return Double.NaN;
  }

}