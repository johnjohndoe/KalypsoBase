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

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.core.runtime.Assert;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeHeader;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.geometry.SHPMultiPoint;
import org.kalypso.shape.geometry.SHPMultiPointm;
import org.kalypso.shape.geometry.SHPMultiPointz;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointm;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.geometry.SHPPolyLinem;
import org.kalypso.shape.geometry.SHPPolyLinez;
import org.kalypso.shape.geometry.SHPPolygon;
import org.kalypso.shape.geometry.SHPPolygonz;
import org.kalypso.shape.shx.SHXRecord;
import org.kalypso.shape.tools.BufferedRandomAccessFile;
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

public class SHPFile extends AbstractSHPFile
{
  public static SHPFile create( final File file, final ShapeType shapeType ) throws IOException
  {
    if( file.isFile() && file.exists() )
      file.delete();

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
    super( openInput( file, mode ), mode );
  }

  private static DataInput openInput( final File file, final FileMode mode ) throws IOException
  {
    final String rwMode = mode == FileMode.READ ? "r" : "rw"; //$NON-NLS-1$ //$NON-NLS-2$

    switch( mode )
    {
      case READ:
        return new BufferedRandomAccessFile( file, rwMode, 1024 * 8 );
      case WRITE:
        return new RandomAccessFile( file, rwMode );
    }

    throw new IllegalStateException();
  }

  private RandomAccessFile getRandomAccessFile( )
  {
    return (RandomAccessFile)getInput();
  }

  @Override
  public void close( ) throws IOException
  {
    final RandomAccessFile raf = getRandomAccessFile();

    if( isWriting() )
    {
      // Update header with length
      final long fileLength = raf.length();
      updateHeader( (int)fileLength, getMBR() );

      /* and write the header */
      raf.seek( 0 );

      final ShapeHeader header = getHeader();
      header.write( raf );
    }

    raf.close();
  }

  /**
   * returns the minimum bound rectangle of RecNo's Geometrie of the shape-file <BR>
   */
  public SHPEnvelope getEnvelope( final SHXRecord record ) throws IOException
  {
    final RandomAccessFile raf = getRandomAccessFile();

    final int position = record.getOffset() * 2;
    raf.seek( position + 8 );
    final ShapeType shpType = ShapeType.valueOf( DataUtils.readLEInt( raf ) );

    /*
     * only for PolyLines, Polygons and MultiPoints minimum bounding rectangles are defined
     */
    if( shpType == ShapeType.POLYLINE || shpType == ShapeType.POLYGON || shpType == ShapeType.MULTIPOINT || shpType == ShapeType.POLYLINEZ || shpType == ShapeType.POLYGONZ
        || shpType == ShapeType.MULTIPOINTZ )
      return new SHPEnvelope( raf );

    return null;
  }

  /**
   * method: getByRecNo (int RecNo) <BR>
   * returns a ShapeRecord-Geometry by RecorcNumber <BR>
   */
  public ISHPGeometry getShape( final SHXRecord record ) throws IOException
  {
    final RandomAccessFile raf = getRandomAccessFile();

    final int position = record.getOffset() * 2;
    final int contentLength = record.getLength() * 2;

    final byte[] recBuf = new byte[contentLength];
    raf.seek( position + 8 );
    raf.readFully( recBuf );

    final ShapeType shpType = ShapeType.valueOf( ByteUtils.readLEInt( recBuf, 0 ) );
    if( shpType == ShapeType.NULL )
      return new SHPNullShape();

    // create a geometry out of record buffer with shape type
    switch( shpType )
    {
      case NULL:
        return new SHPNullShape();
      case POINT:
        return new SHPPoint( recBuf );
      case MULTIPOINT:
        return SHPMultiPoint.read( recBuf );
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
        return SHPMultiPointz.read( recBuf );
      case POINTM:
        return new SHPPointm( recBuf );
      case MULTIPOINTM:
        return SHPMultiPointm.read( recBuf );
      case POLYLINEM:
        return new SHPPolyLinem( recBuf );
      case POLYGONM:
        return new SHPPolyLinem( recBuf );
    }

    throw new UnsupportedOperationException( "Unknown shape type: " + shpType );
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

    if( !(shape instanceof SHPNullShape) && shape.getType() != getShapeType() )
      throw new SHPException( "Cannot add shape, wrong type." );

    final RandomAccessFile raf = getRandomAccessFile();

    final long currentFileLength = raf.length();

    /* Convert shape into bytes and write them in one go */
    final byte[] bytes = writeRecordAsBytes( recordNumber, shape );
    raf.seek( currentFileLength );
    raf.write( bytes );

    /* Update header */
    final SHPEnvelope shapeMbr = shape.getEnvelope();
    expandMbr( shapeMbr );

    /* Create and return index record */
    final int contentLength = bytes.length - RECORD_HEADER_BYTES;
    return new SHXRecord( (int)currentFileLength / 2, contentLength / 2 );
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

    // TODO: why flush here?
    os.flush();

    final byte[] byteArray = out.toByteArray();

    Assert.isTrue( recordLength == byteArray.length );

    return byteArray;
  }

  private void expandMbr( final SHPEnvelope shapeMbr )
  {
    final SHPEnvelope mbr = getMBR();
    final SHPEnvelope newMbr = mbr == null ? shapeMbr : mbr.expand( shapeMbr );

    // PERFORMANCE: we always set -1 as file length here. The file length will e updated as soon as the
    // header gets really written
    updateHeader( -1, newMbr );
  }

  private boolean isWriting( )
  {
    return getMode() == FileMode.WRITE;
  }
}