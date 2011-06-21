package org.kalypso.chart.ui.editor.commandhandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * This handler sets the axis ranges of all axes to the union range of all visible layers' ranges
 * 
 * @author burtscher1
 */
public class MaximizeHandler extends AbstractHandler
{
  private IChartComposite m_chartComposite = null;

  public MaximizeHandler( final IChartComposite chartComposite )
  {
    super();
    m_chartComposite = chartComposite;
  }

  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final IChartComposite chart = getChartComposite( context );
    if( chart == null || chart.getChartModel() == null )
      return Status.CANCEL_STATUS;
    final IChartModel model = chart.getChartModel();
    model.autoscale( new IAxis[] {} );

    return Status.OK_STATUS;
  }

  private IChartComposite getChartComposite( final IEvaluationContext context )
  {
    if( m_chartComposite != null )
      return m_chartComposite;
    return ChartHandlerUtilities.getChart( context );
  }

}
