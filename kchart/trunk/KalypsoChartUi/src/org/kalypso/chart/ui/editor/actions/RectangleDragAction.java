package org.kalypso.chart.ui.editor.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

/**
 * @author gernot
 */
public abstract class RectangleDragAction implements IChartDragAction
{
  private final ChartComposite m_chart;

  private final IAxis[] m_axess;

  public RectangleDragAction( final ChartComposite chart, final IAxis[] ranges )
  {
    m_chart = chart;
    m_axess = ranges;
  }

  /**
   * @return Returns the chart.
   */
  protected final ChartComposite getChart( )
  {
    return m_chart;
  }

  /**
   * @return Returns the ranges.
   */
  protected final IAxis< ? >[] getAxes( )
  {
    return m_axess;
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#dragTo(org.eclipse.swt.graphics.Rectangle)
   */
  public final void dragTo( final Point start, final Point stop )
  {
    m_chart.getPlot().setDragArea( RectangleUtils.createNormalizedRectangle( start, stop ) );
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#getCursorType()
   */
  public final int getCursorType( )
  {
    return SWT.CURSOR_CROSS;
  }
}
