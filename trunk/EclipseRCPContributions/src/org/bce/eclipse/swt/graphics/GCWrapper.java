package org.bce.eclipse.swt.graphics;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

/**
 * Same as {@link org.eclipse.swt.graphics.GC}, but adds helper methods and
 * perform some sanity checks.
 * 
 * @author gernot
 * 
 */
public class GCWrapper
{
  public final GC m_gc;

  public GCWrapper( final GC gc )
  {
    m_gc = gc;
  }

  public void dispose( )
  {
    m_gc.dispose();
  }

  public void drawrectangle( final Rectangle r )
  {
    drawRectangle( r.x, r.y, r.width, r.height );
  }

  public void drawRectangle( final int x, final int y, final int width,
      final int height )
  {
    m_gc.drawRectangle( x, y, Math.max( 1, width ), Math.max( 1, height ) );
  }

  public void drawLine( final int x1, final int y1, final int x2, final int y2 )
  {
    m_gc.drawLine( Math.max( -10000, Math.min( 10000, x1 ) ), Math.max( -10000, Math.min( 10000, y1 ) ), Math.max( -10000, Math.min( 10000, x2 ) ), Math.max( -10000, Math.min( 10000, y2 ) ) );
  }

  public void drawImage( final Image image, final int x, final int y )
  {
    m_gc.drawImage( image, x, y );
  }

  public void copyArea( int srcX, int srcY, int width, int height, int destX,
      int destY )
  {
    m_gc.copyArea( srcX, srcY, width, height, destX, destY );
  }

  public void copyArea( Image image, int x, int y )
  {
    m_gc.copyArea( image, x, y );
  }

  public void drawArc( int x, int y, int width, int height, int startAngle,
      int arcAngle )
  {
    m_gc.drawArc( x, y, width, height, startAngle, arcAngle );
  }

  public void drawFocus( int x, int y, int width, int height )
  {
    m_gc.drawFocus( x, y, Math.max( 1, width ), Math.max( 1, height ) );
  }

  public void drawImage( Image image, int srcX, int srcY, int srcWidth,
      int srcHeight, int destX, int destY, int destWidth, int destHeight )
  {
    m_gc.drawImage( image, srcX, srcY, srcWidth, srcHeight, destX, destY,
        destWidth, destHeight );
  }

  public void drawOval( int x, int y, int width, int height )
  {
    m_gc.drawOval( x, y, width, height );
  }

  public void drawPoint( int x, int y )
  {
    m_gc.drawPoint( x, y );
  }

  public void drawPolygon( int[] pointArray )
  {
    m_gc.drawPolygon( pointArray );
  }

  public void drawPolyline( final int[] pointArray )
  {
    // TODO: we got ugly lines if zoom is to small
    // check if we have the same effekt on windows
//    final int[] newpoints = new int[pointArray.length];
//    for( int i = 0; i < pointArray.length; i++ )
//    {
//      System.out.println( pointArray[i++] + " - " + pointArray[i] );
//    }
//    System.out.println(  );
//    System.out.println(  );
//    System.out.println(  );
    
    m_gc.drawPolyline( pointArray );
  }

  public void drawRectangle( Rectangle rect )
  {
    m_gc.drawRectangle( rect );
  }

  public void drawRoundRectangle( int x, int y, int width, int height,
      int arcWidth, int arcHeight )
  {
    m_gc.drawRoundRectangle( x, y, width, height, arcWidth, arcHeight );
  }

  public void drawString( final String string, final int x, final int y )
  {
    m_gc.drawString( string, x, y );
  }
  
  public void drawString( final String string, int x, int y, boolean isTransparent )
  {
    m_gc.drawString( string, x, y, isTransparent );
  }

  public void drawText( final String string, final int x, final int y )
  {
    m_gc.drawText( string, x, y );
  }
  
  public void drawText( final String string, int x, int y, boolean isTransparent )
  {
    m_gc.drawText( string, x, y, isTransparent );
  }

  public void drawText( final String string, int x, int y, int flags )
  {
    m_gc.drawText( string, x, y, flags );
  }

