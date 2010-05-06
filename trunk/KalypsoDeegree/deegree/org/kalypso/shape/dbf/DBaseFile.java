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
package org.kalypso.shape.dbf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import org.kalypso.shape.FileMode;

/**
 * DBase III File. see http://www.clicketyclick.dk/databases/xbase/format/dbf.html<br>
 * The datatypes of the dBase file and their representation as java types: <br>
 * dBase-type dBase-type-ID java-type <br>
 * character "C" String <br>
 * float "F" Float <br>
 * number "N" Double <br>
 * logical "L" String <br>
 * memo "M" String <br>
 * date "D" Date <br>
 * binary "B" ByteArrayOutputStream<br>
 * <br>
 * Original Author: Andreas Poth
 */
public class DBaseFile
{
  private final RandomAccessFile m_raf;

  private final DBFHeader m_header;

  private final FileMode m_fileMode;

  private int m_numRecords;

  private final Charset m_charset;

// // TODO: refaktor: create wrapper class over RandomAccessFile instead
// private final long m_cacheSize = 1000000;
//
// private final byte[] m_cacheData = null;
//
// private long m_cachePosition = 0;

  public static DBaseFile create( final File file, final DBFField[] fields, final Charset charset ) throws IOException, DBaseException
  {
    final DBFHeader header = new DBFHeader( new DBFFields( fields ) );

    final RandomAccessFile rdbf = new RandomAccessFile( file, "rw" );
    header.write( rdbf, 0, charset );
    rdbf.writeByte( 0x1A ); // EOF
    rdbf.close();

    return new DBaseFile( file, FileMode.WRITE, charset );
  }

  /**
   * Open a dbase file.
   */
  public DBaseFile( final File file, final FileMode mode, final Charset charset ) throws IOException, DBaseException
  {
    m_charset = charset;
    final String rwMode = mode == FileMode.READ ? "r" : "rw";
    m_raf = new RandomAccessFile( file, rwMode );
    m_header = DBFHeader.read( m_raf, charset );
    m_numRecords = m_header.getNumRecords();
    m_fileMode = mode;
  }

  public void close( ) throws IOException
  {
    if( m_fileMode == FileMode.WRITE )
      m_header.writeNumRecords( m_raf, m_numRecords );

    m_raf.close();
  }

  /**
   * method: getRecordNum() <BR>
   * Get the number of records in the table
   */
  public int getNumRecords( )
  {
    return m_numRecords;
  }

  public DBFField[] getFields( )
  {
    return m_header.getFields().getFields();
  }

  /**
   * returns A record of the dbase file. Returns <code>null</code>, if the record is marked as deleted.
   */
  public Object[] getRecord( final int recordIndex ) throws DBaseException, IOException
  {
    if( recordIndex < 0 || recordIndex >= m_numRecords )
      throw new DBaseException( "Invalid index: " + recordIndex );

    seekRecord( recordIndex );

    final DBFFields fields = m_header.getFields();
    return fields.readRecord( m_raf, m_charset );
  }

  private void writeRecord( final Object[] data, final int numRecords ) throws DBaseException, IOException
  {
    seekRecordForWrite( numRecords );

    final DBFFields fields = m_header.getFields();
    fields.writeRecord( m_raf, data, m_charset );
  }

  /**
   * Adds a new record to the end of this file.
   * 
   * @throws ArrayIndexOutOfBoundsException
   *           If the given recordNum is bigger than the current number of records.
   */
  public void addRecord( final Object[] data ) throws DBaseException, IOException
  {
    writeRecord( data, m_numRecords );
    m_numRecords++;
    m_raf.writeByte( 0x1A ); // EOF
  }

  /**
   * Replaces an existing record with the given one.
   * 
   * @throws ArrayIndexOutOfBoundsException
   *           If the given recordNum is bigger than the current number of records.
   */
  public void setRecord( final int recordIndex, final Object[] data ) throws DBaseException, IOException
  {
    if( recordIndex < 0 || recordIndex >= m_numRecords )
      throw new DBaseException( "Invalid index: " + recordIndex );

    writeRecord( data, recordIndex );
  }

  /**
   * Marks a record as deleted. Does not change the number of records or the order of records.<br>
   * A deleted record can be overwritten by {@link #setRecord(int, Object[])}.<br>
   * Accessing the data of a deleted record returns <code>null</code>.
   * 
   * @throws ArrayIndexOutOfBoundsException
   *           If the given recordNum is bigger or equal ro the current number of records.
   */
  public void deleteRecord( final int recordIndex ) throws DBaseException, IOException
  {
    if( recordIndex < 0 || recordIndex >= m_numRecords )
      throw new DBaseException( "Invalid index: " + recordIndex );

    seekRecordForWrite( recordIndex );
    m_raf.writeByte( 0x2A );
  }

  /**
   * @throws ArrayIndexOutOfBoundsException
   *           If the given recordNum is bigger than the current number of records.
   */
  private void seekRecordForWrite( final int recordIndex ) throws DBaseException, IOException
  {
    if( m_fileMode != FileMode.WRITE )
      throw new DBaseException( "class is initialized in read-only mode" );

    seekRecord( recordIndex );
  }

  private void seekRecord( final int recordIndex ) throws IOException
  {
    final long position = m_header.getRecordPosition( recordIndex );

    /* Although it is possible ,we do not allow to write behind the end of the file (that would leave corrupt records) */
    if( position > m_raf.length() )
      throw new ArrayIndexOutOfBoundsException( recordIndex );

    m_raf.seek( position );
  }
}
