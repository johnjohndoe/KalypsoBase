/**
 *
 */
package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

/**
 * @author Stefan Kurzbach
 */
public class RemoveScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    // REMARK: the shell from the context is already disposed, as the breadcrumb menu was closed
    final IWorkbenchSite site = HandlerUtil.getActiveSite( event );
    final Shell shell = site.getShell();

    final ISelection selection = HandlerUtil.getCurrentSelectionChecked( event );

    if( selection.isEmpty() || !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    final Object firstElement = structuredSelection.getFirstElement();

    if( !(firstElement instanceof IScenario) )
      return null;

    final String windowTitle = HandlerUtils.getCommandName( event );
    deleteScenario( shell, (IScenario) firstElement, windowTitle );

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

    if( KalypsoAFGUIFrameworkPlugin.getActiveWorkContext().getCurrentCase() == scenario )
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