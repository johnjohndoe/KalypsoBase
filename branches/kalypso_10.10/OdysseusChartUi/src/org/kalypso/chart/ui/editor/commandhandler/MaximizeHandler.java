package org.kalypso.chart.ui.editor.commandhandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.kalypso.chart.ui.IChartPart;

import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * This handler sets the axis ranges of all axes to the union range of all visible layers' ranges
 * 
 * 
 * @author burtscher1
 * 
 */
public class MaximizeHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final IChartPart chartPart = ChartHandlerUtilities.findChartComposite( context );
    if( chartPart == null )
      return null;
    ChartUtilities.maximize( chartPart.getChartComposite().getChartModel() );

    return null;
  }

}
