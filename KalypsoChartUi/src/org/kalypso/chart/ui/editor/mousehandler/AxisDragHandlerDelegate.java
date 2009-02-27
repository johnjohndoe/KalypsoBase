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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.view.AxisCanvas;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.framework.view.IChartDragHandler;

/**
 * 
 * delegate to manage handlers for mouse dragging; only one handler is active at a time
 * 
 * @author burtscher1
 * 
 */
public class AxisDragHandlerDelegate
{
  private IAxisDragHandler m_handler;

  private final ChartComposite m_chartComposite;

  public AxisDragHandlerDelegate( final ChartComposite chartComposite )
  {
    m_chartComposite = chartComposite;
  }

  public void setActiveHandler( final IAxisDragHandler handler )
  {
    if( m_handler != null )
    {
      for( AxisCanvas ac : getAxesMap().keySet() )
      {
        ac.removeMouseListener( m_handler );
        ac.removeMouseMoveListener( m_handler );
        ac.removeKeyListener( m_handler );
      }

    }
    m_handler = handler;

    for( AxisCanvas ac : getAxesMap().keySet() )
    {
      if( handler == null )
        ac.setCursor( ac.getDisplay().getSystemCursor( SWT.CURSOR_ARROW ) );
      else
      {
        ac.setCursor( m_handler.getCursor() );
        ac.addMouseListener( m_handler );
        ac.addMouseMoveListener( m_handler );
        ac.addKeyListener( m_handler );
      }
    }

  }

  public IChartDragHandler getActiveHandler( )
  {
    return m_handler;
  }

  public void dispose( )
  {
    if( m_handler != null )
    {
      for( final AxisCanvas ac : getAxesMap().keySet() )
      {
        if( !ac.isDisposed() )
        {
          ac.removeMouseListener( m_handler );
          ac.removeMouseMoveListener( m_handler );
          ac.removeKeyListener( m_handler );
        }
      }
    }
  }

  /**
   * this needs to be called each time axes are needed, as it might be that no axes exist at initialisation time
   */
  private Map<AxisCanvas, IAxis> getAxesMap( )
  {
    Map<AxisCanvas, IAxis> axesMap = new HashMap<AxisCanvas, IAxis>();
    final IMapperRegistry reg = m_chartComposite.getModel().getMapperRegistry();
    final IAxis< ? >[] axes = reg.getAxes();
    for( final IAxis< ? > axis : axes )
    {
      final IAxisComponent component = reg.getComponent( axis );
      if( component != null )
      {
        final AxisCanvas ac = (AxisCanvas) component;
        axesMap.put( ac, axis );
      }
    }
    return axesMap;
  }
}
