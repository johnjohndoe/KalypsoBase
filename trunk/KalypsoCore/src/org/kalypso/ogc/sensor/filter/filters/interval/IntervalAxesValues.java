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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.Interval;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author Dirk Kuch
 */
public class IntervalAxesValues
{
  private final IAxis[] m_axes;

  private final IAxis[] m_valueAxes;

  private final double m_defaultValue;

  private final int m_defaultStatus;

  private final DataSourceHandler m_sourceHandler;

  private final IAxis m_dateAxis;

  public IntervalAxesValues( final DataSourceHandler sourceHandler, final IAxis[] axes, final double defaultValue, final int defaultStatus )
  {
    m_sourceHandler = sourceHandler;
    m_axes = axes;
    m_defaultValue = defaultValue;
    m_defaultStatus = defaultStatus;
    m_valueAxes = AxisUtils.findValueAxes( axes );
    m_dateAxis = AxisUtils.findDateAxis( axes );
  }

  public IAxis getDateAxis( )
  {
    return AxisUtils.findDateAxis( m_axes );
  }

  public IAxis getDataSourcesAxes( final IAxis valueAxis )
  {
    return AxisUtils.findDataSourceAxis( m_axes, valueAxis );
  }

  public TupleModelDataSet[] getPlainValues( )
  {
    final Set<TupleModelDataSet> dataSets = new LinkedHashSet<>();
    for( final IAxis valueAxis : m_valueAxes )
    {
      dataSets.add( new TupleModelDataSet( valueAxis, 0d, KalypsoStati.BIT_OK, IntervalSourceHandler.SOURCE_INITIAL_VALUE ) );
    }

    return dataSets.toArray( new TupleModelDataSet[] {} );
  }

  public IAxis[] getValueAxes( )
  {
    return m_valueAxes;
  }

  public IAxis getStatusAxis( final IAxis valueAxis )
  {
    return AxisUtils.findStatusAxis( m_axes, valueAxis );
  }

  public double getDefaultValue( )
  {
    return m_defaultValue;
  }

  public int getDefaultStatus( )
  {
    return m_defaultStatus;
  }

  public TupleModelDataSet[] getDefaultValues( )
  {
    final Set<TupleModelDataSet> dataSets = new LinkedHashSet<>();
    for( final IAxis valueAxis : m_valueAxes )
    {
      dataSets.add( new TupleModelDataSet( valueAxis, m_defaultValue, m_defaultStatus, IntervalSourceHandler.SOURCE_INITIAL_VALUE ) );
    }

    return dataSets.toArray( new TupleModelDataSet[] {} );
  }

  public int[] getDefaultStatis( )
  {
    final int[] defaultStati = new int[ArrayUtils.getLength( AxisUtils.findStatusAxes( m_axes ) )];
    Arrays.fill( defaultStati, m_defaultStatus );

    return defaultStati;
  }

  public IAxis[] getAxes( )
  {
    return m_axes;
  }

  public IntervalData asIntervalData( final Interval interval, final ITupleModel model, final int index ) throws SensorException
  {
    final Set<TupleModelDataSet> dataSets = new LinkedHashSet<>();

    for( final IAxis valueAxis : m_valueAxes )
    {
      final Number value = (Number) model.get( index, valueAxis );
      final Number status = (Number) model.get( index, getStatusAxis( valueAxis ) );
      final Number sourceIndex = (Number) model.get( index, getDataSourcesAxes( valueAxis ) );
      final String source = m_sourceHandler.getDataSourceIdentifier( sourceIndex.intValue() );

      dataSets.add( new TupleModelDataSet( valueAxis, value.doubleValue(), status.intValue(), source ) );
    }

    return new IntervalData( interval, dataSets.toArray( new TupleModelDataSet[] {} ) );
  }

  public IntervalData createDefaultData( final Interval interval )
  {
    final Set<TupleModelDataSet> dataSets = new LinkedHashSet<>();
    for( final IAxis axis : m_valueAxes )
    {
      dataSets.add( new TupleModelDataSet( axis, m_defaultValue, m_defaultStatus, IntervalSourceHandler.SOURCE_INTERVAL_FITLER ) );
    }

    return new IntervalData( interval, dataSets.toArray( new TupleModelDataSet[] {} ) );
  }

  public IntervalData createPlainData( final Interval interval )
  {

    final Set<TupleModelDataSet> dataSets = new LinkedHashSet<>();
    for( final IAxis axis : m_valueAxes )
    {
      dataSets.add( new TupleModelDataSet( axis, 0d, KalypsoStati.BIT_OK, IntervalSourceHandler.SOURCE_INTERVAL_FITLER ) );
    }

    return new IntervalData( interval, dataSets.toArray( new TupleModelDataSet[] {} ) );
  }

  public Object[] asValueTuple( final IntervalData data, final ITupleModel model ) throws SensorException
  {
    final Object[] tuple = new Object[m_axes.length];
    final Interval interval = data.getInterval();
    final Date key = interval.getEnd().toDate();

    final int datePos = model.getPosition( m_dateAxis );
    tuple[datePos] = key;

    final TupleModelDataSet[] dataSets = data.getDataSets();
    for( final TupleModelDataSet dataSet : dataSets )
    {
      final IAxis valueAxis = dataSet.getValueAxis();
      final int valuePosition = model.getPosition( valueAxis );
      tuple[valuePosition] = dataSet.getValue();

      final IAxis statusAxis = getStatusAxis( valueAxis );
      setModelValue( statusAxis, model, tuple, dataSet.getStatus() );

      final IAxis dataSourcesAxes = getDataSourcesAxes( valueAxis );
      final int dataSourceIndex = m_sourceHandler.addDataSource( dataSet.getSource(), dataSet.getSource() );
      setModelValue( dataSourcesAxes, model, tuple, dataSourceIndex );
    }

    return tuple;
  }

  private void setModelValue( final IAxis axis, final ITupleModel model, final Object[] tuple, final Object value ) throws SensorException
  {
    if( Objects.isNull( axis ) )
      return;

    final int position = model.getPosition( axis );
    if( position < 0 )
      return;

    tuple[position] = value;
  }
}
