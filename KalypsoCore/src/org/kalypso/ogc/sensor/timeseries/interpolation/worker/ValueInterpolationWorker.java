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
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;

/**
 * @author Dirk Kuch
 */
public class ValueInterpolationWorker extends AbstractInterpolationWorker
{
  protected class LocalCalculationStack
  {
    public Date d1 = null;

    public Date d2 = null;

    public final double[] v1;

    public final double[] v2;

    public LocalCalculationStack( final int size )
    {
      v1 = new double[size + 1];
      v2 = new double[size + 1];
    }
  }

  public ValueInterpolationWorker( final IInterpolationFilter filter, final ITupleModel values, final DateRange dateRange )
  {
    super( filter, values, dateRange );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<IStatus>();

    try
    {
      final IAxis[] valueAxes = getValueAxes();
      final Calendar calendar = Calendar.getInstance();

      final LocalCalculationStack stack = new LocalCalculationStack( valueAxes.length );

      // do we need to fill before the beginning of the base model?
      setStartValue( stack, calendar );

      for( int index = 1; index < getBaseModel().getCount(); index++ )
      {
        setInterpolationValues( stack, calendar, index );

        stack.d1 = stack.d2;
        System.arraycopy( stack.v2, 0, stack.v1, 0, stack.v2.length );
      }

      setEndValues( calendar );
    }
    catch( final SensorException e )
    {
      statis.add( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), 0, "Interpolating values failed", e ) );
    }

    return StatusUtilities.createStatus( statis, "Interpolating values" );
  }

  private void setEndValues( final Calendar calendar ) throws SensorException
  {
    final IAxis dateAxis = getDateAxis();
    final IAxis[] valueAxes = getValueAxes();

    // do we need to fill after the end of the base model?
    if( getDateRange() != null && isFilled() )
    {
      // optionally remember the last interpolated values in order
      // to fill them till the end of the new model
      Object[] lastValidTuple = null;
      if( isLastFilledWithValid() && getInterpolatedModel().getCount() > 0 )
      {
        final int position = getInterpolatedModel().getCount() - 1;

        lastValidTuple = new Object[valueAxes.length + 1];
        final int datePosition = getInterpolatedModel().getPositionFor( dateAxis );
        lastValidTuple[datePosition] = getInterpolatedModel().getElement( position, dateAxis );

        for( int i = 0; i < valueAxes.length; i++ )
        {
          final int valuePosition = getInterpolatedModel().getPositionFor( valueAxes[i] );

          if( KalypsoStatusUtils.isStatusAxis( valueAxes[i] ) )
            lastValidTuple[valuePosition] = getDefaultStatus();
          else
            lastValidTuple[valuePosition] = getInterpolatedModel().getElement( position, valueAxes[i] );
        }
      }

      // FIXME: what happens if lastValidTuple == null
      if( lastValidTuple == null )
        return;

      // FIXME: compare date with Date.before !
      while( calendar.getTime().compareTo( getDateRange().getTo() ) <= 0 )
      {
        appendTuple( lastValidTuple, calendar );
      }
    }
  }

  private void setInterpolationValues( final LocalCalculationStack stack, final Calendar calendar, final int index ) throws SensorException
  {
    final IAxis dateAxis = getDateAxis();
    final IAxis[] valueAxes = getValueAxes();

    final LinearEquation eq = new LinearEquation();

    stack.d2 = (Date) getBaseModel().getElement( index, dateAxis );

    for( int ia = 0; ia < valueAxes.length; ia++ )
    {
      final Number nb = (Number) getBaseModel().getElement( index, valueAxes[ia] );
      final int valuePosition = getInterpolatedModel().getPositionFor( valueAxes[ia] );

      stack.v2[valuePosition] = nb.doubleValue();
    }

    while( calendar.getTime().compareTo( stack.d2 ) <= 0 )
    {
      final long ms = calendar.getTimeInMillis();

      final int datePosition = getInterpolatedModel().getPositionFor( dateAxis );

      final Object[] tuple = new Object[valueAxes.length + 1];
      tuple[datePosition] = calendar.getTime();

      boolean interpolated = false;

      for( int ia = 0; ia < valueAxes.length; ia++ )
      {
        final IAxis valueAxis = valueAxes[ia];

        final int position = getInterpolatedModel().getPositionFor( valueAxis );

        final double valStart = stack.v1[position];
        final double valStop = stack.v2[position];

        final long linearStart = stack.d1.getTime();
        final long linearStop = stack.d2.getTime();

        if( KalypsoStatusUtils.isStatusAxis( valueAxis ) )
        {
          // BUGFIX: do not interpolate, if we have the exact date
          if( linearStart == ms )
            tuple[position] = new Integer( (int) valStart );
          else if( linearStop == ms )
            tuple[position] = new Integer( (int) valStop );
          else
          {
            // this is the status axis: no interpolation
            tuple[position] = new Integer( KalypsoStatusUtils.performInterpolation( (int) valStart, (int) valStop ) );

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
              eq.setPoints( linearStart, valStart, linearStop, valStop );
              tuple[position] = new Double( eq.computeY( ms ) );

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

      nextStep( calendar );
    }

  }

  private void updateDataSource( final Object[] tuple, final boolean interpolated ) throws SensorException
  {
    if( !interpolated )
      return;

    // FIXME: what happens if dataSource is null?
    IAxis dataSourceAxis = getDataSourceAxis();
    if( dataSourceAxis == null )
      return;

    final int position = getInterpolatedModel().getPositionFor( dataSourceAxis );
    tuple[position] = getDataSourceIndex();
  }

  private void setStartValue( final LocalCalculationStack stack, final Calendar calendar ) throws SensorException
  {
    final SimpleTupleModel interpolated = getInterpolatedModel();

    final IAxis dateAxis = getDateAxis();
    final IAxis[] valueAxes = getValueAxes();

    final Object[] defaultValues = parseDefaultValues( valueAxes );

    final Date timeSeriesStart = (Date) getBaseModel().getElement( 0, dateAxis );

    if( getDateRange() != null && isFilled() )
    {
      calendar.setTime( getDateRange().getFrom() );
      stack.d1 = calendar.getTime();

      for( int i = 0; i < valueAxes.length; i++ )
      {
        // keep original data_src!
        final Number number = (Number) getBaseModel().getElement( 0, valueAxes[i] );
        final int position = interpolated.getPositionFor( valueAxes[i] );

        stack.v1[position] = number.doubleValue();
      }

      while( calendar.getTime().compareTo( timeSeriesStart ) < 0 )
      {
        stack.d1 = calendar.getTime();
        fillWithDefault( dateAxis, valueAxes, defaultValues, calendar );
      }
    }
    else
    {
      calendar.setTime( timeSeriesStart );

      final Object[] tuple = new Object[valueAxes.length + 1];
      tuple[interpolated.getPositionFor( dateAxis )] = calendar.getTime();

      for( int i = 0; i < valueAxes.length; i++ )
      {
        final Number number = (Number) getBaseModel().getElement( 0, valueAxes[i] );

        final int position = interpolated.getPositionFor( valueAxes[i] );
        tuple[position] = number;

        stack.v1[position] = number.doubleValue();
      }

      interpolated.addTuple( tuple );

      stack.d1 = calendar.getTime();
      nextStep( calendar );
    }
  }

}
