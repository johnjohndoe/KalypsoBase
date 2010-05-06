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
package org.kalypso.shape;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.shp.SHPException;
import org.kalypso.shape.tools.TmpFileHelper;

/**
 * @author Gernot Belger
 */
public class ShapeFileTest extends Assert
{
  private final TmpFileHelper m_tmpFiles = new TmpFileHelper( true );

  @Test
  public void writeReadEmpty( ) throws IOException, DBaseException
  {
    final File file = m_tmpFiles.create( "shapeTest-writeEmpty", "" );
    final String basePath = file.getAbsolutePath();

    final Charset charset = Charset.forName( "UTF-8" );
    final DBFField[] fields = createFields();
    final ShapeFile shapeFile = ShapeFile.create( basePath, ShapeConst.SHAPE_TYPE_POLYGON, charset, fields );
    shapeFile.close();

    final File[] shpFiles = createFiles( basePath );
    m_tmpFiles.addFiles( shpFiles );
    for( final File shpFile : shpFiles )
      assertTrue( shpFile.exists() );

    assertNull( shapeFile.getFileMBR() );
    assertEquals( ShapeConst.SHAPE_TYPE_POLYGON, shapeFile.getShapeType() );
  }

  private File[] createFiles( final String basePath )
  {
    final File[] files = new File[3];
    files[0] = new File( basePath + ".shp" );
    files[1] = new File( basePath + ".shx" );
    files[2] = new File( basePath + ".dbf" );
    return files;
  }

  private DBFField[] createFields( ) throws DBaseException
  {
    final DBFField[] fields = new DBFField[2];
    fields[0] = new DBFField( "text", FieldType.C, (byte) 10, (byte) 0 );
    fields[1] = new DBFField( "number", FieldType.N, (byte) 5, (byte) 0 );
    return fields;
  }

  @Test
  public void writeReadData( ) throws Exception
  {
    final File file = m_tmpFiles.create( "shapeTest-writeData", "" );
    final String basePath = file.getAbsolutePath();

    final Charset charset = Charset.forName( "UTF-8" );
    final DBFField[] fields = createFields();
    final ShapeFile shapeFile = ShapeFile.create( basePath, ShapeConst.SHAPE_TYPE_POLYLINE, charset, fields );

    final File[] shpFiles = createFiles( basePath );
    m_tmpFiles.addFiles( shpFiles );

    final ISHPGeometry[] goodShapes = createGoodShapes();
    final ISHPGeometry[] badShapes = createBadShapes();
    final Object[][] goodData = createGoodData();
    final Object[][] badData = createBadData();

    addShapes( shapeFile, goodShapes, goodData, true );
    addShapes( shapeFile, goodShapes, badData, false );
    addShapes( shapeFile, badShapes, goodData, false );
    addShapes( shapeFile, badShapes, badData, false );

    final int elementNumber = goodShapes.length * goodData.length;
    assertEquals( elementNumber, shapeFile.getNumRecords() );

    shapeFile.close();

    final ShapeFile readShapeFile = new ShapeFile( basePath, charset, FileMode.READ );

    assertEquals( elementNumber, readShapeFile.getNumRecords() );

    // TODO: compare with written data

    final int numRecords = readShapeFile.getNumRecords();
    assertEquals( goodShapes.length * goodData.length, numRecords );

    readShapeFile.close();
  }

  private void addShapes( final ShapeFile shpFile, final ISHPGeometry[] shapes, final Object[][] data, final boolean isGood ) throws Exception
  {
    for( final ISHPGeometry shape : shapes )
    {
      for( final Object[] objects : data )
      {
        try
        {
          shpFile.addFeature( shape, objects );
          if( !isGood )
          {
            final String msg = String.format( "This combination should not work: shape = %s;  data = %s", shape, objects );
            fail( msg );
          }
        }
        catch( final IOException e )
        {
          if( isGood )
            throw e;
        }
        catch( final SHPException e )
        {
          if( isGood )
            throw e;
        }
        catch( final DBaseException e )
        {
          if( isGood )
            throw e;
        }
      }
    }
  }

  private Object[][] createGoodData( )
  {
    final Object[] dataOk = new Object[] { "someText", 123 };
    final Object[] dataAllNull = new Object[] { null, null };

    final Collection<Object[]> data = new ArrayList<Object[]>();

    data.add( dataOk );
    data.add( dataAllNull );

    return data.toArray( new Object[data.size()][] );
  }

  private Object[][] createBadData( )
  {
    final Object[] dataNull = null;
    final Object[] dataTooLong = new Object[] { "tooLong", 123, 456 };
    final Object[] dataTooShort = new Object[] { "tooShort" };
    final Object[] dataWrongTypes = new Object[] { 123, "wrongTypeHere" };

    final Collection<Object[]> data = new ArrayList<Object[]>();

    data.add( dataNull );
    data.add( dataTooLong );
    data.add( dataTooShort );
    data.add( dataWrongTypes );

    return data.toArray( new Object[data.size()][] );
  }

  private ISHPGeometry[] createGoodShapes( )
  {
    final ISHPGeometry shapeNormalLine = new SHPPolyLine( new SHPPoint[][] { new SHPPoint[] { new SHPPoint( 1, 2 ), new SHPPoint( 2, 3 ), new SHPPoint( 3, 4 ) } } );
    final ISHPGeometry shapeNullShape = new SHPNullShape();

    final Collection<ISHPGeometry> shapes = new ArrayList<ISHPGeometry>();

    shapes.add( shapeNormalLine );
    shapes.add( shapeNullShape );

    return shapes.toArray( new ISHPGeometry[shapes.size()] );
  }

  private ISHPGeometry[] createBadShapes( )
  {
    final ISHPGeometry shapeNull = null;
    final ISHPGeometry shapeWrongType = new SHPPointz( 1.0, 2.0, 3.0, Double.NaN );

    final Collection<ISHPGeometry> shapes = new ArrayList<ISHPGeometry>();

    shapes.add( shapeNull );
    shapes.add( shapeWrongType );

    return shapes.toArray( new ISHPGeometry[shapes.size()] );
  }

}
