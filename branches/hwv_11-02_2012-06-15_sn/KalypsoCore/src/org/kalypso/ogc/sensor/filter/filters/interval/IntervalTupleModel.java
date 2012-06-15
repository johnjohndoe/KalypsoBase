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
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.java.util.CalendarIterator;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalCalculationStack.PROCESSING_INSTRUCTION;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilter.MODE;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
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

  private final IntervalDefinition m_definition;

  private final IntervalAxesValues m_axes;

  private final MetadataList m_metadata;

  private final DataSourceHandler m_handler;

  public IntervalTupleModel( final MODE mode, final IntervalDefinition definition, final MetadataList metadata, final ITupleModel srcModel, final Date from, final Date to ) throws SensorException
  {
    super( srcModel.getAxes() );

    m_mode = mode;
    m_definition = definition;
    m_metadata = metadata;
    m_handler = new DataSourceHandler( m_metadata );

    m_srcModel = srcModel;

    m_axes = new IntervalAxesValues( m_handler, srcModel.getAxes(), definition.getDefaultValue(), definition.getDefaultStatus() );

    final IAxisRange sourceModelRange = getSourceModelRange( srcModel, m_axes.getDateAxis() );

    m_from = initFrom( from, sourceModelRange );
    m_to = initTo( to, sourceModelRange );

    m_intervallModel = initModell( getAxes() );
  }

  public MetadataList getMetadata( )
  {
    return m_metadata;
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
    return srcModel.getRange( dateAxis );
  }

  private ITupleModel initModell( final IAxis[] axisList ) throws SensorException
  {
    if( m_from == null || m_to == null )
      return createTuppleModell( axisList, 0 );

    m_definition.adjustStart( m_from );

    return doInitModell();
  }

  private ITupleModel doInitModell( ) throws SensorException
  {
    // create empty model
    final IAxis[] axisList = getAxes();

    final int targetStepField = m_definition.getCalendarField();
    final int targetStepAmount = m_definition.getAmount();

    final CalendarIterator iterator = new CalendarIterator( m_from, m_to, targetStepField, targetStepAmount );
    final int stepCount = iterator.size();

    Assert.isTrue( stepCount > 0, String.format( "Empty intervall tuple model. Check from (%s)/to(%s).", m_from, m_to ) );

    final int rows = stepCount - 1;
    final ITupleModel intervalModel = createTuppleModell( axisList, rows );

    final IntervalCalculationStack stack = new IntervalCalculationStack();

    // initialize target
    stack.lastTargetCalendar = iterator.next(); // TODO hasNext() ?

    // initialize values
    final int srcMaxRows = m_srcModel.size();

    // initialize source
    stack.lastSrcCalendar = stack.lastTargetCalendar;

    // BUGFIX: handle case when source starts before from
    // Before this fix, this lead to a endless loop
    final Calendar firstSrcCal = getFirstSrcCalendar();
    if( firstSrcCal.before( stack.lastSrcCalendar ) )
      stack.lastSrcCalendar = firstSrcCal;

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
          stack.targetInterval.merge( stack.srcInterval, intersection, m_mode );
          instruction = PROCESSING_INSTRUCTION.eGoToNextTarget;
          break;

        case Interval.STATUS_INTERSECTION_END:
        case Interval.STATUS_INTERSECTION_ARROUND:
          stack.targetInterval.merge( stack.srcInterval, intersection, m_mode );
          instruction = PROCESSING_INSTRUCTION.eGoToNextSource;
          break;

        default:
          break;
      }
    }

    MetadataHelper.setTimestep( m_metadata, targetStepField, targetStepAmount );

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
      stack.targetInterval = new Interval( stack.lastTargetCalendar, cal, m_axes.getPlainValues() );
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
    // REMARK: not really a problem, because this is only used in the case if we are
    // after the last real source intervall -> interval length is irrelevant in that case
    // FIXME: no! it is used and produces real errors...!
    srcCalIntervallEnd.add( m_definition.getCalendarField(), m_definition.getAmount() );

    // if we are after the source time series
    if( stack.srcRow >= srcMaxRows )
    {
      // generate defaults
      // create dummy interval
      // FIXME: strange: empty interval?!
      stack.srcInterval = new Interval( stack.lastSrcCalendar, srcCalIntervallEnd, m_axes.getDefaultValues() );

      stack.lastSrcCalendar = (Calendar) srcCalIntervallEnd.clone();
      // TODO m_to, defaults
      return PROCESSING_INSTRUCTION.eNothing;
    }

    final Calendar srcCal = createCalendar( (Date) m_srcModel.get( stack.srcRow, m_axes.getDateAxis() ) );

    stack.srcInterval = null;

    if( stack.lastSrcCalendar.after( srcCal ) )
    {
      // FIXME: leads to endless loop if timeseries is not correctly ordered
      // We should at least step srsRow here?!
      return PROCESSING_INSTRUCTION.eNothing;
    }

    /* we need next source interval */
    if( srcCalIntervallEnd.before( firstSrcCal ) )
    {
      // we are before the source time series
      stack.srcInterval = new Interval( stack.lastSrcCalendar, srcCalIntervallEnd, m_axes.getDefaultValues() );
      stack.lastSrcCalendar = srcCalIntervallEnd;
    }
    /* we are inside source time series */
    else
    {
      final TupleModelDataSet[] srcValues = getSourceValues( stack.srcRow );

      stack.srcInterval = new Interval( stack.lastSrcCalendar, srcCal, srcValues );
      stack.lastSrcCalendar = srcCal;
      stack.srcRow++;
    }

    return PROCESSING_INSTRUCTION.eNothing;
  }

  private TupleModelDataSet[] getSourceValues( final int index ) throws SensorException
  {
    final DataSourceHandler handler = new DataSourceHandler( m_metadata );

    final Set<TupleModelDataSet> sourceValues = new LinkedHashSet<TupleModelDataSet>();

    for( final IAxis valueAxis : m_axes.getValueAxes() )
    {
      final IAxis statusAxis = m_axes.getStatusAxis( valueAxis );
      final IAxis dataSourcesAxes = m_axes.getDataSourcesAxes( valueAxis );

      final Number value = (Number) m_srcModel.get( index, valueAxis );
      final Number status = (Number) m_srcModel.get( index, statusAxis );
      final Number dataSourceIndex = (Number) m_srcModel.get( index, dataSourcesAxes );
      final String dataSource = handler.getDataSourceIdentifier( dataSourceIndex.intValue() );

      sourceValues.add( new TupleModelDataSet( valueAxis, value.doubleValue(), status.intValue(), dataSource ) );
    }

    return sourceValues.toArray( new TupleModelDataSet[] {} );
  }

  private Calendar getFirstSrcCalendar( ) throws SensorException
  {
    // check if source time series is empty
    if( m_srcModel.size() != 0 ) // not empty
      return createCalendar( (Date) m_srcModel.get( 0, m_axes.getDateAxis() ) );
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
    final TupleModelDataSet[] targetDataSets = targetInterval.getDataSets();

    final Date time = cal.getTime();
    model.set( targetRow, m_axes.getDateAxis(), time );

    final DataSourceHandler handler = new DataSourceHandler( m_metadata );

    for( final TupleModelDataSet dataSet : targetDataSets )
    {
      final IAxis statusAxis = m_axes.getStatusAxis( dataSet.getValueAxis() );
      final IAxis dataSourceAxis = m_axes.getDataSourcesAxes( dataSet.getValueAxis() );

      model.set( targetRow, dataSet.getValueAxis(), dataSet.getValue() );
      model.set( targetRow, statusAxis, dataSet.getStatus() );

      final String dataSource = dataSet.getSource();
      final int dataSourceIndex = handler.addDataSource( dataSource, dataSource );
      model.set( targetRow, dataSourceAxis, dataSourceIndex );
    }

  }

  private static Calendar createCalendar( final Date date )
  {
    final Calendar result = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
    result.setTime( date );
    return result;
  }

  @Override
  public int size( ) throws SensorException
  {
    return m_intervallModel.size();
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
  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    return m_intervallModel.get( index, axis );
  }

  @Override
  public void set( final int index, final IAxis axis, final Object element )
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