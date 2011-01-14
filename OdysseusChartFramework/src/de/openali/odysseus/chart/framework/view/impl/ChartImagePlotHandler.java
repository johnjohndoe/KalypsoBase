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
package de.openali.odysseus.chart.framework.view.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;

import de.openali.odysseus.chart.framework.view.IChartDragHandler;
import de.openali.odysseus.chart.framework.view.IPlotHandler;

/**
 * @author Dirk Kuch
 */
public class ChartImagePlotHandler implements IPlotHandler
{

  private final Set<IChartDragHandler> m_dragHandlers = new LinkedHashSet<IChartDragHandler>();

  private final ChartImageComposite m_chart;

  public ChartImagePlotHandler( final ChartImageComposite chart )
  {
    m_chart = chart;
  }

  /**
   * @see de.openali.odysseus.chart.framework.view.IPlotHandler#activatePlotHanlder(de.openali.odysseus.chart.framework.view.IChartDragHandler)
   */
  @Override
  public void activatePlotHandler( final IChartDragHandler handler )
  {
    removeAllPlotHandler();
    addPlotHandler( handler );
  }

  /**
   * @see de.openali.odysseus.chart.framework.view.IPlotHandler#addPlotHandler(de.openali.odysseus.chart.framework.view.IChartDragHandler)
   */
  @Override
  public void addPlotHandler( final IChartDragHandler handler )
  {
    if( handler == null )
      m_chart.setCursor( m_chart.getDisplay().getSystemCursor( SWT.CURSOR_ARROW ) );
    else
    {
      m_chart.addMouseListener( handler );
      m_chart.addMouseMoveListener( handler );
      m_chart.addKeyListener( handler );

      m_dragHandlers.add( handler );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.view.IPlotHandler#removePlotHandler(de.openali.odysseus.chart.framework.view.IChartDragHandler)
   */
  @Override
  public void removePlotHandler( final IChartDragHandler handler )
  {
    if( handler != null )
    {
      m_chart.removeMouseListener( handler );
      m_chart.removeMouseMoveListener( handler );
      m_chart.removeKeyListener( handler );

      m_dragHandlers.remove( handler );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.view.IPlotHandler#removeAllPlotHandler()
   */
  @Override
  public void removeAllPlotHandler( )
  {
    final IChartDragHandler[] handlers = m_dragHandlers.toArray( new IChartDragHandler[] {} );
    for( final IChartDragHandler handler : handlers )
    {
      removePlotHandler( handler );
    }

    m_dragHandlers.clear(); // "normally" not necessary removePlotHandler(handler) removes handler from list!
  }

}
