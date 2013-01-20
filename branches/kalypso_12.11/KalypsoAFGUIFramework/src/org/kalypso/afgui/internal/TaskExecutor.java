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
package org.kalypso.afgui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.kalypso.afgui.internal.workflow.WorkflowView;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.util.command.WaitForFeatureChanges;

import de.renew.workflow.base.EActivityType;
import de.renew.workflow.base.ITask;
import de.renew.workflow.base.ITaskGroup;
import de.renew.workflow.connector.worklist.ITaskExecutionAuthority;
import de.renew.workflow.connector.worklist.ITaskExecutionListener;
import de.renew.workflow.connector.worklist.ITaskExecutor;
import de.renew.workflow.connector.worklist.TaskExecutionListener;
import de.renew.workflow.contexts.ContextType;
import de.renew.workflow.contexts.IContextHandlerFactory;
import de.renew.workflow.contexts.WorkbenchPartContextType;
import de.renew.workflow.contexts.WorkbenchSiteContext;

/**
 * @author Stefan Kurzbach
 */
public class TaskExecutor implements ITaskExecutor
{
  private static final String TASK_COMMNAND_ROLE_ACTIVATE = StringUtils.EMPTY;

  private final TaskPerspectiveStore m_perspectiveStore = new TaskPerspectiveStore( this );

  private ITask m_activeTask;

  private final ITaskExecutionAuthority m_authority;

  private ICommandService m_commandService;

  private IHandlerService m_handlerService;

  private final IContextHandlerFactory m_contextHandlerFactory;

  private final List<ITaskExecutionListener> m_taskChangeListeners;

  public TaskExecutor( final IContextHandlerFactory contextHandlerFactory, final ITaskExecutionAuthority authority, final ICommandService commandService, final IHandlerService handlerService )
  {
    m_contextHandlerFactory = contextHandlerFactory;
    m_authority = authority;
    m_commandService = commandService;
    m_handlerService = handlerService;
    m_taskChangeListeners = new ArrayList<>();
  }

  @Override
  public ITask getActiveTask( )
  {
    return m_activeTask;
  }

  @Override
  public boolean stopActiveTask( )
  {
    if( m_activeTask == null )
      return true;

    // // TODO: would be nice:
    // // execute a Command (convention on command-id?) on task stop. Command might refuse to stop the task
    // IStatus executeStatus = executeTaskCommand( m_activeTask, TASK_COMMNAND_ROLE_STOP );
    // if( executeStatus.matches( IStatus.CANCEL ))
    // return false;

    /* BUGFIX: some feature may still be in progress to be changed, we wait for every feature command to finish */
    final ICoreRunnableWithProgress commandWaiter = new WaitForFeatureChanges();
    ProgressUtilities.busyCursorWhile( commandWaiter );

    // REMARK: this is used to ask the user, if the data should be saved or not
    // It is a bit questionable if this is the right place...; should be reconsidered
    if( !m_authority.canStopTask( m_activeTask ) )
      return false;

    m_perspectiveStore.saveTaskPerspective( m_activeTask );

    /* Reset active task */
    final ITask oldTask = m_activeTask;

    m_activeTask = null;

    /* Tell the listeners, that the task was stopped. */
    fireActiveTaskChanged( null, oldTask, null );

    return true;
  }

  @Override
  public IStatus execute( final ITask task )
  {
    if( m_activeTask != null )
    {
      // if the same task is executed again, but it is asynchronous, don't do anything
      if( m_activeTask.getType() == EActivityType.ASYNCHRONOUS && m_activeTask == task )
        return Status.OK_STATUS;
    }

    if( !stopActiveTask() )
      return Status.OK_STATUS;

    final boolean isPerspectiveConfigured = m_perspectiveStore.restoreTaskPerspective( task );

    // REMARK: we return AFTER closing all unnecessary views, else some open views may remain in case of errors
    final IStatus contextStatus = activateTaskContext( task, isPerspectiveConfigured );
    if( !contextStatus.isOK() )
      return contextStatus;

    /* Activate new task and execute */
    final IStatus taskExecutionStatus = executeTaskCommand( task, TASK_COMMNAND_ROLE_ACTIVATE );
    if( taskExecutionStatus.matches( IStatus.CANCEL ) )
    {
      // if command is not handled nothing is to do.
      // This also prohibits the activation of task-groups that do nothing themselves
      return taskExecutionStatus;
    }

    final ITask oldTask = m_activeTask;

    // if( task instanceof ITaskGroup )
    // m_activeTask = null;
    // else
      m_activeTask = task;

    /* Tell the listeners, that the task was executed. */
    fireActiveTaskChanged( taskExecutionStatus, oldTask, task );

    return taskExecutionStatus;
  }

  private IStatus executeTaskCommand( final ITask task, final String role )
  {
    if( task == null )
      return Status.OK_STATUS;

    final String commandID = String.format( "%s%s", task.getURI(), role ); //$NON-NLS-1$

    final String categoryId = task instanceof ITaskGroup ? TaskExecutionListener.CATEGORY_TASKGROUP : TaskExecutionListener.CATEGORY_TASK;

    final Command command = getCommand( m_commandService, commandID, categoryId );
    if( !command.isHandled() )
    {
      // if command is not handled nothing is to do.
      // This also prohibits the activation of task-groups that do nothing themselves
      return Status.OK_STATUS;
    }

    try
    {
      final Object result = m_handlerService.executeCommand( command.getId(), null );
      if( result instanceof IStatus )
        return (IStatus) result;
      else
        return Status.OK_STATUS;
    }
    catch( final NotHandledException e )
    {
      // this just means that the command is not handled, so there is nothing else to do
      return Status.OK_STATUS;
    }
    catch( final Throwable e )
    {
      return StatusUtilities.statusFromThrowable( e );
    }
  }

