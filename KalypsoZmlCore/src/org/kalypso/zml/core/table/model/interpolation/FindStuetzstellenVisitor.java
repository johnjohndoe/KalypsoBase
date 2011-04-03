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
package org.kalypso.zml.core.table.model.interpolation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.core.table.model.references.ZmlValues;

/**
 * @author Dirk Kuch
 */
public class FindStuetzstellenVisitor implements IObservationVisitor
{
  private final Set<Integer> m_references = new LinkedHashSet<Integer>();

  private IAxis m_sourceAxis;

  private IAxis m_statusAxis;

  private DataSourceHandler m_dataSourceHandler;

  public FindStuetzstellenVisitor( )
  {

  }

  public Integer[] getStuetzstellen( )
  {
    return m_references.toArray( new Integer[] {} );
  }

  private IAxis getStatusAxis( final IObservationValueContainer container )
  {
    if( Objects.isNull( m_statusAxis ) )
      m_statusAxis = AxisUtils.findStatusAxis( container.getAxes() );

    return m_statusAxis;
  }

  private IAxis getSourceAxis( final IObservationValueContainer container )
  {
    if( Objects.isNull( m_sourceAxis ) )
      m_sourceAxis = AxisUtils.findDataSourceAxis( container.getAxes() );

    return m_sourceAxis;
  }

  private DataSourceHandler getDataSourceHandler( final IObservationValueContainer container )
  {
    if( Objects.isNull( m_dataSourceHandler ) )
      m_dataSourceHandler = new DataSourceHandler( container.getMetaData() );

    return m_dataSourceHandler;
  }

  @Override
  public void visit( final IObservationValueContainer container )
  {
    try
    {

      final IAxis sourceAxis = getSourceAxis( container );
      String source = null;
      if( Objects.isNotNull( sourceAxis ) )
      {
        final Object sourceIndexObject = container.get( sourceAxis );
        final Number sourceIndex = sourceIndexObject instanceof Number ? (Number) sourceIndexObject : -1;
        source = getDataSourceHandler( container ).getDataSourceIdentifier( sourceIndex.intValue() );
      }

      final IAxis statusAxis = getStatusAxis( container );
      Number status = null;
      if( Objects.isNotNull( statusAxis ) )
      {
        final Object statusObject = container.get( statusAxis );
        status = statusObject instanceof Number ? (Number) statusObject : KalypsoStati.BIT_OK;
      }

      if( ZmlValues.isStuetzstelle( status, source ) )
        m_references.add( container.getIndex() );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }
  }

}
