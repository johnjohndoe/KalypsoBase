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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.kalypso.shape.tools.DataUtils;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * @author Thomas Jung
 */
public class SHPRange
{
  private final double m_min;

  private final double m_max;

  /**
   * Create {@link SHPRange} from a DataInput at its current position.<br>
   * Reads the min. and max. value of a shp-file (as double).
   */
  public SHPRange( final DataInput input ) throws IOException
  {
    m_min = DataUtils.readLEDouble( input );
    m_max = DataUtils.readLEDouble( input );
  }

  /**
   * Create {@link SHPRange} from raw bytes.<br>
   * Reads the min. and max. value of a shp-file (as double).
   * 
   * @param b
   *          the raw data buffer
   * @param off
   *          the offset into the buffer where the values resides
   */
  public SHPRange( final byte[] b, final int off )
  {
    m_min = ByteUtils.readLEDouble( b, off );
    m_max = ByteUtils.readLEDouble( b, off + 8 );
  }

  public SHPRange( final double min, final double max )
  {
    m_min = min;
    m_max = max;
  }

  public void write( final DataOutput output ) throws IOException
  {
    DataUtils.writeLEDouble( output, m_min );
    DataUtils.writeLEDouble( output, m_max );
  }

  @Override
  public String toString( )
  {
    return "RANGE" + "\n[min: " + m_min + "]" + "\n[max: " + m_max + "]" + "]";
  }

  public double getMin( )
  {
    return m_min;
  }

  public double getMax( )
  {
    return m_max;
  }
}