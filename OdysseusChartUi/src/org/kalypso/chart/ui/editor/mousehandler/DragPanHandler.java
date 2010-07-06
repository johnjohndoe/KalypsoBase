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
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;
import de.openali.odysseus.chart.framework.view.impl.AxisCanvas;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author burtscher1
 */
public class DragPanHandler implements IChartDragHandler
{
  private Point m_start = null;

  private final ChartComposite m_chartComposite;

  public DragPanHandler( final ChartComposite chartComposite )
  {
    m_chartComposite = chartComposite;
  }

  private final void clearPanOffset( )
  {
    m_chartComposite.getPlot().setPanOffset( null, null );
    final IChartModel cm = m_chartComposite.getChartModel();
    final IMapperRegistry mr = cm == null ? null : cm.getMapperRegistry();
    final IAxis[] axes = mr == null ? new IAxis[] {} : mr.getAxes();
    for( final IAxis axis : axes )
    {
      final AxisCanvas ac = m_chartComposite.getAxisCanvas( axis );
      if( ac != null )
        ac.setPanOffsetInterval( null );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDoubleClick( final MouseEvent e )
  {
    m_start = null;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDown( final MouseEvent e )
  {
    m_start = new Point( e.x, e.y );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
    clearPanOffset();

    // dann pannen
    if( m_start != null )
    {
      m_chartComposite.getChartModel().panTo( m_start, new Point( e.x, e.y ) );
    }

    // dann pan resetten
    m_start = null;
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    if( m_start != null )
    {
      m_chartComposite.getPlot().setPanOffset( null, new Point( m_start.x - e.x, m_start.y - e.y ) );
      setAxisPanOffset( m_start.x, e.x, m_start.y, e.y );
    }
  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  @Override
  public Cursor getCursor( )
  {
    return Display.getDefault().getSystemCursor( SWT.CURSOR_SIZEALL );
  }

  private void setAxisPanOffset( int panXStart, int panXEnd, int panYStart, int panYEnd )
  {
    IMapperRegistry mapperRegistry = m_chartComposite.getChartModel().getMapperRegistry();
    IAxis[] axes = mapperRegistry.getAxes();
    for( IAxis axis : axes )
    {
      final AxisCanvas ac = m_chartComposite.getAxisCanvas( axis );
      if( ac == null )
        continue;

      if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
      {
        ac.setPanOffsetInterval( new Point( panXEnd - panXStart, 0 ) );
      }
      else
      {
        ac.setPanOffsetInterval( new Point( 0, panYEnd - panYStart ) );
      }
    }
  }

}
