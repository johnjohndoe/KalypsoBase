package de.openali.odysseus.chart.framework.util.math;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

/**
 * @author alibu
 */
public class ChartMath
{

  /**
   * Calculates angle alpha from an acute-angled triangle defined by the points pointA, pointB, pointC using the law of
   * cosines
   * 
   * @return angle alpha
   */
  public static double alphaFromTriangle( Point pointA, Point pointB, Point pointC )
  {
    final double sideA = Math.sqrt( Math.pow( (pointC.x - pointB.x), 2 ) + Math.pow( (pointC.y - pointB.y), 2 ) );
    final double sideB = Math.sqrt( Math.pow( (pointC.x - pointA.x), 2 ) + Math.pow( (pointC.y - pointA.y), 2 ) );
    final double sideC = Math.sqrt( Math.pow( (pointA.x - pointB.x), 2 ) + Math.pow( (pointA.y - pointB.y), 2 ) );
    final double cosAlpha = (Math.pow( sideB, 2 ) + Math.pow( sideC, 2 ) - Math.pow( sideA, 2 )) / (2 * sideB * sideC);
    final double alpha = Math.acos( cosAlpha );
    return alpha;
  }

  /**
   * returns the length of the perpendicular through pointA in an acute-angled triangle defined by pointA, pointB and
   * pointC
   */
  public static double perpendicularThrougB( Point pointA, Point pointB, Point pointC )
  {
    final double sideC = Math.sqrt( Math.pow( (pointA.x - pointB.x), 2 ) + Math.pow( (pointA.y - pointB.y), 2 ) );
    final double alpha = alphaFromTriangle( pointA, pointB, pointC );
    final double height = sideC * Math.sin( alpha );

    return height;
  }

  /**
   * Implementation of Douglas-Peuker-Algorithm
   */
  public static List<Point> douglasPeucker( List<Point> path, double epsilon )
  {
    final int pathSize = path.size();
    if( pathSize > 2 && epsilon > 0 )
    {
      final List<Point> newPath = new ArrayList<Point>();
      // Start und Endpunkt bestimmen
      final Point start = path.get( 0 );
      final Point end = path.get( pathSize - 1 );

      // l�ngste Entfernung finden
      double maxHeight = 0;
      int maxHeightIndex = 0;

      for( int i = 1; i < pathSize - 1; i++ )
      {
        final Point p = path.get( i );
        final double pHeight = perpendicularThrougB( start, p, end );
        if( maxHeight < pHeight )
        {
          // wenn pHeight gr��er maxHeight: maxHeight anpassen
          maxHeight = pHeight;
          // und index merken
          maxHeightIndex = i;
        }
      }
      // wenn
      if( maxHeight < epsilon )
      {
        newPath.add( start );
        newPath.add( end );
      }
      else
      {
        final List<Point> firstPath = path.subList( 0, maxHeightIndex );
        final List<Point> secondPath = path.subList( maxHeightIndex, pathSize - 1 );
        newPath.addAll( douglasPeucker( firstPath, epsilon ) );
        newPath.addAll( douglasPeucker( secondPath, epsilon ) );
      }
      return newPath;
    }
    else
      return path;
  }

}