  private IStatus activateTaskContext( final ITask task, final boolean isPerspectiveConfigured )
  {
    final ContextType context = task == null ? null : task.getContext();

    final IStatus contextStatus = activateContext( context );

    if( !isPerspectiveConfigured )
    {
      // For backwards compatibility: open/close views according to context
      // but only, if we do not have a preconfigured layout

      // collect the views that were just opened
      final Collection<String> partsToKeep = collectOpenedViews( context );
      partsToKeep.add( WorkflowView.ID );
      partsToKeep.add( PerspectiveWatcher.SCENARIO_VIEW_ID );

      // forks a new job for cleaning perspective
      final IWorkbench workbench = PlatformUI.getWorkbench();
      PerspectiveWatcher.cleanPerspective( workbench, partsToKeep );
    }

    return contextStatus;
  }

  private Collection<String> collectOpenedViews( final ContextType context )
  {
    if( context == null )
      return new ArrayList<>();

    final ContextType parentContext = context.getParent();
    final Collection<String> result;
    if( parentContext != null )
      result = collectOpenedViews( parentContext );
    else
      result = new ArrayList<>();

    if( context instanceof WorkbenchSiteContext )
    {
      final WorkbenchSiteContext multiContext = (WorkbenchSiteContext) context;
      final List<JAXBElement< ? extends WorkbenchPartContextType>> subContexts = multiContext.getPartContexts();
      for( final JAXBElement< ? extends WorkbenchPartContextType> element : subContexts )
      {
        final ContextType value = element.getValue();
        if( value instanceof WorkbenchPartContextType )
          result.add( ((WorkbenchPartContextType) value).getPartId() );
      }
    }
    else if( context instanceof WorkbenchPartContextType )
      result.add( ((WorkbenchPartContextType) context).getPartId() );

    return result;
  }

  /**
   * This function activates the context, which is given and all its parents.
   *
   * @param context
   *          The context.
   * @return A status object indicating the success of the function.
   */
  private IStatus activateContext( final ContextType context )
  {
    if( context == null )
      return Status.OK_STATUS;

    final ContextType parentContext = context.getParent();
    if( parentContext != null )
    {
      // first activate all parent contexts (loop)
      final IStatus activateContext = activateContext( parentContext );
      // break the loop as soon as we have one error, makes no sense to activate context whose parent is not active,
      // because this will lead probably to more errors (or even deadlocks in the case of the map)
      if( !activateContext.isOK() )
        return activateContext;

      return internalActivateContext( context );
    }

    // loop termination
    return internalActivateContext( context );
  }

  /**
   * This function activates the context, which is given.
   *
   * @param context
   *          The context, which should be activated.
   * @return A status object indicating the success of the function.
   */
  private IStatus internalActivateContext( final ContextType context )
  {
    if( m_commandService == null )
      m_commandService = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );

    if( m_handlerService == null )
      m_handlerService = (IHandlerService) PlatformUI.getWorkbench().getService( IHandlerService.class );

    // then execute the associated handler
    final IHandler handler = m_contextHandlerFactory.getHandler( context );
    final Command contextCommand = getCommand( m_commandService, context.getId(), TaskExecutionListener.CATEGORY_CONTEXT );
    contextCommand.setHandler( handler );

    try
    {
      final Object result = m_handlerService.executeCommand( context.getId(), null );

      // if result is a status, return it
      if( result instanceof IStatus )
        return (IStatus) result;

      // otherwise everything must be ok
      return Status.OK_STATUS;
    }
    catch( final Throwable e )
    {
      return StatusUtilities.statusFromThrowable( e );
    }
  }

  private static Command getCommand( final ICommandService commandService, final String commandId, final String categoryId )
  {
    final Command command = commandService.getCommand( commandId );
    if( !command.isDefined() )
    {
      final Category category = commandService.getCategory( categoryId );
      if( !category.isDefined() )
      {
        category.define( categoryId, null );
      }
      command.define( commandId, null, category );
    }
    return command;
  }

  @Override
  public void addTaskExecutionListener( final ITaskExecutionListener listener )
  {
    m_taskChangeListeners.add( listener );
  }

  @Override
  public void removeTaskExecutionListener( final ITaskExecutionListener listener )
  {
    m_taskChangeListeners.remove( listener );
  }

  /**
   * This function notifies all registered listeners.
   *
   * @param results
   *          The results of the task, which was activated, as well of all of its associated tasks.
   * @param task
   *          The final task, that was activated.
   */
  private void fireActiveTaskChanged( final IStatus result, final ITask previouslyActive, final ITask activeTask )
  {
    final List<ITaskExecutionListener> listeners = m_taskChangeListeners;
    for( final ITaskExecutionListener listener : listeners )
      listener.handleActiveTaskChanged( result, previouslyActive, activeTask );
  }
}