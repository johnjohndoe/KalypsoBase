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
import java.nio.charset.Charset;
import java.util.Arrays;

import org.kalypso.shape.tools.DataUtils;

/**
 * Class representing a field descriptor of a dBase III/IV file <br>
 * Original Author: Andreas Poth
 */
public class DBFField
{
  private static final int MAX_COLUMN_NAME_LENGTH = 11;

  private final short m_fieldLength;

  private final short m_decimalCount;

  private final FieldFormatter m_formatter;

  private final FieldType m_type;

  private final String m_name;

  /**
   * constructor recieves name and type of the field, the length of the field in bytes and the decimalcount. the
   * decimalcount is only considered if type id "N" or "F", it's maxvalue if fieldlength - 2!
   */
  public DBFField( final String name, final FieldType type, final short fieldLength, final short decimalCount ) throws DBaseException
  {
    m_name = name;
    m_type = type;
    m_fieldLength = fieldLength;
    m_decimalCount = decimalCount;
    m_formatter = createFormatter( type, fieldLength, decimalCount );

    if( m_decimalCount > 15 )
      throw new DBaseException( "Deicmal count must be smaller than 16" );

    /* Validate arguments */
    if( fieldLength < 0 || fieldLength > 255 )
    {
      final String msg = String.format( "Field length must not exceed 255 (is %d)", fieldLength );
      throw new DBaseException( msg );
    }

    if( decimalCount < 0 || decimalCount > 255 )
    {
      final String msg = String.format( "Decimal count  must not exceed 255 (is %d)", decimalCount );
      throw new DBaseException( msg );
    }

    final int fixedLength = type.getFixedLength();
    final char typeName = type.getName();
    final boolean supportsDecimal = type.isSupportDecimal();
    if( fixedLength != -1 && fieldLength != fixedLength )
    {
      final String msg = String.format( "Datatype '%s' must have fieldLength = %d", typeName, fixedLength );
      throw new DBaseException( msg );
    }

    if( supportsDecimal )
    {
      if( fieldLength - decimalCount < 2 )
        throw new DBaseException( "Invalid fieldlength and/or decimalcount" );
    }
    else if( decimalCount > 0 )
    {
      final String msg = String.format( "Datatype '%s' does not support decimals", typeName );
      throw new DBaseException( msg );
    }
  }

  private FieldFormatter createFormatter( final FieldType type, final short fieldLength, final short decimalCount )
  {
    switch( type )
    {
      case C:
      case M:
        return new FieldFormatterString();

      case N:
      case F:
        return new FieldFormatterNumber( fieldLength, decimalCount );

      case L:
        return new FieldFormatterBoolean();

      case D:
        return new FieldFormatterDate();
    }

    throw new IllegalArgumentException( "Unknow field: " + type );
  }

  public String getName( )
  {
    return m_name;
  }

  public short getLength( )
  {
    return m_fieldLength;
  }

  public FieldType getType( )
  {
    return m_type;
  }

  public short getDecimalCount( )
  {
    return m_decimalCount;
  }

  public byte[] writeValue( final DataOutput output, final Object value, final Charset charset ) throws DBaseException, IOException
  {
    final byte[] bytes = m_formatter.toBytes( value, charset );

    final int bytesToWrite = Math.min( bytes.length, m_fieldLength );

    output.write( bytes, 0, bytesToWrite );

    for( int j = bytesToWrite; j < m_fieldLength; j++ )
      output.writeByte( (byte) 0x20 );

    return bytes;
  }

  public Object readValue( final DataInput input, final Charset charset ) throws IOException, DBaseException
  {
    final byte[] value = new byte[m_fieldLength];
    input.readFully( value );

    final String asString = new String( value, charset ).trim();
    return m_formatter.fromString( asString );
  }

  public static DBFField read( final DataInput input, final Charset charset ) throws IOException, DBaseException
  {
    final byte[] columnNameBytes = new byte[MAX_COLUMN_NAME_LENGTH];
    input.readFully( columnNameBytes );

    final int columNameLength = findColumnNameLength( columnNameBytes );

    final String columnName = new String( columnNameBytes, 0, columNameLength, charset );

    final char columnType = (char) input.readByte();

    input.skipBytes( 4 );

    // get field length and precision
    final short fieldLength = DataUtils.fixByte( input.readByte() );
    final short decimalCount = DataUtils.fixByte( input.readByte() );

    input.skipBytes( 14 );

    final FieldType fieldType = FieldType.valueOf( "" + columnType );
    return new DBFField( columnName, fieldType, fieldLength, decimalCount );
  }

  // HACK: we truncate names at '0' bytes. It is however unclear, if this is according to the specification.
  private static int findColumnNameLength( final byte[] bytes )
  {
    for( int i = 0; i < MAX_COLUMN_NAME_LENGTH; i++ )
    {
      if( bytes[i] == 0 )
        return i;
    }

    return MAX_COLUMN_NAME_LENGTH;
  }

  public void write( final DataOutput output, final Charset charset ) throws IOException
  {
    final byte[] bytes = new byte[32];
    Arrays.fill( bytes, 0, bytes.length, (byte) 0 );

    // copy name into the first 11 bytes
    final String name = getName();
    final byte[] nameBytes = name.getBytes( charset );
    final int nameLength = Math.min( nameBytes.length, 11 );
    System.arraycopy( nameBytes, 0, bytes, 0, nameLength );
    Arrays.fill( bytes, nameLength, 11, (byte) 0 );

    final FieldType type = getType();
    bytes[11] = (byte) type.getName();
    bytes[16] = (byte) getLength();
    bytes[17] = (byte) getDecimalCount();

    bytes[20] = 1; // work area id (don't know if it should be 1)

    bytes[31] = 0x00; // has no index tag in a MDX file

    output.write( bytes );
  }
}