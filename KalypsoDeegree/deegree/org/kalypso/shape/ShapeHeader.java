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

package org.kalypso.shape;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.geometry.SHPZRange;
import org.kalypso.shape.tools.DataUtils;

/**
 * Class representing an ESRI Index File Header.
 * <p>
 * Uses class ByteUtils ShapeUtils modified from the original package com.bbn.openmap.layer.shape <br>
 * Copyright (C) 1998 BBN Corporation 10 Moulton St. Cambridge, MA 02138 <br>
 * <br>
 * Original Author: Andreas Poth
 */
public class ShapeHeader
{
  /**
   * The length of a shape file header in bytes. (100)
   */
  public static final int SHAPE_FILE_HEADER_LENGTH = 100;

  /**
   * A Shape File's magic number.
   */
  public static final int SHAPE_FILE_CODE = 9994;

  /**
   * The currently handled version of Shape Files.
   */
  public static final int SHAPE_FILE_VERSION = 1000;

  private final int m_version = SHAPE_FILE_VERSION;

  private final int m_length;

  private final int m_shapeType;

  private final SHPEnvelope m_mbr;

  /**
   * @param length
   *          File length in bytes.
   */
  public ShapeHeader( final int length, final int shapeType, final SHPEnvelope mbr )
  {
    m_length = length;
    m_shapeType = shapeType;
    m_mbr = mbr;
  }

  public ShapeHeader( final DataInput input ) throws IOException
  {
    final int code = input.readInt();
    if( code != SHAPE_FILE_CODE )
      throw new IOException( "Invalid file code, " + "probably not a shape file" );

    /* Unused */
    input.readInt();
    input.readInt();
    input.readInt();
    input.readInt();
    input.readInt();

    m_length = input.readInt() * 2;

    final int version = DataUtils.readLEInt( input );
    if( version != SHAPE_FILE_VERSION )
      throw new IOException( "Unable to read shape files with version " + version );

    m_shapeType = DataUtils.readLEInt( input );

    /* Set explicitly to null, else we get (0,0,0,0) as bbox */
    if( m_length == SHAPE_FILE_HEADER_LENGTH )
      m_mbr = null;
    else
      m_mbr = new SHPEnvelope( input );
  }

  /**
   * Returns the bounding box of this shape file. The bounding box <BR>
   * is the smallest rectangle that encloses all the shapes in the <BR>
   * file. <BR>
   */

  public SHPEnvelope getMBR( )
  {
    return m_mbr;
  }

  /**
   * returns the length of the shape file in bytes <BR>
   */
  public long getLength( )
  {
    return m_length;
  }

  /**
   * returns the version of the shape file <BR>
   */
  public int getVersion( )
  {
    return m_version;
  }

  /**
   * returns the code for the shape type of the file <BR>
   */
  public int getShapeType( )
  {
    return m_shapeType;
  }

  /**
   * Writes the header into the shape file. <BR>
   */
  public void write( final DataOutput output ) throws IOException
  {
    output.writeInt( SHAPE_FILE_CODE );
    output.writeInt( 0x0 ); // unused
    output.writeInt( 0x0 ); // unused
    output.writeInt( 0x0 ); // unused
    output.writeInt( 0x0 ); // unused
    output.writeInt( 0x0 ); // unused
    output.writeInt( m_length / 2 );
    DataUtils.writeLEInt( output, SHAPE_FILE_VERSION );
    DataUtils.writeLEInt( output, m_shapeType );
    if( m_mbr == null )
      output.write( new byte[4 * 8] ); // just random bytes, does not matter
    else
      m_mbr.writeLESHPEnvelope( output );
    // TODO: ShapeHEader supports z- and m-rnages, we should do so also.
    new SHPZRange( 0.0, 0.0 ).writeLESHPRange( output ); // Z-Range
    new SHPZRange( 0.0, 0.0 ).writeLESHPRange( output ); // M-Range
  }
}
