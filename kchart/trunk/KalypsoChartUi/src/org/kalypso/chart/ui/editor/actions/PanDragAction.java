package org.kalypso.chart.ui.editor.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.view.ChartComposite;

/**
 * @author gernot
 */
public class PanDragAction implements IChartDragAction
{
  private final ChartComposite m_chart;

  private final IAxis< ? >[] m_axes;

  public PanDragAction( final ChartComposite chart, final IAxis< ? >[] ranges )
  {
    m_chart = chart;
    m_axes = ranges;
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#dragFinished(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point)
   */
  public void dragFinished( final Point start, final Point stop )
  {
    for( int i = 0; i < m_axes.length; i++ )
    {
      final IAxis< ? > axis = m_axes[i];
      IDataOperator dataOperator = axis.getDataOperator();

      int axisStart, axisStop;

      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        if( axis.getDirection() == DIRECTION.POSITIVE )
        {
          axisStart = start.x;
          axisStop = stop.x;
        }
        else
        {
          axisStart = stop.x;
          axisStop = start.x;
        }

      }
      else
      {
        if( axis.getDirection() == DIRECTION.POSITIVE )
        {
          axisStart = stop.y;
          axisStop = start.y;
        }
        else
        {
          axisStart = start.y;
          axisStop = stop.y;
        }
      }

      double numericOffset = axis.screenToNumeric( axisStop ).doubleValue() - axis.screenToNumeric( axisStart ).doubleValue();

      IDataRange<Number> numericRange = axis.getNumericRange();
      axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { numericRange.getMin().doubleValue() + numericOffset, numericRange.getMax().doubleValue() + numericOffset } ) );

      m_chart.redraw();
    }

  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#dragTo(org.eclipse.swt.graphics.Rectangle)
   */
  public void dragTo( final Point start, final Point stop )
  {

  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#getCursorType()
   */
  public int getCursorType( )
  {
    return SWT.CURSOR_SIZEALL;
  }
}
