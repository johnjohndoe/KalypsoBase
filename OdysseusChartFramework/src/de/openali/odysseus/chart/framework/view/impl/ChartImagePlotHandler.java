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

import de.openali.odysseus.chart.framework.view.IChartHandler;
import de.openali.odysseus.chart.framework.view.IChartHandler.CHART_HANDLER_TYPE;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;

/**
 * @author Dirk Kuch
 */
public class ChartImagePlotHandler implements IChartHandlerManager
{
  private final Set<IChartHandler> m_dragHandlers = new LinkedHashSet<>();

  private final ChartImageComposite m_chart;

  public ChartImagePlotHandler( final ChartImageComposite chart )
  {
    m_chart = chart;
  }

  @Override
  public void activatePlotHandler( final IChartHandler handler )
  {
    if( CHART_HANDLER_TYPE.eRadio.equals( handler.getType() ) )
      doClean();

    addPlotHandler( handler );
  }

  @Override
  public void addPlotHandler( final IChartHandler handler )
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

  @Override
  public void removePlotHandler( final IChartHandler handler )
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
   * disable all ChartHandler with getType() == CHART_HANDLER_TYPE.eToggle
   */
  @Override
  public void doClean( )
  {
    final IChartHandler[] handlers = m_dragHandlers.toArray( new IChartHandler[] {} );
    for( final IChartHandler handler : handlers )
    {
      if( CHART_HANDLER_TYPE.eRadio.equals( handler.getType() ) )
        removePlotHandler( handler );
    }
  }

  @Override
  public IChartHandler[] getActiveHandlers( )
  {
    return m_dragHandlers.toArray( new IChartHandler[] {} );
  }

  @Override
  public void dispose( )
  {
    final IChartHandler[] handlers = m_dragHandlers.toArray( new IChartHandler[] {} );
    for( final IChartHandler handler : handlers )
      removePlotHandler( handler );
  }
}