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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Date utilities.
 * 
 * @author schlienger
 */
public final class DateUtilities
{
  private static DatatypeFactory DATATYPE_FACTORY;
  static
  {
    try
    {
      DATATYPE_FACTORY = DatatypeFactory.newInstance();
    }
    catch( final DatatypeConfigurationException e )
    {
      e.printStackTrace();
    }
  }

  private DateUtilities( )
  {
    // not intended to be instantiated
  }


  /**
   * @return the minimum Date that the Calendar can deliver.
   */
  public final static Date getMinimum( )
  {
    final Calendar cal = Calendar.getInstance();

    final int yearMin = cal.getMinimum( Calendar.YEAR );
    final int monthMin = cal.getMinimum( Calendar.MONTH );
    final int dayMin = cal.getMinimum( Calendar.DAY_OF_MONTH );
    final int hourMin = cal.getMinimum( Calendar.HOUR_OF_DAY );
    final int minMin = cal.getMinimum( Calendar.MINUTE );
    final int secMin = cal.getMinimum( Calendar.SECOND );

    cal.clear();
    cal.set( yearMin, monthMin, dayMin, hourMin, minMin, secMin );

    return cal.getTime();
  }

  public static Date toDate( final XMLGregorianCalendar xmlGregorianCalendar )
  {
    if( xmlGregorianCalendar == null )
      return null;

    final GregorianCalendar greg = xmlGregorianCalendar.toGregorianCalendar();
    return greg.getTime();
  }

  /**
   * Converts a date into a {@link XMLGregorianCalendar}. <br>
   * Important: it is assumed, that all dates in memory are in UTC timezone. Never do anything else!
   */
  public static XMLGregorianCalendar toXMLGregorianCalendar( final Date date )
  {
    if( date == null )
      return null;

    final TimeZone timeZone = TimeZone.getTimeZone( "UTC" );
    final GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance( timeZone );
    calendar.setTime( date );

    return DATATYPE_FACTORY.newXMLGregorianCalendar( calendar );
  }

  public static Object toXMLGregorianCalendar( final GregorianCalendar dateTime )
  {
    return DATATYPE_FACTORY.newXMLGregorianCalendar( dateTime );
  }

  public static Date parseDateTime( final String lexicalXSDDateTime )
  {
    final Calendar dateTime = DatatypeConverter.parseDateTime( lexicalXSDDateTime );
    return dateTime.getTime();
  }

  /** Prints a date as xs:dateTime using {@link DatatypeConverter#printDateTime(Calendar)}. */
  public static String printDateTime( final Date date, final TimeZone tz )
  {
    final Calendar cal = Calendar.getInstance( tz );
    cal.setTime( date );
    return DatatypeConverter.printDateTime( cal );
  }

  public static long getDifferenceInMinutes( final Calendar c1, final Calendar c2 )
  {
    long difference = (c1.getTimeInMillis() - c2.getTimeInMillis());
    return difference / 1000 / 60;
  }

  public static double getDifferenceInHours( final Calendar c1, final Calendar c2 )
  {
    return getDifferenceInMinutes( c1, c2 ) / 60.0;
  }

  public static double getDifferenceInDays( final Calendar c1, final Calendar c2 )
  {
    return getDifferenceInHours( c1, c2 ) / 24.0;
  }


  /**
   * Supports {@link Date}, {@link Calendar}, {@link XMLGregorianCalendar}.
   */
  public static Date toDate( Object protoDate )
  {
    if( protoDate instanceof Date )
      return (Date) protoDate;
    
    if( protoDate instanceof Calendar )
      return ((Calendar) protoDate).getTime();
    
    if( protoDate instanceof XMLGregorianCalendar )
      return toDate( (XMLGregorianCalendar)protoDate );
    
    return null;
  }


  public static boolean isLeapYear( Calendar calendar )
  {
    boolean lBoolRes = false;
    if( calendar != null && calendar.isSet( Calendar.YEAR ) ){
      int lIntYear = calendar.get( Calendar.YEAR );
      lBoolRes = ( (lIntYear % 4 == 0 && lIntYear % 100 != 0) || (lIntYear % 400 == 0) ) ? true : false;
    }
    return lBoolRes;
  }
}
