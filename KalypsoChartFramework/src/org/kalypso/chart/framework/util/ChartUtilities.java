package org.kalypso.chart.framework.util;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.data.impl.DataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;

/**
 * @author burtscher some helper methods to ease your everyday life programming chart stuff
 */
public class ChartUtilities
{
  private ChartUtilities( )
  {
    // not to be instanciated
  }

  /**
   * @return true if the screen coordinates should be inverted
   */
  public static boolean isInverseScreenCoords( final IAxis< ? > axis )
  {
    final ORIENTATION ori = axis.getPosition().getOrientation();
    final DIRECTION dir = axis.getDirection();

    return ori == ORIENTATION.VERTICAL && dir == DIRECTION.POSITIVE || ori == ORIENTATION.HORIZONTAL && dir == DIRECTION.NEGATIVE;
  }

  /**
   * sets the given GC to an initial state - this methosd should be called before any chart painting action is processed
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
    final IAxis< ? >[] axes = chart.getMapperRegistry().getAxes();
    chart.autoscale( axes );
  }

  /**
   * finds the smallest and biggest value of all ranges and creates a new DataRange with these values
   */
  public static <T> IDataRange<T> mergeDataRanges( IDataRange<T>[] ranges, Comparator<T> comparator )
  {

    // if there are no input ranges, we return null
    if( ranges.length == 0 )
    {
      return null;
    }

    T min = null;
    T max = null;

    for( final IDataRange<T> element : ranges )
    {
      if( min == null || comparator.compare( min, element.getMin() ) > 0 )
        min = element.getMin();
      if( max == null || comparator.compare( max, element.getMax() ) < 0 )
        max = element.getMax();
    }

    try
    {
      final DataRange<T> range = new DataRange<T>( min, max );
      return range;
    }
    catch( final ZeroSizeDataRangeException e )
    {
      e.printStackTrace(); // etwas was ein treffen darf (auch wenns dubios ist) sollte man nicht auf die konsole
      // loggen

      // TODO: what to do in that case?
      return null;
    }
  }

  /**
   * determines screen values describing the minimal and maximal values concerning a dragged rectangle
   */
  public static IDataRange<Integer> rectangleToAxisSection( IAxis< ? > axis, Rectangle rect )
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
      // verticale Achse verläuft wenn positiv von unten nach oben
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
    return new ComparableDataRange<Integer>( new Integer[] { min, max } );
  }

}
