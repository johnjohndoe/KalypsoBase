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
package org.kalypso.chart.ui.editor.mousehandler;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author burtscher1
 */
public class AxisDragZoomOutHandler extends AxisDragZoomHandler
{

  public AxisDragZoomOutHandler( ChartComposite chartComposite )
  {
    super( chartComposite );
  }

  @Override
  public void performZoomAction( IAxis axis )
  {
    double from = Double.NaN;
    double to = Double.NaN;

    switch( axis.getPosition().getOrientation() )
    {
      case HORIZONTAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = axis.screenToNumeric( Math.min( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            to = axis.screenToNumeric( Math.max( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            break;

          case NEGATIVE:
            from = axis.screenToNumeric( Math.max( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            to = axis.screenToNumeric( Math.min( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            break;
        }
        break;

      case VERTICAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = axis.screenToNumeric( Math.max( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            to = axis.screenToNumeric( Math.min( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            break;

          case NEGATIVE:
            from = axis.screenToNumeric( Math.min( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            to = axis.screenToNumeric( Math.max( m_mouseDragStart, m_mouseDragEnd ) ).doubleValue();
            break;
        }
        break;
    }

    if( from != Double.NaN && to != Double.NaN )
    {
      IDataRange<Number> numericRange = axis.getNumericRange();

      double oldmin = numericRange.getMin().doubleValue();
      double oldmax = numericRange.getMax().doubleValue();
      double oldrange = Math.abs( oldmin - oldmax );
      double mouserange = Math.abs( from - to );
      double newrange = (oldrange / mouserange) * oldrange;

      double newFrom = oldmin - ((Math.abs( from - oldmin ) / oldrange) * newrange);
      double newTo = oldmax + ((Math.abs( to - oldmax ) / oldrange) * newrange);

      axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { new Double( newFrom ), new Double( newTo ) } ) );
    }
  }

}
