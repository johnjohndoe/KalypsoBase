/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.shape.dbf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.tools.TmpFileHelper;

/**
 * @author Gernot Belger
 */
public class DBaseFileTest extends Assert
{
  private final TmpFileHelper m_tmpFiles = new TmpFileHelper( true );

  @Test
  public void testFields( ) throws DBaseException
  {
    newFieldException( "stringWithPrecision", FieldType.C, (byte) 10, (byte) 2 );
    newFieldOK( "string", FieldType.C, (byte) 10, (byte) 0 );

    newFieldException( "dateLengthTooLong", FieldType.D, (byte) 100, (byte) 2 );
    newFieldException( "dateLengthTooShort", FieldType.D, (byte) 7, (byte) 0 );
    newFieldException( "dateWithPrecision", FieldType.D, (byte) 8, (byte) 3 );
    newFieldOK( "dateWithPrecision", FieldType.D, (byte) 8, (byte) 0 );

    newFieldOK( "float", FieldType.F, (byte) 10, (byte) 5 );
    newFieldException( "floatWithTooBigPrecision", FieldType.F, (byte) 10, (byte) 9 );
    newFieldException( "anotherFloatWithTooBigPrecision", FieldType.F, (byte) 100, (byte) 16 );
    newFieldOK( "double", FieldType.F, (byte) 20, (byte) 10 );
    newFieldOK( "integer", FieldType.F, (byte) 10, (byte) 0 );
    newFieldOK( "long", FieldType.F, (byte) 20, (byte) 0 );

    newFieldException( "booleanTooLong", FieldType.L, (byte) 2, (byte) 0 );
    newFieldException( "booleanWithPrecision", FieldType.L, (byte) 1, (byte) 1 );
    newFieldOK( "boolean", FieldType.L, (byte) 1, (byte) 0 );

    newFieldException( "memoTooShort", FieldType.M, (byte) 9, (byte) 0 );
    newFieldException( "memoTooLong", FieldType.M, (byte) 11, (byte) 0 );
    newFieldException( "memoWithPrecision", FieldType.M, (byte) 10, (byte) 2 );
    newFieldOK( "memo", FieldType.M, (byte) 10, (byte) 0 );

    newFieldOK( "number_float", FieldType.F, (byte) 10, (byte) 5 );
    newFieldException( "number_floatWithTooBigPrecision", FieldType.F, (byte) 10, (byte) 9 );
    newFieldOK( "number_double", FieldType.F, (byte) 20, (byte) 10 );
    newFieldOK( "number_integer", FieldType.F, (byte) 10, (byte) 0 );
    newFieldOK( "number_long", FieldType.F, (byte) 20, (byte) 0 );
  }

  /*
   * Create a field, should not throw an exception
   */
  private void newFieldOK( final String name, final FieldType type, final byte length, final byte precision ) throws DBaseException
  {
    new DBFField( name, type, length, precision );
  }

  private void newFieldException( final String name, final FieldType type, final byte length, final byte precision )
  {
    try
    {
      final DBFField field = new DBFField( name, type, length, precision );
      fail( "Field with wrong arguments was created: " + field );
    }
    catch( final DBaseException e )
    {
    }
  }

  @Test
  public void writeReadEmpty( ) throws IOException, DBaseException
  {
    final File dbfFile = m_tmpFiles.create( "dbaseTest-writeEmpty", ".dbf" );

    final DBFField[] fields = createAllFields();

    final Charset charset = Charset.forName( "ISO-8859-1" );

    final DBaseFile dBaseFile = DBaseFile.create( dbfFile, fields, charset );
    dBaseFile.close();

    assertTrue( dbfFile.exists() );

    final int recordNum = dBaseFile.getNumRecords();
    assertEquals( 0, recordNum );

    final DBFField[] readFields = dBaseFile.getFields();
    assertEquals( fields.length, readFields.length );

    for( int i = 0; i < readFields.length; i++ )
    {
      final DBFField orgField = fields[i];
      final DBFField readField = readFields[i];

      assertEquals( orgField.getName(), readField.getName() );
      assertEquals( orgField.getLength(), readField.getLength() );
      assertEquals( orgField.getDecimalCount(), readField.getDecimalCount() );
      assertEquals( orgField.getType(), readField.getType() );
    }
  }

