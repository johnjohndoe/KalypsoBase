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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.kalypso.commons.pair.IKeyValue;
import org.kalypso.commons.pair.KeyValueFactory;
import org.kalypso.commons.time.PeriodUtils;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author Gernot Belger
 */
public class IntervalValuesOperation
{
  final SimpleTupleModel m_model;

  private final IntervalDefinition m_definition;

  private final IntervalAxesValues m_axes;

  private final ITupleModel m_sourceModel;

  private final Period m_sourceTimestep;

  /**
   * @param sourceTimestep
   *          Timestep of the source values.
   * @param targetSourcesHandler
   *          Data handler for source axis for target metadata.
   */
  public IntervalValuesOperation( final ITupleModel sourceModel, final Period sourceTimestep, final DataSourceHandler targetSourcesHandler, final IntervalDefinition definition )
  {
    m_sourceModel = sourceModel;
    m_sourceTimestep = sourceTimestep;
    m_definition = definition;

    final IAxis[] axes = sourceModel.getAxes();

    m_model = new SimpleTupleModel( axes );

    m_axes = new IntervalAxesValues( targetSourcesHandler, axes, definition.getDefaultValue(), definition.getDefaultStatus() );
  }

  public ITupleModel getModel( )
  {
    return m_model;
  }

  public void execute( final DateRange range ) throws SensorException
  {
    final IntervalIndex<IntervalData> index = buildSourceIndex();

    buildValues( index, range );
  }

  private IntervalIndex<IntervalData> buildSourceIndex( ) throws SensorException
  {
    final IntervalIndex<IntervalData> index = new IntervalIndex<IntervalData>( IntervalData.class );

    final IAxis[] axes = m_sourceModel.getAxes();
    final IAxis dateAxis = AxisUtils.findDateAxis( axes );

    final Period step = findStep();

    for( int i = 0; i < m_sourceModel.size(); i++ )
    {
      final Date date = (Date) m_sourceModel.get( i, dateAxis );

      final DateTime to = new DateTime( date );
      final DateTime from = to.minus( step );
      final Interval sourceInterval = new Interval( from, to );

      final IntervalData sourceData = m_axes.asIntervalData( sourceInterval, m_sourceModel, i );
      index.insert( sourceData );
    }

    return index;
  }

  private Period findStep( ) throws SensorException
  {
    if( m_sourceTimestep != null )
      return m_sourceTimestep;

    /* Return null for empty model (probably created by a request) -> does not matter anyways */
    if( m_sourceModel.size() == 0 )
      return null;

    // TODO: handle better?
    final String message = String.format( "Quellzeitreihe muss Metadatum '%s' definieren.", ITimeseriesConstants.MD_TIMESTEP );
    throw new SensorException( message );
  }

  private void buildValues( final IntervalIndex<IntervalData> index, final DateRange range ) throws SensorException
  {
    final int amount = m_definition.getAmount();
    final int calendarField = m_definition.getCalendarField();

    final IntervalIterator targetIterator = createTargetIterator( range, calendarField, amount );

    for( final Interval targetInterval : targetIterator )
    {
      final IntervalData targetData = m_axes.createDefaultData( targetInterval );

      final IKeyValue<IntervalData, IntervalData>[] matchingSourceIntervals = findSourceIntervals( index, targetInterval );

      final IntervalData mergedTargetData = merge( targetData, matchingSourceIntervals );

      final Object[] values = m_axes.asValueTuple( mergedTargetData, m_model );
      m_model.addTuple( values );
    }
  }

  private IKeyValue<IntervalData, IntervalData>[] findSourceIntervals( final IntervalIndex<IntervalData> index, final Interval targetInterval )
  {
    final IntervalData[] items = index.query( targetInterval );
    return findOverlappingSourceIntervals( targetInterval, items );
  }

  @SuppressWarnings("unchecked")
  private IKeyValue<IntervalData, IntervalData>[] findOverlappingSourceIntervals( final Interval targetInterval, final IntervalData[] sourceIntervals )
  {
    final Collection<IKeyValue<IntervalData, IntervalData>> intervals = new ArrayList<IKeyValue<IntervalData, IntervalData>>();

    for( final IntervalData sourceData : sourceIntervals )
    {
      final IntervalData overlappingPart = overlapSourcePart( targetInterval, sourceData );
      if( overlappingPart != null )
        intervals.add( KeyValueFactory.createPairEqualsBoth( sourceData, overlappingPart ) );
    }

    return intervals.toArray( new IKeyValue[intervals.size()] );
  }

  private IntervalData overlapSourcePart( final Interval targetInterval, final IntervalData sourceData )
  {
    final Interval sourceInterval = sourceData.getInterval();
    final long sourceDuration = sourceInterval.toDurationMillis();
    final Interval sourcePart = targetInterval.overlap( sourceInterval );
    if( sourcePart == null )
      return null;

    final double sourcePartDuration = sourcePart.toDurationMillis();

    /*
     * The partial source interval gets only a part of the original value, depending on how much it covers the original
     * source interval
     */
    final double factor = sourcePartDuration / sourceDuration;
    final TupleModelDataSet[] clonedValues = TupleModelDataSet.clone( sourceData.getDataSets() );

    for( final TupleModelDataSet clone : clonedValues )
    {
      clone.setValue( ((Number) clone.getValue()).doubleValue() * factor );
    }

    return new IntervalData( sourcePart, clonedValues );
  }

  private IntervalData merge( final IntervalData targetData, final IKeyValue<IntervalData, IntervalData>[] matchingSourceIntervals )
  {
    final Interval targetInterval = targetData.getInterval();
    final double targetDuration = targetInterval.toDurationMillis();

    /* Can this happen? */
    // TODO: check i needed
    if( targetDuration == 0 )
      return targetData;

    /* No match: return default target */
    if( matchingSourceIntervals.length == 0 )
      return targetData;

    /* Special case: if we have only one exact match, return it to keep the original source */
    if( matchingSourceIntervals.length == 1 )
    {
      final IntervalData sourceData = matchingSourceIntervals[0].getKey();
      if( sourceData.getInterval().equals( targetInterval ) )
        return sourceData;
    }

    /* Really merge: start with plain data (containing 0.0 values) */
    IntervalData mergedData = m_axes.createPlainData( targetInterval );
    for( final IKeyValue<IntervalData, IntervalData> sourcePair : matchingSourceIntervals )
    {
      final IntervalData sourcePart = sourcePair.getValue();
      mergedData = mergedData.plus( sourcePart );
    }

    return mergedData;
  }

  private IntervalIterator createTargetIterator( final DateRange range, final int calendarField, final int amount )
  {
    final DateRange adjustedRange = adjustRange( range );

    final DateTime fromTime = new DateTime( adjustedRange.getFrom() );
    final DateTime toTime = new DateTime( adjustedRange.getTo() );

    final Period step = PeriodUtils.getPeriod( calendarField, amount );

    return new IntervalIterator( fromTime, toTime, step );
  }

  private DateRange adjustRange( final DateRange range )
  {
    final Date from = range.getFrom();
    final Calendar start = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
    start.setTime( from );

    m_definition.adjustStart( start );
    final Date adjustedFrom = start.getTime();

    return new DateRange( adjustedFrom, range.getTo() );
  }
}
