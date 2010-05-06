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

import java.io.IOException;
import java.nio.charset.Charset;

import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.shp.SHPException;

/**
 * Writes a {@link ShapeFile} based on data from a {@link IShapeDataProvider}.
 * 
 * @author Gernot Belger
 */
public class ShapeWriter
{
  private final IShapeDataProvider m_dataProvider;

  public ShapeWriter( final IShapeDataProvider dataProvider )
  {
    m_dataProvider = dataProvider;
  }

  /**
   * @param shapeFileBase
   *          The absolute file path to the shape file that will be written.
   */
  public void write( final String shapeFileBase, final Charset charset ) throws IOException, DBaseException, SHPException, ShapeDataException
  {
    final byte shapeType = m_dataProvider.getShapeType();
    final DBFField[] fields = m_dataProvider.getFields();

    final ShapeFile shapeFile = ShapeFile.create( shapeFileBase, shapeType, charset, fields );
    try
    {
      writeData( fields, shapeFile );
      // verbose close
      shapeFile.close();
    }
    finally
    {
      try
      {
        // quiet close
        shapeFile.close();
      }
      catch( final Exception e )
      {
        // ignore
      }
    }
  }

  private void writeData( final DBFField[] fields, final ShapeFile shapeFile ) throws IOException, DBaseException, SHPException, ShapeDataException
  {
    final int rows = m_dataProvider.size();
    for( int row = 0; row < rows; row++ )
    {
      final ISHPGeometry geometry = m_dataProvider.getGeometry( row );

      final Object[] data = getRow( row, fields );
      shapeFile.addFeature( geometry, data );
    }
  }

  private Object[] getRow( final int row, final DBFField[] fields ) throws ShapeDataException
  {
    final Object[] data = new Object[fields.length];
    for( int i = 0; i < data.length; i++ )
      data[i] = m_dataProvider.getData( row, i );
    return data;
  }

}
