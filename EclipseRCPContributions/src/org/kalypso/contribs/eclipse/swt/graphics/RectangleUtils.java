package org.kalypso.contribs.eclipse.swt.graphics;

import java.awt.Insets;

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

  public static final Rectangle inflateRect( final Rectangle rect, final Insets insets )
  {
    if( rect == null )
      return null;
    return new Rectangle( rect.x + insets.left, rect.y + insets.top, rect.width - insets.left - insets.right, rect.height - insets.bottom - insets.top );
  }

  public static final Rectangle bufferRect( final Rectangle rect, final Insets insets )
  {
    if( rect == null )
      return null;
    return new Rectangle( rect.x - insets.left, rect.y - insets.top, rect.width + insets.left + insets.right, rect.height + insets.bottom + insets.top );
  }

  public static final Rectangle bufferRect( final Rectangle rect, final int inset )
  {
    return bufferRect( rect, new Insets( inset, inset, inset, inset ) );
  }

  public static final Rectangle inflateRect( final Rectangle rect, final int inset )
  {
    return inflateRect( rect, new Insets( inset, inset, inset, inset ) );
  }

  public static final Point getCenterPoint( final Rectangle r )
  {
    final int x = Math.min( r.x, r.x + r.width );
    final int y = Math.min( r.y, r.y + r.height );
    final int w = Math.abs( r.width );
    final int h = Math.abs( r.height );

    return new Point( x + w / 2, y + h / 2 );
  }

  public final static Rectangle buffer( final Point p )
  {
    if( p == null )
      return null;
    return new Rectangle( p.x - 5, p.y - 5, 10, 10 );
  }
}
