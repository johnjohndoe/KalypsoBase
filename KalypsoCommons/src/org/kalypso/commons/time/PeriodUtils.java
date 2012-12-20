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
package org.kalypso.commons.time;

import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.kalypso.contribs.java.util.CalendarUtilities.FIELD;

/**
 * @author Gernot Belger
 */
public final class PeriodUtils
{
  private PeriodUtils( )
  {
    throw new UnsupportedOperationException();
  }

  public static Period getPeriod( final int calendarField, final int amount )
  {
    switch( calendarField )
    {
      case Calendar.YEAR:
        return Period.years( amount );

      case Calendar.MONTH:
        return Period.months( amount );

      case Calendar.WEEK_OF_YEAR:
      case Calendar.WEEK_OF_MONTH:
        return Period.weeks( amount );

      case Calendar.DAY_OF_MONTH:
      case Calendar.DAY_OF_YEAR:
      case Calendar.DAY_OF_WEEK:
      case Calendar.DAY_OF_WEEK_IN_MONTH:
        return Period.days( amount );

      case Calendar.HOUR:
      case Calendar.HOUR_OF_DAY:
        return Period.hours( amount );

      case Calendar.MINUTE:
        return Period.minutes( amount );

      case Calendar.SECOND:
        return Period.seconds( amount );

      case Calendar.MILLISECOND:
        return Period.millis( amount );

      case Calendar.AM_PM:
      case Calendar.ERA:
      default:
        throw new UnsupportedOperationException();
    }
  }

  /**
   * Formats a {@link Period} with a default format, using the current default locale.
   */
  public static String formatDefault( final Period period )
  {
    if( period == null )
      return StringUtils.EMPTY;

    final PeriodFormatter formatter = PeriodFormat.wordBased( Locale.getDefault() );
    return formatter.print( period );
  }

  private static int countNonZeroFields( final Period period )
  {
    int fieldCount = 0;

    final int[] values = period.getValues();
    for( final int value : values )
    {
      if( value != 0 )
        fieldCount++;
    }

    return fieldCount;
  }

  /**
   * @return {@link Integer#MAX_VALUE} if the amount could not be determined.
   */
  public static int findCalendarAmount( final Period period )
  {
    final int fieldCount = countNonZeroFields( period );
    if( fieldCount > 1 )
      throw new IllegalArgumentException( "Unable to find calendar amount for periods with more than one field: " + period ); //$NON-NLS-1$

    if( period.getDays() != 0 )
      return period.getDays();

    if( period.getHours() != 0 )
      return period.getHours();

    if( period.getMillis() != 0 )
      return period.getMillis();

    if( period.getMinutes() != 0 )
      return period.getMinutes();

    if( period.getMonths() != 0 )
      return period.getMonths();

    if( period.getSeconds() != 0 )
      return period.getSeconds();

    if( period.getWeeks() != 0 )
      return period.getWeeks();

    if( period.getYears() != 0 )
      return period.getYears();

    return Integer.MAX_VALUE;
  }

  public static FIELD findCalendarField( final Period period )
  {
    final int fieldCount = countNonZeroFields( period );

    if( fieldCount > 1 )
      throw new IllegalArgumentException( "Unable to find calendar field for periods with more than one field: " + period ); //$NON-NLS-1$

    if( period.getDays() != 0 )
      return FIELD.DAY_OF_MONTH;

    if( period.getHours() != 0 )
      return FIELD.HOUR_OF_DAY;

    if( period.getMillis() != 0 )
      return FIELD.MILLISECOND;

    if( period.getMinutes() != 0 )
      return FIELD.MINUTE;

    if( period.getMonths() != 0 )
      return FIELD.MONTH;

    if( period.getSeconds() != 0 )
      return FIELD.SECOND;

    if( period.getWeeks() != 0 )
      return FIELD.WEEK_OF_YEAR;

    if( period.getYears() != 0 )
      return FIELD.YEAR;

    return null;
  }
}
