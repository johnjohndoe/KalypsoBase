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

/**
 * Class representing a rectangle - envelope.
 * <P>
 * <B>Last changes <B>: <BR>
 * 07.01.2000 ap: all methods copied from Rectangle.java <BR>
 * 07.01.2000 ap: constructor renamed <BR>
 * 17.01.2000 ap: constructor SHPEnvelope(ESRIBoundingBox Ebb) removed <BR>
 * 17.01.2000 ap: constructor SHPEnvelope(SHPEnvelope env)implemented <BR>
 * 01.08.2000 ap: method writeSHPEnvelope() added <BR>
 * <!---------------------------------------------------------------------------->
 * 
 * @version 01.08.2000
 * @author Andreas Poth
 */

public class SHPEnvelope
{
  private double m_west;

  private double m_east;

  private double m_north;

  private double m_south;

  public SHPEnvelope( )
  {
    m_west = 0.0;
    m_east = 0.0;
    m_north = 0.0;
    m_south = 0.0;
  }

  /**
   * Create {@link SHPEnvelope} from raw bytes.<br>
   * Reads a bounding box record. A bounding box is four double representing, in order, xmin, ymin, xmax, ymax.
   * 
   * @param b
   *          the raw data buffer
   * @param off
   *          the offset into the buffer
   * @return the point read from the buffer at the offset location
   */
  public SHPEnvelope( final byte[] b, final int off )
  {
    this( new SHPPoint( b, off ), new SHPPoint( b, off + 16 ) );
  }

  public SHPEnvelope( final double westbc, final double eastbc, final double northbc, final double southbc )
  {
    m_west = westbc;
    m_east = eastbc;
    m_north = northbc;
    m_south = southbc;
  }

  /**
   * Transform from WKBPoint to Rectangle
   */
  public SHPEnvelope( final ISHPPoint min, final ISHPPoint max )
  {
    m_west = min.getX();
    m_east = max.getX();
    m_north = max.getY();
    m_south = min.getY();
  }

  /**
   * create from an existing SHPEnvelope
   */
  public SHPEnvelope( final SHPEnvelope env )
  {
    m_west = env.m_west;
    m_east = env.m_east;
    m_north = env.m_north;
    m_south = env.m_south;
  }

  public SHPEnvelope( final DataInput input ) throws IOException
  {
    m_west = DataUtils.readLEDouble( input );
    m_south = DataUtils.readLEDouble( input );
    m_east = DataUtils.readLEDouble( input );
    m_north = DataUtils.readLEDouble( input );
  }

  public void writeLESHPEnvelope( final DataOutput output ) throws IOException
  {
    DataUtils.writeLEDouble( output, m_west );
    DataUtils.writeLEDouble( output, m_south );
    DataUtils.writeLEDouble( output, m_east );
    DataUtils.writeLEDouble( output, m_north );
  }

  @Override
  public String toString( )
  {
    return "RECTANGLE" + "\n[west: " + m_west + "]" + "\n[east: " + m_east + "]" + "\n[north: " + m_north + "]" + "\n[south: " + m_south + "]";
  }

  public SHPEnvelope expand( final SHPEnvelope mbr )
  {
    if( mbr == null )
      return this;

    // actualize mbr
    if( m_west > mbr.m_west )
      m_west = mbr.m_west;
    if( m_east < mbr.m_east )
      m_east = mbr.m_east;
    if( m_south > mbr.m_south )
      m_south = mbr.m_south;
    if( m_north < mbr.m_north )
      m_north = mbr.m_north;

    return this;
  }

  public double getWest( )
  {
    return m_west;
  }

  public double getEast( )
  {
    return m_east;
  }

  public double getNorth( )
  {
    return m_north;
  }

  public double getSouth( )
  {
    return m_south;
  }
}