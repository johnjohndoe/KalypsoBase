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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.math.LinearEquation;
import org.kalypso.commons.math.LinearEquation.SameXValuesException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * @author Dirk Kuch
 */
public class ValueInterpolationWorker extends AbstractInterpolationWorker
{
  private final Integer m_bitOK = new Integer( KalypsoStati.BIT_OK );

  public ValueInterpolationWorker( final IInterpolationFilter filter, final ITupleModel values, final DateRange dateRange )
  {
    super( filter, values, dateRange );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<IStatus>();

    try
    {
      final DateRange dateRange = getDateRange();
      final IAxis[] valueAxes = getValueAxes();

      // FIXME: timezone is missing! So we might get unexpected timesteps
      // Which one to use here? We cannot change it globally, in order to make it backwards compatible
      final Calendar calendar = Calendar.getInstance();

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
      statis.add( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), 0, "Interpolating values failed", e ) );
    }

    return StatusUtilities.createStatus( statis, "Interpolating values" );
  }

  /**
   * sets the start value of stack
   */
  private void setStartValues( final LocalCalculationStack stack, final Calendar calendar ) throws SensorException
  {
    final SimpleTupleModel interpolated = getInterpolatedModel();
    AxisUtils.findDateAxis( interpolated.getAxes() );

    final IAxis dateAxis = getDateAxis();

    final LocalCalculationStackValue[] values = stack.getValues();

    final Date timeSeriesStartDate = (Date) getBaseModel().get( 0, dateAxis );
    final Calendar timeSeriesStart = Calendar.getInstance();
    timeSeriesStart.setTime( timeSeriesStartDate );

    if( getDateRange() != null && isFilled() )
    {
      calendar.setTime( getDateRange().getFrom() );
      stack.setDate1( calendar.getTime() );

      for( final LocalCalculationStackValue value : values )
      {
        final Number number = (Number) getBaseModel().get( 0, value.getAxis() );
        value.setValue1( number.doubleValue() );
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

      final Object[] tuple = new Object[values.length + 1];
      tuple[interpolated.getPosition( dateAxis )] = calendar.getTime();

      for( final LocalCalculationStackValue value : values )
      {
        final IAxis valueAxis = value.getAxis();
        final Number number = (Number) getBaseModel().get( 0, valueAxis );

        final int position = interpolated.getPosition( valueAxis );
        tuple[position] = number;

        value.setValue1( number.doubleValue() );
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

    final Object[] lastValidTuple = new Object[values.length + 1];

    final int dateAxisPosition = interpolatedModel.getPosition( dateAxis );
    if( lastValidValuePosition < 0 )
      lastValidTuple[dateAxisPosition] = null;
    else
      lastValidTuple[dateAxisPosition] = interpolatedModel.get( lastValidValuePosition, dateAxis );

    for( final LocalCalculationStackValue value : values )
    {
      final IAxis valueAxis = value.getAxis();
      final int valueAxisPosition = interpolatedModel.getPosition( valueAxis );

      if( KalypsoStatusUtils.isStatusAxis( valueAxis ) )
        lastValidTuple[valueAxisPosition] = getDefaultStatus();
      else
      {
        if( isLastFilledWithValid() && interpolatedModel.size() > 0 && lastValidValuePosition >= 0 )
          lastValidTuple[valueAxisPosition] = interpolatedModel.get( lastValidValuePosition, valueAxis );
        else
          lastValidTuple[valueAxisPosition] = value.getDefaultValue( getFilter() );
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
    final IAxis[] dataSourceAxes = getDataSourceAxes();

    final SimpleTupleModel interpolatedModel = getInterpolatedModel();
    final int datePosition = interpolatedModel.getPosition( dateAxis );

    final Object[] add = tuple.clone();
    add[datePosition] = calendar.getTime();

    // FIXME: what to do, if data source is null ?!
    for( final IAxis dataSourceAxis : dataSourceAxes )
    {
      final int dataSrcPosition = interpolatedModel.getPosition( dataSourceAxis );
      add[dataSrcPosition] = getDataSourceIndex();
    }

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
      final Number nb = (Number) getBaseModel().get( index, value.getAxis() );
      value.setValue2( nb.doubleValue() );
    }

    // FIXME: use .before...
    while( calendar.getTime().compareTo( stack.getDate2() ) <= 0 )
    {
      final long ms = calendar.getTimeInMillis();

      final int datePosition = getInterpolatedModel().getPosition( dateAxis );

      final Object[] tuple = new Object[values.length + 1];
      tuple[datePosition] = calendar.getTime();

      boolean interpolated = false;

      for( final LocalCalculationStackValue value : values )
      {
        final LinearEquation equation = new LinearEquation();

        final IAxis valueAxis = value.getAxis();
        final int position = getInterpolatedModel().getPosition( valueAxis );

        final double valStart = value.getValue1();
        final double valStop = value.getValue2();

        final long linearStart = stack.getDate1().getTime();
        final long linearStop = stack.getDate2().getTime();

        if( KalypsoStatusUtils.isStatusAxis( valueAxis ) )
        {
          // BUGFIX: do not interpolate, if we have the exact date
          if( linearStart == ms )
            tuple[position] = new Integer( (int) valStart );
          else if( linearStop == ms )
            tuple[position] = new Integer( (int) valStop );
          else
          {
            tuple[position] = m_bitOK;
            interpolated = true;
          }
        }
        else
        {
          // normal case: perform the interpolation
          try
          {
            // BUGFIX: do not interpolate, if we have the exact date
            if( linearStart == ms )
              tuple[position] = new Double( valStart );
            else if( linearStop == ms )
              tuple[position] = new Double( valStop );
            else
            {
              equation.setPoints( linearStart, valStart, linearStop, valStop );
              tuple[position] = new Double( equation.computeY( ms ) );

              interpolated = true;
            }
          }
          catch( final SameXValuesException e )
          {
            tuple[position] = new Double( valStart );
          }
        }
      }

      updateDataSource( tuple, interpolated );
      getInterpolatedModel().addTuple( tuple );

      doStep( calendar );
    }

  }

  private void updateDataSource( final Object[] tuple, final boolean interpolated ) throws SensorException
  {
    if( !interpolated )
      return;

    // FIXME: what happens if dataSource is null? Shouldn't we add it?
    final IAxis[] dataSourceAxes = getDataSourceAxes();
    for( final IAxis dataSourceAxis : dataSourceAxes )
    {
      final int position = getInterpolatedModel().getPosition( dataSourceAxis );
      tuple[position] = getDataSourceIndex();
    }

  }

}
