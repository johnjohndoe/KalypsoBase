/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.ui.commands;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ElementUpdateHelper;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.commons.java.lang.Objects;

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;

/**
 * @author Dirk Kuch
 */
public class UpdateProfileCursorCommandHandler extends AbstractHandler implements IElementUpdater
{
  private UpdateProfileCursorChartHandler m_widget;

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartComposite chart = ChartHandlerUtilities.getChart( context );
    if( chart == null )
      return Status.CANCEL_STATUS;

    final IChartHandlerManager manager = chart.getPlotHandler();
    if( ElementUpdateHelper.getSelection( event ) )
    {
      m_widget = new UpdateProfileCursorChartHandler( chart );
      manager.activatePlotHandler( m_widget );
    }
    else if( Objects.isNotNull( m_widget ) )
    {
      manager.removePlotHandler( m_widget );
      m_widget = null;
    }

    // FIXME!
    final IChartPart part = ChartHandlerUtilities.findChartComposite( context );
    if( part != null )
      ChartHandlerUtilities.updateElements( part );

    return Status.OK_STATUS;
  }

  @Override
  public void updateElement( final UIElement element, final Map parameters )
  {
    element.setChecked( Objects.isNotNull( m_widget ) );
  }
}
