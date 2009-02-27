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
package org.kalypso.swtchart.chart.mouse;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.swtchart.chart.Plot;

/**
 * @author Gernot Belger
 */
public class DragMouseHandler implements IChartMouseHandler
{
  private Rectangle m_mouseDragRect = null;

  private final Plot m_plot;

  public DragMouseHandler( final Plot plot )
  {
    m_plot = plot;
  }

  public void paint( final GCWrapper gcw )
  {
    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if( m_mouseDragRect != null )
    {
      gcw.setLineWidth( 1 );

      final Rectangle r = RectangleUtils.createNormalizedRectangle( m_mouseDragRect );
      gcw.drawFocus( r.x, r.y, r.width, r.height );
    }
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
    m_mouseDragRect = new Rectangle( e.x, e.y, 0, 0 );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( m_mouseDragRect == null )
      return;

    try
    {
      // Wenn nach rechts unten gezogen wurde, wird reingezoomt
      final int endX = m_mouseDragRect.x + m_mouseDragRect.width;
      final int endY = m_mouseDragRect.y + m_mouseDragRect.height;

      if( Math.abs( m_mouseDragRect.width ) > 5 && Math.abs( m_mouseDragRect.height ) > 5 )
        m_plot.zoomIn( new Point( m_mouseDragRect.x, m_mouseDragRect.y ), new Point( endX, endY ) );
      else
        m_plot.redraw();
    }
    finally
    {
      m_mouseDragRect = null;
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_mouseDragRect != null )
    {
      final int newwidth = e.x - m_mouseDragRect.x;
      final int newheight = e.y - m_mouseDragRect.y;

      if( newwidth == m_mouseDragRect.width && newheight == m_mouseDragRect.height )
        return;

      m_mouseDragRect.width = newwidth;
      m_mouseDragRect.height = newheight;

      m_plot.redraw();
    }
  }

}
