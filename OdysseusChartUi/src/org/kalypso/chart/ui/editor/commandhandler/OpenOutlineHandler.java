package org.kalypso.chart.ui.editor.commandhandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.view.ChartOutlinePopupDialog;

public class OpenOutlineHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartPart chartPart = ChartHandlerUtilities.findChartComposite( context );
    if( chartPart == null )
      return null;

    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );
    if( part == null )
      throw new ExecutionException( "No active part." ); //$NON-NLS-1$

    final IWorkbenchPartSite site = part.getSite();
    if( site == null )
      throw new ExecutionException( "No active site." ); //$NON-NLS-1$

    final IWorkbenchPage page = site.getPage();
    if( page == null )
      throw new ExecutionException( "No active page." ); //$NON-NLS-1$

    final ChartOutlinePopupDialog d = new ChartOutlinePopupDialog( (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME ), chartPart );

    d.open();
    final Shell shell = d.getShell();
    final Point shellSize = shell.getSize();
    final Point mousePos = Display.getCurrent().getCursorLocation();
    shell.setBounds( new Rectangle( mousePos.x - shellSize.x, mousePos.y, shellSize.x, shellSize.y ) );

    return null;
  }
}
