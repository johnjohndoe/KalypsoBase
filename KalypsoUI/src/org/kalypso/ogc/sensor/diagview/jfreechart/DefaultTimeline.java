/* Copied from package org.jfree.chart.axis.DateAxis because its not visible there...
 *
 */
package org.kalypso.ogc.sensor.diagview.jfreechart;

import java.io.Serializable;
import java.util.Date;

import org.jfree.chart.axis.Timeline;

/**
 * A timeline that includes all milliseconds (as defined by java.util.Date) in the real time line.
 */
public class DefaultTimeline implements Timeline, Serializable
{
  /**
   * Converts a millisecond into a timeline value.
   * 
   * @param millisecond
   *          the millisecond.
   * @return The timeline value.
   */
  @Override
  public long toTimelineValue( final long millisecond )
  {
    return millisecond;
  }

  /**
   * Converts a date into a timeline value.
   * 
   * @param date
   *          the domain value.
   * @return The timeline value.
   */
  @Override
  public long toTimelineValue( final Date date )
  {
    return date.getTime();
  }

  /**
   * Converts a timeline value into a millisecond (as encoded by java.util.Date).
   * 
   * @param value
   *          the value.
   * @return The millisecond.
   */
  @Override
  public long toMillisecond( final long value )
  {
    return value;
  }

  /**
   * Returns <code>true</code> if the timeline includes the specified domain value.
   * 
   * @param millisecond
   *          the millisecond.
   * @return <code>true</code>.
   */
  @Override
  public boolean containsDomainValue( final long millisecond )
  {
    return true;
  }

  /**
   * Returns <code>true</code> if the timeline includes the specified domain value.
   * 
   * @param date
   *          the date.
   * @return <code>true</code>.
   */
  @Override
  public boolean containsDomainValue( final Date date )
  {
    return true;
  }

  /**
   * Returns <code>true</code> if the timeline includes the specified domain value range.
   * 
   * @param from
   *          the start value.
   * @param to
   *          the end value.
   * @return <code>true</code>.
   */
  @Override
  public boolean containsDomainRange( final long from, final long to )
  {
    return true;
  }

  /**
   * Returns <code>true</code> if the timeline includes the specified domain value range.
   * 
   * @param from
   *          the start date.
   * @param to
   *          the end date.
   * @return <code>true</code>.
   */
  @Override
  public boolean containsDomainRange( final Date from, final Date to )
  {
    return true;
  }

  @Override
  public int hashCode( )
  {
    // REMARK: implemented so that if fits to the euqals implementation;
    // however it is not clear, if equals makes sense at all.
    return DefaultTimeline.class.hashCode();
  }

  /**
   * Tests an object for equality with this instance.
   * 
   * @param object
   *          the object.
   * @return A boolean.
   */
  @Override
  public boolean equals( final Object object )
  {
    if( object == null )
    {
      return false;
    }

    if( object == this )
    {
      return true;
    }

    if( object instanceof DefaultTimeline )
    {
      return true;
    }

    return false;
  }
}