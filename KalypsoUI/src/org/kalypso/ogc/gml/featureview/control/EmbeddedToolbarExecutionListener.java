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
package org.kalypso.ogc.gml.featureview.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.ogc.gml.om.table.command.ToolbarCommandUtils;

/**
 * Helper that handles command execution for toolbars that are embedded within a feature view or similar (i.e. in a
 * context where the normal source mechanism does not work).
 * 
 * @author Gernot Belger
 */
public class EmbeddedToolbarExecutionListener implements IExecutionListener
{
  private final Map<String, Object> m_contextConfiguration = new HashMap<>();

  private final IToolBarManager m_toolBar;

  private final IServiceLocator m_serviceLocator;

  public EmbeddedToolbarExecutionListener( final IToolBarManager toolBar, final IServiceLocator serviceLocator )
  {
    m_toolBar = toolBar;
    m_serviceLocator = serviceLocator;

    final ICommandService cmdService = getCommandService();
    cmdService.addExecutionListener( this );

    // as a last resort, always dispose this listener if the tool bar gets disposed;
    // normally the client should dispose this object
    final ToolBar control = findToolbarControl();
    control.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );
  }

  public void dispose( )
  {
    final ICommandService cmdService = getCommandService();
    cmdService.removeExecutionListener( this );
  }

  public void addContextVariable( final String key, final Object value )
  {
    m_contextConfiguration.put( key, value );
  }

  private ICommandService getCommandService( )
  {
    return (ICommandService)m_serviceLocator.getService( ICommandService.class );
  }

  private IHandlerService getHandlerService( )
  {
    return (IHandlerService)m_serviceLocator.getService( IHandlerService.class );
  }

  /** Checks if the event was triggered by my tool-bar. */
  private boolean isThisToolbar( final ExecutionEvent event )
  {
    final Object trigger = event.getTrigger();
    if( !(trigger instanceof Event) )
      return false;

    final Event eventTrigger = (Event)trigger;
    final Widget widget = eventTrigger.widget;
    if( !(widget instanceof ToolItem) )
      return false;

    final ToolItem toolItem = (ToolItem)widget;
    final ToolBar parentToolbar = toolItem.getParent();
    final ToolBar managerToolbar = findToolbarControl();
    return parentToolbar == managerToolbar;
  }

  @Override
  public void notHandled( final String commandId, final NotHandledException exception )
  {
  }

  @Override
  public void preExecute( final String commandId, final ExecutionEvent event )
  {
    final boolean isThisToolbar = isThisToolbar( event );
    if( isThisToolbar )
    {
      final IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();
      configureContext( context );
    }
  }

  private void configureContext( final IEvaluationContext context )
  {
    for( final Entry<String, Object> entry : m_contextConfiguration.entrySet() )
      context.addVariable( entry.getKey(), entry.getValue() );
  }

  private void unconfigureContext( )
  {
    final IEvaluationContext currentState = getHandlerService().getCurrentState();

    for( final String key : m_contextConfiguration.keySet() )
      currentState.removeVariable( key );
  }

  @Override
  public void postExecuteFailure( final String commandId, final ExecutionException exception )
  {
    unconfigureContext();

    // REMARK: it would be nice to have an error mesage here, but:
    // If we have several tabs, we get several msg-boxes, as we have several listeners.
    // How-to avoid that??
    // final IStatus errorStatus = StatusUtilities.createStatus( IStatus.ERROR, "Kommando mit Fehler beendet",
    // exception );
    // ErrorDialog.openError( getShell(), "Kommando ausführen", "Fehler bei der Ausführung eines Kommandos",
    // errorStatus );
  }

  @Override
  public void postExecuteSuccess( final String commandId, final Object returnValue )
  {
    final IEvaluationContext currentState = getHandlerService().getCurrentState();
    currentState.removeVariable( ToolbarCommandUtils.ACTIVE_TUPLE_RESULT_TABLE_VIEWER_NAME );
    currentState.removeVariable( ToolbarCommandUtils.ACTIVE_TUPLE_RESULT_FEATURE_CONTROL_NAME );
  }

  private ToolBar findToolbarControl( )
  {
    if( m_toolBar instanceof ToolBarManager )
      return ((ToolBarManager)m_toolBar).getControl();

    throw new UnsupportedOperationException();
  }
}