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
package de.openali.odysseus.chart.framework.model.data.impl;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.contribs.java.util.DateUtilities;

/**
 * @author alibu
 */
public class CalendarFormat extends Format
{
  private final SimpleDateFormat m_sdf;

  public CalendarFormat( final String formatString )
  {
    m_sdf = new SimpleDateFormat( formatString );
    m_sdf.setTimeZone( new SimpleTimeZone( 0, "" ) );
  }

  /**
   * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
   */
  @Override
  public StringBuffer format( final Object obj, final StringBuffer toAppendTo, final FieldPosition pos )
  {
    if( obj instanceof Calendar )
    {
      final Calendar cal = (Calendar) obj;
      return m_sdf.format( cal.getTimeInMillis(), toAppendTo, pos );
    }
    else if( obj instanceof Number )
    {
      return m_sdf.format( obj, toAppendTo, pos );
    }
    else if( obj instanceof XMLGregorianCalendar )
    {
      final Date date = DateUtilities.toDate( obj );
      return m_sdf.format( date, toAppendTo, pos );
    }
    return m_sdf.format( obj, toAppendTo, pos );
  }

  /**
   * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
   */
  @Override
  public Object parseObject( final String source, final ParsePosition pos )
  {
    final Date date = (Date) m_sdf.parseObject( source, pos );
    final Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis( date.getTime() );
    return cal;
  }
}
