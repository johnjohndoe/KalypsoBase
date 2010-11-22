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

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;
import org.kalypso.zml.ui.table.IZmlColumnModel;
import org.kalypso.zml.ui.table.IZmlTableColumn;
import org.kalypso.zml.ui.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlTableColumn implements IObservationListener
{
  private final IZmlTableColumn m_column;

  private final ITupleModel m_model;

  private final IObservation m_observation;

  private final DataColumnType m_type;

  private final IZmlColumnModel m_tabelModel;

  public ZmlTableColumn( final IZmlColumnModel tabelModel, final IZmlTableColumn column, final IObservation observation, final ITupleModel model, final DataColumnType type )
  {
    m_tabelModel = tabelModel;
    m_column = column;
    m_observation = observation;
    m_model = model;
    m_type = type;

    observation.addListener( this );
  }

  public void dispose( )
  {
    m_observation.removeListener( this );
  }

  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    return m_model.get( index, axis );
  }

  public String getIdentifier( )
  {
    return m_column.getIdentifier();
  }

  public IAxis getIndexAxis( )
  {
    final String type = m_type.getIndexAxis();
    final IAxis[] axes = m_model.getAxisList();

    return AxisUtils.findAxis( axes, type );
  }

  public String getLabel( )
  {
    return m_column.getTitle( getValueAxis() );
  }

  public IAxis getValueAxis( )
  {
    final String type = m_type.getValueAxis();
    final IAxis[] axes = m_model.getAxisList();

    return AxisUtils.findAxis( axes, type );
  }

  private boolean isTargetAxis( final IAxis axis )
  {
    return axis.getType().equals( m_type.getValueAxis() );
  }

  public int size( ) throws SensorException
  {
    return m_model.size();
  }

  public void update( final int index, final Object value ) throws SensorException
  {
    final IAxis[] axes = m_model.getAxisList();

    for( final IAxis axis : axes )
    {
      if( AxisUtils.isDataSrcAxis( axis ) )
      {
        final DataSourceHandler handler = new DataSourceHandler( m_observation.getMetadataList() );
        final int source = handler.addDataSource( IDataSourceItem.SOURCE_MANUAL_CHANGED, IDataSourceItem.SOURCE_MANUAL_CHANGED );

        m_model.set( index, axis, source );
      }
      else if( AxisUtils.isStatusAxis( axis ) )
      {
        m_model.set( index, axis, KalypsoStati.BIT_USER_MODIFIED );
      }
      else if( isTargetAxis( axis ) )
      {
        m_model.set( index, axis, value );
      }
    }

    // FIXME improve update value handling
    m_observation.setValues( m_model );
    m_observation.fireChangedEvent( this );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationListener#observationChanged(org.kalypso.ogc.sensor.IObservation,
   *      java.lang.Object)
   */
  @Override
  public void observationChanged( final IObservation obs, final Object source )
  {
    m_tabelModel.fireModelChanged();
  }
}
