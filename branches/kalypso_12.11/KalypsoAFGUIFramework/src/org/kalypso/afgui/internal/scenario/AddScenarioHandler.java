/**
 *
 */
package org.kalypso.afgui.internal.scenario;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.kalypso.afgui.helper.ScenarioHandlerUtils;
import org.kalypso.afgui.internal.handlers.CreateScenarioOperation;
import org.kalypso.afgui.internal.handlers.IScenarioOperation;
import org.kalypso.afgui.internal.handlers.ScenarioData;
import org.kalypso.afgui.internal.handlers.ScenarioWizard;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.internal.workflow.WorkflowView;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;

import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Patrice Congo, Stefan Kurzbach
 */
public class AddScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    // REMARK: the shell from the context is already disposed, as the breadcrumb menu was closed
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
    final IViewPart workflowView = window.getActivePage().findView( WorkflowView.ID );
    final Shell shell = workflowView.getSite().getShell();

    /* Find scenario */
    final IScenario scenario = findParentScenario( event );
    if( scenario == null )
    {
      final String commandName = HandlerUtils.getCommandName( event );
      final String message = Messages.getString( "AddScenarioHandler_0" ); //$NON-NLS-1$
      MessageDialog.openInformation( shell, commandName, message );
      return null;
    }

    final IScenarioOperation operation = new CreateScenarioOperation();
    final ScenarioData data = new ScenarioData( scenario, scenario, operation, false );
    data.setCopySubScenariosEnabled( false );

    ScenarioWizard.stopTaskAndOpenWizard( shell, data );

    return null;
  }

  private IScenario findParentScenario( final ExecutionEvent event )
  {
    final IScenario currentCase = ScenarioHelper.getActiveScenario();

    final IScenario selectedScenario = ScenarioHandlerUtils.findScenario( event );

    if( selectedScenario != null )
      return selectedScenario;

    return currentCase;
  }
}