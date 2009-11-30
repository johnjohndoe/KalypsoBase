package de.openali.odysseus.chart.ext.base.data;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;

import de.openali.odysseus.chart.factory.config.parameters.impl.CalendarParser;
import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.AbstractDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;

public class CalendarDataOperator extends AbstractDataOperator<Calendar>
{

  private final CalendarParser m_stringParser;

  private final CalendarFormat m_dateFormat;

  public CalendarDataOperator( Comparator<Calendar> comparator, String formatString )
  {
    super( comparator );
    m_dateFormat = new CalendarFormat( formatString );
    m_stringParser = new CalendarParser();
  }

  public IDataRange<Calendar> getContainingInterval( Calendar logVal, Number numIntervalWidth, Calendar logFixedPoint )
  {
    Long min = logFixedPoint.getTimeInMillis();
    Long max = logVal.getTimeInMillis();

    int dirFactor = 1;

    final Long longFixedPoint = logFixedPoint.getTimeInMillis();
    final Long longVal = logVal.getTimeInMillis();
    final Long longIntervalWidth = numIntervalWidth.longValue();

    if( longFixedPoint.compareTo( longVal ) > 0 )
    {
      dirFactor = -1;
    }
    for( long d = longFixedPoint; d > longVal; d += dirFactor * longIntervalWidth )
    {
      min = longFixedPoint + d;
    }
    max = min + dirFactor * longIntervalWidth;
    return new ComparableDataRange<Calendar>( new Calendar[] { numericToLogical( Math.min( min, max ) ), numericToLogical( Math.max( min, max ) ) } );
  }

  public Long logicalToNumeric( Calendar logVal )
  {
    if( logVal != null )
    {
      return logVal.getTimeInMillis();
    }
    return null;
  }

  public Calendar numericToLogical( Number numVal )
  {
    if( numVal == null )
    {
      return null;
    }
    else if( numVal.longValue() < 0 )
    {
      return null;
    }
    final Calendar cal = Calendar.getInstance();
    cal.set( Calendar.DST_OFFSET, 0 );
    cal.set( Calendar.ZONE_OFFSET, 0 );
    cal.setTimeInMillis( numVal.longValue() );
    return cal;
  }

  public String logicalToString( Calendar value )
  {
    final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
    System.out.println( sdf.format( value.getTime() ) );
    return sdf.format( value.getTime() );
  }

  public Calendar stringToLogical( String value ) throws MalformedValueException
  {
    if( value == null )
    {
      return null;
    }
    if( value.trim().equals( "" ) )
    {
      return null;
    }
    final Calendar cal = m_stringParser.stringToLogical( value );
    return cal;
  }

  public String getFormatHint( )
  {
    return "yyyy-MM-dd (HH:mm)";
  }

  /**
   * generates a DataRange beginning today at 0:00 and ending tomorrow 0:00
   * 
   * @see org.kalypso.chart.framework.model.data.IDataOperator#getDefaultRange()
   */
  public IDataRange<Calendar> getDefaultRange( )
  {
    Calendar today = Calendar.getInstance();
    today.set( Calendar.HOUR_OF_DAY, 0 );
    today.set( Calendar.MINUTE, 0 );
    today.set( Calendar.SECOND, 0 );
    today.set( Calendar.MILLISECOND, 0 );

    Calendar tomorrow = Calendar.getInstance();
    tomorrow.set( Calendar.HOUR_OF_DAY, 0 );
    tomorrow.set( Calendar.MINUTE, 0 );
    tomorrow.set( Calendar.SECOND, 0 );
    tomorrow.set( Calendar.MILLISECOND, 0 );
    tomorrow.add( Calendar.DAY_OF_MONTH, 1 );

    return new ComparableDataRange<Calendar>( new Calendar[] { today, tomorrow } );
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#getFormat(org.kalypso.chart.framework.model.data.IDataRange)
   */
  public Format getFormat( IDataRange<Number> range )
  {
    return m_dateFormat;
  }

}
