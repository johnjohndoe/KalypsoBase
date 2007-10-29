package org.kalypso.chart.ui.editor.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.mouse.DragZoomOutHandler;

public class ZoomOutHandler extends AbstractHandler
{

  @Override
  public Object execute( ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );

    final ChartComposite chart = (ChartComposite) part.getAdapter( ChartComposite.class );
    if( chart == null && part instanceof IChartPart )
      return null;
    else
    {
      DragZoomOutHandler dzh = new DragZoomOutHandler( chart );
      ((IChartPart) part).getChartDragHandler().setActiveHandler( dzh, event.getCommand() );
    }
    return null;
  }

}
