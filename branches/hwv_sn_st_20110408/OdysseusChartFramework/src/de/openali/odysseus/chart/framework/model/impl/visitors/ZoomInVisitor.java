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
package de.openali.odysseus.chart.framework.model.impl.visitors;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;

/**
 * @author Dirk Kuch
 */
public class ZoomInVisitor implements IAxisVisitor
{

  private final Point m_start;

  private final Point m_end;

  public ZoomInVisitor( final Point start, final Point end )
  {
    m_start = start;
    m_end = end;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor#visit(de.openali.odysseus.chart.framework.model.mapper.IAxis)
   */
  @Override
  public void visit( final IAxis axis )
  {
    if( m_start == null || m_end == null )
      return;

    Number from = null;
    Number to = null;

    switch( axis.getPosition().getOrientation() )
    {
      case HORIZONTAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = axis.screenToNumeric( Math.min( m_start.x, m_end.x ) );
            to = axis.screenToNumeric( Math.max( m_start.x, m_end.x ) );
            break;

          case NEGATIVE:
            from = axis.screenToNumeric( Math.max( m_start.x, m_end.x ) );
            to = axis.screenToNumeric( Math.min( m_start.x, m_end.x ) );
            break;
        }
        break;

      case VERTICAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = axis.screenToNumeric( Math.max( m_start.y, m_end.y ) );
            to = axis.screenToNumeric( Math.min( m_start.y, m_end.y ) );
            break;

          case NEGATIVE:
            from = axis.screenToNumeric( Math.min( m_start.y, m_end.y ) );
            to = axis.screenToNumeric( Math.max( m_start.y, m_end.y ) );
            break;
        }
        break;
    }

    if( from != null && to != null )
    {
      axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { from, to } ) );
    }

  }

}
