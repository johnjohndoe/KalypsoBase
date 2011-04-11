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
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author Gernot Belger
 */
public class IntervalFilterOperation
{
  private final IObservation m_input;

  private final IntervalDefinition m_definition;

  private final IntervalAxesValues m_axes;

  private final IntervalIndex m_sourceIndex = new IntervalIndex();

  private final MetadataList m_metadata;

  public IntervalFilterOperation( final IObservation input, final IntervalDefinition definition )
  {
    m_input = input;
    m_definition = definition;
    m_metadata = MetadataHelper.clone( m_input.getMetadataList() );
    final DataSourceHandler sourceHandler = new DataSourceHandler( m_metadata );

    m_axes = new IntervalAxesValues( sourceHandler, input.getAxes(), definition.getDefaultValue(), definition.getDefaultStatus() );
  }

  public IObservation execute( final DateRange range ) throws SensorException
  {
    final String href = m_input.getHref();
    final String name = m_input.getName();
    final IAxis[] axes = m_input.getAxes();

    buildSourceIndex( range );

    final SimpleTupleModel model = new SimpleTupleModel( axes );
    buildValues( model, range );

    return new SimpleObservation( href, name, m_metadata, model );
  }

  private void buildSourceIndex( final DateRange range ) throws SensorException
  {
    // BUGIFX: fixes the problem with the first value:
    // the first value was always ignored, because the interval
    // filter cannot handle the first value of the source observation
    // FIX: we just make the request a big bigger in order to get a new first value
    // HACK: we always use DAY, so that work fine only up to time series of DAY-quality.
    // Maybe there should be one day a mean to determine, which is the right amount.
    final ITupleModel sourceModel = ObservationUtilities.requestBuffered( m_input, range, Calendar.DAY_OF_MONTH, 2 );

    final IAxis[] axes = sourceModel.getAxes();
    final IAxis dateAxis = AxisUtils.findDateAxis( axes );

    final Period step = MetadataHelper.getTimestep( m_input.getMetadataList() );
    if( step == null )
      throw new SensorException( String.format( "Quellzeitreihe muss Metadatum '%s' definieren.", MetadataHelper.MD_TIMESTEP ) );

    for( int i = 0; i < sourceModel.size(); i++ )
    {
      final Date date = (Date) sourceModel.get( i, dateAxis );

      final DateTime to = new DateTime( date );
      final DateTime from = to.minus( step );
      final Interval sourceInterval = new Interval( from, to );

      final IntervalData sourceData = m_axes.asIntervalData( sourceInterval, sourceModel, i );
      m_sourceIndex.insert( sourceData );
    }
  }

  private void buildValues( final SimpleTupleModel model, final DateRange range ) throws SensorException
  {
    final int amount = m_definition.getAmount();
    final int calendarField = m_definition.getCalendarField();
    /* Directly update metadata with that timestep */
    MetadataHelper.setTimestep( m_metadata, calendarField, amount );

    final IntervalIterator targetIterator = createTargetIterator( range, calendarField, amount );

    for( final Interval targetInterval : targetIterator )
    {
      final IntervalData targetData = m_axes.createDefaultData( targetInterval );

      final IKeyValue<IntervalData, IntervalData>[] matchingSourceIntervals = findSourceIntervals( targetInterval );

      final IntervalData mergedTargetData = merge( targetData, matchingSourceIntervals );

      final Object[] values = m_axes.asValueTuple( mergedTargetData, model );
      model.addTuple( values );
    }
  }

  private IKeyValue<IntervalData, IntervalData>[] findSourceIntervals( final Interval targetInterval )
  {
    final IntervalData[] items = m_sourceIndex.query( targetInterval );
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
    final double[] values = sourceData.getValues();
    final double[] partValues = new double[values.length];
    for( int i = 0; i < partValues.length; i++ )
      partValues[i] = values[i] * factor;

    return new IntervalData( sourcePart, partValues, sourceData.getStati(), sourceData.getSource() );
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

  protected IntervalIterator createTargetIterator( final DateRange range, final int calendarField, final int amount )
  {
    final DateTime fromTime = new DateTime( range.getFrom() );
    final DateTime toTime = new DateTime( range.getTo() );

    final Period step = PeriodUtils.getPeriod( calendarField, amount );

    return new IntervalIterator( fromTime, toTime, step );
  }
}