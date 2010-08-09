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
package org.kalypso.ogc.sensor.timeseries.interpolation;

import java.util.Calendar;
import java.util.Date;

import org.kalypso.commons.math.LinearEquation;
import org.kalypso.commons.math.LinearEquation.SameXValuesException;
import org.kalypso.commons.parser.IParser;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;

/**
 * InterpolationFilter. This is a simple yet tricky interpolation filter. It steps through the time and eventually
 * interpolates the values at t, using the values at t-1 and t+1.
 * <p>
 * This filter can also deal with Kalypso Status Axes. In that case it does not perform an interpolation, but uses the
 * policy defined in KalypsoStatusUtils. When no status is available, it uses the default value for the status provided
 * in the constructor.
 * 
 * @author schlienger
 */
public class InterpolationFilter extends AbstractObservationFilter
{
  private final int m_calField;

  private final int m_amount;

  private final boolean m_fill;

  private final String m_defValue;

  private final Integer m_defaultStatus;

  private final boolean m_fillLastWithValid;

  /**
   * Constructor.
   * 
   * @param calendarField
   *          which field of the date will be used for stepping through the time series
   * @param amount
   *          amount of time for the step
   * @param forceFill
   *          when true, fills the model with defaultValue when no base value
   * @param defaultValue
   *          default value to use when filling absent values
   * @param defaultStatus
   *          value of the default status when base status is absent or when status-interpolation cannot be proceeded
   * @param fillLastWithValid
   *          when true, the last tuples of the model get the last valid tuple from the original, not the default one
   */
  public InterpolationFilter( final int calendarField, final int amount, final boolean forceFill, final String defaultValue, final int defaultStatus, final boolean fillLastWithValid )
  {
    m_calField = calendarField;
    m_amount = amount;
    m_fill = forceFill;
    m_fillLastWithValid = fillLastWithValid;
    m_defaultStatus = new Integer( defaultStatus );
    m_defValue = defaultValue;
  }

  public InterpolationFilter( final int calendarField, final int amount, final boolean forceFill, final String defaultValue, final int defaultStatus )
  {
    this( calendarField, amount, forceFill, defaultValue, defaultStatus, false );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest request ) throws SensorException
  {
    final DateRange dr = request == null ? null : request.getDateRange();

    // BUGIFX: fixes the problem with the first value:
    // the first value was always ignored, because the intervall
    // filter cannot handle the first value of the source observation
    // FIX: we just make the request a big bigger in order to get a new first value
    // HACK: we always use DAY, so that work fine only up to timeseries of DAY-quality.
    // Maybe there should be one day a mean to determine, which is the right amount.
    final ITupleModel values = ObservationUtilities.requestBuffered( getObservation(), dr, Calendar.DAY_OF_MONTH, 2 );

    final IAxis dateAxis = ObservationUtilities.findAxisByClass( values.getAxisList(), Date.class );
    final IAxis[] valueAxes = ObservationUtilities.findAxesByClasses( values.getAxisList(), new Class[] { Number.class, Boolean.class } );
    final Object[] defaultValues = parseDefaultValues( valueAxes );

    final SimpleTupleModel intModel = new SimpleTupleModel( values.getAxisList() );

    final Calendar cal = Calendar.getInstance();

    if( values.getCount() == 0 )
    {
      // no values, and fill is not set, so return
      if( !m_fill )
        return values;

      // no values but fill is set, generate them
      if( dr != null )
      {
        cal.setTime( dr.getFrom() );

        while( cal.getTime().compareTo( dr.getTo() ) <= 0 )
        {
          fillWithDefault( dateAxis, valueAxes, defaultValues, intModel, cal );
        }

        return intModel;
      }
    }

    if( values.getCount() != 0 )
    {
      final Date begin = (Date) values.getElement( 0, dateAxis );

      Date d1 = null;
      Date d2 = null;
      final double[] v1 = new double[valueAxes.length + 1];
      final double[] v2 = new double[valueAxes.length + 1];

      int startIx = 0;

      // do we need to fill before the begining of the base model?
      if( dr != null && m_fill )
      {
        cal.setTime( dr.getFrom() );
        d1 = cal.getTime();

        for( int i = 0; i < valueAxes.length; i++ )
        {
          final Number nb = (Number) values.getElement( startIx, valueAxes[i] );
          v1[intModel.getPositionFor( valueAxes[i] )] = nb.doubleValue();
        }

        while( cal.getTime().compareTo( begin ) < 0 )
        {
          d1 = cal.getTime();
          fillWithDefault( dateAxis, valueAxes, defaultValues, intModel, cal );
        }
      }
      else
      {
        cal.setTime( begin );

        final Object[] tupple = new Object[valueAxes.length + 1];
        tupple[intModel.getPositionFor( dateAxis )] = cal.getTime();

        for( int i = 0; i < valueAxes.length; i++ )
        {
          final Number nb = (Number) values.getElement( startIx, valueAxes[i] );

          final int pos = intModel.getPositionFor( valueAxes[i] );
          tupple[pos] = nb;
          v1[pos] = nb.doubleValue();
        }

        intModel.addTupple( tupple );

        d1 = cal.getTime();

        cal.add( m_calField, m_amount );

        startIx++;
      }

      final LinearEquation eq = new LinearEquation();

      for( int ix = startIx; ix < values.getCount(); ix++ )
      {
        d2 = (Date) values.getElement( ix, dateAxis );

        for( int ia = 0; ia < valueAxes.length; ia++ )
        {
          final Number nb = (Number) values.getElement( ix, valueAxes[ia] );
          v2[intModel.getPositionFor( valueAxes[ia] )] = nb.doubleValue();
        }

        while( cal.getTime().compareTo( d2 ) <= 0 )
        {
          final long ms = cal.getTimeInMillis();

          final Object[] tupple = new Object[valueAxes.length + 1];
          tupple[intModel.getPositionFor( dateAxis )] = cal.getTime();

          for( int ia = 0; ia < valueAxes.length; ia++ )
          {
            final int pos = intModel.getPositionFor( valueAxes[ia] );

            final double valStart = v1[pos];
            final double valStop = v2[pos];

            final long linearStart = d1.getTime();
            final long linearStop = d2.getTime();

            if( KalypsoStatusUtils.isStatusAxis( valueAxes[ia] ) )
            {
              // BUGFIX: do not interpolate, if we have the exact date
              if( linearStart == ms )
                tupple[pos] = new Integer( (int) valStart );
              else if( linearStop == ms )
                tupple[pos] = new Integer( (int) valStop );
              else
                // this is the status axis: no interpolation
                tupple[pos] = new Integer( KalypsoStatusUtils.performInterpolation( (int) valStart, (int) valStop ) );
            }
            else
            {
              // normal case: perform the interpolation
              try
              {
                // BUGFIX: do not interpolate, if we have the exact date
                if( linearStart == ms )
                  tupple[pos] = new Double( valStart );
                else if( linearStop == ms )
                  tupple[pos] = new Double( valStop );
                else
                {
                  eq.setPoints( linearStart, valStart, linearStop, valStop );
                  tupple[pos] = new Double( eq.computeY( ms ) );
                }
              }
              catch( final SameXValuesException e )
              {
                tupple[pos] = new Double( valStart );
              }
            }
          }

          intModel.addTupple( tupple );

          cal.add( m_calField, m_amount );
        }

        d1 = d2;
        System.arraycopy( v2, 0, v1, 0, v2.length );
      }
    }

    // do we need to fill after the end of the base model?
    if( dr != null && m_fill )
    {
      // optionally remember the last interpolated values in order
      // to fill them till the end of the new model
      Object[] lastValidTupple = null;
      if( m_fillLastWithValid && intModel.getCount() > 0 )
      {
        final int pos = intModel.getCount() - 1;

        lastValidTupple = new Object[valueAxes.length + 1];
        lastValidTupple[intModel.getPositionFor( dateAxis )] = intModel.getElement( pos, dateAxis );
        for( int i = 0; i < valueAxes.length; i++ )
        {
          if( KalypsoStatusUtils.isStatusAxis( valueAxes[i] ) )
            lastValidTupple[intModel.getPositionFor( valueAxes[i] )] = m_defaultStatus;
          else
            lastValidTupple[intModel.getPositionFor( valueAxes[i] )] = intModel.getElement( pos, valueAxes[i] );
        }
      }

      while( cal.getTime().compareTo( dr.getTo() ) <= 0 )
      {
        fillWithDefault( dateAxis, valueAxes, defaultValues, intModel, cal, lastValidTupple );
      }
    }

    return intModel;
  }

