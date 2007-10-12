package org.kalypso.chart.ui.editor.actions;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.view.ChartComposite;

/**
 * ZoomOut for ChartCanvas. Zooms out such that the current visible area will be squeezed into the dragged rectangle.
 * 
 * @author gernot
 */
public final class ZoomOutDragAction extends RectangleDragAction
{
  public ZoomOutDragAction( final ChartComposite chart, final IAxis[] ranges )
  {
    super( chart, ranges );
  }

  /**
   * @see de.belger.swtchart.action.IChartDragAction#runWithRect(org.eclipse.swt.graphics.Rectangle)
   */
  public void dragFinished( final Point start, final Point stop )
  {
    if( Math.abs( start.x - stop.x ) < 10 && Math.abs( start.y - stop.y ) < 10 )
      return;
/*
 * final IAxis[] axes = getRanges(); for( int i = 0; i < axes.length; i++ ) { final IAxis axis = axes[i]; final
 * SwitchDelegate crdSwitch = axis.getSwitch(); IDataRange<Number> numericRange = axis.getNumericRange(); final double
 * oldfrom = axis.getLogicalFrom(); final double oldto = axis.getLogicalTo(); final int startx = crdSwitch.getX( start );
 * final int stopx = crdSwitch.getX( stop ); final int screenfrom = axis.getScreenFrom(); final int screento =
 * axis.getScreenTo(); final int screenwidth = (screento - screenfrom); final double a = screento - startx; final double
 * b = startx - screenfrom; final double c = screento - stopx; final double d = stopx - screenfrom; final double x =
 * oldfrom * screenwidth; final double y = oldto * screenwidth; final double fakt = 1 / (a * d - b * c); final double
 * from = fakt * (d * x - b * y); final double to = fakt * (a * y - c * x); axis.setLogicalRange( new LogicalRange(
 * from, to ) ); } (/ getChart().redraw(); } }
 */
  }
}