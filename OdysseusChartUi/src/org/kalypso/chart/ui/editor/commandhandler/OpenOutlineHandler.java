package org.kalypso.chart.ui.editor.commandhandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.chart.ui.view.ChartOutlinePopupDialog;

import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.view.IChartComposite;

public class OpenOutlineHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartComposite chartPart = (IChartComposite) context.getVariable( ChartSourceProvider.ACTIVE_CHART_NAME );
    if( chartPart == null )
      return null;

    final ChartOutlinePopupDialog d = new ChartOutlinePopupDialog( (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME ), chartPart );
    d.open();
    final Shell shell = d.getShell();
    final Point shellSize = shell.getSize();
    final Point mousePos = ChartUtilities.getDisplay().getCursorLocation();
    shell.setBounds( new Rectangle( mousePos.x - shellSize.x, mousePos.y, shellSize.x, shellSize.y ) );

    return null;
  }
}