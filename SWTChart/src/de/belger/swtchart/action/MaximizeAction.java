package de.belger.swtchart.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;

import de.belger.swtchart.ChartCanvas;

/**
 * @author gernot
 *
 */
public class MaximizeAction extends Action
{
  private final ChartCanvas m_chart;

  public MaximizeAction( final ChartCanvas chart )
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
    m_chart.maximize();
  }
}
