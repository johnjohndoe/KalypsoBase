/**
 *
 */
package org.kalypso.afgui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.afgui.internal.handlers.CreateScenarioOperation;
import org.kalypso.afgui.internal.handlers.IScenarioOperation;
import org.kalypso.afgui.internal.handlers.ScenarioData;
import org.kalypso.afgui.internal.handlers.ScenarioWizard;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.afgui.views.WorkflowView;
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
      final String message = Messages.getString("AddScenarioHandler_0"); //$NON-NLS-1$
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

    final IScenario selectedScenario = findScenario( event );

    if( selectedScenario != null )
      return selectedScenario;

    return currentCase;
  }

  public static IScenario findScenario( final ExecutionEvent event )
  {
    final ISelection selection = HandlerUtil.getCurrentSelection( event );
    return findSelectedScenario( selection );
  }

  /**
   * Get the selected scenario from a selection (first element of selection ).
   * 
   * @return the selected scenario of <code>null</code>
   */
  private static IScenario findSelectedScenario( final ISelection selection )
  {
    if( !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection) selection;

    if( structSel.isEmpty() )
      return null;

    final Object o = structSel.getFirstElement();
    if( o instanceof IScenario )
      return (IScenario) o;

    return null;
  }
}