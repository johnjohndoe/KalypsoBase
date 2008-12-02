package org.kalypso.contribs.eclipse.swt.graphics;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author gernot
 */
public final class RectangleUtils
{
  private RectangleUtils( )
  {
  }

  public static final Rectangle createNormalizedRectangle( final Rectangle r )
  {
    final int x = Math.min( r.x, r.x + r.width );
    final int y = Math.min( r.y, r.y + r.height );
    final int w = Math.abs( r.width );
    final int h = Math.abs( r.height );

    return new Rectangle( x, y, w, h );
  }

  public static final Rectangle createNormalizedRectangle( final Point p1, final Point p2 )
  {
    final int x = Math.min( p1.x, p2.x );
    final int y = Math.min( p1.y, p2.y );
    final int w = Math.abs( p1.x - p2.x );
    final int h = Math.abs( p1.y - p2.y );

    return new Rectangle( x, y, w, h );
  }

  public final static Rectangle buffer( final Point p )
  {
    return new Rectangle( p.x - 5, p.y - 5, 10, 10 );
  }
}
