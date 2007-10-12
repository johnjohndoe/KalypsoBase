package org.kalypso.chart.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.chart.framework.view.ChartComposite;

/**
 * @author gernot
 */
public class MaximizeAction extends Action
{
  private final ChartComposite m_chart;

  public MaximizeAction( final ChartComposite chart )
  {
    super( "Maximize", AS_PUSH_BUTTON );

    m_chart = chart;

    setToolTipText( "Maximize chart to show all data." );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    ChartUtilities.maximize( m_chart.getModel() );
  }
}
