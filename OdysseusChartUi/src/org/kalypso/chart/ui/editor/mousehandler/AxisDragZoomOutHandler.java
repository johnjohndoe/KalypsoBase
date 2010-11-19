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
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author burtscher1
 */
public class AxisDragZoomOutHandler extends AxisDragZoomHandler
{

  public AxisDragZoomOutHandler( final IChartComposite chartComposite )
  {
    super( chartComposite );
  }

  @Override
  public void performZoomAction( final int start, final int end, final IAxis[] axes )
  {
    for( final IAxis axis : axes )
    {
      double from = Double.NaN;
      double to = Double.NaN;

      switch( axis.getPosition().getOrientation() )
      {
        case HORIZONTAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToNumeric( Math.min( start, end ) ).doubleValue();
              to = axis.screenToNumeric( Math.max( start, end ) ).doubleValue();
              break;

            case NEGATIVE:
              from = axis.screenToNumeric( Math.max( start, end ) ).doubleValue();
              to = axis.screenToNumeric( Math.min( start, end ) ).doubleValue();
              break;
          }
          break;

        case VERTICAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToNumeric( Math.max( start, end ) ).doubleValue();
              to = axis.screenToNumeric( Math.min( start, end ) ).doubleValue();
              break;

            case NEGATIVE:
              from = axis.screenToNumeric( Math.min( start, end ) ).doubleValue();
              to = axis.screenToNumeric( Math.max( start, end ) ).doubleValue();
              break;
          }
          break;
      }

      if( from != Double.NaN && to != Double.NaN )
      {
        final IDataRange<Number> numericRange = axis.getNumericRange();

        final double oldmin = numericRange.getMin().doubleValue();
        final double oldmax = numericRange.getMax().doubleValue();
        final double oldrange = Math.abs( oldmin - oldmax );
        final double mouserange = Math.abs( from - to );
        final double newrange = (oldrange / mouserange) * oldrange;

        final double newFrom = oldmin - ((Math.abs( from - oldmin ) / oldrange) * newrange);
        final double newTo = oldmax + ((Math.abs( to - oldmax ) / oldrange) * newrange);

        axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { new Double( newFrom ), new Double( newTo ) } ) );
      }
    }
  }
}
