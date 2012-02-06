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

import org.joda.time.DateTimeFieldType;

import de.openali.odysseus.chart.ext.base.axisrenderer.DateTimeAxisField;
import de.openali.odysseus.chart.ext.base.axisrenderer.IDateTimeAxisField;
import de.openali.odysseus.chart.framework.model.data.IDataRange;

/**
 * @author kimwerner
 */
public class DateTimeAxisFieldProvider implements IDateTimeAxisFieldProvider
{

  @Override
  public IDateTimeAxisField getDateTimeAxisField( final IDataRange<Number> range )
  {

    final long dr = range.getMax().longValue() - range.getMin().longValue();
    final long sec = 1000;
    final long min = 60 * sec;
    final long hour = 60 * min;
    final long day = 24 * hour;

    if( dr < 3 * sec )
      return new DateTimeAxisField( DateTimeFieldType.millisOfSecond(), "YYYY.dd.MM\nHH:mm:ss:SSS", new int[] { 1, 10, 100, 500 }, new int[] {} );
    else if( dr < 3 * min )
      return new DateTimeAxisField( DateTimeFieldType.secondOfMinute(), "YY.dd.MM\nHH:mm:ss", new int[] { 1, 15, 30 }, new int[] {} );
    else if( dr < 3 * hour )
      return new DateTimeAxisField( DateTimeFieldType.minuteOfHour(), "YY.dd.MM\nHH:mm:ss", new int[] { 1, 15, 30 }, new int[] {} );
    else if( dr < 3 * day )
      return new DateTimeAxisField( DateTimeFieldType.minuteOfDay(), "dd.MM\nHH:mm", new int[] { 1,15,30 }, new int[] { } );
    else if( dr < 7 * day )
      return new DateTimeAxisField( DateTimeFieldType.hourOfDay(), "dd.MM\nHH:mm", new int[] {1,2,4,6,8,12}, new int[] {12 } );
    else if( dr < 30 * day )
      return new DateTimeAxisField( DateTimeFieldType.dayOfMonth(), "YYYY.dd.MM\ndddd", new int[] { 1, 2, 7, 14 }, new int[] { 7, 14, 28 } );
    else
      return new DateTimeAxisField( DateTimeFieldType.monthOfYear(), "YYYY.dd.MM", new int[] { 1, 2, 3, 4, 6 }, new int[] { 6 } );
  }
}
