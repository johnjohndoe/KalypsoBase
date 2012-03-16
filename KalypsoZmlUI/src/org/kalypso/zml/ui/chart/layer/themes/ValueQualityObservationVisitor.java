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
package org.kalypso.zml.ui.chart.layer.themes;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.core.table.model.references.ZmlValues;

/**
 * @author Dirk Kuch
 */
public class ValueQualityObservationVisitor implements IObservationVisitor
{
  Set<DateRange> m_fehlwerte = new TreeSet<>();

  Set<DateRange> m_stuetzstellen = new TreeSet<>();

  private Period m_timestep;

  @Override
  public void visit( final IObservationValueContainer container ) throws SensorException
  {
    final DataSourceHandler handler = new DataSourceHandler( container.getMetaData() );

    final Period timestep = getTimestep( container );
    if( Objects.isNull( timestep ) )
      return;

    final long timestepMs = timestep.toStandardSeconds().getSeconds() * 1000;

    final IAxis[] axes = container.getAxes();
    final IAxis[] valueAxes = AxisUtils.findValueAxes( axes, true );
    for( final IAxis valueAxis : valueAxes )
    {
      final IAxis statusAxis = AxisUtils.findStatusAxis( axes, valueAxis );
      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( axes, valueAxis );

      final Number status = (Number) container.get( statusAxis );
      final Number dataSourceIndex = (Number) container.get( dataSourceAxis );
      final String dataSourceIdentifier = handler.getDataSourceIdentifier( dataSourceIndex.intValue() );

      final IAxis dateAxis = AxisUtils.findDateAxis( axes );
      final Date to = (Date) container.get( dateAxis );
      final Date from = new Date( to.getTime() - timestepMs );

      final DateRange daterange = new DateRange( from, to );
      if( ZmlValues.isStuetzstelle( status, dataSourceIdentifier ) )
        m_stuetzstellen.add( daterange );
      else
        m_fehlwerte.add( daterange );
    }
  }

  private Period getTimestep( final IObservationValueContainer container )
  {
    if( Objects.isNotNull( m_timestep ) )
      return m_timestep;

    m_timestep = MetadataHelper.getTimestep( container.getMetaData() );

    if( Objects.isNull( m_timestep ) )
    {
      try
      {
        final IAxis dateAxis = AxisUtils.findDateAxis( container.getAxes() );
        final Object currentDate = container.get( dateAxis );
        final Object nextDate = container.getNext( dateAxis );
        if( Objects.allNotNull( currentDate, nextDate ) )
        {
          final Date d1 = (Date) currentDate;
          final Date d2 = (Date) nextDate;

          m_timestep = Period.millis( Long.valueOf( Math.abs( d1.getTime() - d2.getTime() ) ).intValue() );
        }
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }

    }

    return m_timestep;
  }

  public DateRange[] getFehlwerte( )
  {
    return m_fehlwerte.toArray( new DateRange[] {} );
  }

  public DateRange[] getStuetzstellen( )
  {
    return m_stuetzstellen.toArray( new DateRange[] {} );
  }

}
