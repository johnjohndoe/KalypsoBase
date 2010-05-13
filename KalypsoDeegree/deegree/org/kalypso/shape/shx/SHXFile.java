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

package org.kalypso.shape.shx;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeHeader;
import org.kalypso.shape.geometry.SHPEnvelope;

/**
 * Uses class ShapeUtils modified from the original package com.bbn.openmap.layer.shape <br>
 * Copyright (C) 1998 BBN Corporation 10 Moulton St. Cambridge, MA 02138 <br>
 * <br>
 * Original Author: Andreas Poth
 */
public class SHXFile
{
  private static final int INDEX_RECORD_LENGTH = 8;

  private final RandomAccessFile m_raf;

  private final List<SHXRecord> m_index = new ArrayList<SHXRecord>();

  /**
   * IndexFileHeader is equal to ShapeFileHeader
   */
  private ShapeHeader m_header;

  private final boolean m_syncWriteHeader = false;

  private final boolean m_syncWriteRecord = false;

  private final FileMode m_mode;

  public static SHXFile create( final File file, final int shapeType ) throws IOException
  {
    final RandomAccessFile raf = new RandomAccessFile( file, "rw" );

    final ShapeHeader header = new ShapeHeader( ShapeHeader.SHAPE_FILE_HEADER_LENGTH, shapeType, null );
    header.write( raf );
    raf.close();

    return new SHXFile( file, FileMode.WRITE );
  }

  /**
   * Open index for reading.
   */
  public SHXFile( final File file, final FileMode mode ) throws IOException
  {
    m_mode = mode;
    final String rwMode = mode == FileMode.READ ? "r" : "rw";

    m_raf = new RandomAccessFile( file, rwMode );

    m_header = new ShapeHeader( m_raf );

    m_raf.seek( ShapeHeader.SHAPE_FILE_HEADER_LENGTH );

    // loop over index records, until EOF
    final byte[] recBuf = new byte[INDEX_RECORD_LENGTH];
    while( m_raf.read( recBuf, 0, INDEX_RECORD_LENGTH ) != -1 )
    {
      final SHXRecord ir = new SHXRecord( recBuf );
      m_index.add( ir );
    }
  }

  public void close( ) throws IOException
  {
    if( m_mode == FileMode.WRITE && !m_syncWriteRecord )
    {
      m_raf.seek( 0 );
      for( int i = 0; i < m_index.size(); i++ )
      {
        final SHXRecord record = m_index.get( i );
        record.write( m_raf );
      }
    }

    if( m_mode == FileMode.WRITE && !m_syncWriteHeader )
      writeHeader();

    m_raf.close();
  }

  public ShapeHeader getHeader( )
  {
    return m_header;
  }

  /**
   * method: getRecordNum() <BR>
   * function to get number of Records <BR>
   */
  public int getNumRecords( )
  {
    return m_index.size();
  }

  public SHXRecord getRecord( final int recordIndex )
  {
    return m_index.get( recordIndex );
  }

  /**
   * appends an index record to the indexfile<br>
   * The underlying file must be openend for writing and wil be immediately updated.
   */
  public void addRecord( final SHXRecord record, final SHPEnvelope mbr ) throws IOException
  {
    final int index = m_index.size();

    m_index.add( record );

    final int newLength = ShapeHeader.SHAPE_FILE_HEADER_LENGTH + m_index.size() * INDEX_RECORD_LENGTH;
    final SHPEnvelope fileMbr = m_header.getMBR();
    final SHPEnvelope newMbr = fileMbr == null ? mbr : fileMbr.expand( mbr );
    m_header = new ShapeHeader( newLength, m_header.getShapeType(), newMbr );

    if( m_syncWriteRecord )
    {
      final int filePos = ShapeHeader.SHAPE_FILE_HEADER_LENGTH + index * INDEX_RECORD_LENGTH;
      m_raf.seek( filePos );
      record.write( m_raf );
    }

    if( m_syncWriteHeader )
      writeHeader();
  }

  private void writeHeader( ) throws IOException
  {
    m_raf.seek( 0 );
    m_header.write( m_raf );
  }

}