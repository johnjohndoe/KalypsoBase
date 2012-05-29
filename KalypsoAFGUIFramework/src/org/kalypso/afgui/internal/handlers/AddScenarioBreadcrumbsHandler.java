/**
 *
 */
package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.kalypso.afgui.internal.ui.workflow.WorkflowBreadcrumbItemSourceProvider;
import org.kalypso.afgui.views.WorkflowView;

import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Gernot Belger
 */
public class AddScenarioBreadcrumbsHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    // REMARK: the shell from the context is already disposed, as the breadcrumb menu was closed
    // REMARK: the shell from the context is already disposed, as the breadcrumb menu was closed
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
    final IViewPart workflowView = window.getActivePage().findView( WorkflowView.ID );
    final Shell shell = workflowView.getSite().getShell();

    /* Find scenario */
    final IEvaluationContext context = (EvaluationContext) event.getApplicationContext();
    final Object itemSelection = context.getVariable( WorkflowBreadcrumbItemSourceProvider.VARIABLE_MENU_SELECTION );
    if( !(itemSelection instanceof IScenario) )
      return null;

    final IScenario parentScenario = (IScenario) itemSelection;

    AddScenarioHandler.stopTaskAndOpenWizard( shell, parentScenario );

    return null;
  }
}
