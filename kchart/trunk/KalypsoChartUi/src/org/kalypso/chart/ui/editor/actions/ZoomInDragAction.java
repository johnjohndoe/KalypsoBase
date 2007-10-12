package org.kalypso.chart.ui.editor.actions;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.chart.framework.view.ChartComposite;

/**
 * @author gernot
 */
public final class ZoomInDragAction extends RectangleDragAction
{
  public ZoomInDragAction( final ChartComposite chart, final IAxis[] axes )
  {
    super( chart, axes );
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#runWithRect(org.eclipse.swt.graphics.Rectangle)
   */
  public void dragFinished( final Point start, final Point stop )
  {
    if( Math.abs( start.x - stop.x ) < 10 && Math.abs( start.y - stop.y ) < 10 )
      return;

    final IAxis< ? >[] axes = getAxes();

    int rectx = Math.min( start.x, stop.x );
    int recty = Math.min( start.y, stop.y );
    int rectwidth = Math.max( start.x, stop.x ) - rectx;
    int rectheight = Math.max( start.y, stop.y ) - recty;

    Rectangle rect = new Rectangle( rectx, recty, rectwidth, rectheight );
    for( int i = 0; i < axes.length; i++ )
    {
      final IAxis< ? > axis = axes[i];
      IDataRange<Integer> section = ChartUtilities.rectangleToAxisSection( axis, rect );

      IDataRange<Number> newRange = new ComparableDataRange<Number>( new Number[] { axis.screenToNumeric( section.getMin() ), axis.screenToNumeric( section.getMax() ) } );

      axis.setNumericRange( newRange );
    }

    getChart().redraw();
  }
}
