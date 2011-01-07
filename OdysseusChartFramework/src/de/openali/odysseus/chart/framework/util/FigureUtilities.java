package de.openali.odysseus.chart.framework.util;

import org.eclipse.swt.graphics.Point;

public class FigureUtilities
{

  /**
   * creates an array of integer values containing alternately x and y position of the given points
   * 
   * @param points
   * @return
   */
  public static int[] pointArrayToIntArray( final Point[] points )
  {
    final int[] intArray = new int[points.length * 2];
    for( int i = 0; i < points.length; i++ )
    {
      intArray[i * 2] = points[i].x;
      intArray[(i * 2) + 1] = points[i].y;
    }
    return intArray;
  }

  /**
   * Transforms a position assumed to be the center of a rectangle (by given width and height) to a position
   * representing the left top of the rectangle. Warning: if width or height are straight numbers, the returned position
   * will be displaced by one half pixel to the left or top
   * 
   * @param point
   *          Point representing the center of the rectangle
   * @param width
   *          width of the rectangle
   * @param height
   *          height of the rectangle
   * @return a new Point object representing the left top position of the rectangle
   */
  public static Point centerToLeftTop( final Point point, final int width, final int height )
  {
    final int x = point.x - (int) (width / 2.0f);
    final int y = point.y - (int) (height / 2.0f);
    return new Point( x, y );
  }

  /**
   * Translates a polygon to a position
   * 
   * @param points
   *          array of points describing the polygon where x and y are greater than zero
   * @param pos
   *          left top of new position
   * @return
   */
  public static Point[] translateTo( final Point[] points, final Point pos )
  {
    final Point min = getMin( points );
    final Point[] translatedPolygon = new Point[points.length];

    for( int i = 0; i < points.length; i++ )
    {
      final Point oldPoint = points[i];
      final Point newPoint = new Point( oldPoint.x - (min.x - pos.x), oldPoint.y - (min.y - pos.y) );
      translatedPolygon[i] = newPoint;
    }
    return translatedPolygon;
  }

  /**
   * inverts the y-Coordinates of the points; the vertical screen coordinates are counted from top to bottom; users
   * think in coordinate systems which count from bottom to top
   * 
   * @param points
   * @param pos
   * @return Array of inverted points
   */
  public static Point[] invertY( final Point[] points )
  {
    final Point max = getMax( points );
    final Point[] invertedPolygon = new Point[points.length];

    for( int i = 0; i < points.length; i++ )
    {
      final Point oldPoint = points[i];
      final Point newPoint = new Point( oldPoint.x, max.y - oldPoint.y );
      invertedPolygon[i] = newPoint;
    }
    return invertedPolygon;
  }

  /**
   * moves a polygon to the origin and resizes it to fit into a rectangle by the given width and height
   * 
   * @param points
   *          points describing the polygon
   * @param width
   *          width of the rectangle
   * @param height
   *          height of the rectangle
   * @return
   */
  public static Point[] resizeInOrigin( final Point[] points, final int width, final int height )
  {
    // in Ursprung verschieben
    final Point[] translated = translateTo( points, new Point( 0, 0 ) );

    // Größe ändern
    final Point[] resizedPolygon = new Point[points.length];
    final Point max = getMax( translated );

    for( int i = 0; i < translated.length; i++ )
    {
      final Point oldPoint = translated[i];
      final int resizedX = (int) (((float) oldPoint.x / (float) max.x) * width);
      final int resizedY = (int) (((float) oldPoint.y / (float) max.y) * height);
      resizedPolygon[i] = new Point( resizedX, resizedY );
    }
    return resizedPolygon;
  }

  /**
   * @param points
   *          array of points
   * @return point containing the smallest x and y values
   */
  public static Point getMin( final Point[] points )
  {
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    for( final Point p : points )
    {
      if( p.x < minX )
      {
        minX = p.x;
      }
      if( p.y < minY )
      {
        minY = p.y;
      }
    }
    return new Point( minX, minY );
  }

  /**
   * @param points
   *          array of points
   * @return point containing the biggest x and y values
   */
  public static Point getMax( final Point[] points )
  {
    int maxX = 0;
    int maxY = 0;
    for( final Point p : points )
    {
      if( p.x > maxX )
      {
        maxX = p.x;
      }
      if( p.y > maxY )
      {
        maxY = p.y;
      }
    }
    return new Point( maxX, maxY );
  }

}
