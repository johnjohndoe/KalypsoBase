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
package org.kalypso.chart.ui.editor.mouse;

import java.util.Map;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.framework.view.PlotCanvas;
import org.kalypso.chart.ui.editor.actions.IChartDragAction;

/**
 * @author burtscher1
 */
public class DragPanHandler implements MouseListener, MouseMoveListener
{
  private Point m_start = null;

  private final ChartComposite m_chartComposite;

  private final PlotCanvas m_plot;

  int m_pressedKey = -1;

  private Map<IAxis< ? >, IDataRange<Number>> m_axisMap;

  public DragPanHandler( final ChartComposite chartComposite )
  {
    m_chartComposite = chartComposite;
    m_plot = m_chartComposite.getPlot();
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( MouseEvent e )
  {
    m_start = null;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    m_start = new Point( e.x, e.y );
    m_axisMap = m_chartComposite.getModel().getMapperRegistry().getNumericRangeAxisSnapshot();
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( m_start == null )
      return;
    m_start = null;
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_start != null )
    {
      // aktueller Punkt
      final int nowX = e.x;
      final int nowY = e.y;

      m_chartComposite.getModel().panTo( m_axisMap, m_start, new Point( nowX, nowY ) );

    }
  }

  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  public void setDragAction( IChartDragAction chartDragAction )
  {
    // TODO Auto-generated method stub
    System.out.println( "!!!!!!!!!!!!!!!!! TODO: DragAction Setzen" );
  }

}