  public boolean equals( Object obj )
  {
    return m_gc.equals( obj );
  }

  public void fillArc( int x, int y, int width, int height, int startAngle,
      int arcAngle )
  {
    m_gc.fillArc( x, y, width, height, startAngle, arcAngle );
  }

  public void fillGradientRectangle( int x, int y, int width, int height,
      boolean vertical )
  {
    m_gc.fillGradientRectangle( x, y, width, height, vertical );
  }

  public void fillOval( int x, int y, int width, int height )
  {
    m_gc.fillOval( x, y, width, height );
  }

  public void fillPolygon( int[] pointArray )
  {
    m_gc.fillPolygon( pointArray );
  }

  public void fillRectangle( int x, int y, int width, int height )
  {
    m_gc.fillRectangle( x, y, width, height );
  }

  public void fillRectangle( Rectangle rect )
  {
    m_gc.fillRectangle( rect );
  }

  public void fillRoundRectangle( int x, int y, int width, int height,
      int arcWidth, int arcHeight )
  {
    m_gc.fillRoundRectangle( x, y, width, height, arcWidth, arcHeight );
  }

  public int getAdvanceWidth( char ch )
  {
    return m_gc.getAdvanceWidth( ch );
  }

  public Color getBackground( )
  {
    return m_gc.getBackground();
  }

  public int getCharWidth( char ch )
  {
    return m_gc.getCharWidth( ch );
  }

  public Rectangle getClipping( )
  {
    return m_gc.getClipping();
  }

  public void getClipping( Region region )
  {
    m_gc.getClipping( region );
  }

  public Font getFont( )
  {
    return m_gc.getFont();
  }

  public FontMetrics getFontMetrics( )
  {
    return m_gc.getFontMetrics();
  }

  public Color getForeground( )
  {
    return m_gc.getForeground();
  }

  public int getLineCap( )
  {
    return m_gc.getLineCap();
  }

  public int[] getLineDash( )
  {
    return m_gc.getLineDash();
  }

  public int getLineJoin( )
  {
    return m_gc.getLineJoin();
  }

  public int getLineStyle( )
  {
    return m_gc.getLineStyle();
  }

  public int getLineWidth( )
  {
    return m_gc.getLineWidth();
  }

  public int getStyle( )
  {
    return m_gc.getStyle();
  }

  public boolean getXORMode( )
  {
    return m_gc.getXORMode();
  }

  public int hashCode( )
  {
    return m_gc.hashCode();
  }

  public boolean isClipped( )
  {
    return m_gc.isClipped();
  }

  public boolean isDisposed( )
  {
    return m_gc.isDisposed();
  }

  public void setBackground( Color color )
  {
    m_gc.setBackground( color );
  }

  public void setClipping( int x, int y, int width, int height )
  {
    m_gc.setClipping( x, y, width, height );
  }

  public void setClipping( Rectangle rect )
  {
    m_gc.setClipping( rect );
  }

  public void setClipping( Region region )
  {
    m_gc.setClipping( region );
  }

  public void setFont( Font font )
  {
    m_gc.setFont( font );
  }

  public void setForeground( Color color )
  {
    m_gc.setForeground( color );
  }

  public void setLineCap( int cap )
  {
    m_gc.setLineCap( cap );
  }

  public void setLineDash( int[] dashes )
  {
    m_gc.setLineDash( dashes );
  }

  public void setLineJoin( int join )
  {
    m_gc.setLineJoin( join );
  }

  public void setLineStyle( int lineStyle )
  {
    m_gc.setLineStyle( lineStyle );
  }

  public void setLineWidth( int width )
  {
    m_gc.setLineWidth( width );
  }

  public void setXORMode( boolean xor )
  {
    m_gc.setXORMode( xor );
  }

  public Point stringExtent( String string )
  {
    return m_gc.stringExtent( string );
  }

  public Point textExtent( String string )
  {
    return m_gc.textExtent( string );
  }

  public Point textExtent( String string, int flags )
  {
    return m_gc.textExtent( string, flags );
  }

  public String toString( )
  {
    return m_gc.toString();
  }
}
