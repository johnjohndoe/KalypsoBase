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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeHeader;
import org.kalypso.shape.ShapeType;
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

  private RandomAccessFile m_rafx = null;

  private final List<SHXRecord> m_index = new ArrayList<>();

  /**
   * IndexFileHeader is equal to ShapeFileHeader
   */
  private ShapeHeader m_header;

  private final boolean m_syncWriteHeader = false;

  private final boolean m_syncWriteRecord = false;

  private final FileMode m_mode;

  private final File m_shxFile;

  public static SHXFile create( final File file, final ShapeType shapeType ) throws IOException
  {
    if( file.isFile() && file.exists() )
      file.delete();

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
    m_shxFile = file;

    m_mode = mode;

    try( DataInputStream inputStream = new DataInputStream( new BufferedInputStream( new FileInputStream( file ) ) ) )
    {
      m_header = new ShapeHeader( inputStream );

      // FIXME: not nice to load shape index in constructor, would better be lazy

      // FIXME: index positions are fixed, do we need to read the whole file in one go? maybe also use random access

      // loop over index records, until EOF
      final byte[] recBuf = new byte[INDEX_RECORD_LENGTH];
      while( inputStream.read( recBuf, 0, INDEX_RECORD_LENGTH ) != -1 )
      {
        final SHXRecord ir = new SHXRecord( recBuf );
        m_index.add( ir );
      }
    }
  }

  public void close( ) throws IOException
  {
    if( m_rafx != null )
    {
      m_rafx.close();
      m_rafx = null;
    }

    if( m_mode == FileMode.WRITE && !m_syncWriteRecord )
      writeContent();
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
      final RandomAccessFile raf = getRandomAccessFile();

      final int filePos = ShapeHeader.SHAPE_FILE_HEADER_LENGTH + index * INDEX_RECORD_LENGTH;
      raf.seek( filePos );
      record.write( raf );
    }

    if( m_syncWriteHeader )
    {
      final RandomAccessFile raf = getRandomAccessFile();

      raf.seek( 0 );
      m_header.write( raf );
    }
  }

  private RandomAccessFile getRandomAccessFile( ) throws FileNotFoundException
  {
    if( m_rafx == null )
    {
      final String rwMode = m_mode == FileMode.READ ? "r" : "rw"; //$NON-NLS-1$ //$NON-NLS-2$
      m_rafx = new RandomAccessFile( m_shxFile, rwMode );
    }

    return m_rafx;
  }

  /**
   * Writes the contents of the index in one go into the shx file.
   */
  private void writeContent( ) throws IOException
  {
    try( final DataOutputStream outputStream = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( m_shxFile ) ) ) )
    {
      m_header.write( outputStream );

      for( int i = 0; i < m_index.size(); i++ )
      {
        final SHXRecord record = m_index.get( i );
        record.write( outputStream );
      }
    }
  }
}