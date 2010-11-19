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
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;
import org.kalypso.zml.ui.table.IZmlTableColumn;
import org.kalypso.zml.ui.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlValueReference
{
  private final IZmlTableColumn m_column;

  private final ITupleModel m_model;

  private final int m_position;

  private final DataColumnType m_type;

  private final IObservation m_observation;

  // FIXME clean up - not all values needed!
  /**
   * @param position
   *          position of value in model
   */
  public ZmlValueReference( final IZmlTableColumn column, final IObservation observation, final ITupleModel model, final int position, final DataColumnType type )
  {
    m_column = column;
    m_observation = observation;
    m_model = model;
    m_position = position;
    m_type = type;
  }

  public String getId( )
  {
    return m_column.getId();
  }

  public Object getValue( ) throws SensorException
  {
    return m_model.get( m_position, getAxis() );
  }

  public IAxis getAxis( )
  {
    final IAxis axis = AxisUtils.findAxis( m_model.getAxisList(), m_type.getValueAxis() );

    return axis;
  }

  public void update( final Object value ) throws SensorException
  {
    final IAxis[] axes = m_model.getAxisList();

    for( final IAxis axis : axes )
    {
      if( AxisUtils.isDataSrcAxis( axis ) )
      {
        final DataSourceHandler handler = new DataSourceHandler( m_observation.getMetadataList() );
        final int source = handler.addDataSource( IDataSourceItem.SOURCE_MANUAL_CHANGED, IDataSourceItem.SOURCE_MANUAL_CHANGED );

        m_model.set( m_position, axis, source );
      }
      else if( AxisUtils.isStatusAxis( axis ) )
      {
        m_model.set( m_position, axis, KalypsoStati.BIT_USER_MODIFIED );
      }
      else if( isTargetAxis( axis ) )
      {
        m_model.set( m_position, axis, value );
      }
    }
  }

  private boolean isTargetAxis( final IAxis axis )
  {
    return axis.getType().equals( m_type.getValueAxis() );
  }

}