  private Object[] parseDefaultValues( final IAxis[] valueAxes ) throws SensorException
  {
    final Object[] defaultValues = new Object[valueAxes.length];
    for( int i = 0; i < defaultValues.length; i++ )
    {
      try
      {
        if( KalypsoStatusUtils.isStatusAxis( valueAxes[i] ) )
          defaultValues[i] = m_defaultStatus;
        else
        {
          final IParser parser = ZmlFactory.createParser( valueAxes[i] );
          defaultValues[i] = parser.parse( m_defValue );
        }
      }
      catch( final Exception e )
      {
        throw new SensorException( e );
      }
    }
    return defaultValues;
  }

  /**
   * Fill the model with default values
   */
  private void fillWithDefault( final IAxis dateAxis, final IAxis[] valueAxes, final Object[] defaultValues, final SimpleTupleModel intModel, final Calendar cal ) throws SensorException
  {
    fillWithDefault( dateAxis, valueAxes, defaultValues, intModel, cal, null );
  }

  /**
   * Fills the model with default values
   * 
   * @param masterTupple
   *          if not null, the values from this tupple are used instead of the default one
   */
  private void fillWithDefault( final IAxis dateAxis, final IAxis[] valueAxes, final Object[] defaultValues, final SimpleTupleModel intModel, final Calendar cal, final Object[] masterTupple ) throws SensorException
  {
    final Object[] tupple;

    if( masterTupple == null )
    {
      tupple = new Object[valueAxes.length + 1];
      tupple[intModel.getPositionFor( dateAxis )] = cal.getTime();

      for( int i = 0; i < valueAxes.length; i++ )
      {
        final IAxis axis = valueAxes[i];
        final int pos = intModel.getPositionFor( axis );
        tupple[pos] = defaultValues[i];
      }
    }
    else
    {
      tupple = masterTupple.clone();
      tupple[intModel.getPositionFor( dateAxis )] = cal.getTime();
    }

    intModel.addTupple( tupple );

    cal.add( m_calField, m_amount );
  }
}