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

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.Assert;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeHeader;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.geometry.SHPMultiPoint;
import org.kalypso.shape.geometry.SHPMultiPointz;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.geometry.SHPPolyLinez;
import org.kalypso.shape.geometry.SHPPolygon;
import org.kalypso.shape.geometry.SHPPolygonz;
import org.kalypso.shape.shx.SHXRecord;
import org.kalypso.shape.tools.DataUtils;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * Class representing an ESRI Shape File.
 * <p>
 * Uses class ByteUtils modified from the original package com.bbn.openmap.layer.shape <br>
 * Copyright (C) 1998 BBN Corporation 10 Moulton St. Cambridge, MA 02138 <br>
 * <br>
 * Original Author: Andreas Poth
 */

public class SHPFile
{
  private static int RECORD_HEADER_BYTES = 4 + 4;

  private ShapeHeader m_header;

  private final RandomAccessFile m_raf;

  private final FileMode m_mode;

  public static SHPFile create( final File file, final ShapeType shapeType ) throws IOException
  {
    final RandomAccessFile raf = new RandomAccessFile( file, "rw" );

    final ShapeHeader header = new ShapeHeader( ShapeHeader.SHAPE_FILE_HEADER_LENGTH, shapeType, null );
    header.write( raf );
    raf.close();

    return new SHPFile( file, FileMode.WRITE );
  }

  /**
   * Opens a .shp file which must already exist.
   */
  public SHPFile( final File file, final FileMode mode ) throws IOException
  {
    m_mode = mode;

    final String rwFlags = mode == FileMode.READ ? "r" : "rw";
    m_raf = new RandomAccessFile( file, rwFlags );

    m_header = new ShapeHeader( m_raf );
  }

  public void close( ) throws IOException
  {
    if( isWriting() )
    {
      final long fileLength = m_raf.length();
      m_header = new ShapeHeader( (int) fileLength, getShapeType(), getMBR() );
      m_raf.seek( 0 );
      // Update header with length
      m_header.write( m_raf );
    }

    m_raf.close();
  }

  /**
   * returns the minimum bounding rectangle of geometries <BR>
   * within the shape-file
   */
  public SHPEnvelope getMBR( )
  {
    return m_header.getMBR();
  }

  /**
   * returns the minimum bound rectangle of RecNo's Geometrie of the shape-file <BR>
   */
  public SHPEnvelope getEnvelope( final SHXRecord record ) throws IOException
  {
    final int position = record.getOffset() * 2;
    m_raf.seek( position + 8 );
    final ShapeType shpType = ShapeType.valueOf( DataUtils.readLEInt( m_raf ) );

    /*
     * only for PolyLines, Polygons and MultiPoints minimum bounding rectangles are defined
     */
    if( (shpType == ShapeType.POLYLINE) || (shpType == ShapeType.POLYGON) || (shpType == ShapeType.MULTIPOINT) || (shpType == ShapeType.POLYLINEZ) || (shpType == ShapeType.POLYGONZ)
        || (shpType == ShapeType.MULTIPOINTZ) )
      return new SHPEnvelope( m_raf );

    return null;
  }

  /**
   * method: getByRecNo (int RecNo) <BR>
   * returns a ShapeRecord-Geometry by RecorcNumber <BR>
   */
  public ISHPGeometry getShape( final SHXRecord record ) throws IOException
  {
    final int position = record.getOffset() * 2;
    final int contentLength = record.getLength() * 2;

    final byte[] recBuf = new byte[contentLength];
    m_raf.seek( position + 8 );
    m_raf.readFully( recBuf );

    final ShapeType shpType = ShapeType.valueOf( ByteUtils.readLEInt( recBuf, 0 ) );

    // create a geometry out of record buffer with shapetype
    switch( shpType )
    {
      case NULL:
        return new SHPNullShape();
      case POINT:
        return new SHPPoint( recBuf );
      case MULTIPOINT:
        return new SHPMultiPoint( recBuf );
      case POLYLINE:
        return new SHPPolyLine( recBuf );
      case POLYGON:
        return new SHPPolygon( recBuf );
      case POINTZ:
        return new SHPPointz( recBuf );
      case POLYLINEZ:
        return new SHPPolyLinez( recBuf );
      case POLYGONZ:
        return new SHPPolygonz( recBuf );
      case MULTIPOINTZ:
        return new SHPMultiPointz( recBuf );
      default:
        throw new NotImplementedException( "Unknown shape type: " + shpType );
    }
  }

  public ShapeType getShapeType( )
  {
    return m_header.getShapeType();
  }

  /**
   * Adds a new shape entry to the end of the file.<br>
   * This method is atomic in that sense that if an exception occurs while a shape convertet to bytes, inetad a
   * Null-Shape will be written.
   */
  public SHXRecord addShape( final ISHPGeometry shape, final int recordNumber ) throws IOException, SHPException
  {
    if( shape == null )
      throw new SHPException( "shape == null not allowed. Add SHPNullShape instead." );

    if( !(shape instanceof SHPNullShape) && shape.getType() != m_header.getShapeType() )
      throw new SHPException( "Cannot add shape, wrong type." );

    final long currentFileLength = m_raf.length();

    /* Convert shape into bytes and write them in one go */
    final byte[] bytes = writeRecordAsBytes( recordNumber, shape );
    m_raf.seek( currentFileLength );
    m_raf.write( bytes );

    /* Update header */
    final SHPEnvelope shapeMbr = shape.getEnvelope();
    expandMbr( shapeMbr );

    /* Create and return index record */
    final int contentLength = bytes.length - RECORD_HEADER_BYTES;
    return new SHXRecord( (int) currentFileLength / 2, contentLength / 2 );
  }

  private byte[] writeRecordAsBytes( final int recordNumber, final ISHPGeometry shape ) throws IOException
  {
    final int contentLength = shape.length() + 4;
    final int recordLength = RECORD_HEADER_BYTES + contentLength;

    final ByteArrayOutputStream out = new ByteArrayOutputStream( recordLength );
    final DataOutputStream os = new DataOutputStream( out );

    /* Record Header */
    os.writeInt( recordNumber );
    os.writeInt( contentLength / 2 );

    /* record Content */
    DataUtils.writeLEInt( os, shape.getType().getType() );
    shape.write( os );

    os.flush();

    final byte[] byteArray = out.toByteArray();

    Assert.isTrue( recordLength == byteArray.length );

    return byteArray;
  }

  private void expandMbr( final SHPEnvelope shapeMbr )
  {
    final SHPEnvelope mbr = m_header.getMBR();
    final SHPEnvelope newMbr = mbr == null ? shapeMbr : mbr.expand( shapeMbr );
    final ShapeType shapeType = m_header.getShapeType();
    // PERFORMANCE: we always set -1 as file length here. The file length will e updated as soon as the
    // header gets really written
    m_header = new ShapeHeader( -1, shapeType, newMbr );
  }

  private boolean isWriting( )
  {
    return m_mode == FileMode.WRITE;
  }

}