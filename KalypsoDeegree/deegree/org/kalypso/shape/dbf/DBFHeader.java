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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Calendar;

import org.kalypso.shape.tools.DataUtils;

/**
 * Original Author: Andreas Poth
 */
class DBFHeader
{
  private final int m_numRecords;

  private final int m_dataStartPosition;

  private final DBFFields m_fields;

  public DBFHeader( final DBFFields fields )
  {
    this( fields, 0, 32 + fields.size() * 32 + 1 );
  }

  public DBFHeader( final DBFFields fields, final int numRecords, final int dataStartPosition )
  {
    m_fields = fields;
    m_numRecords = numRecords;
    m_dataStartPosition = dataStartPosition;
  }

  public static DBFHeader read( final DataInput raf, final Charset charset ) throws IOException, DBaseException
  {
    final int fileType = raf.readUnsignedByte();
    if( (fileType & 3) != 3 )
      throw new DBaseException( "Only DBase III supported" );

    // get the last update date
    /* final int year = */raf.readUnsignedByte();
    /* final int month = */raf.readUnsignedByte();
    /* final int day = */raf.readUnsignedByte();

    final int numRecords = DataUtils.readLEInt( raf );
    final int dataStartPosition = DataUtils.readLEShort( raf );
    final int headerRecordLength = DataUtils.readLEShort( raf );

    final int numColumns = (dataStartPosition - 33) / 32;

    raf.skipBytes( 19 );

    final DBFFields fields = DBFFields.read( raf, numColumns, charset );

    final int recordLength = fields.getRecordLength();
    if( recordLength > headerRecordLength )
      throw new DBaseException( "Actual record length bigger than described in file." );

    return new DBFHeader( fields, numRecords, dataStartPosition );
  }

  public void write( final DataOutput output, final int numberOfRecords, final Charset charset ) throws IOException
  {
    output.writeByte( (byte) 3 ); // File-Type: DBase III // TODO: add bits for memo file if necessary

    // set date YYMMDD
    final Calendar now = Calendar.getInstance();
    output.writeByte( (byte) (now.get( Calendar.YEAR ) - 1900) );
    output.writeByte( (byte) (now.get( Calendar.MONTH ) + 1) );
    output.writeByte( (byte) now.get( Calendar.DAY_OF_MONTH ) );

    DataUtils.writeLEInt( output, numberOfRecords );
    DataUtils.writeLEShort( output, 32 + m_fields.size() * 32 + 1 );
    DataUtils.writeLEShort( output, m_fields.getRecordLength() );

    // Unused stuff...
    DataUtils.writeLEShort( output, 0 ); // Reserved
    output.writeByte( (byte) 0 ); // No incomplete transaction
    output.writeByte( (byte) 0 ); // Not encrypted
    for( int i = 16; i < 27; i++ )
      // Reserved for multi-user processing.
      output.writeByte( (byte) 0 );

    output.writeByte( (byte) 0 ); // no mdx-file exists

    // TODO: write charset name here?
    output.writeByte( (byte) 0x03 ); // Code page 1252

    DataUtils.writeLEShort( output, 0 ); // Reserved

    m_fields.write( output, charset );

    output.writeByte( (byte) 0x0D ); // field terminator
  }

  public int getNumRecords( )
  {
    return m_numRecords;
  }

  public long getRecordPosition( final int recordIndex )
  {
    return m_dataStartPosition + m_fields.getRecordLength() * recordIndex;
  }

  public void writeNumRecords( final RandomAccessFile raf, final int numberOfRecords ) throws IOException
  {
// final byte[] numRecordBytes = new byte[4];
// ByteUtils.writeLEInt( numRecordBytes, 0, numberOfRecords );
    raf.seek( 4 );
    DataUtils.writeLEInt( raf, numberOfRecords );
// raf.write( numRecordBytes );
  }

  public DBFFields getFields( )
  {
    return m_fields;
  }
}
