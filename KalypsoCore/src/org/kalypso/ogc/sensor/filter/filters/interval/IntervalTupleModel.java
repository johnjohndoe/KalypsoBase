/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.filter.filters.interval;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.java.util.CalendarIterator;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalCalculationStack.PROCESSING_INSTRUCTION;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilter.MODE;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author doemming
 * @author Dirk Kuch
 */
public class IntervalTupleModel extends AbstractTupleModel
{

  private final ITupleModel m_srcModel;

  private final MODE m_mode;

  private final Calendar m_from;

  private final Calendar m_to;

  private final ITupleModel m_intervallModel;

  protected final double m_defaultValue;

  protected final int m_defaultStatus;

  private final IntervalCalendar m_calendar;

  private final IntervalModelAxes m_axes;

  private final MetadataList m_metadata;

  public IntervalTupleModel( final MODE mode, final IntervalCalendar calendar, final MetadataList metadata, final ITupleModel srcModel, final Date from, final Date to, final double defaultValue, final int defaultStatus ) throws SensorException
  {
    super( srcModel.getAxisList() );

    m_mode = mode;
    m_calendar = calendar;
    m_metadata = metadata;
    m_srcModel = srcModel;
    m_defaultValue = defaultValue;
    m_defaultStatus = defaultStatus;

    m_axes = new IntervalModelAxes( getAxisList() );

    final IAxisRange sourceModelRange = getSourceModelRange( srcModel, m_axes.getDateAxis() );

    m_from = initFrom( from, sourceModelRange );
    m_to = initTo( to, sourceModelRange );

    m_intervallModel = initModell( getAxisList() );
  }

  private Calendar initFrom( final Date from, final IAxisRange sourceModelRange )
  {
    if( from != null )
      return createCalendar( from );

    if( sourceModelRange != null )
      return createCalendar( (Date) sourceModelRange.getLower() );

    return null;
  }

  private Calendar initTo( final Date to, final IAxisRange sourceModelRange )
  {
    if( to != null )
      return createCalendar( to );

    if( sourceModelRange != null )
      return createCalendar( (Date) sourceModelRange.getUpper() );

    return null;
  }

  private static IAxisRange getSourceModelRange( final ITupleModel srcModel, final IAxis dateAxis ) throws SensorException
  {
    return srcModel.getRangeFor( dateAxis );
  }

  private ITupleModel initModell( final IAxis[] axisList ) throws SensorException
  {
    if( m_from == null || m_to == null )
      return createTuppleModell( axisList, 0 );

    // correct from
    final String startCalendarField = m_calendar.getStartCalendarField();
    if( startCalendarField != null && !startCalendarField.trim().isEmpty() )
      m_from.set( CalendarUtilities.getCalendarField( startCalendarField ), m_calendar.getStartCalendarValue() );

    return doInitModell();
  }

  private ITupleModel doInitModell( ) throws SensorException
  {
    // create empty model
    final IAxis[] axisList = getAxisList();
    final CalendarIterator iterator = new CalendarIterator( m_from, m_to, m_calendar.getCalendarField(), m_calendar.getAmount() );
    final int stepCount = iterator.size();

    Assert.isTrue( stepCount > 0, String.format( "Empty intervall tuple model. Check from (%s)/to(%s).", m_from, m_to ) );

    final int rows = stepCount - 1;
    final ITupleModel intervalModel = createTuppleModell( axisList, rows );

    final IntervalCalculationStack stack = new IntervalCalculationStack( m_axes.getValueAxes(), m_axes.getStatusAxes(), m_defaultValue, m_defaultStatus );

    // initialize target
    stack.lastTargetCalendar = iterator.next(); // TODO hasNext() ?

    // initialize values
    final int srcMaxRows = m_srcModel.getCount();

    // initialize source
    stack.lastSrcCalendar = stack.lastTargetCalendar;

    // BUGFIX: handle case when source start before from
    // Before this fix, this lead to a endless loop
    final Calendar firstSrcCal = getFirstSrcCalendar();
    if( firstSrcCal.before( stack.lastSrcCalendar ) )
      stack.lastSrcCalendar = firstSrcCal;

    // fill initial row
    // final Interval initialIntervall = new Interval( m_from, m_from, defaultStatus, defaultValues );
    // updateModelfromintervall( m_intervallModel, targetRow, initialIntervall );
    // targetRow++;
    // doemming: removed last 3 rows to avoid generating beginning "0" value.

    PROCESSING_INSTRUCTION instruction = PROCESSING_INSTRUCTION.eNothing;
    while( !instruction.isFinished() )
    {
      /* set next source interval */
      if( stack.srcInterval == null || instruction.isGoToNextSource() )
      {
        instruction = setNextSourceInterval( stack, srcMaxRows );
        continue;
      }
      /* next target interval */
      else if( stack.targetInterval == null || instruction.isGoToNextTarget() )
      {
        instruction = setNextTargetInterval( intervalModel, stack, iterator );
        continue;
      }

      // compute intersection interval
      final int matrix = stack.srcInterval.calcIntersectionMatrix( stack.targetInterval );
      Interval intersection = null;
      if( matrix != Interval.STATUS_INTERSECTION_NONE_BEFORE && matrix != Interval.STATUS_INTERSECTION_NONE_AFTER )
        intersection = stack.srcInterval.getIntersection( stack.targetInterval, m_mode );

      switch( matrix )
      {
        case Interval.STATUS_INTERSECTION_NONE_BEFORE:
          instruction = PROCESSING_INSTRUCTION.eGoToNextTarget;
          break;

        case Interval.STATUS_INTERSECTION_NONE_AFTER:
          instruction = PROCESSING_INSTRUCTION.eGoToNextSource;
          break;

        case Interval.STATUS_INTERSECTION_START:
        case Interval.STATUS_INTERSECTION_INSIDE:
          stack.targetInterval.merge( intersection, m_mode );
          instruction = PROCESSING_INSTRUCTION.eGoToNextTarget;
          break;

        case Interval.STATUS_INTERSECTION_END:
        case Interval.STATUS_INTERSECTION_ARROUND:
          stack.targetInterval.merge( intersection, m_mode );
          instruction = PROCESSING_INSTRUCTION.eGoToNextSource;
          break;

        default:
          break;
      }
    }

    return intervalModel;
  }

