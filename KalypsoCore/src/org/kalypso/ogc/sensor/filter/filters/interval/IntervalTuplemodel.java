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

import java.util.Arrays;
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
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilter.MODE;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author doemming
 */
public class IntervalTuplemodel extends AbstractTupleModel
{
  protected enum TODO
  {
    eNothing,
    eGoToNextTarget,
    eGoToNextSource;
  }

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

  public IntervalTuplemodel( final MODE mode, final IntervalCalendar calendar, final MetadataList metadata, final ITupleModel srcModel, final Date from, final Date to, final double defaultValue, final int defaultStatus ) throws SensorException
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

  protected class LocalCalculationStack
  {
    public LocalCalculationStack( final IAxis[] valueAxes, final IAxis[] statusAxes )
    {
      defaultValues = new double[valueAxes.length];
      defaultStatus = new int[statusAxes.length];

      Arrays.fill( defaultValues, m_defaultValue );
      Arrays.fill( defaultStatus, m_defaultStatus );
    }

    public Calendar lastTargetCalendar = null;

    public Calendar lastSrcCalendar = null;

    public Interval targetIntervall = null;

    public int targetRow = 0;

    public Interval srcIntervall = null;

    public int srcRow = 0;

    public final double[] defaultValues;

    public final int[] defaultStatus;
  }

  private ITupleModel doInitModell( ) throws SensorException
  {
    // create empty model
    final IAxis[] axisList = getAxisList();
    final CalendarIterator iterator = new CalendarIterator( m_from, m_to, m_calendar.getCalendarField(), m_calendar.getAmount() );
    final int stepCount = iterator.size();

    Assert.isTrue( stepCount > 0, String.format( "Empty intervall tuple model. Check from (%s)/to(%s).", m_from, m_to ) );

    final int rows = stepCount - 1;
    final ITupleModel intervallModel = createTuppleModell( axisList, rows );

    final LocalCalculationStack stack = new LocalCalculationStack( m_axes.getValueAxes(), m_axes.getStatusAxes() );

    // new Values
    final double[] newValues = new double[stack.defaultValues.length];
    Arrays.fill( newValues, 0d );

    final int[] newStatus = new int[stack.defaultStatus.length];
    Arrays.fill( newStatus, KalypsoStati.BIT_OK );

    // initialize target
    stack.lastTargetCalendar = iterator.next(); // TODO hasNext() ?

    // initialize values
    final int srcMaxRows = m_srcModel.getCount();

    final Calendar firstSrcCal = getFirstSrcCalendar( srcMaxRows );

    // initialize source
    stack.lastSrcCalendar = stack.lastTargetCalendar;

    // BUGFIX: handle case when source start before from
    // Before this fix, this lead to a endless loop
    if( firstSrcCal.before( stack.lastSrcCalendar ) )
      stack.lastSrcCalendar = firstSrcCal;

    // fill initial row
    // final Interval initialIntervall = new Interval( m_from, m_from, defaultStatus, defaultValues );
    // updateModelfromintervall( m_intervallModel, targetRow, initialIntervall );
    // targetRow++;
    // doemming: removed last 3 rows to avoid generating beginning "0" value.

    TODO todo = TODO.eNothing;
    while( true )
    {
      // set next source interval
      if( stack.srcIntervall == null || TODO.eGoToNextSource.equals( todo ) )
      {
        // calculate the end of a source interval with given distance
        final Calendar srcCalIntervallEnd = (Calendar) stack.lastSrcCalendar.clone();
        srcCalIntervallEnd.add( m_calendar.getCalendarField(), m_calendar.getAmount() );

        // if we are after the source time series
        if( stack.srcRow >= srcMaxRows )
        {
          // generate defaults
          // create dummy interval
          stack.srcIntervall = new Interval( stack.lastSrcCalendar, srcCalIntervallEnd, stack.defaultStatus, stack.defaultValues );

          stack.lastSrcCalendar = stack.srcIntervall.getEnd();
          // TODO m_to, defaults
          todo = TODO.eNothing;
          continue;
        }

        // read current values from source time series
        final Calendar srcCal = createCalendar( (Date) m_srcModel.getElement( stack.srcRow, m_axes.getDateAxis() ) );
        final Object[] srcStatusValues = ObservationUtilities.getElements( m_srcModel, stack.srcRow, m_axes.getStatusAxes() );
        final Integer[] srcStati = new Integer[srcStatusValues.length];
        for( int i = 0; i < srcStatusValues.length; i++ )
        {
          srcStati[i] = new Integer( ((Number) srcStatusValues[i]).intValue() );
        }

        final Object[] srcValuesObjects = ObservationUtilities.getElements( m_srcModel, stack.srcRow, m_axes.getValueAxes() );
        final Double[] srcValues = new Double[srcValuesObjects.length];
        for( int i = 0; i < srcValuesObjects.length; i++ )
        {
          srcValues[i] = (Double) srcValuesObjects[i];
        }
        stack.srcIntervall = null;

        if( !stack.lastSrcCalendar.after( srcCal ) )
        {
          // we need next source interval

          if( srcCalIntervallEnd.before( firstSrcCal ) )
          {
            // we are before the source time series
            stack.srcIntervall = new Interval( stack.lastSrcCalendar, srcCalIntervallEnd, stack.defaultStatus, stack.defaultValues );
            stack.lastSrcCalendar = srcCalIntervallEnd;
          }
          else
          // we are inside source time series
          {
            switch( m_mode )
            {
              case eIntensity:
                stack.srcIntervall = new Interval( stack.lastSrcCalendar, srcCal, srcStati, srcValues );
                break;
              default:
                // (IntervallFilter.MODE_SUM) as length of first interval is undefined, we ignore first value
                // TODO solve: for which interval is the first value valid ?
                // there is no definition :-(
                // bugfix: we use it nevertheless, as it works OK if intervals are equal;
                // also, always no warning produces problems elsewhere
// if( srcRow > 0 )
                stack.srcIntervall = new Interval( stack.lastSrcCalendar, srcCal, srcStati, srcValues );
                break;
            }
            stack.lastSrcCalendar = srcCal;
            stack.srcRow++;
          }
        }
        todo = TODO.eNothing;
      }
      // next target interval
      if( stack.targetIntervall == null || todo == TODO.eGoToNextTarget )
      {
        if( stack.targetIntervall != null )
        {
          updateModelfromIntervall( intervallModel, stack.targetRow, stack.targetIntervall );
          stack.targetRow++;
        }
        if( !iterator.hasNext() )
        {
          return intervallModel;
        }
        final Calendar cal = iterator.next();
        if( stack.lastTargetCalendar.before( cal ) )
          stack.targetIntervall = new Interval( stack.lastTargetCalendar, cal, newStatus, newValues );
        else
          stack.targetIntervall = null;
        stack.lastTargetCalendar = cal;
        todo = TODO.eNothing;
      }
      // check validity of intervals
      if( stack.srcIntervall == null )
      {
        todo = TODO.eGoToNextSource;
        continue;
      }
      if( stack.targetIntervall == null )
      {
        todo = TODO.eGoToNextTarget;
        continue;
      }
      // compute intersection interval
      final int matrix = stack.srcIntervall.calcIntersectionMatrix( stack.targetIntervall );
      Interval intersection = null;
      if( matrix != Interval.STATUS_INTERSECTION_NONE_BEFORE && matrix != Interval.STATUS_INTERSECTION_NONE_AFTER )
        intersection = stack.srcIntervall.getIntersection( stack.targetIntervall, m_mode );

      switch( matrix )
      {
        case Interval.STATUS_INTERSECTION_NONE_BEFORE:
          todo = TODO.eGoToNextTarget;
          break;
        case Interval.STATUS_INTERSECTION_NONE_AFTER:
          todo = TODO.eGoToNextSource;
          break;
        case Interval.STATUS_INTERSECTION_START:
        case Interval.STATUS_INTERSECTION_INSIDE:
          stack.targetIntervall.merge( intersection, m_mode );
          todo = TODO.eGoToNextTarget;
          break;
        case Interval.STATUS_INTERSECTION_END:
        case Interval.STATUS_INTERSECTION_ARROUND:
          stack.targetIntervall.merge( intersection, m_mode );
          todo = TODO.eGoToNextSource;
          break;
        default:
          break;
      }
    }
  }

