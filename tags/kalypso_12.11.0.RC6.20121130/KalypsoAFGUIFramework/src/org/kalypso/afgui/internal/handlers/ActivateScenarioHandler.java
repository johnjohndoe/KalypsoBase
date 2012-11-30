package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.afgui.scenarios.ScenarioHelper;

import de.renew.workflow.connector.cases.IScenario;

public class ActivateScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final ISelection selection = HandlerUtil.getCurrentSelection( event );
    final Shell shell = HandlerUtil.getActiveShell( event );

    if( selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection )
    {
      final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      final Object firstElement = structuredSelection.getFirstElement();
      if( firstElement instanceof IScenario )
      {
        final IScenario scenario = (IScenario) firstElement;
        ScenarioHelper.activateScenario2( shell, scenario );
      }
    }

    return Status.OK_STATUS;
  }
}