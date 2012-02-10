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
package org.kalypso.zml.ui.chart.layer.filters;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.zml.core.table.model.references.ZmlValues;

/**
 * TODO multiple value axes!
 * 
 * @author Gernot Belger
 */
public class ContainerAsValue
{
  private final IObservationValueContainer m_container;

  public ContainerAsValue( final IObservationValueContainer container )
  {
    m_container = container;
  }

  public Number getStatus( ) throws SensorException
  {
    final IAxis valueAxis = AxisUtils.findValueAxis( m_container.getAxes() );

    final IAxis axis = AxisUtils.findStatusAxis( m_container.getAxes(), valueAxis );
    if( Objects.isNull( axis ) )
      return null;

    return (Number) m_container.get( axis );
  }

  public String getDataSource( ) throws SensorException
  {
    final IAxis valueAxis = AxisUtils.findValueAxis( m_container.getAxes() );

    final IAxis axis = AxisUtils.findDataSourceAxis( m_container.getAxes(), valueAxis );
    if( Objects.isNull( axis ) )
      return null;

    final Object object = m_container.get( axis );
    if( !(object instanceof Number) )
      return null;

    final Number source = (Number) object;
    final DataSourceHandler handler = new DataSourceHandler( m_container.getMetaData() );
    return handler.getDataSourceIdentifier( source.intValue() );
  }

  public Number getValue( ) throws SensorException
  {
    final IAxis valueAxis = AxisUtils.findValueAxis( m_container.getAxes() );

    final Object value = m_container.get( valueAxis );
    if( !(value instanceof Number) )
      return null;
    return (Number) value;
  }

  public boolean isStuetzstelle( ) throws SensorException
  {
    final String dataSource = getDataSource();
    final Number status = getStatus();

    return ZmlValues.isStuetzstelle( status, dataSource );
  }

  public boolean isNullstelle( ) throws SensorException
  {
    final String dataSource = getDataSource();
    final Number status = getStatus();
    final Number value = getValue();

    return ZmlValues.isNullstelle( value, status, dataSource );
  }
}