  private DBFField[] createAllFields( ) throws DBaseException
  {
    final DBFField[] fields = new DBFField[7];

    fields[0] = new DBFField( "shortString", FieldType.C, (byte) 10, (byte) 0 );
    fields[1] = new DBFField( "longString", FieldType.C, (byte) 100, (byte) 0 );

    fields[2] = new DBFField( "date", FieldType.D, (byte) 8, (byte) 0 );

    fields[3] = new DBFField( "int", FieldType.N, (byte) 10, (byte) 0 );
    fields[4] = new DBFField( "double", FieldType.F, (byte) 20, (byte) 10 );

    fields[5] = new DBFField( "boolean", FieldType.L, (byte) 1, (byte) 0 );

    fields[6] = new DBFField( "memo÷ƒ‹", FieldType.M, (byte) 10, (byte) 0 );

    return fields;
  }

  @Test
  public void writeReadData( ) throws DBaseException, IOException
  {
    final File dbfFile = m_tmpFiles.create( "dbaseTest-writeData", ".dbf" );
    final DBFField[] fields = createAllFields();

    final Charset charset = Charset.forName( "UTF-8" );
    final DBaseFile dBaseFile = DBaseFile.create( dbfFile, fields, charset );

    final Object[][] data = createData();

    dBaseFile.addRecord( data[0] );
    dBaseFile.addRecord( data[1] );
    dBaseFile.addRecord( data[2] );
    dBaseFile.deleteRecord( 1 );
    dBaseFile.deleteRecord( 2 );
    dBaseFile.setRecord( 1, data[3] );
    dBaseFile.addRecord( data[4] );

    try
    {
      dBaseFile.deleteRecord( 4 );
      fail( "Should be out of bounds" );
    }
    catch( final DBaseException e )
    {
    }

    try
    {
      dBaseFile.setRecord( 4, data[4] );
      fail( "Shoudl be out of bounds" );
    }
    catch( final DBaseException e )
    {
      // should be out of bounds
    }

    assertEquals( 4, dBaseFile.getNumRecords() );
    dBaseFile.close();

    /* Read and compare results */
    final DBaseFile readFile = new DBaseFile( dbfFile, FileMode.READ, charset );

    final int numRecords = readFile.getNumRecords();
    assertEquals( 4, numRecords );

    compareRecord( data[0], readFile.getRecord( 0 ) );
    compareRecord( data[3], readFile.getRecord( 1 ) );
    compareRecord( null, readFile.getRecord( 2 ) );
    compareRecord( data[4], readFile.getRecord( 3 ) );

    readFile.close();
  }

  private Object[][] createData( )
  {
    final Object[][] data = new Object[6][];

    final Calendar now = Calendar.getInstance();
    final Calendar today = Calendar.getInstance();
    today.clear();
    today.set( Calendar.YEAR, now.get( Calendar.YEAR ) );
    today.set( Calendar.MONTH, now.get( Calendar.MONTH ) );
    today.set( Calendar.DAY_OF_MONTH, now.get( Calendar.DAY_OF_MONTH ) );
    final Date todayDate = today.getTime();

    data[0] = new Object[] { "abc", "Some nice long string.......", todayDate, new Long( 10000 ), 1234.56, Boolean.TRUE, "ˆ‰¸*^ﬂ" };
    data[1] = new Object[] { "", "", todayDate, 0, 0.1, null, null };
    data[2] = new Object[] { "dkk", "Another nice long string.......", todayDate, -100, Math.PI, Boolean.FALSE, "131846" };
    data[3] = new Object[] { "qwertz", "Yet another nice long string.......", todayDate, new Long( 1234566 ), 0.0001, null, "xabcdgh" };
    data[4] = new Object[] { "", "", null, null, null, null, "" };

    return data;
  }

  private void compareRecord( final Object[] expected, final Object[] actual )
  {
    if( expected == null )
    {
      assertNull( actual );
      return;
    }

    assertEquals( expected.length, actual.length );
    for( int i = 0; i < actual.length; i++ )
    {
      final Object expectedValue = expected[i];
      final Object actualValue = actual[i];

      if( expectedValue instanceof Number && actualValue instanceof Number )
      {
        final double expectedDouble = ((Number) expectedValue).doubleValue();
        final double actualDouble = ((Number) actualValue).doubleValue();
        assertEquals( expectedDouble, actualDouble, 0.0001 );
      }
      else
        assertEquals( "Field#: " + i, expectedValue, actualValue );
    }
  }

}
