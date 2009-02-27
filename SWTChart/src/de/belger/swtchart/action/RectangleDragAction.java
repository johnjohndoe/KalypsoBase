package de.belger.swtchart.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.axis.AxisRange;

/**
 * @author gernot
 *
 */
public abstract class RectangleDragAction implements IChartDragAction
{
  private final ChartCanvas m_chart;
  private final AxisRange[] m_ranges;

  public RectangleDragAction( final ChartCanvas chart, final AxisRange[] ranges )
  {
    m_chart = chart;
    m_ranges = ranges;
  }
  
  /**
   * @return Returns the chart.
   */
  protected final ChartCanvas getChart( )
  {
    return m_chart;
  }
  
  /**
   * @return Returns the ranges.
   */
  protected final AxisRange[] getRanges( )
  {
    return m_ranges;
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#dragTo(org.eclipse.swt.graphics.Rectangle)
   */
  public final void dragTo( final Point start, final Point stop )
  {
    m_chart.setDragArea(RectangleUtils.createNormalizedRectangle( start, stop )  );
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#getCursorType()
   */
  public final int getCursorType( )
  {
    return SWT.CURSOR_CROSS;
  }
}
