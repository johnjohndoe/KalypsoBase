package de.openali.diagram.factory.configuration.parameters.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import de.openali.diagram.factory.configuration.exception.MalformedValueException;
import de.openali.diagram.factory.configuration.parameters.IStringParser;
import de.openali.diagram.framework.logging.Logger;

/**
 * @author alibu
 *
 * StringParser implementation for dates - these dates are Strings like "2006-10-23T10:00:00"
 * where almost any "part" (that is years, moths, days, hour, minutes) can be coded to a dynamic value
 * using terms like "(THISYEAR-5)"
 *
 * Not complete, so do not use
 *
 */
public class CalendarParser implements IStringParser<Calendar>
{

  private String m_formatHint="yyyy-MM-dd HH:mm";

  public CalendarParser()
  {

  }

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
   */
  public Calendar createValueFromString( String value ) throws MalformedValueException
  {
    //erst mal versuchen, ein korrektes Datum zu parsen
    Calendar c=Calendar.getInstance();
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
        throw new MalformedValueException();
      }
    }

    if (date!=null)
      c.setTime( date );
    c.set( Calendar.DST_OFFSET, 0);
    c.set( Calendar.ZONE_OFFSET, 0);

    Logger.logInfo(Logger.TOPIC_LOG_GENERAL, "CalendarParser Offset: "+ (c.getTimeInMillis() % (1000*60*60*24)));
    return c;
  }

  public String getFormatHint()
  {
    return m_formatHint;
  }

}
