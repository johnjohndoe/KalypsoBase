package org.kalypso.contribs.eclipse.swt.graphics;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Transform;

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

  @Override
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

  @Override
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

  @Override
  public String toString( )
  {
    return m_gc.toString();
  }

  public void drawPath( final Path path )
  {
    m_gc.drawPath( path );
  }

  public void fillPath( final Path path )
  {
    m_gc.fillPath( path );
  }

  public int getAlpha( )
  {
    return m_gc.getAlpha();
  }

  public int getFillRule( )
  {
    return m_gc.getFillRule();
  }

  public void getTransform( Transform transform )
  {
    m_gc.getTransform( transform );
  }

  public void setAlpha( int alpha )
  {
    m_gc.setAlpha( alpha );
  }

  public void setClipping( Path path )
  {
    m_gc.setClipping( path );
  }

  public void setFillRule( int rule )
  {
    m_gc.setFillRule( rule );
  }

  public void setTransform( Transform transform )
  {
    m_gc.setTransform( transform );
  }

  /**
   * @param srcX
   * @param srcY
   * @param width
   * @param height
   * @param destX
   * @param destY
   * @param paint
   * @see org.eclipse.swt.graphics.GC#copyArea(int, int, int, int, int, int, boolean)
   */
  public void copyArea( int srcX, int srcY, int width, int height, int destX, int destY, boolean paint )
  {
    m_gc.copyArea( srcX, srcY, width, height, destX, destY, paint );
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.GC#getAdvanced()
   */
  public boolean getAdvanced( )
  {
    return m_gc.getAdvanced();
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.GC#getAntialias()
   */
  public int getAntialias( )
  {
    return m_gc.getAntialias();
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.GC#getBackgroundPattern()
   */
  public Pattern getBackgroundPattern( )
  {
    return m_gc.getBackgroundPattern();
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.Resource#getDevice()
   */
  public Device getDevice( )
  {
    return m_gc.getDevice();
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.GC#getForegroundPattern()
   */
  public Pattern getForegroundPattern( )
  {
    return m_gc.getForegroundPattern();
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.GC#getGCData()
   */
  public GCData getGCData( )
  {
    return m_gc.getGCData();
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.GC#getInterpolation()
   */
  public int getInterpolation( )
  {
    return m_gc.getInterpolation();
  }

  /**
   * @return
   * @see org.eclipse.swt.graphics.GC#getTextAntialias()
   */
  public int getTextAntialias( )
  {
    return m_gc.getTextAntialias();
  }

  /**
   * @param advanced
   * @see org.eclipse.swt.graphics.GC#setAdvanced(boolean)
   */
  public void setAdvanced( boolean advanced )
  {
    m_gc.setAdvanced( advanced );
  }

  /**
   * @param antialias
   * @see org.eclipse.swt.graphics.GC#setAntialias(int)
   */
  public void setAntialias( int antialias )
  {
    m_gc.setAntialias( antialias );
  }

  /**
   * @param pattern
   * @see org.eclipse.swt.graphics.GC#setBackgroundPattern(org.eclipse.swt.graphics.Pattern)
   */
  public void setBackgroundPattern( Pattern pattern )
  {
    m_gc.setBackgroundPattern( pattern );
  }

  /**
   * @param pattern
   * @see org.eclipse.swt.graphics.GC#setForegroundPattern(org.eclipse.swt.graphics.Pattern)
   */
  public void setForegroundPattern( Pattern pattern )
  {
    m_gc.setForegroundPattern( pattern );
  }

  /**
   * @param interpolation
   * @see org.eclipse.swt.graphics.GC#setInterpolation(int)
   */
  public void setInterpolation( int interpolation )
  {
    m_gc.setInterpolation( interpolation );
  }

  /**
   * @param antialias
   * @see org.eclipse.swt.graphics.GC#setTextAntialias(int)
   */
  public void setTextAntialias( int antialias )
  {
    m_gc.setTextAntialias( antialias );
  }
}