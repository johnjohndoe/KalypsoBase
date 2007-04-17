package de.openali.diagram.factory.configuration.parameters.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


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
public class GregorianCalendarParser implements IStringParser<GregorianCalendar>
{

  private String m_formatHint="yyyy-MM-dd HH:mm";

  /**
   * @see de.openali.diagram.factory.configuration.parameters.IStringParser#createValueFromString(java.lang.String)
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
