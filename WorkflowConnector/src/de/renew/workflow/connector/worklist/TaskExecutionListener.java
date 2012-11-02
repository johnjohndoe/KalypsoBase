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
package de.renew.workflow.connector.worklist;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;

/**
 * A {@link TaskExecutionListener} handles requesting and confirming work items for commands. This
 * {@link IExecutionListener} requests a work item from the workflow system after the command has been executed and
 * confirms it before the execution of the next command.
 * 
 * @author Stefan Kurzbach
 */
public class TaskExecutionListener implements IExecutionListener
{
  public static final String ACTIVITY_PARAMETER = "activityParameter"; //$NON-NLS-1$

  public static final String CATEGORY_CONTEXT = "de.renew.workflow.contexts.category";//$NON-NLS-1$

  public static final String CATEGORY_TASK = "de.renew.workflow.tasks.category";//$NON-NLS-1$

  public static final String CATEGORY_TASKGROUP = "de.renew.workflow.taskgroups.category";//$NON-NLS-1$

  private final ICommandService m_commandService;

  public TaskExecutionListener( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final ICommandService commandService = (ICommandService) workbench.getService( ICommandService.class );
    // remember command service, it is needed several times
    m_commandService = commandService;
  }

  @Override
  public void preExecute( final String commandId, final ExecutionEvent event )
  {
  }

  @Override
  public void notHandled( final String commandId, final NotHandledException exception )
  {
    requestWorkitem( commandId );
  }

  @Override
  public void postExecuteFailure( final String commandId, final ExecutionException exception )
  {
    cancelWorkitem( commandId );
  }

  @Override
  public void postExecuteSuccess( final String commandId, final Object returnValue )
  {
    confirmWorkitem( commandId, returnValue );
  }

  private void confirmWorkitem( final String commandId, final Object returnValue )
  {
    final Command command = m_commandService.getCommand( commandId );
    String categoryId = null;
    try
    {
      categoryId = command.getCategory().getId();
    }
    catch( final NotDefinedException e )
    {
      // the command should always be defined
      e.printStackTrace();
    }
    if( categoryId != null && CATEGORY_TASK.equals( categoryId ) )
    {
      WorkflowConnectorPlugin.getDefault().getConnector().confirm( commandId, returnValue );
    }
  }

  private void cancelWorkitem( final String commandId )
  {
    final Command command = m_commandService.getCommand( commandId );
    String categoryId = null;
    try
    {
      categoryId = command.getCategory().getId();
    }
    catch( final NotDefinedException e )
    {
      // the command should always be defined
      e.printStackTrace();
    }
    if( categoryId != null && CATEGORY_TASK.equals( categoryId ) )
    {
      WorkflowConnectorPlugin.getDefault().getConnector().cancel( commandId );
    }
  }

  private Object requestWorkitem( final String commandId )
  {
    final Command command = m_commandService.getCommand( commandId );
    String categoryId = null;
    try
    {
      categoryId = command.getCategory().getId();
    }
    catch( final NotDefinedException e )
    {
      // the command should always be defined
      e.printStackTrace();
    }
    if( categoryId != null && CATEGORY_TASK.equals( categoryId ) && WorkflowConnectorPlugin.getDefault().getConnector().canRequest( commandId ) )
    {
      return WorkflowConnectorPlugin.getDefault().getConnector().request( commandId );
    }
    return null;
  }
}