/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ogc.sensor.util;

import java.util.Calendar;
import java.util.Date;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.SensorException;

/**
 * @author Dirk Kuch
 */
public final class DateRanges
{
  private DateRanges( )
  {

  }

  /**
   * removes unnecessary seconds and milliseconds from date range
   */
  // FIXME: bad name -> normalize should make sure from < to.
  public static DateRange normalize( final DateRange dateRange )
  {
    if( Objects.isNull( dateRange ) )
      return null;

    final Date from = normalize( dateRange.getFrom() );
    final Date to = normalize( dateRange.getTo() );

    return new DateRange( from, to );
  }

  /**
   * removes unnecessary seconds and milliseconds from date
   */
  private static Date normalize( final Date date )
  {
    if( Objects.isNull( date ) )
      return null;

    final Calendar calendar = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
    calendar.setTime( date );
    calendar.set( Calendar.SECOND, 0 );
    calendar.set( Calendar.MILLISECOND, 0 );

    return calendar.getTime();
  }

  /**
   * @return common range of date range d1 and d2.
   */
  public static DateRange intersect( final DateRange d1, final DateRange d2 ) throws SensorException
  {
    if( Objects.allNull( d1, d2 ) )
      return new DateRange();
    else if( Objects.isNull( d1 ) )
      return d2;
    else if( Objects.isNull( d2 ) )
      return d1;

    if( !d1.intersects( d2 ) || !d2.intersects( d1 ) )
      throw new SensorException( Messages.getString( "DateRanges_0" ) ); //$NON-NLS-1$

    final Long from = getMax( d1.getFrom(), d2.getFrom() );
    final Long to = getMin( d1.getTo(), d2.getTo() );

    return new DateRange( new Date( from ), new Date( to ) );
  }

  /**
   * @return the minimal interval that contains both date ranges d1 and d2.
   */
  public static DateRange union( final DateRange d1, final DateRange d2 )
  {
    if( Objects.allNull( d1, d2 ) )
      return new DateRange();
    else if( Objects.isNull( d1 ) )
      return d2;
    else if( Objects.isNull( d2 ) )
      return d1;

    final Long from = getMin( d1.getFrom(), d2.getFrom() );
    final Long to = getMax( d1.getTo(), d2.getTo() );

    return new DateRange( new Date( from ), new Date( to ) );
  }

  private static Long getMin( final Date date1, final Date date2 )
  {
    if( Objects.isNull( date1, date2 ) )
      return Objects.firstNonNull( date1, date2 ).getTime();

    final long time1 = date1.getTime();
    final long time2 = date2.getTime();

    return Math.min( time1, time2 );
  }

  private static Long getMax( final Date date1, final Date date2 )
  {
    if( Objects.isNull( date1, date2 ) )
      return Objects.firstNonNull( date1, date2 ).getTime();

    final long time1 = date1.getTime();
    final long time2 = date2.getTime();

    return Math.max( time1, time2 );
  }
}
