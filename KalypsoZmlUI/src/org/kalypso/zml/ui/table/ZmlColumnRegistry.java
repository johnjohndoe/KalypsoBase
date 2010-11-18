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
package org.kalypso.zml.ui.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.template.IObsProvider;
import org.kalypso.zml.ui.table.schema.ZmlTableType;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnRegistry
{
  List<IZmlTableColumn> m_columns = new ArrayList<IZmlTableColumn>();

  private final ZmlTableType m_type;

  public class ZmlData
  {
    Map<Date, Number> m_values = new HashMap<Date, Number>();

    public void add( final Date date, final Number value )
    {
      m_values.put( date, value );
    }
  }

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

  public void getInput( ) throws SensorException
  {
    // TODO always date?!?
    final Set<Date> index = new TreeSet<Date>();

    for( final IZmlTableColumn column : m_columns )
    {
      final IObsProvider provider = column.getObsProvider();
      final IObservation observation = provider.getObservation();
      final ITupleModel model = observation.getValues( null );

      for( int i = 0; i < model.size(); i++ )
      {

      }
    }

  }
}
