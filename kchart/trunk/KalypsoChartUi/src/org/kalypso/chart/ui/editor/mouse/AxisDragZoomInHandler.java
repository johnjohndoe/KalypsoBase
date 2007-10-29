/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.chart.ui.editor.mouse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.view.AxisCanvas;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.framework.view.IChartDragHandler;

/**
 * @author Gernot Belger
 * @author burtscher1
 */
public class AxisDragZoomInHandler implements IChartDragHandler
{
  private int m_mouseDragStart = -1;

  private int m_mouseDragEnd = -1;

  private final ChartComposite m_chartComposite;

  int m_pressedKey = -1;

  private final AxisCanvas m_axisCanvas;

  private IAxis< ? > m_axis;

  private final ORIENTATION m_orientation;

  public AxisDragZoomInHandler( final ChartComposite chartComposite, final AxisCanvas axisCanvas )
  {
    m_chartComposite = chartComposite;
    m_axisCanvas = axisCanvas;

    // Achse rausfinden
    IMapperRegistry reg = m_chartComposite.getModel().getMapperRegistry();
    IAxis< ? >[] axes = reg.getAxes();
    for( IAxis< ? > axis : axes )
    {
      if( reg.getComponent( axis ) == m_axisCanvas )
      {
        m_axis = axis;
        break;
      }
    }

    m_orientation = m_axis.getPosition().getOrientation();
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( MouseEvent e )
  {

  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    m_mouseDragStart = getPos( e );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( m_mouseDragStart == -1 )
      return;
    m_mouseDragEnd = getPos( e );

    if( Math.abs( m_mouseDragStart - m_mouseDragEnd ) > 5 )
    {
      // Achsenwerte setzen
      IDataRange<Number> numericRange = m_axis.getNumericRange();
      Number num1 = m_axis.screenToNumeric( m_mouseDragStart );
      Number num2 = m_axis.screenToNumeric( m_mouseDragEnd );
      m_axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { num1, num2 } ) );
    }
    // zurücksetzen
    m_mouseDragStart = -1;
    m_mouseDragEnd = -1;
    m_axisCanvas.setDragInterval( -1, -1 );
    m_chartComposite.getPlot().setDragArea( null );

  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_mouseDragStart != -1 )
    {
      m_mouseDragEnd = getPos( e );
      // zoom-Rechteck
      // m_axisCanvas.setDragInterval( m_mouseDragStart, m_mouseDragEnd );
      Rectangle dragArea;
      Rectangle plotBounds = m_chartComposite.getPlot().getBounds();
      if( m_orientation == ORIENTATION.HORIZONTAL )
        dragArea = new Rectangle( Math.min( m_mouseDragStart, m_mouseDragEnd ), 0, Math.abs( m_mouseDragStart - m_mouseDragEnd ), plotBounds.height );
      else
        dragArea = new Rectangle( 0, Math.min( m_mouseDragStart, m_mouseDragEnd ), plotBounds.width, Math.abs( m_mouseDragStart - m_mouseDragEnd ) );
      m_chartComposite.getPlot().setDragArea( dragArea );
    }
  }

  private int getPos( MouseEvent e )
  {
    if( m_orientation == ORIENTATION.HORIZONTAL )
      return e.x;
    else
      return e.y;
  }

  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  public Cursor getCursor( )
  {
    return Display.getDefault().getSystemCursor( SWT.CURSOR_CROSS );
  }

}
