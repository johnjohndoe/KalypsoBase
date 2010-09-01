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
package org.kalypso.shape.shx;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeHeader;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.tools.TmpFileHelper;

/**
 * @author Gernot Belger
 */
public class SHXFileTest extends Assert
{
  private final TmpFileHelper m_tmpFiles = new TmpFileHelper( true );

  @Test
  public void writeReadEmpty( ) throws IOException
  {
    final File file = m_tmpFiles.create( "shxTest-writeEmpty", ".shx" );

    final SHXFile shxFile = SHXFile.create( file, ShapeType.POINTZ );
    shxFile.close();

    assertTrue( file.exists() );

    final int recordNum = shxFile.getNumRecords();
    assertEquals( 0, recordNum );

    final ShapeHeader header = shxFile.getHeader();
    assertNull( header.getMBR() );
    assertEquals( ShapeType.POINTZ, header.getShapeType() );
  }

  @Test
  public void writeReadData( ) throws IOException
  {
    final File file = m_tmpFiles.create( "shxTest-writeEmpty", ".shx" );

    final SHXFile shxFile = SHXFile.create( file, ShapeType.POINTZ );

    shxFile.addRecord( new SHXRecord( 10, 100 ), null );
    shxFile.addRecord( new SHXRecord( 20, 199 ), new SHPEnvelope( 0.1, 0.2, 0.3, 0.4 ) );
    shxFile.addRecord( new SHXRecord( 30, 10 ), new SHPEnvelope( 1, 2, 3, 4 ) );

    assertEquals( 3, shxFile.getNumRecords() );

    shxFile.close();

    final long shxLength = shxFile.getHeader().getLength();
    assertEquals( file.length(), shxLength );

    final SHXFile readShxFile = new SHXFile( file, FileMode.READ );

    assertEquals( 3, readShxFile.getNumRecords() );

    final SHXRecord record0 = readShxFile.getRecord( 0 );
    assertEquals( 10, record0.getOffset() );
    assertEquals( 100, record0.getLength() );

    final SHXRecord record1 = readShxFile.getRecord( 1 );
    assertEquals( 20, record1.getOffset() );
    assertEquals( 199, record1.getLength() );

    final SHXRecord record2 = readShxFile.getRecord( 2 );
    assertEquals( 30, record2.getOffset() );
    assertEquals( 10, record2.getLength() );

    final SHPEnvelope mbr = readShxFile.getHeader().getMBR();
    assertEquals( 0.1, mbr.west, 0.0001 );
    assertEquals( 2, mbr.east, 0.0001 );
    assertEquals( 3, mbr.north, 0.0001 );
    assertEquals( 0.4, mbr.south, 0.0001 );

    readShxFile.close();
  }

}
