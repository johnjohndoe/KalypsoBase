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
import org.joda.time.DurationField;
import org.joda.time.chrono.GregorianChronology;

import de.openali.odysseus.chart.ext.base.axisrenderer.provider.DateTimeAxisFieldProvider;
import de.openali.odysseus.chart.ext.base.axisrenderer.provider.IDateTimeAxisFieldProvider;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author kimwerner
 */
public class DateTimeTickCalculator implements ITickCalculator
{

  private final IDateTimeAxisFieldProvider m_fieldTypeProvider;

  public DateTimeTickCalculator( final IDateTimeAxisFieldProvider dateTimeFieldProvider )
  {
    m_fieldTypeProvider = dateTimeFieldProvider;
  }

  public DateTimeTickCalculator( )
  {
    m_fieldTypeProvider = new DateTimeAxisFieldProvider();

  }

  private long getFirstRollValue( final IDateTimeAxisField axisField, final long start, final long end )
  {
    final DateTimeField field = axisField.getFieldType().getField( GregorianChronology.getInstance() );
    final long firstRoll = field.roundFloor( start );
    if( firstRoll + end - start <= start )
      // out of range, precision too small so we return without adjustment
      // maybe try roundCeil instead
      return start;
    final int fieldValue = field.get( firstRoll );
    if( fieldValue == 0 )
      return firstRoll;

    final int[] beginners = axisField.getBeginners();
    for( int i = 1; i < beginners.length; i++ )
    {
      if( fieldValue < beginners[i] )
      {
        return field.add( firstRoll, beginners[i - 1] - fieldValue );
      }
    }

    return field.add( firstRoll, - fieldValue );
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

    final IDateTimeAxisField axisField = m_fieldTypeProvider.getDateTimeAxisField( numRange );

    final DurationField field = axisField.getFieldType().getDurationType().getField( GregorianChronology.getInstance() );
    final int tickCount = Math.max( 1, field.getDifference( end, start ) );
    final int maximumTickCount = axis.getScreenHeight() / (ticklabelSize.x + 2/* Pixel */);
    int rollOver = 1;
    if( tickCount > maximumTickCount )
    {
      for( final int i : axisField.getRollovers() )
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
    long tick = getFirstRollValue( axisField, start, end );
    ticks.add( tick );
    while( tick < end )
    {
      tick = field.add( tick, rollOver );
      ticks.add( tick );
    }

    return ticks.toArray( new Number[] {} );
  }
}
