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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;
import org.joda.time.chrono.GregorianChronology;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author kimwerner
 */
public class DateTimeTickCalculator implements ITickCalculator
{

  private DateTimeFieldType m_fieldType;

  public DateTimeTickCalculator( )
  {
    m_fieldType = null;

  }

  public DateTimeTickCalculator( final DateTimeFieldType fixDateTimeFieldType )
  {
    m_fieldType = fixDateTimeFieldType;

  }

  private long getFirstRollValue( final DateTimeField field, final long start, final long end )
  {
    final long firstRoll = field.roundFloor( start );
    if( firstRoll + end - start <= start )
      // out of range, precision too small so we return without adjustment
      // maybe try roundCeil instead
      return start;
    final int fieldValue = field.get( firstRoll );
    if( fieldValue == 0 )
      return firstRoll;
    final int[] rollOvers = getRollOver( field.getDurationField().getType() );
    for( int i = 1; i < rollOvers.length; i++ )
    {
      if( fieldValue < rollOvers[i] )
      {
        if( rollOvers[i - 1] == 1 )
          return field.add( firstRoll, -fieldValue );
        else
          return field.add( firstRoll, rollOvers[i - 1] - fieldValue );
      }
    }

    return field.add( firstRoll, rollOvers[rollOvers.length - 1] - fieldValue );
  }

  private int[] getRollOver( final DurationFieldType durationFieldType )
  {
    if( durationFieldType == DurationFieldType.hours() )
      return new int[] { 1, 2, 4, 6, 12 };
    else if( durationFieldType == DurationFieldType.days() )
      return new int[] { 1, 2, 4, 7, 14 };
    else if( durationFieldType == DurationFieldType.halfdays() )
      return new int[] { 1 };
    else if( durationFieldType == DurationFieldType.minutes() || durationFieldType == DurationFieldType.seconds() )
      return new int[] { 1, 15, 30 };
    else if( durationFieldType == DurationFieldType.months() )
      return new int[] { 1, 2, 3, 4, 6 };
    else
      return new int[] { 1, 10, 100, 500 };
  }

  /**
   * @see org.kalypso.chart.ext.test.axisrenderer.ITickCalculator#calcTicks(org.eclipse.swt.graphics.GC,
   *      org.kalypso.chart.framework.model.mapper.IAxis)
   */
  /**
   * Calculates the ticks shown for the given Axis
   */
  @Override
  public Number[] calcTicks( final GC gc, final IAxis axis, final Number minDisplayInterval, final Point ticklabelSize )
  {
    final IDataRange<Number> numRange = axis.getNumericRange();
    final long start = numRange.getMin().longValue();
    final long end = numRange.getMax().longValue();

    final DateTimeFieldType fieldType = m_fieldType != null ? m_fieldType : DateTimeAxisRenderer.getFieldType( numRange );

    final DateTimeField field = fieldType.getField( GregorianChronology.getInstance() );
    final int tickCount = Math.max( 1, field.getDifference( end, start ) );
    final int maximumTickCount = axis.getScreenHeight() / (ticklabelSize.x + 2/* Pixel */);
    int rollOver = 1;
    if( tickCount > maximumTickCount )
    {
      for( final int i : getRollOver( field.getDurationField().getType() ) )
      {
        if( tickCount / i < maximumTickCount )
        {
          rollOver = i;
          break;
        }
        rollOver = i;
      }
      while( tickCount / rollOver > maximumTickCount )
      {
        rollOver = 2 * rollOver;
      }
    }
    final List<Number> ticks = new ArrayList<Number>();
    long tick = getFirstRollValue( field, start, end );
    ticks.add( tick );
    while( tick < end )
    {
      tick = field.add( tick, rollOver );
      ticks.add( tick );
    }

    return ticks.toArray( new Number[] {} );
  }

  public void setFieldType( final DateTimeFieldType fixDateTimeFieldType )
  {
    m_fieldType = fixDateTimeFieldType;
  }
}
