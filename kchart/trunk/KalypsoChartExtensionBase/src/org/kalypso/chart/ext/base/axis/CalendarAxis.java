package org.kalypso.chart.ext.base.axis;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ext.base.data.CalendarDataOperator;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;

/**
 * logical range consist of calendar values, internal range consists of long values (time in millis)
 * 
 * @author burtscher Concrete IAxis implementation - to be used for calendar data from XML documents
 */
public class CalendarAxis extends AbstractAxis<Calendar>
{
  private final CalendarDataOperator m_dataOperator;

  public CalendarAxis( String id, String label, PROPERTY prop, POSITION pos, DIRECTION dir, String formatString )
  {
    super( id, label, prop, pos, dir, Calendar.class );
    m_dataOperator = new CalendarDataOperator( getComparator(), formatString );
  }

  public double logicalToNormalized( final Calendar value )
  {
    final IDataRange<Calendar> dataRange = getLogicalRange();
    final Calendar gcTo = dataRange.getMax();
    final long toMillis = gcTo.getTimeInMillis();

    final Calendar gcFrom = dataRange.getMin();
    final long fromMillis = gcFrom.getTimeInMillis();

    final long valueMillis = value.getTimeInMillis();

    // r should not be 0 here (see AbstractAxis)
    final long r = toMillis - fromMillis;

    // Die rechte Seite muss unbedingt nach double gecastet werden, da sonst auf die Werte 0 oder 1 gerundet wird.
    final double norm = ((double) (valueMillis - fromMillis)) / r;
    // Logger.trace("Normalization: "+DateFormat.getInstance().format(value)+" => "+norm+"; from:
    return norm;
  }

  public Calendar normalizedToLogical( final double value )
  {
    final double r = getLogicalRange().getMax().getTimeInMillis() - getLogicalRange().getMin().getTimeInMillis();

    final long logical = (long) (value * r + getLogicalRange().getMin().getTimeInMillis());

    final Calendar c = Calendar.getInstance();

    c.setTime( new Date( logical ) );

    return c;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( Calendar value )
  {
    if( getRegistry() == null )
      return 0;

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return 0;

    final double norm = logicalToNormalized( value );
    final int screen = comp.normalizedToScreen( norm );
    return screen;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#screenToLogical(int)
   */
  public Calendar screenToLogical( final int value )
  {
    if( getRegistry() == null )
      return null;

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return null;

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreenInterval(T, T, double)
   */
  public Point logicalToScreenInterval( Calendar value, Calendar fixedPoint, double intervalSize )
  {
    // intervalSize wird als Millisecond-Angabe interpretiert
    final long intervalMillis = (long) intervalSize;
    final long fixedMillis = fixedPoint.getTimeInMillis();
    final long valueMillis = value.getTimeInMillis();
    /*
     * der Increment-Wert für die Schleife richtet sich nach der Richtung, in der der Wert vom fixedPoint gesehen liegt
     */
    long start = fixedMillis;
    long end = fixedMillis;
    Logger.trace( "Axis (" + getIdentifier() + "): starting IntervalCalculation" );
    if( start >= valueMillis )
    {
      while( start > valueMillis )
        start -= intervalMillis;
      end = start + intervalMillis;
    }
    else
    {
      while( end < valueMillis )
        end += intervalMillis;
      start = end - intervalMillis;
    }
    Logger.trace( "Axis (" + getIdentifier() + "): IntervalCalculation finished" );

    // Start-Zeitpunkt des Wert-Intervalls
    final Calendar startCal = Calendar.getInstance();
    startCal.setTimeInMillis( start );

    // End-Zeitpunkt des Wert-Intervalls
    final Calendar endCal = Calendar.getInstance();
    endCal.setTimeInMillis( end );

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return null;

    final int startPoint = comp.normalizedToScreen( logicalToNormalized( startCal ) );
    final int endPoint = comp.normalizedToScreen( logicalToNormalized( endCal ) );
    Logger.trace( "Axis (" + getIdentifier() + "): Using Interval: " + startPoint + ":" + endPoint );
    return new Point( startPoint, endPoint );
  }

  public int zeroToScreen( )
  {
    // ist hier nicht anwendbar
    return 0;
  }

  public IDataOperator<Calendar> getDataOperator( )
  {
    return m_dataOperator;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getNumericRange()
   */
  public IDataRange<Number> getNumericRange( )
  {
    IDataRange<Calendar> logicalRange = getLogicalRange();
    IDataRange<Number> numericRange = new ComparableDataRange<Number>( new Long[] { logicalRange.getMin().getTimeInMillis(), logicalRange.getMax().getTimeInMillis() } );
    return numericRange;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  public int numericToScreen( Number value )
  {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis( value.longValue() );
    return logicalToScreen( cal );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  public Number screenToNumeric( int value )
  {
    return screenToLogical( value ).getTimeInMillis();
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#setNumericRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  public void setNumericRange( IDataRange<Number> range )
  {
    Calendar min = Calendar.getInstance();
    min.setTimeInMillis( range.getMin().longValue() );
    Calendar max = Calendar.getInstance();
    max.setTimeInMillis( range.getMax().longValue() );

    setLogicalRange( new ComparableDataRange<Calendar>( new Calendar[] { min, max } ) );
  }
}
