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
package org.kalypso.swtchart.configuration.parameters.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.kalypso.swtchart.configuration.parameters.IStringParser;
import org.kalypso.swtchart.logging.Logger;

/**
 * @author burtscher1
 *
 * StringParser implementation for dates - these dates are Strings like "2006-10-23T10:00:00"
 * where almost any "part" (that is years, moths, days, hour, minutes) can be coded to a dynamic value
 * using terms like "(THISYEAR-5)"
 *
 * Not complete, so do not use
 *
 */
public class GregorianCalendarParser implements IStringParser<GregorianCalendar>
{

  private String m_formatHint="yyyy-MM-dd HH:mm";

  /**
   * @see org.kalypso.swtchart.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public GregorianCalendar createValueFromString( String value )
  {
    //erst mal versuchen, ein korrektes Datum zu parsen
    GregorianCalendar gc=new GregorianCalendar();
    SimpleDateFormat sdf=null;
    Date date=null;
    try
    {
      sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
      date=sdf.parse( value );
    }
    catch( ParseException e )
    {
      //Nochmal mit anderem String probieren
      try
      {
        sdf=new SimpleDateFormat("yyyy-MM-dd");
        date=sdf.parse( value );
      }
      catch( ParseException e2 )
      {
        //TODO: nichts machen - wir probieren mal, das über die anderer Syntax an das Datum zu kommen
        Logger.logError(Logger.TOPIC_LOG_GENERAL, "Unable to parse date: "+value);
      }
    }

    if (date!=null)
      gc.setTime( date );


    return gc;
  }

  public String getFormatHint()
  {
    return m_formatHint;
  }

}
