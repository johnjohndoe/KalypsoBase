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
package org.kalypso.model.wspm.ui.featureview;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ChartEditorTreeOutlinePage;
import org.kalypso.chart.ui.editor.commandhandler.ChartSourceProvider;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IPlotHandler;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;

/**
 * Class for charts inserted as tabs into the chart feature control; this has to be isolated in a seperate class as each
 * IChartPart can only return one ChartComposite and one ChartDragHandler
 * 
 * @author burtscher1
 */
public class ChartTabItem extends Composite implements IChartPart
{
  private final IChartComposite m_chartComposite;

  private final IExecutionListener m_executionListener;

  // private final AxisDragHandlerDelegate m_axisDragHandlerDelegate;

  private ChartEditorTreeOutlinePage m_outlinePage;

  public ChartTabItem( final Composite parent, final int style, final Map<String, Integer> commands )
  {
    super( parent, style );

    final GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    setLayout( gridLayout );

    final IWorkbench serviceLocator = PlatformUI.getWorkbench();
    final ToolBarManager manager = new ToolBarManager( SWT.HORIZONTAL | SWT.FLAT );
    if( commands.size() > 0 )
    {
      final ToolBar toolBar = manager.createControl( this );
      for( final Entry<String, Integer> entry : commands.entrySet() )
      {
        final String cmdId = entry.getKey();
        final Integer cmdStyle = entry.getValue();

        final CommandContributionItemParameter cmdParams = new CommandContributionItemParameter( serviceLocator, cmdId + "_item_", cmdId, cmdStyle ); //$NON-NLS-1$
        final CommandContributionItem contribItem = new CommandContributionItem( cmdParams );
        manager.add( contribItem );
      }
      manager.update( true );
    }

    final IChartModel chartModel = new ChartModel();
    m_chartComposite = new ChartImageComposite( this, SWT.BORDER, chartModel, new RGB( 255, 255, 255 ) );
    final GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );

    m_chartComposite.getPlot().setLayoutData( gridData );

    // m_axisDragHandlerDelegate = new AxisDragHandlerDelegate( m_chartComposite );

    final ICommandService cmdService = (ICommandService) serviceLocator.getService( ICommandService.class );
    final IHandlerService handlerService = (IHandlerService) serviceLocator.getService( IHandlerService.class );

    m_executionListener = new IExecutionListener()
    {
      @Override
      public void notHandled( final String commandId, final NotHandledException exception )
      {
      }

      @Override
      public void preExecute( final String commandId, final ExecutionEvent event )
      {
        if( !commands.keySet().contains( commandId ) )
          return;

        final ToolBar parentToolbar = findToolbar( event );

        final ToolBar managerToolbar = manager.getControl();

        if( commands.keySet().contains( commandId ) && parentToolbar == managerToolbar )
        {
          final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
          context.addVariable( ChartSourceProvider.ACTIVE_CHART_NAME, ChartTabItem.this.getChartComposite() );
        }
      }

      private ToolBar findToolbar( final ExecutionEvent event )
      {
        final Event trigger = (Event) event.getTrigger();

        if( trigger.widget instanceof ToolItem )
        {
          final ToolItem toolItem = (ToolItem) trigger.widget;
          final ToolBar parentToolbar = toolItem.getParent();
          return parentToolbar;
        }

        if( trigger.widget instanceof ToolBar )
          return (ToolBar) trigger.widget;

        throw new IllegalArgumentException();
      }

      @Override
      public void postExecuteFailure( final String commandId, final ExecutionException exception )
      {
        if( !commands.keySet().contains( commandId ) )
          return;

        final IEvaluationContext currentState = handlerService.getCurrentState();
        currentState.removeVariable( ChartSourceProvider.ACTIVE_CHART_NAME );

        // REMARK: it would be nice to have an error mesage here, but:
        // If we have several tabs, we get several msg-boxes, as we have several listeners.
        // How-to avoid that??
        // final IStatus errorStatus = StatusUtilities.createStatus( IStatus.ERROR, "Kommando mit Fehler beendet",
// exception );
        // ErrorDialog.openError( getShell(), "Kommando ausf�hren", "Fehler bei der Ausf�hrung eines Kommandos",
// errorStatus );
      }

      @Override
      public void postExecuteSuccess( final String commandId, final Object returnValue )
      {
        if( !commands.keySet().contains( commandId ) )
          return;

        final IEvaluationContext currentState = handlerService.getCurrentState();
        currentState.removeVariable( ChartSourceProvider.ACTIVE_CHART_NAME );
      }
    };

    cmdService.addExecutionListener( m_executionListener );

    final Event event = new Event();
    event.widget = manager.getControl();
    final String firstCommand = commands.keySet().toArray( new String[] {} )[0];
    try
    {
      handlerService.executeCommand( firstCommand, event );
    }
    catch( final Throwable e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getChartComposite()
   */
  @Override
  public IChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getChartDragHandler()
   */
  @Override
  public IPlotHandler getPlotDragHandler( )
  {
    return m_chartComposite.getPlotHandler();
  }

  @Override
  public void dispose( )
  {
    final ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
    cmdService.removeExecutionListener( m_executionListener );

    if( m_chartComposite != null && !m_chartComposite.getPlot().isDisposed() )
      m_chartComposite.getPlot().dispose();
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getOutlinePage()
   */
  @Override
  public IContentOutlinePage getOutlinePage( )
  {
    if( m_outlinePage == null && m_chartComposite != null )
    {
      final IChartModel model = getChartComposite().getChartModel();
      m_outlinePage = new ChartEditorTreeOutlinePage();
      m_outlinePage.setModel( model );
    }
    return m_outlinePage;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#addListener(java.lang.Object)
   */
  @Override
  public void addListener( final IChartModelEventListener listener )
  {
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#removeListener(java.lang.Object)
   */
  @Override
  public void removeListener( final IChartModelEventListener listener )
  {
  }
}