  private PROCESSING_INSTRUCTION setNextTargetInterval( final ITupleModel intervalModel, final IntervalCalculationStack stack, final CalendarIterator iterator ) throws SensorException
  {
    if( stack.targetInterval != null )
    {
      updateModelfromIntervall( intervalModel, stack.targetRow, stack.targetInterval );
      stack.targetRow++;
    }
    if( !iterator.hasNext() )
    {
      return PROCESSING_INSTRUCTION.eFinished;
    }

    final Calendar cal = iterator.next();
    if( stack.lastTargetCalendar.before( cal ) )
      stack.targetInterval = new Interval( stack.lastTargetCalendar, cal, stack.getPlainValues(), stack.getPlainStatis(), stack.getPlainSources() );
    else
      stack.targetInterval = null;

    stack.lastTargetCalendar = cal;

    return PROCESSING_INSTRUCTION.eNothing;
  }

  private PROCESSING_INSTRUCTION setNextSourceInterval( final IntervalCalculationStack stack, final int srcMaxRows ) throws SensorException
  {
    final Calendar firstSrcCal = getFirstSrcCalendar();

    // calculate the end of a source interval with given distance
    final Calendar srcCalIntervallEnd = (Calendar) stack.lastSrcCalendar.clone();

    // FIXME: no! use real source values instead!
    srcCalIntervallEnd.add( m_calendar.getCalendarField(), m_calendar.getAmount() );

    // if we are after the source time series
    if( stack.srcRow >= srcMaxRows )
    {
      // generate defaults
      // create dummy interval
      // FIXME: strange: empty interval?!
      stack.srcInterval = new Interval( stack.lastSrcCalendar, srcCalIntervallEnd, stack.defaultValues, stack.defaultStatis, stack.getPlainSources() );

      stack.lastSrcCalendar = (Calendar) srcCalIntervallEnd.clone();
      // TODO m_to, defaults
      return PROCESSING_INSTRUCTION.eNothing;
    }

    final Calendar srcCal = createCalendar( (Date) m_srcModel.getElement( stack.srcRow, m_axes.getDateAxis() ) );

    stack.srcInterval = null;

    if( stack.lastSrcCalendar.after( srcCal ) )
      return PROCESSING_INSTRUCTION.eNothing;

    /* we need next source interval */
    if( srcCalIntervallEnd.before( firstSrcCal ) )
    {
      // we are before the source time series
      stack.srcInterval = new Interval( stack.lastSrcCalendar, srcCalIntervallEnd, stack.defaultValues, stack.defaultStatis, stack.getPlainSources() );
      stack.lastSrcCalendar = srcCalIntervallEnd;
    }
    /* we are inside source time series */
    else
    {
      final Double[] srcValues = getSourceValues( stack.srcRow );
      final String[] dataSources = getDataSources( stack.srcRow );
      // read current values from source time series
      final Object[] srcStatusValues = ObservationUtilities.getElements( m_srcModel, stack.srcRow, m_axes.getStatusAxes() );
      final Integer[] srcStati = new Integer[srcStatusValues.length];
      for( int i = 0; i < srcStatusValues.length; i++ )
      {
        srcStati[i] = Integer.valueOf( ((Number) srcStatusValues[i]).intValue() );
      }

      stack.srcInterval = new Interval( stack.lastSrcCalendar, srcCal, srcValues, srcStati, dataSources );
      stack.lastSrcCalendar = srcCal;
      stack.srcRow++;
    }

    return PROCESSING_INSTRUCTION.eNothing;
  }

