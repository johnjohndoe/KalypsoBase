package de.belger.swtchart.action;

import org.eclipse.swt.graphics.Point;

import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.axis.AxisRange;
import de.belger.swtchart.util.LogicalRange;
import de.belger.swtchart.util.SwitchDelegate;

/**
 * ZoomOut for ChartCanvas.
 * 
 * Zooms out such that the current visible area will 
 * be squeezed into the dragged rectangle. 
 * 
 * @author gernot
 *
 */
public final class ZoomOutDragAction extends RectangleDragAction
{
  public ZoomOutDragAction( final ChartCanvas chart, final AxisRange[] ranges )
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

    final AxisRange[] ranges = getRanges();
    for( int i = 0; i < ranges.length; i++ )
    {
      final AxisRange range = ranges[i];
      final SwitchDelegate crdSwitch = range.getSwitch();

      final double oldfrom = range.getLogicalFrom();
      final double oldto = range.getLogicalTo();
      
      final int startx = crdSwitch.getX( start );
      final int stopx = crdSwitch.getX( stop );
      
      final int screenfrom = range.getScreenFrom();
      final int screento = range.getScreenTo();
      final int screenwidth = ( screento - screenfrom );
      
      final double a = screento - startx;
      final double b = startx - screenfrom;
      final double c = screento - stopx;
      final double d = stopx - screenfrom;
      final double x = oldfrom * screenwidth;
      final double y = oldto * screenwidth;

      final double fakt = 1 / ( a * d - b * c );
      final double from = fakt * ( d * x -  b * y );
      final double to = fakt * ( a * y - c * x );
      
      range.setLogicalRange( new LogicalRange( from, to ) );
    }
    
    getChart().repaint();
  }
}
