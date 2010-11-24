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
package org.kalypso.chart.ui.editor.commandhandler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceScopes;
import org.kalypso.chart.ui.IChartPart;

import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Gernot Belger
 */
public class ChartHandlerUtilities
{
  /**
   * @deprecated Use {@link ChartSourceProvider#ACTIVE_CHART_NAME} instead
   */
  @Deprecated
  public final static String ACTIVE_CHART_PART_NAME = "activeChartComposite"; //$NON-NLS-1$

  private ChartHandlerUtilities( )
  {
    throw new UnsupportedOperationException( "Do not instantiate this helper class." ); //$NON-NLS-1$
  }

  /**
   * Gets the currently active chart from the handler event.<br>
   * To be more precise, gets the {@link ChartSourceProvider#ACTIVE_CHART_NAME} source from the events context.
   * 
   * @return <code>null</code>, if no {@link IMapPanel} was found in the context.
   */
  public static IChartComposite getChart( final IEvaluationContext context )
  {
    return (IChartComposite) context.getVariable( ChartSourceProvider.ACTIVE_CHART_NAME );
  }

  /**
   * Gets the currently active chart from the handler event.<br>
   * To be more precise, gets the {@link ChartSourceProvider#ACTIVE_CHART_NAME} source from the events context.
   * 
   * @throws ExecutionException
   *           If the current context contains no chart.
   */
  public static IChartComposite getChartChecked( final IEvaluationContext context ) throws ExecutionException
  {
    final IChartComposite chart = getChart( context );
    if( chart == null )
      throw new ExecutionException( "No chart in context." ); //$NON-NLS-1$

    return chart;
  }

  /**
   * Helps finding the chart composite in the context.<br>
   * Normally (for editor and view) this is done via adapting the active workbench part.<br>
   * However for the feature view some hack was needed: here it is found via the activeChartComposite variable.
   * 
   * @deprecated Use {@link #getChart(IEvaluationContext)} instead.
   */
  // FIXME: replace with ISourceProvider stuff
  @Deprecated
  public static IChartPart findChartComposite( final IEvaluationContext context )
  {
    final Object chartVar = context.getVariable( ACTIVE_CHART_PART_NAME );
    if( chartVar instanceof IChartPart )
      return (IChartPart) chartVar;

    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );
    return (IChartPart) part.getAdapter( IChartPart.class );
  }

  /**
   * helper function - it calls all commands from the chart ui commands category to refresh their state; function is
   * public as it is called from the command handlers so multiple contributions of a command (e.g. toolbar and menu) are
   * synchronized
   * 
   * @deprecated Should happen automatically, use ISourceProvider mechanism instead.
   */
  @Deprecated
  public static void updateElements( final IChartPart part )
  {
    final Map<Object, Object> filter = new HashMap<Object, Object>();

    final ICommandService commandService;

    if( part instanceof IWorkbenchPart )
    {
      final IWorkbenchWindow window = ((IWorkbenchPart) part).getSite().getWorkbenchWindow();
      filter.put( IServiceScopes.WINDOW_SCOPE, window );

      commandService = (ICommandService) window.getService( ICommandService.class );
    }
    else
      commandService = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );

    final Command[] commands = commandService.getDefinedCommands();

    for( final Command command : commands )
    {
      try
      {
        final Category category = command.getCategory();
        if( category != null && category.getId().equals( ChartSourceProvider.CHART_COMMAND_CATEGORY ) )
          commandService.refreshElements( command.getId(), filter );
      }
      catch( final NotDefinedException e )
      {
        // nothing to do; we just ignore the command
      }
    }
  }

}
