package org.kalypso.commons.math.geom;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Comparator;

import org.kalypso.contribs.java.util.DoubleComparator;

/**
 * Utilities for java.awt.geom.Point2D
 * 
 * @author schlienger
 */
public class PointUtilities
{
  /**
   * Creates a new array having the x and y coordinates
   */
  public static Point2D[] createPoints( final double[] x, final double[] y )
  {
    if( x == null || y == null )
      throw new IllegalArgumentException( "null argument(s)" ); //$NON-NLS-1$

    if( x.length != y.length )
      throw new IllegalArgumentException( "arrays length not equal" ); //$NON-NLS-1$

    final Point2D[] pts = new Point2D[x.length];

    for( int i = 0; i < y.length; i++ )
      pts[i] = new Point2D.Double( x[i], y[i] );

    return pts;
  }

  /**
   * sorts the array of Points on X coordinates
   * 
   * @param pts
   * @param delta
   *          used for double comparison
   */
  public static void sortX( final Point2D[] pts, final double delta )
  {
    java.util.Arrays.sort( pts, new PointXComparator( delta ) );
  }

  /**
   * sorts the array of Points on Y coordinates
   * 
   * @param pts
   * @param delta
   *          used for double comparison
   */
  public static void sortY( final Point2D[] pts, final double delta )
  {
    java.util.Arrays.sort( pts, new PointYComparator( delta ) );
  }

  /**
   * computes the segment's length.
   * 
   * @param p1
   * @param p2
   * @return
   * @see segmentLength( double, double, double, double )
   */
  public static double segmentLength( final Point p1, final Point p2 )
  {
    return segmentLength( p1.getX(), p1.getY(), p2.getX(), p2.getY() );
  }

  /**
   * computes the length of the segment defined by the points [x1, y1] and [x2, y2], using Pythagore.
   * 
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return
   */
  public static double segmentLength( final double x1, final double y1, final double x2, final double y2 )
  {
    final double xdif = x2 - x1;
    final double ydif = y2 - y1;

    return Math.sqrt( xdif * xdif + ydif * ydif );
  }

  /**
   * Computes the distance between two points using Thales Theorem.
   * 
   * @param p1
   * @param p2
   * @return
   */
  public static double distance( final Point2D p1, final Point2D p2 )
  {
    final double dx = Math.abs( p1.getX() - p2.getX() );
    final double dy = Math.abs( p1.getY() - p2.getY() );

    return Math.sqrt( dx * dx + dy * dy );
  }

  /**
   * Compares points on the X coordinate
   * 
   * @author schlienger
   */
  public static class PointXComparator implements Comparator<Point2D>
  {
    /** makes the comparison on the x values */
    private final DoubleComparator m_dc;

    /**
     * Constructor
     * 
     * @param delta
     *          used for double precision
     */
    public PointXComparator( final double delta )
    {
      m_dc = new DoubleComparator( delta );
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare( final Point2D p1, final Point2D p2 )
    {
      return m_dc.compare( p1.getX(), p2.getX() );
    }
  }

  /**
   * Compares points on the Y coordinate
   * 
   * @author schlienger
   */
  public static class PointYComparator implements Comparator<Point2D>
  {
    /** makes the comparison on the x values */
    private final DoubleComparator m_dc;

    /**
     * Constructor
     * 
     * @param delta
     *          used for double precision
     */
    public PointYComparator( final double delta )
    {
      m_dc = new DoubleComparator( delta );
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare( final Point2D p1, final Point2D p2 )
    {
      return m_dc.compare( p1.getY(), p2.getY() );
    }
  }
}
