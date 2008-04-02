package de.belger.swtchart.action;

import org.eclipse.swt.graphics.Point;

import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.axis.AxisRange;
import de.belger.swtchart.util.LogicalRange;
import de.belger.swtchart.util.SwitchDelegate;

/**
 * @author gernot
 */
public final class ZoomInDragAction extends RectangleDragAction
{
  public ZoomInDragAction( final ChartCanvas chart, final AxisRange[] ranges )
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

      final int screenFrom = crdSwitch.getX( start );
      final int screenTo = crdSwitch.getX( stop );

      double from = range.screen2Logical( screenFrom );
      double to = range.screen2Logical( screenTo );

      
      range.setLogicalRange( new LogicalRange( from, to  ),true);//(getChart().getFixAspectRatio() == null) );
    }

    getChart().repaint();
  }
}
