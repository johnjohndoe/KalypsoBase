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
package org.kalypso.ogc.sensor.filter.filters.interval;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.Interval;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.repository.IDataSourceItem;

/**
 * @author Dirk Kuch
 */
public class IntervalAxesValues
{
  private final IAxis[] m_axes;

  private final double[] m_defaultValues;

  private final int[] m_defaultStatis;

  private final IAxis[] m_valueAxes;

  private final IAxis[] m_statusAxes;

  private final double m_defaultValue;

  private final int m_defaultStatus;

  private final IAxis m_sourceAxis;

  private final DataSourceHandler m_sourceHandler;

  private final IAxis m_dateAxis;

  public IntervalAxesValues( final DataSourceHandler sourceHandler, final IAxis[] axes, final double defaultValue, final int defaultStatus )
  {
    m_sourceHandler = sourceHandler;
    m_axes = axes;
    m_defaultValue = defaultValue;
    m_defaultStatus = defaultStatus;
    m_valueAxes = AxisUtils.findValueAxes( m_axes );
    m_statusAxes = AxisUtils.findStatusAxes( m_axes );
    m_sourceAxis = AxisUtils.findDataSourceAxis( axes );
    m_dateAxis = AxisUtils.findDateAxis( axes );

    m_defaultValues = new double[m_valueAxes.length];
    Arrays.fill( m_defaultValues, defaultValue );

    m_defaultStatis = new int[m_statusAxes.length];
    Arrays.fill( m_defaultStatis, defaultStatus );
  }

  public IAxis getDateAxis( )
  {
    return AxisUtils.findDateAxis( m_axes );
  }

  public IAxis[] getDataSourcesAxes( )
  {
    return AxisUtils.findDataSourceAxes( m_axes );
  }

  public int[] getPlainStatis( )
  {
    final int[] statis = new int[m_defaultStatis.length];
    Arrays.fill( statis, KalypsoStati.BIT_OK );
    return statis;
  }

  public double[] getPlainValues( )
  {
    final double[] values = new double[m_defaultValues.length];
    Arrays.fill( values, 0d );

    return values;
  }

  public String[] getPlainSources( )
  {
    final String[] sources = new String[m_defaultValues.length];
    Arrays.fill( sources, IntervalSourceHandler.SOURCE_INITIAL_VALUE );

    return sources;
  }

  public IAxis[] getValueAxes( )
  {
    return m_valueAxes;
  }

  public IAxis[] getStatusAxes( )
  {
    return m_statusAxes;
  }

  public double getDefaultValue( )
  {
    return m_defaultValue;
  }

  public int getDefaultStatus( )
  {
    return m_defaultStatus;
  }

  public double[] getDefaultValues( )
  {
    return m_defaultValues;
  }

  public int[] getDefaultStatis( )
  {
    return m_defaultStatis;
  }

  public IAxis[] getAxes( )
  {
    return m_axes;
  }

  public IntervalData asIntervalData( final Interval interval, final ITupleModel model, final int index ) throws SensorException
  {
    final double[] values = new double[m_valueAxes.length];
    for( int i = 0; i < values.length; i++ )
      values[i] = ((Number) model.get( index, m_valueAxes[i] )).doubleValue();

    final int[] stati = new int[m_statusAxes.length];
    for( int i = 0; i < stati.length; i++ )
      stati[i] = ((Number) model.get( index, m_statusAxes[i] )).intValue();

    final int sourceIndex = ((Number) model.get( index, m_sourceAxis )).intValue();
    final String source = m_sourceHandler.getDataSourceIdentifier( sourceIndex );

    return new IntervalData( interval, values, stati, source );
  }

  public IntervalData createDefaultData( final Interval interval )
  {
    return new IntervalData( interval, getDefaultValues(), getDefaultStatis(), IDataSourceItem.SOURCE_UNKNOWN );
  }

  public IntervalData createPlainData( final Interval interval )
  {
    return new IntervalData( interval, getPlainValues(), getPlainStatis(), IDataSourceItem.SOURCE_UNKNOWN );
  }

  public Object[] asValueTuple( final IntervalData data, final ITupleModel model ) throws SensorException
  {
    final Object[] tuple = new Object[m_axes.length];
    final Interval interval = data.getInterval();
    final Date key = interval.getEnd().toDate();

    final int datePos = model.getPosition( m_dateAxis );
    tuple[datePos] = key;

    final int sourcePos = model.getPosition( m_sourceAxis );
    final String source = data.getSource();
    tuple[sourcePos] = m_sourceHandler.addDataSource( source, source );

    final double[] values = data.getValues();
    for( int i = 0; i < values.length; i++ )
    {
      final int pos = model.getPosition( m_valueAxes[i] );
      tuple[pos] = values[i];
    }

    final int[] stati = data.getStati();
    for( int i = 0; i < stati.length; i++ )
    {
      final int pos = model.getPosition( m_statusAxes[i] );
      tuple[pos] = stati[i];
    }

    return tuple;
  }
}
