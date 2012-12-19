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
package org.kalypso.ogc.sensor.timeseries.interpolation.worker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author Dirk Kuch
 */
public class ValueInterpolationWorker extends AbstractInterpolationWorker
{
  private final TimeZone m_timeZone;

  public ValueInterpolationWorker( final IInterpolationFilter filter, final ITupleModel values, final DateRange dateRange )
  {
    super( filter, values, dateRange );

    m_timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<>();

    try
    {
      final DateRange dateRange = getDateRange();
      final IAxis[] valueAxes = getValueAxes();

      final Calendar calendar = Calendar.getInstance( m_timeZone );

      final LocalCalculationStack stack = new LocalCalculationStack();
      for( final IAxis valueAxis : valueAxes )
      {
        final LocalCalculationStackValue value = new LocalCalculationStackValue( valueAxis );
        stack.add( value );
      }

      // do we need to fill before the beginning of the base model?
      setStartValues( stack, calendar );

      for( int index = 0; index < getBaseModel().size(); index++ )
      {
        setInterpolationValues( stack, calendar, index );

        stack.setDate1( stack.getDate2() );
        stack.copyValues();

        /* If dataRange is specified, only interpolate values up to dateRane.to */
        if( dateRange != null && calendar.getTime().after( dateRange.getTo() ) )
          break;
      }

      setEndValues( stack, calendar );
    }
    catch( final SensorException e )
    {
      statis.add( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), 0, Messages.getString( "ValueInterpolationWorker_0" ), e ) ); //$NON-NLS-1$
    }

    return StatusUtilities.createStatus( statis, Messages.getString( "ValueInterpolationWorker_1" ) ); //$NON-NLS-1$
  }

  /**
   * sets the start value of stack
   */
  private void setStartValues( final LocalCalculationStack stack, final Calendar calendar ) throws SensorException
  {
    final SimpleTupleModel interpolated = getInterpolatedModel();
    final IAxis dateAxis = getDateAxis();

    final LocalCalculationStackValue[] values = stack.getValues();

    final Date timeSeriesStartDate = (Date) getBaseModel().get( 0, dateAxis );
    final Calendar timeSeriesStart = Calendar.getInstance( m_timeZone );
    timeSeriesStart.setTime( timeSeriesStartDate );

    if( getDateRange() != null && isFilled() )
    {
      calendar.setTime( getDateRange().getFrom() );
      stack.setDate1( calendar.getTime() );

      for( final LocalCalculationStackValue value : values )
      {
        final TupleModelDataSet dataSet = toDataSet( getBaseModel(), 0, value );
        value.setValue1( dataSet );
      }

      while( calendar.before( timeSeriesStart ) )
      {
        stack.setDate1( calendar.getTime() );
        addDefaultTupple( dateAxis, stack, calendar );
      }
    }
    else
    {
      calendar.setTime( timeSeriesStart.getTime() );

      final Object[] tuple = new Object[getInterpolatedModel().getAxes().length];
      tuple[interpolated.getPosition( dateAxis )] = calendar.getTime();

      for( final LocalCalculationStackValue value : values )
      {
        final TupleModelDataSet dataSet = toDataSet( getBaseModel(), 0, value );
        value.setValue1( dataSet );

        final IAxis statusAxis = AxisUtils.findStatusAxis( interpolated.getAxes(), dataSet.getValueAxis() );
        final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( interpolated.getAxes(), dataSet.getValueAxis() );

        final int posValueAxis = interpolated.getPosition( dataSet.getValueAxis() );
        final int posStatusAxis = Objects.isNotNull( statusAxis ) ? interpolated.getPosition( statusAxis ) : -1;
        final int posDataSourceAxis = Objects.isNotNull( dataSourceAxis ) ? interpolated.getPosition( dataSourceAxis ) : -1;

        if( posValueAxis >= 0 )
          tuple[posValueAxis] = dataSet.getValue();

        if( posStatusAxis >= 0 )
          tuple[posStatusAxis] = dataSet.getStatus();

        if( posDataSourceAxis >= 0 )
        {
          final DataSourceHandler dataSourceHandler = new DataSourceHandler( getFilter().getMetaDataList() );

          final String source = dataSet.getSource();
          if( StringUtils.isNotEmpty( source ) )
          {
            tuple[posDataSourceAxis] = dataSourceHandler.addDataSource( source, source );
          }
        }
      }

      interpolated.addTuple( tuple );

      stack.setDate1( calendar.getTime() );
      doStep( calendar );
    }
  }

  private void setEndValues( final LocalCalculationStack stack, final Calendar calendar ) throws SensorException
  {
    final IAxis dateAxis = getDateAxis();

    // do we need to fill after the end of the base model?
    final DateRange dateRange = getDateRange();
    if( dateRange != null && isFilled() )
    {
      final Object[] lastValidTuple = determineLastValid( dateAxis, stack );

      final Date until = dateRange.getTo();
      while( !calendar.getTime().after( until ) )
        appendTuple( lastValidTuple, calendar );
    }
  }

  private Object[] determineLastValid( final IAxis dateAxis, final LocalCalculationStack stack ) throws SensorException
  {
    final LocalCalculationStackValue[] values = stack.getValues();

    // optionally remember the last interpolated values in order
    // to fill them till the end of the new model
    final SimpleTupleModel interpolatedModel = getInterpolatedModel();

    // FIXME: this is not really correct... maybe the last value is not valid? We need first to determine the real last
    // valid position
    final int lastValidValuePosition = interpolatedModel.size() - 1;

    final Object[] lastValidTuple = new Object[interpolatedModel.getAxes().length];

    final int dateAxisPosition = interpolatedModel.getPosition( dateAxis );
    if( lastValidValuePosition < 0 )
      lastValidTuple[dateAxisPosition] = null;
    else
      lastValidTuple[dateAxisPosition] = interpolatedModel.get( lastValidValuePosition, dateAxis );

    for( final LocalCalculationStackValue value : values )
    {

      final IAxis valueAxis = value.getAxis();
      final IAxis statusAxis = AxisUtils.findStatusAxis( interpolatedModel.getAxes(), valueAxis );
      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( interpolatedModel.getAxes(), valueAxis );

      final int posValueAxis = interpolatedModel.getPosition( valueAxis );
      final int posStatusAxis = Objects.isNotNull( statusAxis ) ? interpolatedModel.getPosition( statusAxis ) : -1;
      final int posDataSourceAxis = Objects.isNotNull( dataSourceAxis ) ? interpolatedModel.getPosition( dataSourceAxis ) : -1;

      final TupleModelDataSet defaultValue = value.getDefaultValue( getFilter() ); // FIXME

      if( isLastFilledWithValid() && interpolatedModel.size() > 0 && lastValidValuePosition >= 0 )
        lastValidTuple[posValueAxis] = interpolatedModel.get( lastValidValuePosition, valueAxis );
      else
      {
        lastValidTuple[posValueAxis] = defaultValue.getValue();
      }

      if( posStatusAxis >= 0 )
        lastValidTuple[posStatusAxis] = defaultValue.getStatus();

      if( posDataSourceAxis >= 0 )
      {
        final DataSourceHandler dataSourceHandler = new DataSourceHandler( getFilter().getMetaDataList() );

        final String source = defaultValue.getSource();
        if( StringUtils.isNotEmpty( source ) )
        {
          lastValidTuple[posDataSourceAxis] = dataSourceHandler.addDataSource( source, source );
        }
      }
    }

    return lastValidTuple;
  }

  /**
   * @param valueAxes
   *          includes data_source axes, too
   */
  private void appendTuple( final Object[] tuple, final Calendar calendar ) throws SensorException
  {
    final IAxis dateAxis = getDateAxis();

    final SimpleTupleModel interpolatedModel = getInterpolatedModel();
    final int datePosition = interpolatedModel.getPosition( dateAxis );

    final Object[] add = tuple.clone();
    add[datePosition] = calendar.getTime();

    interpolatedModel.addTuple( add );
    doStep( calendar );
  }

  private void setInterpolationValues( final LocalCalculationStack stack, final Calendar calendar, final int index ) throws SensorException
  {
    final LocalCalculationStackValue[] values = stack.getValues();
    final IAxis dateAxis = getDateAxis();

    stack.setDate2( (Date) getBaseModel().get( index, dateAxis ) );

    for( final LocalCalculationStackValue value : values )
    {
      final TupleModelDataSet dataSet = toDataSet( getBaseModel(), index, value );
      value.setValue2( dataSet );
    }

    final SimpleTupleModel interpolatedModel = getInterpolatedModel();

    final DataSourceHandler dataSourceHandler = new DataSourceHandler( getFilter().getMetaDataList() );

    // FIXME: use .before...
    while( calendar.getTime().compareTo( stack.getDate2() ) <= 0 )
    {
      final int datePosition = interpolatedModel.getPosition( dateAxis );

      final Object[] tuple = new Object[interpolatedModel.getAxes().length];
      tuple[datePosition] = calendar.getTime();

      for( final LocalCalculationStackValue value : values )
      {
        final TupleModelDataSet dataSet = LocalCalculationStackValues.getInterpolatedValue( calendar, stack, value );

        final IAxis statusAxis = AxisUtils.findStatusAxis( interpolatedModel.getAxes(), dataSet.getValueAxis() );
        final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( interpolatedModel.getAxes(), dataSet.getValueAxis() );

        final int posValueAxis = interpolatedModel.getPosition( dataSet.getValueAxis() );
        final int posStatusAxis = Objects.isNotNull( statusAxis ) ? interpolatedModel.getPosition( statusAxis ) : -1;
        final int posDataSourceAxis = Objects.isNotNull( dataSourceAxis ) ? interpolatedModel.getPosition( dataSourceAxis ) : -1;

        if( posValueAxis >= 0 )
          tuple[posValueAxis] = dataSet.getValue();
        if( posStatusAxis >= 0 )
          tuple[posStatusAxis] = dataSet.getStatus();
        if( posDataSourceAxis >= 0 )
        {
          final String source = dataSet.getSource();
          if( StringUtils.isNotEmpty( source ) )
          {
            final int dataSourceIndex = dataSourceHandler.addDataSource( source, source );
            tuple[posDataSourceAxis] = dataSourceIndex;
          }
        }

      }

      getInterpolatedModel().addTuple( tuple );
      doStep( calendar );
    }

  }
}
