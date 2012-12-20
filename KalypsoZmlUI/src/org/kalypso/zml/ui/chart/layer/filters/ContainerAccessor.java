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
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.zml.core.table.model.references.ZmlValues;

/**
 * TODO multiple value axes!
 *
 * @author Gernot Belger
 */
class ContainerAccessor
{
  private final IAxis m_valueAxis;

  private final IAxis m_statusAxis;

  private final IAxis m_sourceAxis;

  private final DataSourceHandler m_sourceHandler;

  public ContainerAccessor( final MetadataList metadata, final IAxis[] axes )
  {
    m_valueAxis = AxisUtils.findValueAxis( axes );
    m_statusAxis = AxisUtils.findStatusAxis( axes, m_valueAxis );
    m_sourceAxis = AxisUtils.findDataSourceAxis( axes, m_valueAxis );
    m_sourceHandler = new DataSourceHandler( metadata );
  }

  private Number getStatus( final IObservationValueContainer container ) throws SensorException
  {
    if( Objects.isNull( m_statusAxis ) )
      return null;

    return (Number) container.get( m_statusAxis );
  }

  private String getDataSource( final IObservationValueContainer container ) throws SensorException
  {
    if( Objects.isNull( m_sourceAxis ) )
      return null;

    final Object object = container.get( m_sourceAxis );
    if( !(object instanceof Number) )
      return null;

    final Number source = (Number) object;
    return m_sourceHandler.getDataSourceIdentifier( source.intValue() );
  }

  private Number getValue( final IObservationValueContainer container ) throws SensorException
  {
    final Object value = container.get( m_valueAxis );
    if( !(value instanceof Number) )
      return null;
    return (Number) value;
  }

  public boolean isStuetzstelle( final IObservationValueContainer container ) throws SensorException
  {
    final String dataSource = getDataSource( container );
    final Number status = getStatus( container );

    return ZmlValues.isStuetzstelle( status, dataSource );
  }

  public boolean isNullstelle( final IObservationValueContainer container ) throws SensorException
  {
    final String dataSource = getDataSource( container );
    final Number status = getStatus( container );
    final Number value = getValue( container );

    return ZmlValues.isNullstelle( value, status, dataSource );
  }
}