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

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandler;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;
import de.openali.odysseus.chart.framework.view.IPlotDragHandlerDelegate;

/**
 * delegate to manage handlers for mouse dragging; only one handler is active at a time
 * 
 * @author burtscher1
 */
public class PlotDragHandlerDelegate implements IPlotDragHandlerDelegate
{
  private IChartHandler m_handler;

  private final IChartComposite m_chartComposite;

  public PlotDragHandlerDelegate( final IChartComposite chartComposite )
  {
    m_chartComposite = chartComposite;
  }

  @Override
  public void setActiveHandler( final IChartHandler handler )
  {
    final IChartHandlerManager plotHandler = m_chartComposite.getPlotHandler();

    plotHandler.removePlotHandler( m_handler );
    m_handler = handler;
    plotHandler.addPlotHandler( m_handler );
  }

  @Override
  public IChartHandler getActiveHandler( )
  {
    return m_handler;
  }
}
