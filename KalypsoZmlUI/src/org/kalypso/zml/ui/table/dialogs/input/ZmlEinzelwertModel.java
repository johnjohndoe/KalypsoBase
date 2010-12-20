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

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlEinzelwertModel
{
  Set<ZmlEinzelwert> m_rows = new TreeSet<ZmlEinzelwert>( new Comparator<ZmlEinzelwert>()
  {
    @Override
    public int compare( final ZmlEinzelwert o1, final ZmlEinzelwert o2 )
    {
      return o1.getDate().compareTo( o2.getDate() );
    }
  } );

  Set<IZmlEinzelwertModelListener> m_listeners = new LinkedHashSet<IZmlEinzelwertModelListener>();

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

        m_rows.add( new ZmlEinzelwert( this, date, value ) );
      }
    }
    catch( final Exception e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  public ZmlEinzelwert[] getRows( )
  {
    return m_rows.toArray( new ZmlEinzelwert[] {} );
  }

  public void addRow( final ZmlEinzelwert row )
  {
    m_rows.add( row );

    fireModelChanged();
  }

  protected void fireModelChanged( )
  {
    cleanUp();

    final IZmlEinzelwertModelListener[] listeners = m_listeners.toArray( new IZmlEinzelwertModelListener[] {} );
    for( final IZmlEinzelwertModelListener listener : listeners )
    {
      listener.modelChangedEvent();
    }
  }

  /** sort out duplicated entries */
  private void cleanUp( )
  {
    final Map<Date, ZmlEinzelwert> map = new HashMap<Date, ZmlEinzelwert>();

    final ZmlEinzelwert[] rows = m_rows.toArray( new ZmlEinzelwert[] {} );
    m_rows.clear();
    for( final ZmlEinzelwert row : rows )
    {
      map.put( row.getDate(), row );
    }

    m_rows.addAll( map.values() );
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

  public void addRow( final Date steppingBase, final Integer stepping ) throws SensorException
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime( steppingBase );
    calendar.add( Calendar.HOUR_OF_DAY, stepping );
    final Date base = calendar.getTime();

    final IZmlModelColumn modelColumn = m_column.getModelColumn();
    final ITupleModel model = modelColumn.getTupleModel();
    final IAxis indexAxis = modelColumn.getIndexAxis();
    final IAxis valueAxis = modelColumn.getValueAxis();

    for( int index = 0; index < model.size(); index++ )
    {
      final Object objDate = model.get( index, indexAxis );
      if( !(objDate instanceof Date) )
        continue;

      final Date date = (Date) objDate;
      if( base.equals( date ) )
      {
        final Object objValue = model.get( index, valueAxis );
        if( objValue instanceof Number )
          addRow( new ZmlEinzelwert( this, date, ((Number) objValue).doubleValue() ) );
        else
          addRow( new ZmlEinzelwert( this, date, 0.0 ) );

        return;
      }
    }
  }

  public void addListener( final IZmlEinzelwertModelListener listener )
  {
    m_listeners.add( listener );
  }
}
