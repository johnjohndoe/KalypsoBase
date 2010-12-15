/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.dialogs.input;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlEinzelwertModel
{
  Set<ZmlEinzelwert> m_rows = new LinkedHashSet<ZmlEinzelwert>();

  private final IZmlTableColumn m_column;

  public ZmlEinzelwertModel( final IZmlTableColumn column )
  {
    m_column = column;

    init();
  }

  private void init( )
  {
    try
    {
      final IZmlModelColumn column = m_column.getModelColumn();
      final ITupleModel model = column.getTupleModel();
      if( model.size() > 0 )
      {
        final Date date = (Date) model.get( 0, column.getIndexAxis() );
        final Double value = (Double) model.get( 0, column.getValueAxis() );

        m_rows.add( new ZmlEinzelwert( date, value ) );
      }
    }
    catch( final Exception e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public ZmlEinzelwert[] getRows( )
  {
    return m_rows.toArray( new ZmlEinzelwert[] {} );
  }

  public void addRow( final ZmlEinzelwert row )
  {
    m_rows.add( row );
  }

  public String getLabel( )
  {
    return m_column.getModelColumn().getLabel();
  }

  public Date[] getExistingDateValues( ) throws SensorException
  {
    final Set<Date> existing = new TreeSet<Date>();

    final IZmlModelColumn columnModel = m_column.getModelColumn();
    final IAxis axis = columnModel.getIndexAxis();
    final ITupleModel model = columnModel.getTupleModel();

    for( int index = 0; index < model.size(); index++ )
    {
      final Object object = model.get( index, axis );
      if( object instanceof Date )
      {
        existing.add( (Date) object );
      }
    }

    return existing.toArray( new Date[] {} );
  }

}
