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
 * Class representig a collection of pointsz <BR>
 * <B>Last changes <B>: <BR>
 * <!---------------------------------------------------------------------------->
 * 
 * @version 16.01.2007
 * @author Thomas Jung
 */
public class SHPMultiPointz implements ISHPGeometry
{
  public final SHPPointz[] m_pointsz;

  public final SHPZRange m_zrange;

  private final SHPEnvelope m_envelope;

  /**
   * constructor: recieves a stream <BR>
   */
  public SHPMultiPointz( final byte[] recBuf )
  {
    m_envelope = new SHPEnvelope( recBuf, 4 );

    final int numPoints = ByteUtils.readLEInt( recBuf, 36 );

    m_pointsz = new SHPPointz[numPoints];

    for( int i = 0; i < numPoints; i++ )
    {
      final double x = ByteUtils.readLEDouble( recBuf, 40 + i * 16 );
      final double y = ByteUtils.readLEDouble( recBuf, 40 + i * 16 + 8 );

      final int byteposition = ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH + (40 + numPoints * 16) + 16 + (8 * numPoints) + (8 * i);
      final double z = ByteUtils.readLEDouble( recBuf, byteposition );

      m_pointsz[i] = new SHPPointz( x, y, z, 0.0 );
    }

    m_zrange = new SHPZRange( recBuf, 40 + numPoints * 16 );
  }

  @Override
  public void write( final DataOutput output ) throws IOException
  {
    m_envelope.writeLESHPEnvelope( output );
    DataUtils.writeLEInt( output, m_pointsz.length );

    for( final SHPPointz element : m_pointsz )
    {
      DataUtils.writeLEDouble( output, element.getX() );
      DataUtils.writeLEDouble( output, element.getY() );
    }

    m_zrange.writeLESHPRange( output );

    for( final SHPPointz element : m_pointsz )
      DataUtils.writeLEDouble( output, element.getZ() );
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.ISHPGeometry#getType()
   */
  @Override
  public ShapeType getType( )
  {
    return ShapeType.MULTIPOINTZ;
  }

  @Override
  public int length( )
  {
    return 36 + m_pointsz.length * 16 + 16 + (8 * m_pointsz.length);
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.SHPGeometry#getEnvelope()
   */
  @Override
  public SHPEnvelope getEnvelope( )
  {
    return m_envelope;
  }

  public SHPPointz[] getPoints( )
  {
    return m_pointsz;
  }

}