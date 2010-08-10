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
import org.kalypso.commons.math.LinearEquation;
import org.kalypso.commons.math.LinearEquation.SameXValuesException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
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
      statis.add( StatusUtilities.createErrorStatus( "Interpolating values failed", e ) );
    }

    return StatusUtilities.createStatus( statis, "Interpolating values" );
  }

  private void setEndValues( final Calendar calendar ) throws SensorException
  {
    final IAxis dateAxis = getDateAxis();
    final IAxis[] valueAxes = getValueAxes();

    final Object[] defaultValues = parseDefaultValues( valueAxes );

    // do we need to fill after the end of the base model?
    if( getDateRange() != null && isFilled() )
    {
      // optionally remember the last interpolated values in order
      // to fill them till the end of the new model
      Object[] lastValidTupple = null;
      if( isLastFilledWithValid() && getInterpolatedModel().getCount() > 0 )
      {
        final int pos = getInterpolatedModel().getCount() - 1;

        lastValidTupple = new Object[valueAxes.length + 1];
        lastValidTupple[getInterpolatedModel().getPositionFor( dateAxis )] = getInterpolatedModel().getElement( pos, dateAxis );
        for( int i = 0; i < valueAxes.length; i++ )
        {
          if( KalypsoStatusUtils.isStatusAxis( valueAxes[i] ) )
            lastValidTupple[getInterpolatedModel().getPositionFor( valueAxes[i] )] = getDefaultStatus();
          else
            lastValidTupple[getInterpolatedModel().getPositionFor( valueAxes[i] )] = getInterpolatedModel().getElement( pos, valueAxes[i] );
        }
      }

      while( calendar.getTime().compareTo( getDateRange().getTo() ) <= 0 )
      {
        fillWithDefault( dateAxis, valueAxes, defaultValues, calendar, lastValidTupple );
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
      stack.v2[getInterpolatedModel().getPositionFor( valueAxes[ia] )] = nb.doubleValue();
    }

    while( calendar.getTime().compareTo( stack.d2 ) <= 0 )
    {
      final long ms = calendar.getTimeInMillis();

      final Object[] tuple = new Object[valueAxes.length + 1];
      tuple[getInterpolatedModel().getPositionFor( dateAxis )] = calendar.getTime();

      for( int ia = 0; ia < valueAxes.length; ia++ )
      {
        final int pos = getInterpolatedModel().getPositionFor( valueAxes[ia] );

        final double valStart = stack.v1[pos];
        final double valStop = stack.v2[pos];

        final long linearStart = stack.d1.getTime();
        final long linearStop = stack.d2.getTime();

        if( KalypsoStatusUtils.isStatusAxis( valueAxes[ia] ) )
        {
          // BUGFIX: do not interpolate, if we have the exact date
          if( linearStart == ms )
            tuple[pos] = new Integer( (int) valStart );
          else if( linearStop == ms )
            tuple[pos] = new Integer( (int) valStop );
          else
            // this is the status axis: no interpolation
            tuple[pos] = new Integer( KalypsoStatusUtils.performInterpolation( (int) valStart, (int) valStop ) );
        }
        else
        {
          // normal case: perform the interpolation
          try
          {
            // BUGFIX: do not interpolate, if we have the exact date
            if( linearStart == ms )
              tuple[pos] = new Double( valStart );
            else if( linearStop == ms )
              tuple[pos] = new Double( valStop );
            else
            {
              eq.setPoints( linearStart, valStart, linearStop, valStop );
              tuple[pos] = new Double( eq.computeY( ms ) );
            }
          }
          catch( final SameXValuesException e )
          {
            tuple[pos] = new Double( valStart );
          }
        }
      }

      getInterpolatedModel().addTuple( tuple );

      nextStep( calendar );
    }

  }

  private void setStartValue( final LocalCalculationStack stack, final Calendar calendar ) throws SensorException
  {
    final IAxis dateAxis = getDateAxis();
    final IAxis[] valueAxes = getValueAxes();
    final Object[] defaultValues = parseDefaultValues( valueAxes );

    final Date begin = (Date) getBaseModel().getElement( 0, dateAxis );

    if( getDateRange() != null && isFilled() )
    {
      calendar.setTime( getDateRange().getFrom() );
      stack.d1 = calendar.getTime();

      for( int i = 0; i < valueAxes.length; i++ )
      {
        final Number nb = (Number) getBaseModel().getElement( 0, valueAxes[i] );
        stack.v1[getInterpolatedModel().getPositionFor( valueAxes[i] )] = nb.doubleValue();
      }

      while( calendar.getTime().compareTo( begin ) < 0 )
      {
        stack.d1 = calendar.getTime();
        fillWithDefault( dateAxis, valueAxes, defaultValues, calendar );
      }
    }
    else
    {
      calendar.setTime( begin );

      final Object[] tupple = new Object[valueAxes.length + 1];
      tupple[getInterpolatedModel().getPositionFor( dateAxis )] = calendar.getTime();

      for( int i = 0; i < valueAxes.length; i++ )
      {
        final Number nb = (Number) getBaseModel().getElement( 0, valueAxes[i] );

        final int pos = getInterpolatedModel().getPositionFor( valueAxes[i] );
        tupple[pos] = nb;
        stack.v1[pos] = nb.doubleValue();
      }

      getInterpolatedModel().addTuple( tupple );

      stack.d1 = calendar.getTime();
      nextStep( calendar );
    }
  }

}
