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

import org.eclipse.core.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.EditorPart;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.framework.view.IChartDragHandler;
import org.kalypso.chart.framework.view.PlotCanvas;

/**
 * @author burtscher1
 * 
 */
public class ChartDragHandlerDelegate
{

  private IChartDragHandler m_handler;

  private final PlotCanvas m_plot;

  private final ChartComposite m_chartComposite;

  private Command m_command;

  private final EditorPart m_part;

  public ChartDragHandlerDelegate( ChartComposite chartComposite, EditorPart part )
  {
    m_chartComposite = chartComposite;

    m_plot = chartComposite.getPlot();

    m_part = part;
  }

  public void setActiveHandler( IChartDragHandler handler, Command command )
  {
    m_command = command;

    if( m_handler != null )
    {
      m_plot.removeMouseListener( m_handler );
      m_plot.removeMouseMoveListener( m_handler );
    }
    m_handler = handler;
    if( handler != null )
    {
      m_plot.addMouseListener( handler );
      m_plot.addMouseMoveListener( handler );
      m_plot.setCursor( handler.getCursor() );
    }
    else
    {
      m_plot.setCursor( m_plot.getDisplay().getSystemCursor( SWT.CURSOR_ARROW ) );
    }
  }

  public void activateHandler( )
  {

    if( m_command != null )
    {
      ICommandService cs = (ICommandService) m_part.getSite().getService( ICommandService.class );
    }

  }
}
