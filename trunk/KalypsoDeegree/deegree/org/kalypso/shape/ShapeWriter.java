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
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.IDBFValue;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.shp.SHPException;
import org.kalypso.transformation.PrjHelper;

/**
 * Writes a {@link ShapeFile} based on data from a {@link IShapeDataProvider}.
 * 
 * @author Gernot Belger
 */
public class ShapeWriter
{
  private final IShapeData m_data;

  public ShapeWriter( final IShapeData dataProvider )
  {
    m_data = dataProvider;
  }

  /**
   * @param shapeFileBase
   *          The absolute file path to the shape file that will be written.
   */
  public void write( final String shapeFileBase, final IProgressMonitor monitor ) throws IOException, DBaseException, SHPException, ShapeDataException, CoreException
  {
    final String taskMsg = String.format( "Writing shape %s", shapeFileBase );

    monitor.beginTask( taskMsg, m_data.size() );

    final Charset charset = m_data.getCharset();
    final int shapeType = m_data.getShapeType();
    final IDBFValue[] fields = m_data.getFields();
    final DBFField[] dbfFields = new DBFField[fields.length];
    for( int i = 0; i < fields.length; i++ )
      dbfFields[i] = fields[i].getField();

    final ShapeFile shapeFile = ShapeFile.create( shapeFileBase, shapeType, charset, dbfFields );
    try
    {
      writeData( fields, shapeFile, monitor );
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

      monitor.done();
    }
  }

  /**
   * Fetches the coordinate system as ESRI PRJ file to the given destination.<br>
   * Long running, as this involves access to internet.<br>
   */
  public void writePrj( final String shapeFileBase, final IProgressMonitor monitor ) throws CoreException
  {
    final String coordinateSystem = m_data.getCoordinateSystem();

    final File prjFile = new File( shapeFileBase + ".prj" );

    PrjHelper.fetchPrjFile( coordinateSystem, prjFile, monitor );
  }

  private void writeData( final IDBFValue[] fields, final ShapeFile shapeFile, final IProgressMonitor monitor ) throws IOException, DBaseException, SHPException, ShapeDataException, CoreException
  {
    int count = 0;
    final String size = m_data.size() == -1 ? "Unknown" : String.format( "%d", m_data.size() );
    for( final Iterator< ? > iterator = m_data.iterator(); iterator.hasNext(); )
    {
      final Object element = iterator.next();

      if( count % 100 == 0 )
        monitor.subTask( String.format( "Writing shape %d/%s", count, size ) );

      final ISHPGeometry geometry = m_data.getGeometry( element );

      final Object[] data = getRow( element, fields );
      shapeFile.addFeature( geometry, data );

      ProgressUtilities.worked( monitor, 1 );
      count++;
    }
  }

  private Object[] getRow( final Object row, final IDBFValue[] fields ) throws ShapeDataException
  {
    final Object[] data = new Object[fields.length];
    for( int i = 0; i < data.length; i++ )
      data[i] = fields[i].getValue( row );
    return data;
  }

}
