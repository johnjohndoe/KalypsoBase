/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package de.openali.odysseus.chart.framework.model.impl.visitors;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.impl.IAxisVisitorBehavior;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;

/**
 * @author Dirk Kuch
 */
public class ZoomOutVisitor implements IAxisVisitor
{
  private final Point m_start;

  private final Point m_end;

  public ZoomOutVisitor( final Point start, final Point end )
  {
    m_start = start;
    m_end = end;
  }

  @Override
  public void visit( final IAxis axis )
  {
    final IAxisVisitorBehavior visitorBehavior = axis.getAxisVisitorBehavior();
    final boolean isAllowed = visitorBehavior == null ? true : visitorBehavior.isZoomEnabled();
    if( m_start == null || m_end == null || !isAllowed )
      return;

    double from = Double.NaN;
    double to = Double.NaN;

    switch( axis.getPosition().getOrientation() )
    {
      case HORIZONTAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = axis.screenToNumeric( Math.min( m_start.x, m_end.x ) ).doubleValue();
            to = axis.screenToNumeric( Math.max( m_start.x, m_end.x ) ).doubleValue();
            break;

          case NEGATIVE:
            from = axis.screenToNumeric( Math.max( m_start.x, m_end.x ) ).doubleValue();
            to = axis.screenToNumeric( Math.min( m_start.x, m_end.x ) ).doubleValue();
            break;
        }
        break;

      case VERTICAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = axis.screenToNumeric( Math.max( m_start.y, m_end.y ) ).doubleValue();
            to = axis.screenToNumeric( Math.min( m_start.y, m_end.y ) ).doubleValue();
            break;

          case NEGATIVE:
            from = axis.screenToNumeric( Math.min( m_start.y, m_end.y ) ).doubleValue();
            to = axis.screenToNumeric( Math.max( m_start.y, m_end.y ) ).doubleValue();
            break;
        }
        break;
    }

    if( !Double.isNaN( from ) && !Double.isNaN( to ) )
    {
      final double mouserange = Math.abs( from - to );

      final IDataRange<Number> numericRange = axis.getNumericRange();

      final Number min = numericRange.getMin();
      final Number max = numericRange.getMax();

      if( min != null && max != null )
      {
        final double oldmin = min.doubleValue();
        final double oldmax = max.doubleValue();
        final double oldrange = Math.abs( oldmin - oldmax );
        final double newrange = oldrange / mouserange * oldrange;

        final double newFrom = oldmin - Math.abs( from - oldmin ) / oldrange * newrange;
        final double newTo = oldmax + Math.abs( to - oldmax ) / oldrange * newrange;

        axis.setNumericRange( DataRange.createFromComparable( (Number) new Double( newFrom ), (Number) new Double( newTo ) ) );
      }
    }
  }
}