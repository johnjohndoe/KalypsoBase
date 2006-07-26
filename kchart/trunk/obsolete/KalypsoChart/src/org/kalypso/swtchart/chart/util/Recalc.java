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
package org.kalypso.swtchart.chart.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import sun.util.calendar.Gregorian;

/**
 * @author alibu
 *
 */
public class Recalc
{
  
  /**
   * rechnet ein XMLDatum in Millisekunden um
   */
  public static long XMLGregToMillis(XMLGregorianCalendar xmlcal)
  {
    System.out.println("Übergebenes Datum: "+xmlcal.getYear()+" "+xmlcal.getMonth()+" "+xmlcal.getDay()+" "+xmlcal.getHour()+" "+xmlcal.getMinute());
    //Monate im GregorianCalender laufen von 0-11, daher muss dekrementiert werden
    int month=xmlcal.getMonth()-1;
    //getHour und getMinute geben null (oder Long.MIN_VALUE) zurück, wenn sie nicht gesetzt sind - daher mit Math.max aus 0 setzen
    int hour=Math.max(xmlcal.getHour(),0);
    int minute=Math.max(xmlcal.getMinute(),0);
    GregorianCalendar gc= new GregorianCalendar(xmlcal.getYear(), month, xmlcal.getDay(), hour, minute);
    long millis=gc.getTimeInMillis();
    return millis;
  }
  
  
  /**
   * erzeugt ein GregorianCalendar-Objekt aus einem XMLGregorianCalendar
   */
  public static XMLGregorianCalendar gregCalToXMLGregCal(GregorianCalendar gc)
  {
    XMLGregorianCalendarImpl xgc=new XMLGregorianCalendarImpl();
    //Zeitzone setzen
    int tzOffset=(gc.getTimeZone().getRawOffset())/(1000*60) ;
    xgc.setTimezone(tzOffset);
    xgc.setYear(gc.get(GregorianCalendar.YEAR));
    //GregCal-Monate laufen von 0 - 11
    xgc.setMonth(gc.get(GregorianCalendar.MONTH)+1);
    xgc.setDay(gc.get(GregorianCalendar.DAY_OF_MONTH));
    //INFO:  hier muss HOUR_OF_DAY verwendet werden, weil HOUR nur eine Zahl zwischen 1 und 12 zurückgibt 
    xgc.setHour(gc.get(GregorianCalendar.HOUR_OF_DAY));
    xgc.setMinute(gc.get(GregorianCalendar.MINUTE));
    xgc.setSecond(gc.get(GregorianCalendar.SECOND));
    xgc.setMillisecond(gc.get(GregorianCalendar.MILLISECOND));
    return xgc;
  }
  
  
  /**
   * erzeugt ein GregorianCalendar-Objekt anhand einer Millisekunden-Zeit
   */
  public static GregorianCalendar gregCalFromTimeMillis(long tm)
  {
    GregorianCalendar gc=new GregorianCalendar();
    gc.setTime(new Date(tm));
    return gc;
  }
  
  
  
  /**
   * Wandelt einen HexString in eine Dezimalzahl um
   * @param strHex die Zahl als Hexadezimal-String
   * @return die Zahl als Integer
   * 
   * 
   */
  public static int hexToInt(String strHex)
  {
    String str;
    String hexVals = "0123456789ABCDEF";
    int i, val, n;

    val = 0;
    n   = 1;
    str = strHex.toUpperCase();

    for(i=str.length()-1; i>=0; i--, n*=16)
    {
      val+= n * hexVals.indexOf(str.charAt(i)); 
    }

    return val;
  }

}
