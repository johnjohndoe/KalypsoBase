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
package org.kalypso.chart.ui.editor.mousehandler;

import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.view.AxisCanvas;
import org.kalypso.chart.framework.view.ChartComposite;

/**
 * @author burtscher1
 * 
 */
public class AxisDragZoomInHandler extends AbstractAxisDragHandler
{

  public AxisDragZoomInHandler( ChartComposite chartComposite )
  {
    super( chartComposite );
  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  public Cursor getCursor( )
  {
    return Display.getDefault().getSystemCursor( SWT.CURSOR_CROSS );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( MouseEvent e )
  {
    if( m_mouseDragStart == -1 )
      return;
    m_mouseDragEnd = getPos( e );

    // nur anwenden, wenn die Maus mind. 5 Pixel bewegt wurde
    if( Math.abs( m_mouseDragEnd - m_mouseDragStart ) > 5 )
    {

      // Zoom-Anzeige in AxisCanvas
      final AxisCanvas curAc = (AxisCanvas) e.getSource();
      final IAxis curAxis = m_axes.get( curAc );
      final ORIENTATION ori = curAxis.getPosition().getOrientation();

      if( m_applyOnAllAxes )
      {
        for( final Entry<AxisCanvas, IAxis> entry : m_axes.entrySet() )
        {
          IAxis axis = entry.getValue();
          if( axis.getPosition().getOrientation().equals( ori ) )
          {
            performZoomAction( axis );
          }
        }
      }
      else
      {
        performZoomAction( curAxis );
      }
    }

    // zurücksetzen
    m_mouseDragStart = -1;
    m_mouseDragEnd = -1;
    m_chartComposite.getPlot().setDragArea( null );

  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( MouseEvent e )
  {

    if( m_mouseDragStart != -1 )
    {
      m_mouseDragEnd = getPos( e );
      // Zoom-Anzeige in AxisCanvas
      final AxisCanvas curAc = (AxisCanvas) e.getSource();
      final IAxis curAxis = m_axes.get( curAc );
      final ORIENTATION ori = curAxis.getPosition().getOrientation();

      // zoom-Rechteck im Plot
      Rectangle dragArea;
      final Rectangle plotBounds = m_chartComposite.getPlot().getBounds();
      if( ori == ORIENTATION.HORIZONTAL )
        dragArea = new Rectangle( Math.min( m_mouseDragStart, m_mouseDragEnd ), 0, Math.abs( m_mouseDragStart - m_mouseDragEnd ), plotBounds.height );
      else
        dragArea = new Rectangle( 0, Math.min( m_mouseDragStart, m_mouseDragEnd ), plotBounds.width, Math.abs( m_mouseDragStart - m_mouseDragEnd ) );
      m_chartComposite.getPlot().setDragArea( dragArea );

      if( m_applyOnAllAxes )
      {
        // zoom-Rechteck im AxisCanvas
        for( final AxisCanvas ac : m_axes.keySet() )
        {
          final IAxis axis = m_axes.get( ac );
          if( axis.getPosition().getOrientation().equals( ori ) )
          {
            ac.setDragInterval( m_mouseDragStart, m_mouseDragEnd );
          }
        }

      }
      else
      {
        curAc.setDragInterval( m_mouseDragStart, m_mouseDragEnd );
      }

    }

  }

  protected void performZoomAction( IAxis axis )
  {

    final Number numDragStart = axis.screenToNumeric( m_mouseDragStart );
    final Number numDragEnd = axis.screenToNumeric( m_mouseDragEnd );
    Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Zooming from " + m_mouseDragStart + " (" + numDragStart + ") to " + m_mouseDragEnd + " (" + numDragEnd + ")" );
    axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { numDragStart, numDragEnd } ) );

  }

}