  private String[] getDataSources( final int index ) throws SensorException
  {
    final DataSourceHandler handler = new DataSourceHandler( m_metadata );

    final Object[] srcDataSourceObjects = ObservationUtilities.getElements( m_srcModel, index, m_axes.getDataSourcesAxes() );
    final String[] srcDataSources = new String[srcDataSourceObjects.length];

    for( int i = 0; i < srcDataSourceObjects.length; i++ )
    {
      final Number srcIndex = (Number) srcDataSourceObjects[i];
      final String dataSourceIdentifier = handler.getDataSourceIdentifier( srcIndex.intValue() );

      srcDataSources[i] = dataSourceIdentifier;
    }

    return srcDataSources;
  }

  private Double[] getSourceValues( final int index ) throws SensorException
  {
    final Object[] srcValuesObjects = ObservationUtilities.getElements( m_srcModel, index, m_axes.getValueAxes() );

    final Double[] srcValues = new Double[srcValuesObjects.length];
    for( int i = 0; i < srcValuesObjects.length; i++ )
    {
      srcValues[i] = (Double) srcValuesObjects[i];
    }

    return srcValues;
  }

  private Calendar getFirstSrcCalendar( ) throws SensorException
  {
    // check if source time series is empty
    if( m_srcModel.getCount() != 0 ) // not empty
      return createCalendar( (Date) m_srcModel.getElement( 0, m_axes.getDateAxis() ) );
    else
      // if empty, we pretend that it begins at requested range
      return m_from;
  }

  private SimpleTupleModel createTuppleModell( final IAxis[] axisList, final int rows )
  {
    return new SimpleTupleModel( axisList, new Object[rows][axisList.length] );
  }

  // accept values for result
  private void updateModelfromIntervall( final ITupleModel model, final int targetRow, final Interval targetInterval ) throws SensorException
  {
    final Calendar cal = targetInterval.getEnd();
    final int[] status = targetInterval.getStatus();
    final double[] value = targetInterval.getValue();
    final String[] sources = targetInterval.getSources();

    model.setElement( targetRow, cal.getTime(), m_axes.getDateAxis() );

    final IAxis[] statusAxes = m_axes.getStatusAxes();
    final IAxis[] valueAxes = m_axes.getValueAxes();
    final IAxis[] dataSourceAxes = m_axes.getDataSourcesAxes();

    for( int i = 0; i < statusAxes.length; i++ )
    {
      model.setElement( targetRow, Integer.valueOf( status[i] ), statusAxes[i] );
    }

    for( int i = 0; i < valueAxes.length; i++ )
    {
      model.setElement( targetRow, new Double( value[i] ), valueAxes[i] );
    }

    final DataSourceHandler handler = new DataSourceHandler( m_metadata );
    for( int i = 0; i < dataSourceAxes.length; i++ )
    {
      final int dataSource = handler.addDataSource( sources[i], String.format( "filter://%s", IntervalFilter.class.getName() ) );
      model.setElement( targetRow, Integer.valueOf( dataSource ), dataSourceAxes[i] );
    }
  }

  private static Calendar createCalendar( final Date date )
  {
    final Calendar result = Calendar.getInstance();
    result.setTime( date );
    return result;
  }

  @Override
  public int getCount( ) throws SensorException
  {
    return m_intervallModel.getCount();
  }

  @Override
  public int hashCode( )
  {
    return m_intervallModel.hashCode();
  }

  @Override
  public String toString( )
  {
    return m_intervallModel.toString();
  }

  @Override
  public Object getElement( final int index, final IAxis axis ) throws SensorException
  {
    return m_intervallModel.getElement( index, axis );
  }

  @Override
  public void setElement( final int index, final Object element, final IAxis axis )
  {
    // TODO support it
    throw new UnsupportedOperationException( getClass().getName() + Messages.getString( "org.kalypso.ogc.sensor.filter.filters.IntervallTupplemodel.0" ) ); //$NON-NLS-1$
  }

  @Override
  public int indexOf( final Object element, final IAxis axis ) throws SensorException
  {
    // TODO: better than this test: should test if axis.isKey() is true
    if( element instanceof Date )
      return m_srcModel.indexOf( element, axis );
    throw new UnsupportedOperationException( getClass().getName() + Messages.getString( "org.kalypso.ogc.sensor.filter.filters.IntervallTupplemodel.1" ) //$NON-NLS-1$
        + axis.getName() + Messages.getString( "org.kalypso.ogc.sensor.filter.filters.IntervallTupplemodel.2" ) ); //$NON-NLS-1$
  }
}