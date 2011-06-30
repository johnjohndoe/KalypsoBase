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

package org.kalypsodeegree_impl.io.shpapi;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang.NotImplementedException;
import org.kalypsodeegree.model.geometry.ByteUtils;

/**
 * Class representing an ESRI Shape File.
 * <p>
 * Uses class ByteUtils modified from the original package com.bbn.openmap.layer.shape <br>
 * Copyright (C) 1998 BBN Corporation 10 Moulton St. Cambridge, MA 02138 <br>
 * <P>
 * <B>Last changes <B>: <BR>
 * 14.12.1999 ap: import clauses added <BR>
 * 14.12.1999 ap: private variable declarations <BR>
 * 14.12.1999 ap: all public static references removed <BR>
 * 14.12.1999 ap: constructor implemented <BR>
 * 14.12.1999 ap: method: openFiles (String url) implemented <BR>
 * 14.12.1999 ap: method: getFileMBR() implemented <BR>
 * 14.12.1999 ap: method: getRecordNum() implemented <BR>
 * 14.12.1999 ap: method: getRecordMBR(int RecNo) implemented <BR>
 * 14.12.1999 ap: method: getByRecNo(int RecNo) implemented <BR>
 * 14.12.1999 ap: method: getShapeTypeByRecNo(int RecNo) implemented <BR>
 * 21.12.1999 ap: method: openfiles(String url) removed <BR>
 * 21.12.1999 ap: all static final declarations replaced <BR>
 * 07.01.2000 ap: return types of the methods changed from WKBxxxx and Rectangle <BR>
 * to SHPxxxx <BR>
 * 07.01.2000 ap: method getRecordMBR modified - SHAPE_TYPE_MULTIPOINT added <BR>
 * 13.01.2000 ap: method getByRecNo re-implemented <BR>
 * 21.03.2000 ap: method getByRecNo completed; multipoint is now supported <BR>
 * 16.08.2000 ap: method write(..) added <BR>
 * 16.08.2000 ap: method writeHeader(..) added <BR>
 * <!---------------------------------------------------------------------------->
 * 
 * @version 16.08.2000
 * @author Andreas Poth
 */

public class MainFile
{
  /*
   * file suffixes for shp
   */
  private final static String _shp = ".shp";

  /*
   * instance variables
   */
  private final FileHeader sfh;

  private final IndexFile shx;

  /*
   * references to the main file
   */
  private final RandomAccessFile rafShp;

  /**
   * Construct a MainFile from a file name.
   */
  public MainFile( String url ) throws IOException
  {
    rafShp = new RandomAccessFile( url + _shp, "r" );

    sfh = new FileHeader( rafShp );

    shx = new IndexFile( url );
  }

  /**
   * Construct a MainFile from a file name.
   */
  public MainFile( String url, String rwflag ) throws IOException
  {
    // delet file if it exists
    File file = new File( url + _shp );

    if( rwflag.indexOf( 'w' ) > -1 && file.exists() )
      file.delete();
    file = null;

    /*
     * creates rafShp
     */
    rafShp = new RandomAccessFile( url + _shp, rwflag );

    sfh = new FileHeader( rafShp );

    shx = new IndexFile( url, rwflag );
  }

  public void close( ) throws IOException
  {
    rafShp.close();
    shx.close();
  }

  /**
   * method: getFileMBR() <BR>
   * returns the minimum bounding rectangle of geometries <BR>
   * within the shape-file
   */
  public SHPEnvelope getFileMBR( )
  {
    return sfh.getFileMBR();
  }

  /**
   * method: getRecordNum() <BR>
   * returns the number of record with in a shape-file <BR>
   */
  public int getRecordNum( )
  {
    return shx.getRecordNum();
  }

