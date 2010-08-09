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
package org.kalypso.ogc.sensor.timeseries.interpolation;

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
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;

/**
 * @author Dirk Kuch
 */
public class ValueInterpolationWorker extends AbstractInterpolationWorker
{

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
      final IAxis[] axes = getBaseModel().getAxisList();
      final SimpleTupleModel interpolated = new SimpleTupleModel( axes );

      final IAxis dateAxis = ObservationUtilities.findAxisByClass( axes, Date.class );
      final IAxis[] valueAxes = ObservationUtilities.findAxesByClasses( axes, new Class[] { Number.class, Boolean.class } );
      final Object[] defaultValues = parseDefaultValues( valueAxes );

      final Calendar calendar = Calendar.getInstance();
      calendar.setTime( getDateRange().getFrom() );

      final Date begin = (Date) getBaseModel().getElement( 0, dateAxis );

      Date d1 = null;
      Date d2 = null;
      final double[] v1 = new double[valueAxes.length + 1];
      final double[] v2 = new double[valueAxes.length + 1];

      int startIx = 0;

      // do we need to fill before the beginning of the base model?
      if( getDateRange() != null && isFilled() )
      {
        calendar.setTime( getDateRange().getFrom() );
        d1 = calendar.getTime();

        for( int i = 0; i < valueAxes.length; i++ )
        {
          final Number nb = (Number) getBaseModel().getElement( startIx, valueAxes[i] );
          v1[interpolated.getPositionFor( valueAxes[i] )] = nb.doubleValue();
        }

        while( calendar.getTime().compareTo( begin ) < 0 )
        {
          d1 = calendar.getTime();
          fillWithDefault( dateAxis, valueAxes, defaultValues, interpolated, calendar );
        }
      }
      else
      {
        calendar.setTime( begin );

        final Object[] tupple = new Object[valueAxes.length + 1];
        tupple[interpolated.getPositionFor( dateAxis )] = calendar.getTime();

        for( int i = 0; i < valueAxes.length; i++ )
        {
          final Number nb = (Number) getBaseModel().getElement( startIx, valueAxes[i] );

          final int pos = interpolated.getPositionFor( valueAxes[i] );
          tupple[pos] = nb;
          v1[pos] = nb.doubleValue();
        }

        interpolated.addTuple( tupple );

        d1 = calendar.getTime();

        nextStep( calendar );

        startIx++;
      }

      final LinearEquation eq = new LinearEquation();

      for( int ix = startIx; ix < getBaseModel().getCount(); ix++ )
      {
        d2 = (Date) getBaseModel().getElement( ix, dateAxis );

        for( int ia = 0; ia < valueAxes.length; ia++ )
        {
          final Number nb = (Number) getBaseModel().getElement( ix, valueAxes[ia] );
          v2[interpolated.getPositionFor( valueAxes[ia] )] = nb.doubleValue();
        }

        while( calendar.getTime().compareTo( d2 ) <= 0 )
        {
          final long ms = calendar.getTimeInMillis();

          final Object[] tuple = new Object[valueAxes.length + 1];
          tuple[interpolated.getPositionFor( dateAxis )] = calendar.getTime();

          for( int ia = 0; ia < valueAxes.length; ia++ )
          {
            final int pos = interpolated.getPositionFor( valueAxes[ia] );

            final double valStart = v1[pos];
            final double valStop = v2[pos];

            final long linearStart = d1.getTime();
            final long linearStop = d2.getTime();

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

          interpolated.addTuple( tuple );

          nextStep( calendar );
        }

        d1 = d2;
        System.arraycopy( v2, 0, v1, 0, v2.length );
      }

      // do we need to fill after the end of the base model?
      if( getDateRange() != null && isFilled() )
      {
        // optionally remember the last interpolated values in order
        // to fill them till the end of the new model
        Object[] lastValidTupple = null;
        if( isLastFilledWithValid() && interpolated.getCount() > 0 )
        {
          final int pos = interpolated.getCount() - 1;

          lastValidTupple = new Object[valueAxes.length + 1];
          lastValidTupple[interpolated.getPositionFor( dateAxis )] = interpolated.getElement( pos, dateAxis );
          for( int i = 0; i < valueAxes.length; i++ )
          {
            if( KalypsoStatusUtils.isStatusAxis( valueAxes[i] ) )
              lastValidTupple[interpolated.getPositionFor( valueAxes[i] )] = getDefaultStatus();
            else
              lastValidTupple[interpolated.getPositionFor( valueAxes[i] )] = interpolated.getElement( pos, valueAxes[i] );
          }
        }

        while( calendar.getTime().compareTo( getDateRange().getTo() ) <= 0 )
        {
          fillWithDefault( dateAxis, valueAxes, defaultValues, interpolated, calendar, lastValidTupple );
        }
      }

      setInterpolatedModel( interpolated );
    }
    catch( final SensorException e )
    {
      statis.add( StatusUtilities.createErrorStatus( "Interpolating values failed", e ) );
    }

    return StatusUtilities.createStatus( statis, "Interpolating values" );
  }

}
