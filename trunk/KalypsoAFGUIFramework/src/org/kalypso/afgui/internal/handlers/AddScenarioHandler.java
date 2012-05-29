/**
 *
 */
package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;

import de.renew.workflow.base.ITask;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.connector.worklist.ITaskExecutionAuthority;

/**
 * @author Patrice Congo, Stefan Kurzbach
 */
public class AddScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final Shell shell = HandlerUtil.getActiveShell( event );
    final ISelection selection = HandlerUtil.getCurrentSelection( event );

    /* Find scenario */
    final IScenario scenario = findParentScenario( selection );
    if( scenario == null )
    {
      final String commandName = HandlerUtils.getCommandName( event );
      final String message = "Please active the scenario to derive from.";
      MessageDialog.openInformation( shell, commandName, message );
      return null;
    }

    stopTaskAndOpenWizard( shell, scenario );

    return null;
  }

  private IScenario findParentScenario( final ISelection selection )
  {
    final ActiveWorkContext context = KalypsoAFGUIFrameworkPlugin.getDefault().getActiveWorkContext();
    final IScenario currentCase = context.getCurrentCase();

    if( !(selection instanceof IStructuredSelection) )
      return currentCase;

    final IStructuredSelection structSel = (IStructuredSelection) selection;

    if( structSel.isEmpty() )
      return currentCase;

    final Object o = structSel.getFirstElement();
    if( o instanceof IScenario )
      return (IScenario) o;

    return currentCase;
  }

  public static void stopTaskAndOpenWizard( final Shell shell, final IScenario parentScenario )
  {
    /* Stop current task */
    final KalypsoAFGUIFrameworkPlugin plugin = KalypsoAFGUIFrameworkPlugin.getDefault();
    final ITaskExecutionAuthority taskExecutionAuthority = plugin.getTaskExecutionAuthority();
    final ITask activeTask = plugin.getTaskExecutor().getActiveTask();
    if( !taskExecutionAuthority.canStopTask( activeTask ) )
    {
      /* cancelled by user */
      return;
    }

    /* Show wizard */

    final NewScenarioData data = new NewScenarioData( parentScenario );

    final NewScenarioWizard wizard = new NewScenarioWizard( data );

    final WizardDialog wd = new WizardDialog( shell, wizard );
    wd.open();
  }
}
