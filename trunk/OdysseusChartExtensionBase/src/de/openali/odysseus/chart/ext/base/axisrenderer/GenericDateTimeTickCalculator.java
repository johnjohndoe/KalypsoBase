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

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.jfree.chart.axis.DateTick;
import org.joda.time.DateTimeFieldType;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author kimwerner
 */
public class GenericDateTimeTickCalculator implements ITickCalculator
{

// private final TickUnitSource m_tickUnitSource = createStandardDateTickUnits(
// KalypsoCorePlugin.getDefault().getTimeZone() );

  private DateTimeFieldType m_tickRaster = null;

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
//FIXME: alles
    m_tickRaster.getRangeDurationType();
    throw new NotImplementedException();
  }

  protected List<DateTick> refreshTicksHorizontal( final GC gc, final Rectangle plotArea, final Rectangle dataArea, final IAxis axis )
  {

// final List result = new java.util.ArrayList();
//
// final Font tickLabelFont = axis.getRenderer().getTickLabelFont();
// g2.setFont( tickLabelFont );
//
// if( isAutoTickUnitSelection() )
// {
// selectAutoTickUnit( g2, plotArea, dataArea, edge );
// }
//
// final DateTickUnit unit = getTickUnit();
// Date tickDate = calculateLowestVisibleTickValue( unit );
// final Date upperDate = getMaximumDate();
// // float lastX = Float.MIN_VALUE;
// while( tickDate.before( upperDate ) )
// {
//
// if( !isHiddenValue( tickDate.getTime() ) )
// {
// // work out the value, label and position
// String tickLabel;
// final DateFormat formatter = getDateFormatOverride();
// if( formatter != null )
// {
// tickLabel = formatter.format( tickDate );
// }
// else
// {
// tickLabel = tickUnit.dateToString( tickDate );
// }
// TextAnchor anchor = null;
// TextAnchor rotationAnchor = null;
// double angle = 0.0;
// if( isVerticalTickLabels() )
// {
// anchor = TextAnchor.CENTER_RIGHT;
// rotationAnchor = TextAnchor.CENTER_RIGHT;
// if( edge == RectangleEdge.TOP )
// {
// angle = Math.PI / 2.0;
// }
// else
// {
// angle = -Math.PI / 2.0;
// }
// }
// else
// {
// if( edge == RectangleEdge.TOP )
// {
// anchor = TextAnchor.BOTTOM_CENTER;
// rotationAnchor = TextAnchor.BOTTOM_CENTER;
// }
// else
// {
// anchor = TextAnchor.TOP_CENTER;
// rotationAnchor = TextAnchor.TOP_CENTER;
// }
// }
//
// final Tick tick = new DateTick( tickDate, tickLabel, anchor, rotationAnchor, angle );
// result.add( tick );
// tickDate = unit.addToDate( tickDate );
// }
// else
// {
// tickDate = unit.rollDate( tickDate );
// continue;
// }
//
// // could add a flag to make the following correction optional...
// switch( unit.getUnit() )
// {
//
// case (DateTickUnit.MILLISECOND):
// case (DateTickUnit.SECOND):
// case (DateTickUnit.MINUTE):
// case (DateTickUnit.HOUR):
// case (DateTickUnit.DAY):
// break;
// case (DateTickUnit.MONTH):
// tickDate = calculateDateForPosition( new Month( tickDate ), tickMarkPosition );
// break;
// case (DateTickUnit.YEAR):
// tickDate = calculateDateForPosition( new Year( tickDate ), tickMarkPosition );
// break;
//
// default:
// break;
//
// }
//
// }
// return result;
    throw new NotImplementedException();
  }
}
