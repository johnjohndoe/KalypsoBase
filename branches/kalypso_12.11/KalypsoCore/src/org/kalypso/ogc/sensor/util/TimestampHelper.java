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
package org.kalypso.ogc.sensor.util;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.kalypso.core.KalypsoCorePlugin;

/**
 * This class contains functions for dealing with the timestamp.
 * 
 * @author Holger Albert
 */
public class TimestampHelper
{
  /**
   * The constructor.
   */
  private TimestampHelper( )
  {
  }

  /**
   * This function converts the timestamp text (e.g. 11:00) to a timestamp.
   * 
   * @param timestampText
   *          The timestamp text in UTC.
   * @return The timestamp in UTC.
   */
  public static LocalTime parseTimestamp( final String timestampText )
  {
    /* Nothing to do. */
    if( timestampText == null || timestampText.length() == 0 )
      return null;

    /* Create the date time formatter. */
    final DateTimeFormatter formatter = createDateTimeFormatter();

    /* REMARK: This will use the UTC timezone. */
    return formatter.parseLocalTime( timestampText );
  }

  /**
   * This function converts the timestamp to a timestamp text (e.g. 11:00).
   * 
   * @param timestamp
   *          The timestamp in UTC.
   * @return The timestamp text in UTC.
   */
  public static String toTimestampText( final LocalTime timestamp )
  {
    if( timestamp == null )
      return ""; //$NON-NLS-1$

    return timestamp.toString( "HH:mm" ); //$NON-NLS-1$
  }

  /**
   * This function converts the timestamp text (e.g. 11:00) into the kalypso timezone.
   * 
   * @param timestampText
   *          The timestamp text in UTC.
   * @return The timestamp text in the kalypso timezone.
   */
  public static String convertToKalypsoTimezone( final String timestampText )
  {
    /* Nothing to do. */
    if( timestampText == null || timestampText.length() == 0 )
      return ""; //$NON-NLS-1$

    /* Get the timestamp in UTC. */
    final LocalTime timestamp = TimestampHelper.parseTimestamp( timestampText );

    /* Convert to a date with the kalypso timezone. */
    /* The date fields are ignored. */
    final DateTime timestampUTC = timestamp.toDateTimeToday( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "UTC" ) ) ); //$NON-NLS-1$
    final DateTime timestampZone = new DateTime( timestampUTC.toDate(), DateTimeZone.forTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() ) );

    return timestampZone.toString( "HH:mm" ); //$NON-NLS-1$
  }

  /**
   * This function converts the timestamp text (e.g. 11:00) into UTC.
   * 
   * @param timestampText
   *          The timestamp text in the kalypso timezone.
   * @return The timestamp text in UTC.
   */
  public static String convertToUTC( final String timestampText )
  {
    /* Nothing to do. */
    if( timestampText == null || timestampText.length() == 0 )
      return ""; //$NON-NLS-1$

    /* Create the date time formatter. */
    final DateTimeFormatter formatter = createDateTimeFormatter();
    final DateTimeFormatter formatterZone = formatter.withZone( DateTimeZone.forTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() ) );

    /* Get the timestamp in the kalypso timezone. */
    final DateTime timestampZone = formatterZone.parseDateTime( timestampText );
    final DateTime timestampUTC = new DateTime( timestampZone.toDate(), DateTimeZone.forTimeZone( TimeZone.getTimeZone( "UTC" ) ) ); //$NON-NLS-1$

    return timestampUTC.toString( "HH:mm" ); //$NON-NLS-1$
  }

  /**
   * This function creates the date time formatter.
   * 
   * @return The date time formater.
   */
  private static DateTimeFormatter createDateTimeFormatter( )
  {
    /* Create the date time formatter builder. */
    final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendFixedDecimal( DateTimeFieldType.hourOfDay(), 2 );
    builder.appendLiteral( ':' ); // $NON-NLS-1$
    builder.appendFixedDecimal( DateTimeFieldType.minuteOfHour(), 2 );

    return builder.toFormatter();
  }
}