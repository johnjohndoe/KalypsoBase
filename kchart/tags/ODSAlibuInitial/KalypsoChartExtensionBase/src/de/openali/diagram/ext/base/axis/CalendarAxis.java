package de.openali.diagram.ext.base.axis;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.graphics.Point;

import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.diagram.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.diagram.framework.model.mapper.IAxisConstants.PROPERTY;
import de.openali.diagram.framework.model.mapper.component.IAxisComponent;



/**
 * @author burtscher
 *
 * Concrete IAxis implementation - to be used for calendar data from XML documents
 *
 */
public class CalendarAxis extends AbstractAxis<Calendar>
{
  public CalendarAxis( String id, String label, PROPERTY prop, POSITION pos, DIRECTION dir )
  {
    super( id, label, prop, pos, dir, Calendar.class );
  }

  public double logicalToNormalized( final Calendar value )
  {
	IDataRange<Calendar> dataRange = getDataRange();
    Calendar gcTo = dataRange.getMax();
    long toMillis = gcTo.getTimeInMillis();

    Calendar gcFrom = dataRange.getMin();
    long fromMillis = gcFrom.getTimeInMillis();

    long valueMillis = value.getTimeInMillis();

    // r should not be 0 here (see AbstractAxis)
    final long r = toMillis - fromMillis;

    // Die rechte Seite muss unbedingt nach double gecastet werden, da sonst auf die Werte 0 oder 1 gerundet wird.
    final double norm = ((double) (valueMillis - fromMillis)) / r;
    // Logger.trace("Normalization: "+DateFormat.getInstance().format(value)+" => "+norm+"; from:
    return norm;
  }

  public Calendar normalizedToLogical( final double value )
  {
    final double r = getDataRange().getMax().getTimeInMillis() - getDataRange().getMin().getTimeInMillis();

    final long logical = (long) (value * r + getDataRange().getMin().getTimeInMillis());

    Calendar c = Calendar.getInstance();


    c.setTime( new Date( logical ) );

    return c;
  }

  /**
   * @see de.openali.diagram.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( Calendar value )
  {
    if( m_registry == null )
      return 0;


    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return 0;

    double norm = logicalToNormalized( value );
    int screen = comp.normalizedToScreen( norm );
    return screen;
  }

  /**
   * @see de.openali.diagram.framework.axis.IAxis#screenToLogical(int)
   */
  public Calendar screenToLogical( final int value )
  {
    if( m_registry == null )
      return null;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return null;

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }

  /**
   * @see de.openali.diagram.framework.axis.IAxis#logicalToScreenInterval(T, T, double)
   */
  public Point logicalToScreenInterval( Calendar value, Calendar fixedPoint, double intervalSize )
  {
    // intervalSize wird als Millisecond-Angabe interpretiert
    long intervalMillis = (long) intervalSize;
    long fixedMillis = fixedPoint.getTimeInMillis();
    long valueMillis = value.getTimeInMillis();
    /*
     * der Increment-Wert für die Schleife richtet sich nach der Richtung, in der der Wert vom fixedPoint
     *  gesehen liegt
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
    Calendar startCal = Calendar.getInstance();
    startCal.setTimeInMillis( start );

    // End-Zeitpunkt des Wert-Intervalls
    Calendar endCal = Calendar.getInstance();
    endCal.setTimeInMillis( end );

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return null;

    int startPoint = comp.normalizedToScreen( logicalToNormalized( startCal ) );
    int endPoint = comp.normalizedToScreen( logicalToNormalized( endCal ) );
    Logger.trace( "Axis (" + getIdentifier() + "): Using Interval: " + startPoint + ":" + endPoint );
    return new Point( startPoint, endPoint );
  }


public int zeroToScreen()
{
	//ist hier nicht anwendbar
	return 0;
}



}
