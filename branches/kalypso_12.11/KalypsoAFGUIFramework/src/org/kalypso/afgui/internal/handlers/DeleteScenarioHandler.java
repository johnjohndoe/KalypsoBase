/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.afgui.views.WorkflowView;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.eclipse.jface.window.ShellProvider;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

public class DeleteScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    // REMARK: the shell from the context is already disposed, as the breadcrumb menu was closed
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
    final IViewPart workflowView = window.getActivePage().findView( WorkflowView.ID );
    final Shell shell = workflowView.getSite().getShell();

    /* Find selection */
    final Object itemSelection = getSelection( event );
    if( itemSelection instanceof IScenario )
    {
      final String windowTitle = HandlerUtils.getCommandName( event );
      deleteScenario( shell, (IScenario)itemSelection, windowTitle );
      return null;
    }

    // FIXME: does not work: for opened breadcrumb popup menu, the shell is always disposed, never mind where the shell
    // is taken from

    if( itemSelection instanceof IProject )
    {
      final IStructuredSelection selection = new StructuredSelection( itemSelection );

      final IShellProvider shellProvider = new ShellProvider( shell );

      final DeleteResourceAction deleteResourceAction = new DeleteResourceAction( shellProvider );
      deleteResourceAction.selectionChanged( selection );
      deleteResourceAction.run();
      return Status.OK_STATUS;
    }

    return null;
  }

  private Object getSelection( final ExecutionEvent event )
  {
    final ISelection selection = HandlerUtil.getCurrentSelection( event );
    if( !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection)selection;
    final Object[] array = structSel.toArray();
    for( final Object element : array )
    {
      if( element instanceof IResource || element instanceof IScenario )
        return element;
    }

    return null;
  }

  static void deleteScenario( final Shell shell, final IScenario firstElement, final String title )
  {
    final IScenario scenario = firstElement;
    final IScenarioList derivedScenarios = scenario.getDerivedScenarios();
    if( derivedScenarios != null && !derivedScenarios.getScenarios().isEmpty() )
    {
      MessageDialog.openInformation( shell, title, Messages.getString( "org.kalypso.afgui.handlers.RemoveScenarioHandler.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    if( scenario.getParentScenario() == null )
    {
      MessageDialog.openInformation( shell, title, Messages.getString( "org.kalypso.afgui.handlers.RemoveScenarioHandler.3" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    if( scenario.equals( ScenarioHelper.getActiveScenario() ) )
    {
      MessageDialog.openInformation( shell, title, Messages.getString( "org.kalypso.afgui.handlers.RemoveScenarioHandler.5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    final String message = Messages.getString( "org.kalypso.afgui.handlers.RemoveScenarioHandler.8", scenario.getName() ); //$NON-NLS-1$
    if( MessageDialog.openConfirm( shell, title, message ) )
    {
      final UIJob runnable = new UIJob( shell.getDisplay(), Messages.getString( "org.kalypso.afgui.handlers.RemoveScenarioHandler.6" ) ) //$NON-NLS-1$
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          try
          {
            final IProject project = scenario.getProject();
            final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
            final IScenarioManager scenarioManager = nature.getCaseManager();
            scenarioManager.removeCase( scenario, monitor );
            return Status.OK_STATUS;
          }
          catch( final CoreException e )
          {
            return e.getStatus();
          }
        }
      };
      runnable.schedule();
    }
  }
}
