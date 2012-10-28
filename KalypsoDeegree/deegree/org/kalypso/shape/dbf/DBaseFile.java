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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.Assert;
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
public class DBaseFile implements Closeable
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

  private long m_filePosition = -1;

  public static DBaseFile create( final File file, final IDBFField[] fields, final Charset charset ) throws IOException, DBaseException
  {
    if( file.isFile() && file.exists() )
      file.delete();

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

  @Override
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

  public IDBFField[] getFields( )
  {
    return m_header.getFields().getFields();
  }

  /**
   * returns A record of the dbase file. Returns <code>null</code>, if the record is marked as deleted.
   */
  public Object[] getRecord( final int recordIndex ) throws DBaseException, IOException
  {
    final int columns = getFields().length;
    final Object[] container = new Object[columns];

    final boolean isNotDeleted = readRecord( recordIndex, container );
    if( !isNotDeleted )
      return null;

    return container;
  }

  /**
   * Reads a record of the dbase file into a given container. Returns <code>null</code>, if the record is marked as
   * deleted.
   * 
   * @return <code>false</code>, if the record is marked for deletion. The data is not read in this case.
   */
  public boolean readRecord( final int recordIndex, final Object[] container ) throws DBaseException, IOException
  {
    if( recordIndex < 0 || recordIndex >= m_numRecords )
      throw new DBaseException( "Invalid index: " + recordIndex );

    final long position = seekRecord( recordIndex );

    final DBFFields fields = m_header.getFields();
    final boolean recordRead = fields.readRecord( m_raf, m_charset, container );

    m_filePosition = position + fields.getRecordLength();

    return recordRead;
  }

  public Object getValue( final int recordIndex, final String field ) throws DBaseException, IOException
  {
    final int index = getIndex( field );
    if( index < 0 )
      throw new DBaseException( String.format( "Unknown field '%s'", field ) );

    final Object[] record = getRecord( recordIndex );
    return record[index];
  }

  public int getIndex( final String field )
  {
    final IDBFField[] fields = getFields();
    for( int i = 0; i < fields.length; i++ )
    {
      final IDBFField dbfField = fields[i];
      if( dbfField.getName().equalsIgnoreCase( field ) )
        return i;
    }

    return -1;
  }

  /**
   * Really writes the record into the underlying file.<br>
   * This method is atomic, in the sense that a record only gets written if all fields could successfully be written.
   */
  private void writeRecord( final Object[] data, final int numRecords ) throws DBaseException, IOException
  {
    seekRecordForWrite( numRecords );

    final byte[] bytes = recordAsBytes( data );
    m_raf.write( bytes );
  }

  private byte[] recordAsBytes( final Object[] data ) throws DBaseException, IOException
  {
    final DBFFields fields = m_header.getFields();
    final int recordLength = fields.getRecordLength();
    final ByteArrayOutputStream out = new ByteArrayOutputStream( recordLength );
    final DataOutputStream os = new DataOutputStream( out );
    fields.writeRecord( os, data, m_charset );
    out.flush();

    Assert.isTrue( recordLength == out.size() );

    return out.toByteArray();
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

  private long seekRecord( final int recordIndex ) throws IOException
  {
    final long position = m_header.getRecordPosition( recordIndex );

    /* Although it is possible, we do not allow to write behind the end of the file (that would leave corrupt records) */
    if( position > m_raf.length() )
      throw new ArrayIndexOutOfBoundsException( recordIndex );

    if( m_filePosition != -1 && position == m_filePosition )
      return m_filePosition;

    m_raf.seek( position );

    return position;
  }

  /**
   * Returns the index (with regard to the array return by {@link #getFields()} of the field with a specfic name.
   */
  public int findFieldIndex( final String fieldName )
  {
    final IDBFField[] fields = getFields();
    for( int i = 0; i < fields.length; i++ )
    {
      if( fieldName.equals( fields[i].getName() ) )
        return i;
    }

    return -1;
  }
}
