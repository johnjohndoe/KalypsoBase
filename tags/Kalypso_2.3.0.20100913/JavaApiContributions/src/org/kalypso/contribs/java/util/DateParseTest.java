/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.contribs.java.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

/**
 * @author Gernot Belger
 */
public class DateParseTest
{
  @Test
  public void parseDates() throws ParseException
  {
    final SimpleDateFormat DF = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );

    final String midnight2000 = "01.01.2000 00:00";

    final Date utcDate = DF.parse( midnight2000 );
    final Calendar utcCal = Calendar.getInstance();
    utcCal.setTime( utcDate );
    utcCal.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

    final Date utc1Date = DF.parse( midnight2000 );
    final Calendar utc1Cal = Calendar.getInstance();
    utc1Cal.setTime( utc1Date );
    utc1Cal.setTimeZone( TimeZone.getTimeZone( "UTC+1" ) );

//    final long utcMillis = utcCal.getTimeInMillis();
//    final long utc1Millis = utc1Cal.getTimeInMillis();

    // / ?????????

  }

}
