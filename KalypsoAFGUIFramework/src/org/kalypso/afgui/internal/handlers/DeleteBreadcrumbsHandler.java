package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.kalypso.afgui.internal.ui.workflow.WorkflowBreadcrumbItemSourceProvider;
import org.kalypso.afgui.views.WorkflowView;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.eclipse.jface.window.ShellProvider;

import de.renew.workflow.connector.cases.IScenario;

public class DeleteBreadcrumbsHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    // REMARK: the shell from the context is already disposed, as the breadcrumb menu was closed
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
    final IViewPart workflowView = window.getActivePage().findView( WorkflowView.ID );
    final Shell shell = workflowView.getSite().getShell();

    /* Find scenario */
    final IEvaluationContext context = (EvaluationContext) event.getApplicationContext();
    final Object itemSelection = context.getVariable( WorkflowBreadcrumbItemSourceProvider.VARIABLE_MENU_SELECTION );
    if( itemSelection instanceof IScenario )
    {
      final String windowTitle = HandlerUtils.getCommandName( event );
      RemoveScenarioHandler.deleteScenario( shell, (IScenario) itemSelection, windowTitle );
      return null;
    }

    // FIXME: does not work: shell is always disposed, never mind where the shell is taken from

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
}