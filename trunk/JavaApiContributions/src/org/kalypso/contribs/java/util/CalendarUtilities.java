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
package org.kalypso.contribs.java.util;

import java.util.Calendar;

/**
 * CalendarUtilities
 * 
 * @author schlienger
 */
public final class CalendarUtilities
{
  public enum FIELD
  {
    DATE(Calendar.DATE, "Tag", "Tage"),
    DAY_OF_MONTH(Calendar.DAY_OF_MONTH, "Tag", "Tage"),
    DAY_OF_WEEK(Calendar.DAY_OF_WEEK, "Tag", "Tage"),
    DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH, "Tag", "Tage"),
    DAY_OF_YEAR(Calendar.DAY_OF_YEAR, "Tag", "Tage"),
    ERA(Calendar.ERA, "Ära", "Äras"),
    HOUR(Calendar.HOUR, "Std", "Stunden"),
    HOUR_OF_DAY(Calendar.HOUR_OF_DAY, "Std", "Stunden"),
    MILLISECOND(Calendar.MILLISECOND, "Millis", "Millisekunden"),
    MINUTE(Calendar.MINUTE, "Min", "Minuten"),
    MONTH(Calendar.MONTH, "Mon", "Monate"),
    SECOND(Calendar.SECOND, "Sek", "Sekunden"),
    WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH, "W", "Wochen"),
    WEEK_OF_YEAR(Calendar.WEEK_OF_YEAR, "W", "Wochen"),
    YEAR(Calendar.YEAR, "J", "Jahre"),
    ZONE_OFFSET(Calendar.ZONE_OFFSET, "ZONE_OFFSET", "");

    private final int m_field;

    private final String m_abbreviation;

    /* This label may be used for example for combo boxing choosing the add-field. */
    private final String m_addLabel;

    private FIELD( final int field, final String abbreviation, final String addLabel )
    {
      m_field = field;
      m_abbreviation = abbreviation;
      m_addLabel = addLabel;
    }

    public int getField( )
    {
      return m_field;
    }

    public String getAbbreviation( )
    {
      return m_abbreviation;
    }

    public String getAddLabel( )
    {
      return m_addLabel;
    }
  }

  /** do not instanciate */
  private CalendarUtilities( )
  {
    // no instanciation
  }

  /**
   * Helper method that returns the calendar field according to its name.
   * <p>
   * Example: the name "DAY_OF_MONTH" will return Calendar.DAY_OF_MONTH
   * <p>
   * The comparison is not case sensitive.
   * <p>
   * As last resort, if the fieldName does not represent a the literal name of a calendar field, it is assumed to be an
   * integer representing its java-internal value. Finally this value is returned.
   * 
   * @param fieldName
   * @return Calendar.*
   * @see java.util.Calendar
   */
  public static int getCalendarField( final String fieldName )
  {
    try
    {
      return FIELD.valueOf( fieldName ).getField();
    }
    catch( final Throwable t )
    {
// last we assume that it is allready an integer
      return Integer.parseInt( fieldName );
    }
  }

  /**
   * Important note: this method is not localized and is designed to be used in a german environment.
   * 
   * @return the string abbreviation of the given field. For instance if field is the java internal value HOUR_OF_DAY,
   *         'h' is returned
   */
  public static String getAbbreviation( final int fieldValue )
  {
    final FIELD field = fieldForField( fieldValue );
    return field.getAbbreviation();
  }

  private static FIELD fieldForField( final int fieldValue )
  {
    switch( fieldValue )
    {
// case Calendar.DATE:
// return FIELDS.DATE;
      case Calendar.DAY_OF_MONTH:
        return FIELD.DAY_OF_MONTH;
      case Calendar.DAY_OF_WEEK:
        return FIELD.DAY_OF_WEEK;
      case Calendar.DAY_OF_WEEK_IN_MONTH:
        return FIELD.DAY_OF_WEEK_IN_MONTH;
      case Calendar.DAY_OF_YEAR:
        return FIELD.DAY_OF_YEAR;
      case Calendar.ERA:
        return FIELD.ERA;
      case Calendar.HOUR:
        return FIELD.HOUR;
      case Calendar.HOUR_OF_DAY:
        return FIELD.HOUR_OF_DAY;
      case Calendar.MILLISECOND:
        return FIELD.MILLISECOND;
      case Calendar.MINUTE:
        return FIELD.MINUTE;
      case Calendar.MONTH:
        return FIELD.MONTH;
      case Calendar.SECOND:
        return FIELD.SECOND;
      case Calendar.WEEK_OF_MONTH:
        return FIELD.WEEK_OF_MONTH;
      case Calendar.WEEK_OF_YEAR:
        return FIELD.WEEK_OF_YEAR;
      case Calendar.YEAR:
        return FIELD.YEAR;
      case Calendar.ZONE_OFFSET:
        return FIELD.ZONE_OFFSET;

      default:
        throw new IllegalArgumentException( "Unknown field: " + fieldValue );
    }
  }
}
