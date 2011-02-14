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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.chrono.GregorianChronology;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author kimwerner
 */
public class GenericDateTimeTickCalculator implements ITickCalculator
{

  private final DateTimeFieldType m_tickRaster;

  public GenericDateTimeTickCalculator()
  {
    m_tickRaster = null;

  }
  public GenericDateTimeTickCalculator( final DateTimeFieldType tickRaster )
  {
    m_tickRaster = tickRaster;

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

    final DateTimeField field = getTickRaster( numRange ).getField( GregorianChronology.getInstance() );
    final int tickCount = field.getDifference( end, start );
    final int maximumTickCount = axis.getScreenHeight()/ticklabelSize.x;
    final int divisor = Math.max( 1,tickCount/maximumTickCount);
    final Number[] ticks = new Number[tickCount/divisor];
    ticks[0] = field.roundCeiling( start );

    for( int i = 1; i < tickCount/divisor; i++ )
    {
      ticks[i] = field.add( ticks[i - 1].longValue(), divisor );
    }
    return ticks;
  }
  private  DateTimeFieldType calculateTickRaster(final IDataRange<Number> range )
  {
    //TODO: calculate 
    return m_tickRaster;
  }
  public DateTimeFieldType getTickRaster(final IDataRange<Number> range )
  {
    if(m_tickRaster==null)
      return calculateTickRaster(range);
    return m_tickRaster;
  }
}
