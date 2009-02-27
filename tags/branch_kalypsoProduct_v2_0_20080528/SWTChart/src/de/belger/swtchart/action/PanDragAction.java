package de.belger.swtchart.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;


import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.axis.AxisRange;
import de.belger.swtchart.util.LogicalRange;
import de.belger.swtchart.util.SwitchDelegate;

/**
 * @author gernot
 * 
 */
public class PanDragAction implements IChartDragAction
{
  private final ChartCanvas m_chart;

  private final AxisRange[] m_ranges;

  public PanDragAction( final ChartCanvas chart, final AxisRange[] ranges )
  {
    m_chart = chart;
    m_ranges = ranges;
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#dragFinished(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point)
   */
  public void dragFinished( final Point start, final Point stop )
  {
    final int width = stop.x - start.x;
    final int height = stop.y - start.y;

    final Point offset = new Point( width, height );

    for( int i = 0; i < m_ranges.length; i++ )
    {
      final AxisRange range = m_ranges[i];
      final SwitchDelegate crdSwitch = range.getSwitch();

      final double logicalOffset = range.screenLength2Logical( crdSwitch.getX( offset ) );

      final double from = range.getLogicalFrom() - logicalOffset;
      final double to = range.getLogicalTo() - logicalOffset;

      range.setLogicalRange( new LogicalRange( from, to ) );

      m_chart.repaint();
    }

    m_chart.setDrawOffset( null );
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#dragTo(org.eclipse.swt.graphics.Rectangle)
   */
  public void dragTo( final Point start, final Point stop )
  {
    final int width = stop.x - start.x;
    final int height = stop.y - start.y;

    m_chart.setDrawOffset( new Point( width, height ) );
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#getCursorType()
   */
  public int getCursorType( )
  {
    return SWT.CURSOR_SIZEALL;
  }
}
