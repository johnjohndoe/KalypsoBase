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

package org.kalypso.shape.shp;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;

import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeHeader;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.geometry.SHPEnvelope;

/**
 * Class representing an ESRI Shape File. Abstract implementation that just reads the header and can be used for random access or serial file reading.
 * <p>
 * Uses class ByteUtils modified from the original package com.bbn.openmap.layer.shape <br>
 * Copyright (C) 1998 BBN Corporation 10 Moulton St. Cambridge, MA 02138 <br>
 * <br>
 * Original Author: Andreas Poth
 */
abstract class AbstractSHPFile implements Closeable
{
  static int RECORD_HEADER_BYTES = 4 + 4;

  private ShapeHeader m_header;

  private final FileMode m_mode;

  private final DataInput m_input;

  /**
   * Opens a .shp file which must already exist and reads the header from thre given input.
   * 
   * @param input
   *          Must be positioned at the beginnning of the file.
   */
  public AbstractSHPFile( final DataInput input, final FileMode mode ) throws IOException
  {
    m_input = input;
    m_mode = mode;

    m_header = new ShapeHeader( input );
  }

  public FileMode getMode( )
  {
    return m_mode;
  }

  DataInput getInput( )
  {
    return m_input;
  }

  ShapeHeader getHeader( )
  {
    return m_header;
  }

  void updateHeader( final int length, final SHPEnvelope mbr )
  {
    m_header = new ShapeHeader( length, getShapeType(), mbr );
  }

  /**
   * returns the minimum bounding rectangle of geometries <BR>
   * within the shape-file
   */
  public SHPEnvelope getMBR( )
  {
    return m_header.getMBR();
  }

  public ShapeType getShapeType( )
  {
    return m_header.getShapeType();
  }
}