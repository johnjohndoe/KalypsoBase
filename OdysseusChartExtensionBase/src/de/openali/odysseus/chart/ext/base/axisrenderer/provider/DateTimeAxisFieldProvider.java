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
package de.openali.odysseus.chart.ext.base.axisrenderer.provider;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;

import de.openali.odysseus.chart.ext.base.axisrenderer.DateTimeAxisField;
import de.openali.odysseus.chart.ext.base.axisrenderer.IDateTimeAxisField;
import de.openali.odysseus.chart.framework.model.data.IDataRange;

/**
 * @author kimwerner
 */
public class DateTimeAxisFieldProvider implements IDateTimeAxisFieldProvider
{

  private final Map<DurationFieldType, IDateTimeAxisField> m_fieldMap = new HashMap<DurationFieldType, IDateTimeAxisField>();

  public DateTimeAxisFieldProvider( )
  {
    m_fieldMap.put( DurationFieldType.millis(), new DateTimeAxisField( DateTimeFieldType.millisOfSecond(), "dd.MM\nHH:mm:ss:SSS", new int[] { 1, 10, 100, 500 }, new int[] {} ) );
    m_fieldMap.put( DurationFieldType.seconds(), new DateTimeAxisField( DateTimeFieldType.secondOfMinute(), "dd.MM\nHH:mm:ss", new int[] { 1, 15, 30 }, new int[] {} ) );
    m_fieldMap.put( DurationFieldType.minutes(), new DateTimeAxisField( DateTimeFieldType.minuteOfHour(), "dd.MM\nHH:mm:ss", new int[] { 1, 15, 30 }, new int[] {} ) );
    m_fieldMap.put( DurationFieldType.hours(), new DateTimeAxisField( DateTimeFieldType.hourOfDay(), "dd.MM\nHH:mm", new int[] { 1, 2, 4, 6, 12 }, new int[] { 12 } ) );
    m_fieldMap.put( DurationFieldType.days(), new DateTimeAxisField( DateTimeFieldType.dayOfMonth(), "YYYY.dd.MM\ndddd", new int[] { 1, 2, 7, 14 }, new int[] {7, 14, 28 } ) );
    m_fieldMap.put( DurationFieldType.months(), new DateTimeAxisField( DateTimeFieldType.monthOfYear(), "YYYY.dd.MM", new int[] { 1, 2, 3, 4, 6 }, new int[] { 6 } ) );

  }

  @Override
  public IDateTimeAxisField getDateTimeAxisField( final IDataRange<Number> range )
  {

    final long dr = range.getMax().longValue() - range.getMin().longValue();
    final long sec = 1000;
    final long min = 60 * sec;
    final long hour = 60 * min;
    final long day = 24 * hour;

    if( dr < 3 * sec )
      return m_fieldMap.get( DurationFieldType.millis() );
    else if( dr < 3 * min )
      return m_fieldMap.get( DurationFieldType.seconds() );
    else if( dr < 3 * hour )
      return m_fieldMap.get( DurationFieldType.minutes() );
     else if( dr < 7 * day )
      return m_fieldMap.get( DurationFieldType.hours() );
    else if( dr < 30 * day )
      return m_fieldMap.get( DurationFieldType.days() );
    else if( dr < 90 * day )
      return m_fieldMap.get( DurationFieldType.months() );
    else
      return new DateTimeAxisField( DateTimeFieldType.millisOfSecond(), "YYYY.dd\nMM:HH\nss.SSS", new int[] { 1 }, new int[] {} );

  }

}
