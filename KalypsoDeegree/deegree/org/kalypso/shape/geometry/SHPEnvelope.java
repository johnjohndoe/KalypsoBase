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
import java.io.Serializable;

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

public class SHPEnvelope implements Serializable
{
  /**
   * this order: west, east, north, south
   */

  // each double 8 byte distance, offset due to position in .shp-file-record
  public static int recWest = 4;

  public static int recSouth = 12;

  public static int recEast = 20;

  public static int recNorth = 28;

  public double west;

  public double east;

  public double north;

  public double south;

  public SHPEnvelope( )
  {
    west = 0.0;
    east = 0.0;
    north = 0.0;
    south = 0.0;
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
    this( new SHPPoint( b, off ), new SHPPoint( b, (off + 16) ) );
  }

  public SHPEnvelope( final double westbc, final double eastbc, final double northbc, final double southbc )
  {
    this.west = westbc;
    this.east = eastbc;
    this.north = northbc;
    this.south = southbc;
  }

  /**
   * Transform from WKBPoint to Rectangle
   */
  public SHPEnvelope( final ISHPPoint min, final ISHPPoint max )
  {
    this.west = min.getX();
    this.east = max.getX();
    this.north = max.getY();
    this.south = min.getY();
  }

  /**
   * create from an existing SHPEnvelope
   */
  public SHPEnvelope( final SHPEnvelope env )
  {
    this.west = env.west;
    this.east = env.east;
    this.north = env.north;
    this.south = env.south;
  }

  public SHPEnvelope( final DataInput input ) throws IOException
  {
    this.west = DataUtils.readLEDouble( input );
    this.south = DataUtils.readLEDouble( input );
    this.east = DataUtils.readLEDouble( input );
    this.north = DataUtils.readLEDouble( input );
  }

  public void writeLESHPEnvelope( final DataOutput output ) throws IOException
  {
    DataUtils.writeLEDouble( output, west );
    DataUtils.writeLEDouble( output, south );
    DataUtils.writeLEDouble( output, east );
    DataUtils.writeLEDouble( output, north );
  }

  @Override
  public String toString( )
  {
    return "RECTANGLE" + "\n[west: " + this.west + "]" + "\n[east: " + this.east + "]" + "\n[north: " + this.north + "]" + "\n[south: " + this.south + "]";
  }

  public SHPEnvelope expand( final SHPEnvelope mbr )
  {
    if( mbr == null )
      return this;

    // actualize mbr
    if( west > mbr.west )
      west = mbr.west;
    if( east < mbr.east )
      east = mbr.east;
    if( south > mbr.south )
      south = mbr.south;
    if( north < mbr.north )
      north = mbr.north;

    return this;
  }

}