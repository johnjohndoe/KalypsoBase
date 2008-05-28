package org.kalypso.chart.factory.configuration.parameters.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IStringParser;

/**
 * @author alibu StringParser implementation for dates - these dates are Strings like "2006-10-23T10:00:00" Not
 *         complete, so do not use
 */
public class CalendarParser implements IStringParser<Calendar>
{

  /**
   * Format: NOW + Duration as in ISO 8601 (P[n]Y[n]M[n]DT[n]H[n]M[n]S); Groups: 1: VAR 2:DURATIONPART 3:DIRECTION 4:Y
   * 5:M 6:D 7:TIMEPART 8:H 9:M 10:S
   */
  private final String m_regexDuration = "(NOW|TODAY)(([+-])P([1-9]+[0-9]*Y)?([1-9]+[0-9]*M)?([1-9]+[0-9]*D)?(T([1-9]+[0-9]*H)?([1-9]+[0-9]*M)?([1-9]+[0-9]*S)?)?)?";

  private final String m_formatHint = "yyyy-MM-ddTHH:mm or " + m_regexDuration;

  public CalendarParser( )
  {

  }

  /**
   * @see org.kalypso.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  public Calendar stringToLogical( final String value ) throws MalformedValueException
  {
    // erst mal versuchen, ein korrektes Datum zu parsen
    final Calendar cal = Calendar.getInstance();
    // cal.set( Calendar.DST_OFFSET, 0);
    // cal.set( Calendar.ZONE_OFFSET, 0);
    cal.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

    SimpleDateFormat sdf = null;
    Date date = null;
    // TODO: hier ist es wohl sinnvoller, verschieden DateFormatStrings in einem Array zu halten und dann zu durchlaufen

    try
    {
      sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
      date = sdf.parse( value );
      // cal=Calendar.getInstance();
      if( date != null )
        cal.setTime( date );

    }
    catch( final ParseException e )
    {
      // Nochmal mit anderem String probieren
      try
      {
        sdf = new SimpleDateFormat( "yyyy-MM-dd" );
        date = sdf.parse( value );
        // cal=Calendar.getInstance();
        if( date != null )
          cal.setTime( date );
      }
      catch( final ParseException e2 )
      {
        // wir probieren mal, das über die anderer Syntax an das Datum zu kommen

        // Suche nach reulärem Ausdruck für Duration
        if( value.matches( m_regexDuration ) )
        {
          // Calendar-Objekt mit "jetzt" initialisieren
          // cal=Calendar.getInstance();

          if( value.startsWith( "TODAY" ) )
          {
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );
          }

          // DurationPart auswerten
          if( (value.startsWith( "NOW" ) && value.length() > 3) || (value.startsWith( "TODAY" ) && value.length() > 5)

          )
          {

            final Pattern pattern = Pattern.compile( m_regexDuration );
            final Matcher matcher = pattern.matcher( value );

            /*
             * hier werden die ausgelesenen Werte für einzelnen Zeiteinheiten gespeichert; enhält Paare wie zB
             * (Calendar.YEAR, "5Y") erst an späterer Stelle werden die Werte in Integers umgewandelt
             */
            final Map<Integer, String> durationMap = new HashMap<Integer, String>();

            // Matcher initiualisieren
            final boolean matches = matcher.matches();

            // Suche nach DurationDirection
            final String dir = matcher.group( 3 );

            // Suche nach Y, M, D, H, M, S
            final String year = matcher.group( 4 );
            durationMap.put( Calendar.YEAR, year );

            final String month = matcher.group( 5 );
            durationMap.put( Calendar.MONTH, month );

            final String day = matcher.group( 6 );
            durationMap.put( Calendar.DAY_OF_MONTH, day );

            final String hour = matcher.group( 8 );
            durationMap.put( Calendar.HOUR_OF_DAY, hour );

            final String min = matcher.group( 9 );
            durationMap.put( Calendar.MINUTE, min );

            final String sec = matcher.group( 10 );
            durationMap.put( Calendar.SECOND, sec );

            final Set<Integer> durationKeys = durationMap.keySet();
            for( final Integer key : durationKeys )
            {
              final String durationForUnitStr = durationMap.get( key );
              if( durationForUnitStr != null )
              {
                int durationForUnit = Integer.parseInt( durationForUnitStr.substring( 0, (durationForUnitStr.length() - 1) ) );
                // Falls die Duration negativ ist, muss der Wert negiert werden
                if( dir.equals( "-" ) )
                  durationForUnit *= -1;
                cal.add( key, durationForUnit );
              }
            }
          }
        }

        else
        {
          Logger.logError( Logger.TOPIC_LOG_GENERAL, "Unable to parse date: " + value );
          throw new MalformedValueException();
        }
      }
    }

    return cal;
  }

  public String getFormatHint( )
  {
    return m_formatHint;
  }

}
