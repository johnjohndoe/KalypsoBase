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
package org.kalypso.zml.ui.table.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.template.IObsProvider;
import org.kalypso.zml.ui.table.IZmlTableColumn;
import org.kalypso.zml.ui.table.schema.ColumnType;
import org.kalypso.zml.ui.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.utils.ZmlTableHelper;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnRegistry
{
  List<IZmlTableColumn> m_columns = new ArrayList<IZmlTableColumn>();

  private final ZmlTableType m_type;

  public ZmlColumnRegistry( final ZmlTableType type )
  {
    m_type = type;
  }

  public void addColumn( final IZmlTableColumn column )
  {
    m_columns.add( column );
  }

  public void clean( )
  {
    m_columns.clear();
  }

  public Object[] getInput( ) throws SensorException
  {
    // TODO always date?!?
    final Map<Date, ZmlTableRow> map = new TreeMap<Date, ZmlTableRow>();

    for( int c = 0; c < m_columns.size(); c++ )
    {
      final IZmlTableColumn column = m_columns.get( c );

      final IObsProvider provider = column.getObsProvider();
      final IObservation observation = provider.getObservation();
      final ITupleModel model = observation.getValues( null );
      final IAxis[] axes = model.getAxisList();

      // FIXME handle data src axis
      final ColumnType type = getColumn( column.getId() );
      final IAxis indexAxis = ZmlTableHelper.getIndexAxis( type, axes );

      for( int i = 0; i < model.size(); i++ )
      {
        // TODO type less!
        final Date date = (Date) model.get( i, indexAxis );

        ZmlTableRow structure = map.get( date );
        if( structure == null )
        {
          structure = new ZmlTableRow( date, m_columns.size() );
          map.put( date, structure );
        }

        final ZmlValueReference reference = new ZmlValueReference( column, observation, model, i, type );
        structure.add( reference );
      }
    }

    return map.values().toArray();
  }

  private ColumnType getColumn( final String id )
  {
    final List<ColumnType> columns = m_type.getColumns().getColumn();
    for( final ColumnType column : columns )
    {
      if( column.getId().equals( id ) )
        return column;
    }

    return null;
  }
}
