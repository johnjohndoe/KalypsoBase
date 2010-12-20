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
import java.io.Serializable;

import org.kalypso.shape.tools.DataUtils;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * Class representing a z-range of shape-z.
 * <!---------------------------------------------------------------------------->
 * 
 * @version 19.01.2007
 * @author Thomas Jung
 */

public class SHPZRange implements Serializable
{
  private double m_minZ;

  private double m_maxZ;

  /**
   * Create {@link SHPZRange} from raw bytes.<br>
   * Reads the min. and max. z-value of a shpz-file (as double).
   * 
   * @param b
   *          the raw data buffer
   * @param off
   *          the offset into the buffer where the int resides
   */
  public SHPZRange( final byte[] b, final int off )
  {
    final double minZ = ByteUtils.readLEDouble( b, off );
    final double maxZ = ByteUtils.readLEDouble( b, off + 8 );
    m_minZ = minZ;
    m_maxZ = maxZ;
  }

  public SHPZRange( final double minz, final double maxz )
  {
    m_minZ = minz;
    m_maxZ = maxz;
  }

  public byte[] writeLESHPZRange( )
  {
    final byte[] recBuf = new byte[8 * 4];
    ByteUtils.writeLEDouble( recBuf, 0, m_minZ );
    ByteUtils.writeLEDouble( recBuf, 8, m_maxZ );
    return recBuf;
  }

  public void writeLESHPRange( final DataOutput output ) throws IOException
  {
    DataUtils.writeLEDouble( output, m_minZ );
    DataUtils.writeLEDouble( output, m_maxZ );
  }

  public byte[] writeBESHPRange( )
  {
    final byte[] recBuf = new byte[8 * 4];
    // west bounding coordinate = xmin of rec-Box
    ByteUtils.writeBEDouble( recBuf, 0, m_minZ );
    // south bounding coordinate = ymin of rec-Box
    ByteUtils.writeBEDouble( recBuf, 8, m_maxZ );

    return recBuf;
  }

  // ----------------- METHOD IMPLEMENTATION
  @Override
  public String toString( )
  {
    return "ZRANGE" + "\n[zmin: " + this.m_minZ + "]" + "\n[zmax: " + this.m_maxZ + "]" + "]";
  }

  public double getMinZ( )
  {
    return m_minZ;
  }

  public void setMinZ( final double minZ )
  {
    m_minZ = minZ;
  }

  public double getMaxZ( )
  {
    return m_maxZ;
  }

  public void setMaxZ( final double maxZ )
  {
    m_maxZ = maxZ;
  }
}