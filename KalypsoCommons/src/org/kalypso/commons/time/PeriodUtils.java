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
package org.kalypso.commons.time;

import java.util.Calendar;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.Period;

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
        throw new NotImplementedException();
    }
  }
}
