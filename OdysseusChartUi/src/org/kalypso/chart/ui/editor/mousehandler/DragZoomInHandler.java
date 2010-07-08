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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author Gernot Belger
 * @author burtscher1
 */
public class DragZoomInHandler extends AbstractChartDragHandler
{
  private Rectangle m_mouseDragRect = null;

  public DragZoomInHandler( final ChartComposite chartComposite )
  {
    super( chartComposite, 5 );

  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  @Override
  public Cursor getCursor( final MouseEvent e )
  {
    return Display.getDefault().getSystemCursor( SWT.CURSOR_CROSS );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseUpAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseUpAction( Point start, EditInfo editInfo )
  {
    if( m_mouseDragRect == null )
      return;

    try
    {
      // Wenn nach rechts unten gezogen wurde, wird reingezoomt
      final int endX = m_mouseDragRect.x + m_mouseDragRect.width;
      final int endY = m_mouseDragRect.y + m_mouseDragRect.height;

      getChart().getChartModel().zoomIn( new Point( m_mouseDragRect.x, m_mouseDragRect.y ), new Point( endX, endY ) );

    }
    finally
    {
      m_mouseDragRect = null;
      getChart().setDragArea( m_mouseDragRect );
    }
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseMoveAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseMoveAction( Point start, EditInfo editInfo )
  {
    if( m_mouseDragRect == null )
      m_mouseDragRect = new Rectangle( start.x, start.y, 0, 0 );

    final Point p = editInfo.m_pos;
    final int newwidth = p.x - m_mouseDragRect.x;
    final int newheight = p.y - m_mouseDragRect.y;

    if( newwidth == m_mouseDragRect.width && newheight == m_mouseDragRect.height )
      return;

    m_mouseDragRect.width = newwidth;
    m_mouseDragRect.height = newheight;
    // zoom-Rechteck
    getChart().setDragArea( m_mouseDragRect );

  }

}
