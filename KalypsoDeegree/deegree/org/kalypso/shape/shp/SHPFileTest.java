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
package org.kalypso.shape.shp;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeConst;
import org.kalypso.shape.ShapeHeader;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.shx.SHXRecord;
import org.kalypso.shape.tools.TmpFileHelper;

/**
 * @author Gernot Belger
 */
public class SHPFileTest extends Assert
{
  private final TmpFileHelper m_tmpFiles = new TmpFileHelper( true );

  @Test
  public void writeReadEmpty( ) throws IOException
  {
    final File file = m_tmpFiles.create( "shpTest-writeEmpty", ".shp" );

    final SHPFile shpFile = SHPFile.create( file, ShapeConst.SHAPE_TYPE_POLYGON );
    shpFile.close();

    assertTrue( file.exists() );
    assertEquals( ShapeHeader.SHAPE_FILE_HEADER_LENGTH, file.length() );

    assertNull( shpFile.getMBR() );
    assertEquals( ShapeConst.SHAPE_TYPE_POLYGON, shpFile.getShapeType() );
  }

  @Test
  public void writeReadData( ) throws IOException, SHPException
  {
    final File file = m_tmpFiles.create( "shxTest-writeData", ".shp" );

    final SHPFile shpFile = SHPFile.create( file, ShapeConst.SHAPE_TYPE_POINTZ );

    final SHXRecord record0 = shpFile.addShape( new SHPPointz( 1, 2, 3, 4 ), 0 );
    final SHXRecord record1 = shpFile.addShape( new SHPNullShape(), 1 );
    final SHXRecord record2 = shpFile.addShape( new SHPPointz( 10, 20, 30, 40 ), 2 );

    try
    {
      shpFile.addShape( new SHPPoint( 10, 20 ), 0 );
      fail( "Cannot add shape of wrong type" );
    }
    catch( final Exception e )
    {
    }

    assertEquals( ShapeHeader.SHAPE_FILE_HEADER_LENGTH, record0.getOffset() * 2 );

    final SHPEnvelope mbr1 = shpFile.getEnvelope( record1 );
    assertNull( mbr1 );

    final SHPEnvelope mbr2 = shpFile.getEnvelope( record2 );
    assertNull( mbr2 );
    // REMARK: point shapes always have a null-mbr
// assertNotNull( mbr2 );
// assertEquals( 10, mbr2.west );
// assertEquals( 10, mbr2.east );
// assertEquals( 20, mbr2.north );
// assertEquals( 20, mbr2.south );

    shpFile.close();

    final SHPFile readShpFile = new SHPFile( file, FileMode.READ );
    final SHPEnvelope mbr = readShpFile.getMBR();
    assertEquals( 1, mbr.west, 0.0001 );
    assertEquals( 10, mbr.east, 0.0001 );
    assertEquals( 2, mbr.south, 0.0001 );
    assertEquals( 20, mbr.north, 0.0001 );

    final SHPPointz point0 = (SHPPointz) readShpFile.getShape( record0 );
    final ISHPGeometry point1 = readShpFile.getShape( record1 );
    final SHPPointz point2 = (SHPPointz) readShpFile.getShape( record2 );

    assertTrue( point1 instanceof SHPNullShape );

    assertEquals( 1, point0.getX(), 0.0001 );
    assertEquals( 2, point0.getY(), 0.0001 );
    assertEquals( 3, point0.getZ(), 0.0001 );
    assertEquals( 4, point0.getM(), 0.0001 );

    assertEquals( 10, point2.getX(), 0.0001 );
    assertEquals( 20, point2.getY(), 0.0001 );
    assertEquals( 30, point2.getZ(), 0.0001 );
    assertEquals( 40, point2.getM(), 0.0001 );

    readShpFile.close();
  }
}
