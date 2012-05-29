package org.kalypso.afgui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.kalypso.contribs.eclipse.jface.window.ShellProvider;

public class DeleteResourceHandler extends AbstractHandler implements IHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final IStructuredSelection selection = (IStructuredSelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );

    final IShellProvider shellProvider = new ShellProvider( shell );

    final DeleteResourceAction deleteResourceAction = new DeleteResourceAction( shellProvider );
    deleteResourceAction.selectionChanged( selection );
    deleteResourceAction.run();

    return null;
  }
}
