package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;

import de.renew.workflow.connector.cases.IScenario;

public class ActivateScenarioHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final ISelection selection = HandlerUtil.getCurrentSelection( event );
    if( selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection )
    {
      final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      final Object firstElement = structuredSelection.getFirstElement();
      if( firstElement instanceof IScenario )
      {
        try
        {
          final IScenario scenario = (IScenario) firstElement;
          ScenarioHelper.activateScenario( scenario );
        }
        catch( final CoreException e )
        {
          final Shell shell = HandlerUtil.getActiveShellChecked( event );
          final IStatus status = e.getStatus();
          ErrorDialog.openError( shell, Messages.getString( "org.kalypso.afgui.handlers.ActivateScenarioHandler.0" ), Messages.getString( "org.kalypso.afgui.handlers.ActivateScenarioHandler.1" ), status ); //$NON-NLS-1$ //$NON-NLS-2$

          final KalypsoAFGUIFrameworkPlugin plugin = KalypsoAFGUIFrameworkPlugin.getDefault();
          plugin.getLog().log( status );
        }
      }
    }

    return Status.OK_STATUS;
  }
}