  /**
   * method: getRecordMBR(int RecNo) <BR>
   * returns the minimum bound rectangle of RecNo's Geometrie of the shape-file <BR>
   */
  public SHPEnvelope getRecordMBR( final int recordNumber ) throws IOException
  {
    final byte[] recBuf = readRecord( recordNumber );
    final int shpType = ByteUtils.readLEInt( recBuf, 0 );

    /*
     * only for PolyLines, Polygons and MultiPoints minimum bounding rectangles are defined
     */
    if( (shpType == ShapeConst.SHAPE_TYPE_POLYLINE) || (shpType == ShapeConst.SHAPE_TYPE_POLYGON) || (shpType == ShapeConst.SHAPE_TYPE_MULTIPOINT) || (shpType == ShapeConst.SHAPE_TYPE_POLYLINEZ)
        || (shpType == ShapeConst.SHAPE_TYPE_POLYGONZ) || (shpType == ShapeConst.SHAPE_TYPE_MULTIPOINTZ) )
      return new SHPEnvelope( recBuf );

    return null;
  }

  /**
   * method: getByRecNo (int RecNo) <BR>
   * returns a ShapeRecord-Geometry by RecorcNumber <BR>
   */
  public ISHPGeometry getByRecNo( final int recordNumber ) throws IOException
  {
    final byte[] recBuf = readRecord( recordNumber );
    final int shpType = ByteUtils.readLEInt( recBuf, 0 );

    // create a geometry out of record buffer with shapetype
    switch( shpType )
    {
      case ShapeConst.SHAPE_TYPE_NULL:
        return null;
      case ShapeConst.SHAPE_TYPE_POINT:
        return new SHPPoint( recBuf );
      case ShapeConst.SHAPE_TYPE_MULTIPOINT:
        return new SHPMultiPoint( recBuf );
      case ShapeConst.SHAPE_TYPE_POLYLINE:
        return new SHPPolyLine( recBuf );
      case ShapeConst.SHAPE_TYPE_POLYGON:
        return new SHPPolygon( recBuf );
      case ShapeConst.SHAPE_TYPE_POINTZ:
        return new SHPPointz( recBuf );
      case ShapeConst.SHAPE_TYPE_POLYLINEZ:
        return new SHPPolyLinez( recBuf );
      case ShapeConst.SHAPE_TYPE_POLYGONZ:
        return new SHPPolygonz( recBuf );
      case ShapeConst.SHAPE_TYPE_MULTIPOINTZ:
        return new SHPMultiPointz( recBuf );
      default:
        throw new NotImplementedException( "Unknown shape type: " + shpType );
    }
  }

  private byte[] readRecord( int recordNumber ) throws IOException
  {
    // index in IndexArray (see IndexFile)
    final int iaIndex = recordNumber - 1;

    final int off = shx.getRecordOffset( iaIndex );

    // calculate length from 16-bit words (= 2 bytes) to lenght in bytes
    final int len = shx.getRecordLength( iaIndex ) * 2;

    // off holds the offset of the shape-record in 16-bit words (= 2 byte)
    // multiply with 2 gets number of bytes to seek
    final long rafPos = off * 2;

    // fetch shape record
    rafShp.seek( rafPos + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );

    final byte[] recBuf = new byte[len];
    if( rafShp.read( recBuf, 0, len ) == -1 )
      return null;

    return recBuf;
  }

  public int getFileShapeType( )
  {
    return sfh.getFileShapeType();
  }

  /**
   * method: public void write(byte[] bytearray) <BR>
   * appends a bytearray to the shape file <BR>
   */
  public void write( byte[] bytearray, IndexRecord record, SHPEnvelope mbr ) throws IOException
  {
    rafShp.seek( record.offset * 2 );
    rafShp.write( bytearray );
    shx.appendRecord( record, mbr );
  }

  /**
   * method: public void writeHeader(int filelength, byte shptype, SHPEnvelope mbr) <BR>
   * writes a header to the shape and index file <BR>
   */
  public void writeHeader( int filelength, byte shptype, SHPEnvelope mbr ) throws IOException
  {
    sfh.writeHeader( filelength, shptype, mbr );
    shx.writeHeader( shptype, mbr );
  }

}