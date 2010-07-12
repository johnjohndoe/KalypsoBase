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
package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author kimwerner
 */
public class AxisDragPanHandler extends AbstractAxisDragHandler
{

  public AxisDragPanHandler( ChartComposite chartComposite )
  {
    super( chartComposite );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractAxisDragHandler#doMouseMoveAction(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point, de.openali.odysseus.chart.framework.model.mapper.IAxis[])
   */
  @Override
  void doMouseMoveAction( Point start, Point end, IAxis[] axes )
  {
    if( axes == null || axes.length == 0 )
      return;

    getChartComposite().setAxisPanOffset( start, end, axes );
    if( axes[0].getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      getChartComposite().setPlotPanOffset(axes, new Point( end.x, 0 ), new Point( start.x, 0 ) );
    else
      getChartComposite().setPlotPanOffset(axes, new Point( 0, end.y ), new Point( 0, start.y ) );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractAxisDragHandler#doMouseUpAction(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point, de.openali.odysseus.chart.framework.model.mapper.IAxis[])
   */
  @Override
  void doMouseUpAction( Point start, Point end, IAxis[] axes )
  {
    getChartComposite().clearPanOffset();
    if( axes.length > 0 )
    {
      final int startI;
      final int endI;
      if( axes[0].getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
      {
        startI = start.x;
        endI = end.x;
      }
      else
      {
        startI = start.y;
        endI = end.y;
      }
      for( final IAxis axis : axes )
        panAxis( startI, endI, axis );
    }
  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  @Override
  public Cursor getCursor( final MouseEvent e )
  {
    return e.display.getSystemCursor( SWT.CURSOR_SIZEALL );
  }

  private void panAxis( int startPos, int endPos, IAxis axis )
  {
    Number startNum = axis.screenToNumeric( startPos );
    Number endNum = axis.screenToNumeric( endPos );
    double diff = startNum.doubleValue() - endNum.doubleValue();
    if( Double.isNaN( diff ) )
      return;
    IDataRange<Number> oldRange = axis.getNumericRange();
    Number newMin = oldRange.getMin().doubleValue() + diff;
    Number newMax = oldRange.getMax().doubleValue() + diff;
    axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { newMin, newMax } ) );
  }

}
