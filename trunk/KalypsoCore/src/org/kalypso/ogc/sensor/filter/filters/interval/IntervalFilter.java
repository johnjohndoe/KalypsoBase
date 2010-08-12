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

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.filters.IntervallFilterType;

/**
 * @author doemming
 */
public class IntervalFilter extends AbstractObservationFilter
{
  public enum MODE
  {
    eIntensity,
    eSum;

    public static MODE getMode( final IntervallFilterType filter )
    {
      final String mode = filter.getMode();
      if( "sum".equalsIgnoreCase( mode ) ) //$NON-NLS-1$
        return eSum;

      return eIntensity;
    }
  }

  private IObservation m_baseobservation = null;

  private final MODE m_mode;

  private final double m_defaultValue;

  private final int m_defaultStatus;

  private final IntervalCalendar m_calendar;

  public IntervalFilter( final MODE mode, final int defaultStatus, final double defaultValue, final IntervalCalendar calendar )
  {
    m_mode = mode;
    m_defaultStatus = defaultStatus;
    m_defaultValue = defaultValue;

    m_calendar = calendar;
  }

  public IntervalFilter( final IntervallFilterType filter )
  {
    this( MODE.getMode( filter ), filter.getDefaultStatus(), filter.getDefaultValue(), new IntervalCalendar( filter ) );
  }

  @Override
  public void initFilter( final Object dummy, final IObservation baseObs, final URL context ) throws SensorException
  {
    m_baseobservation = baseObs;
    super.initFilter( dummy, baseObs, context );
  }

  @Override
  public ITupleModel getValues( final IRequest request ) throws SensorException
  {
    final DateRange dateRange = request == null ? null : request.getDateRange();

    final Date from = dateRange == null ? null : dateRange.getFrom();
    final Date to = dateRange == null ? null : dateRange.getTo();

    // BUGIFX: fixes the problem with the first value:
    // the first value was always ignored, because the interval
    // filter cannot handle the first value of the source observation
    // FIX: we just make the request a big bigger in order to get a new first value
    // HACK: we always use DAY, so that work fine only up to time series of DAY-quality.
    // Maybe there should be one day a mean to determine, which is the right amount.
    final ITupleModel values = ObservationUtilities.requestBuffered( m_baseobservation, dateRange, Calendar.DAY_OF_MONTH, 2 );

    return new IntervalTupleModel2( m_mode, m_calendar, m_baseobservation.getMetadataList(), values, from, to, m_defaultValue, m_defaultStatus );
  }

  @Override
  public void setValues( final ITupleModel values )
  {
    throw new UnsupportedOperationException( getClass().getName() + Messages.getString( "org.kalypso.ogc.sensor.filter.filters.IntervallFilter.2" ) ); //$NON-NLS-1$
  }
}