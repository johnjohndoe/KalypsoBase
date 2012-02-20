package org.kalypso.contribs.java.awt;

import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 * Compares {@link Point2D} by their x value.
 *
 * @author Gernot Belger
 */
public final class Point2DXComparator implements Comparator<Point2D>
{
  @Override
  public int compare( final Point2D o1, final Point2D o2 )
  {
    return Double.compare( o1.getX(), o2.getX() );
  }
}