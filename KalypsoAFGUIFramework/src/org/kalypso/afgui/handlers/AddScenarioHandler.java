/**
 *
 */
package org.kalypso.afgui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.scenarios.TaskExecutionAuthority;

import de.renew.workflow.base.ITask;
import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Patrice Congo, Stefan Kurzbach
 */
public class AddScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final ISelection selection = (ISelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );
    if( !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection) selection;

    if( structSel.isEmpty() )
      return null;

    final Object o = structSel.getFirstElement();
    if( !(o instanceof IScenario) )
      return null;

    /* Stop current task */
    final KalypsoAFGUIFrameworkPlugin plugin = KalypsoAFGUIFrameworkPlugin.getDefault();
    final TaskExecutionAuthority taskExecutionAuthority = plugin.getTaskExecutionAuthority();
    final ITask activeTask = plugin.getTaskExecutor().getActiveTask();
    if( !taskExecutionAuthority.canStopTask( activeTask ) )
    {
      /* cancelled by user */
      return null;
    }

    /* Show wizard */
    final IScenario scenario = (IScenario) o;

    final NewScenarioData data = new NewScenarioData( scenario );

    final NewScenarioWizard wizard = new NewScenarioWizard( data );

    final WizardDialog wd = new WizardDialog( shell, wizard );
    wd.open();

    return null;
  }
}