  private Calendar getFirstSrcCalendar( final int srcMaxRows ) throws SensorException
  {
    // check if source time series is empty
    if( srcMaxRows != 0 ) // not empty
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
  private void updateModelfromIntervall( final ITupleModel model, final int targetRow, final Interval targetIntervall ) throws SensorException
  {
    final Calendar cal = targetIntervall.getEnd();
    final int[] status = targetIntervall.getStatus();
    final double[] value = targetIntervall.getValue();
    model.setElement( targetRow, cal.getTime(), m_axes.getDateAxis() );

    final IAxis[] statusAxes = m_axes.getStatusAxes();
    final IAxis[] valueAxes = m_axes.getValueAxes();

    for( int i = 0; i < statusAxes.length; i++ )
    {
      model.setElement( targetRow, new Integer( status[i] ), statusAxes[i] );
    }

    for( int i = 0; i < valueAxes.length; i++ )
    {
      model.setElement( targetRow, new Double( value[i] ), valueAxes[i] );
    }

    // FIXME original value or adjusted value?!?
    final IAxis dataSourcesAxes = m_axes.getDataSourcesAxes();
    if( dataSourcesAxes != null )
    {
      final Integer index = getDataSourceIndex();
      model.setElement( targetRow, index, dataSourcesAxes );
    }

  }

  private Integer getDataSourceIndex( )
  {
    final DataSourceHandler handler = new DataSourceHandler( m_metadata );
    return handler.addDataSource( IntervalFilter.class.getName(), IntervalFilter.class.getName() );
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
    throw new UnsupportedOperationException( getClass().getName() + Messages.getString( "org.kalypso.ogc.sensor.filter.filters.IntervallTupplemodel.0" ) ); //$NON-NLS-1$
    // TODO support it
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