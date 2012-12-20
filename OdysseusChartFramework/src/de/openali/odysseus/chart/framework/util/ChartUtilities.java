package de.openali.odysseus.chart.framework.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;

/**
 * @author burtscher some helper methods to ease your everyday life programming chart stuff
 */
public final class ChartUtilities
{
  private ChartUtilities( )
  {
    // not to be instanciated
  }

  /**
   * sets the given GC to an initial state - this methods should be called before any chart painting action is processed
   */
  public static void resetGC( final GC gc )
  {
    final Device dev = gc.getDevice();
    gc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );
    gc.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gc.setLineWidth( 1 );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setLineJoin( SWT.JOIN_ROUND );
    gc.setAlpha( 255 );
    gc.setAntialias( SWT.ON );
  }

  /**
   * maximises the chart view - that means all the available data of all layers is shown
   */
  public static void maximize( final IChartModel chart )
  {
    final IAxis[] axes = chart.getAxisRegistry().getAxes();
    chart.autoscale( axes );
  }

  /**
   * finds the smallest and biggest value of all ranges and creates a new DataRange with these values
   */
  public static IDataRange<Number> mergeDataRanges( final IDataRange< ? >[] ranges )
  {
    // if there are no input ranges, we return null
    if( ranges.length == 0 )
    {
      return null;
    }

    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;

    for( final IDataRange< ? > element : ranges )
    {
      final double eltMin = element.getMin() == null ? Double.NaN : ((Number) element.getMin()).doubleValue();
      if( Double.isNaN( eltMin ) )
        continue;
      final double eltMax = element.getMax() == null ? Double.NaN : ((Number) element.getMax()).doubleValue();
      if( Double.isNaN( eltMax ) )
        continue;

      min = Math.min( min, eltMin );
      max = Math.max( max, eltMax );
    }

    return DataRange.createFromComparable( (Number) min, (Number) max );
  }

  /**
   * determines screen values describing the minimal and maximal values concerning a dragged rectangle
   */
  public static IDataRange<Integer> rectangleToAxisSection( final IAxis axis, final Rectangle rect )
  {
    int min;
    int max;

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        min = rect.x;
        max = min + rect.width;
      }
      else
      {
        min = rect.x + rect.width;
        max = rect.x;
      }
    }
    else
    {
      // verticale Achse verl�uft wenn positiv von unten nach oben
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        min = rect.y + rect.height;
        max = rect.y;
      }
      else
      {
        min = rect.y;
        max = rect.y + rect.height;
      }
    }

    return DataRange.createFromComparable( min, max );
  }

  /**
   * This function returns the display.
   *
   * @return The display.
   */
  public static Display getDisplay( )
  {
    return Display.getCurrent();
  }
}