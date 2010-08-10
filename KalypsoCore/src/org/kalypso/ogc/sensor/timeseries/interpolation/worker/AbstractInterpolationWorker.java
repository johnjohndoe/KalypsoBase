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

import java.util.Calendar;
import java.util.Date;

import org.kalypso.commons.parser.IParser;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractInterpolationWorker implements ICoreRunnableWithProgress
{
  /**
   * the base tuple model which will be processed by derived workers
   */
  private final ITupleModel m_baseModel;

  private final DateRange m_dateRange;

  /**
   * the interpolated result model
   */
  private final SimpleTupleModel m_interpolated;

  private final IInterpolationFilter m_filter;

  public AbstractInterpolationWorker( final IInterpolationFilter filter, final ITupleModel baseModel, final DateRange dateRange )
  {
    m_filter = filter;
    m_baseModel = baseModel;
    m_dateRange = dateRange;
    m_interpolated = new SimpleTupleModel( getBaseModel().getAxisList() );
  }

  protected ITupleModel getBaseModel( )
  {
    return m_baseModel;
  }

  public boolean isFilled( )
  {
    return m_filter.isFilled();
  }

  protected boolean isLastFilledWithValid( )
  {
    return m_filter.isLastFilledWithValid();
  }

  protected DateRange getDateRange( )
  {
    return m_dateRange;
  }

  public static AbstractInterpolationWorker createWorker( final IInterpolationFilter filter, final IRequest request ) throws SensorException
  {
    final DateRange dateRange = request == null ? null : request.getDateRange();

    // BUGIFX: fixes the problem with the first value:
    // the first value was always ignored, because the interval
    // filter cannot handle the first value of the source observation
    // FIX: we just make the request a big bigger in order to get a new first value
    // HACK: we always use DAY, so that work fine only up to time series of DAY-quality.
    // Maybe there should be one day a mean to determine, which is the right amount.
    final ITupleModel values = ObservationUtilities.requestBuffered( filter.getObservation(), dateRange, Calendar.DAY_OF_MONTH, 2 );

    if( values.getCount() == 0 )
    {
      return new EmptyValueInterpolationWorker( filter, values, dateRange );
    }

    return new ValueInterpolationWorker( filter, values, dateRange );
  }

  public SimpleTupleModel getInterpolatedModel( )
  {
    return m_interpolated;
  }

  protected Integer getDefaultStatus( )
  {
    return m_filter.getDefaultStatus();
  }

  protected IAxis getDateAxis( )
  {
    final IAxis[] axes = getBaseModel().getAxisList();

    return ObservationUtilities.findAxisByClass( axes, Date.class );
  }

  protected IAxis[] getValueAxes( )
  {
    final IAxis[] axes = getBaseModel().getAxisList();

    return ObservationUtilities.findAxesByClasses( axes, new Class[] { Number.class, Boolean.class } );
  }

  protected Object[] parseDefaultValues( final IAxis[] valueAxes ) throws SensorException
  {
    final Object[] defaultValues = new Object[valueAxes.length];
    for( int i = 0; i < defaultValues.length; i++ )
    {
      try
      {
        if( KalypsoStatusUtils.isStatusAxis( valueAxes[i] ) )
          defaultValues[i] = m_filter.getDefaultStatus();
        else
        {
          final IParser parser = ZmlFactory.createParser( valueAxes[i] );
          defaultValues[i] = parser.parse( m_filter.getDefaultValue() );
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
  protected void fillWithDefault( final IAxis dateAxis, final IAxis[] valueAxes, final Object[] defaultValues, final Calendar cal ) throws SensorException
  {
    fillWithDefault( dateAxis, valueAxes, defaultValues, cal, null );
  }

  /**
   * Fills the model with default values
   * 
   * @param masterTupple
   *          if not null, the values from this tuple are used instead of the default one
   */
  protected void fillWithDefault( final IAxis dateAxis, final IAxis[] valueAxes, final Object[] defaultValues, final Calendar cal, final Object[] masterTupple ) throws SensorException
  {
    final Object[] tupple;

    if( masterTupple == null )
    {
      tupple = new Object[valueAxes.length + 1];
      tupple[getInterpolatedModel().getPositionFor( dateAxis )] = cal.getTime();

      for( int i = 0; i < valueAxes.length; i++ )
      {
        final IAxis axis = valueAxes[i];
        final int pos = getInterpolatedModel().getPositionFor( axis );
        tupple[pos] = defaultValues[i];
      }
    }
    else
    {
      tupple = masterTupple.clone();
      tupple[getInterpolatedModel().getPositionFor( dateAxis )] = cal.getTime();
    }

    getInterpolatedModel().addTuple( tupple );
    nextStep( cal );
  }

  protected void nextStep( final Calendar calendar )
  {
    calendar.add( m_filter.getCalendarField(), m_filter.getCalendarAmnount() );
  }

}
