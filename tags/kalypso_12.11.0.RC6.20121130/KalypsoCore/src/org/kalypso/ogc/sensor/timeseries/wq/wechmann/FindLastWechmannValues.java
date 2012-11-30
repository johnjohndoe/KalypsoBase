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
package org.kalypso.ogc.sensor.timeseries.wq.wechmann;

import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;

/**
 * @author Monika Thuel
 */
public class FindLastWechmannValues implements IObservationVisitor
{
  private TupleModelDataSet m_eDataSet;

  private TupleModelDataSet m_vDataSet;

  public TupleModelDataSet getWechmannE( )
  {
    return m_eDataSet;
  }

  public TupleModelDataSet getWechmannV( )
  {
    return m_vDataSet;
  }

  @Override
  public void visit( final IObservationValueContainer container ) throws CancelVisitorException, SensorException
  {
    final IAxis eTargetAxis = AxisUtils.findAxis( container.getAxes(), ITimeseriesConstants.TYPE_WECHMANN_E );
    final IAxis vTargetAxis = AxisUtils.findAxis( container.getAxes(), ITimeseriesConstants.TYPE_WECHMANN_SCHALTER_V );
    if( Objects.isNull( eTargetAxis, vTargetAxis ) )
      throw new CancelVisitorException();

    m_eDataSet = findDataSet( container, eTargetAxis );
    m_vDataSet = findDataSet( container, vTargetAxis );

    if( Objects.allNotNull( m_eDataSet, m_vDataSet ) )
      throw new CancelVisitorException();
  }

  private TupleModelDataSet findDataSet( final IObservationValueContainer container, final IAxis axis ) throws SensorException
  {
    final Object value = container.get( axis );
    if( Objects.isNull( value ) )
      return null;

    final IAxis statusAxis = AxisUtils.findStatusAxis( container.getAxes(), axis );
    final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( container.getAxes(), axis );

    Integer status = null;
    if( Objects.isNotNull( statusAxis ) )
    {
      final Number number = (Number) container.get( statusAxis );
      status = number.intValue();
    }

    String source = org.apache.commons.lang3.StringUtils.EMPTY;
    if( Objects.isNotNull( dataSourceAxis ) )
    {
      final Number index = (Number) container.get( dataSourceAxis );
      if( Objects.isNotNull( index ) )
      {
        final DataSourceHandler handler = new DataSourceHandler( container.getMetaData() );
        source = handler.getDataSourceIdentifier( index.intValue() );
      }
    }

    return new TupleModelDataSet( axis, value, status, source );
  }

}
