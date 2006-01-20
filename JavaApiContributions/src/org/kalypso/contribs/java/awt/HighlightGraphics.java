/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.contribs.java.awt;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * HighlightGraphics thic class decorates a graphics context. It behaves a little bit different from the decorated one
 * in the way that it draws allways in a "highlighted" way
 * 
 * @author doemming (08.06.2005)
 */
public class HighlightGraphics extends Graphics2D
{
  private final Color COLOR_FILL = ColorUtilities.createTransparent( Color.YELLOW, 255 );

  private final Color COLOR_TEXT = ColorUtilities.createTransparent( Color.RED, 255 );

  private final Color COLOR_LINE = ColorUtilities.createTransparent( Color.YELLOW, 255 );

  private final Color COLOR_BORDER = ColorUtilities.createTransparent( Color.RED, 255 );

  private static final Color COLOR_BACKGROUND = ColorUtilities.createTransparent( Color.lightGray, 5 );

  // private static final Stroke STROKE_NORMAL = new BasicStroke( 4f );
  // private static final Stroke STROKE_DASHED = new BasicStroke( 4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 15,
  // new float[]
  // {
  // 14,
  // 14 }, 0 );
  private final Graphics2D m_graphics;

  public HighlightGraphics( Graphics2D graphics )
  {
    m_graphics = graphics;
    m_graphics.setColor( Color.YELLOW );
    m_graphics.setBackground( COLOR_BACKGROUND );
    // m_graphics.setStroke( STROKE_DASHED );
  }

  @Override
  public void setColor( Color c )
  {
    // color is controled inside
  }

  @Override
  public void setBackground( Color color )
  {
    // color is controled inside
  }

  @Override
  public void setStroke( Stroke s )
  {
    // stroke is controled inside
  }

  @Override
  public void drawLine( int x1, int y1, int x2, int y2 )
  {
    // m_graphics.setStroke( STROKE_NORMAL );
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.drawLine( x1, y1, x2, y2 );
    // m_graphics.setStroke( STROKE_DASHED );
    m_graphics.setColor( COLOR_LINE );
    m_graphics.drawLine( x1, y1, x2, y2 );
  }

  @Override
  public boolean drawImage( Image img, int x, int y, ImageObserver observer )
  {
    m_graphics.setXORMode( getColor() );
    final boolean result = m_graphics.drawImage( img, x, y, observer );
    m_graphics.setPaintMode();
    return result;
  }

  @Override
  public Color getColor( )
  {
    return m_graphics.getColor();
  }

  @Override
  public void addRenderingHints( Map hints )
  {
    m_graphics.addRenderingHints( hints );
  }

  @Override
  public void clearRect( int x, int y, int width, int height )
  {
    m_graphics.clearRect( x, y, width, height );
  }

  @Override
  public void clip( Shape s )
  {
    m_graphics.clip( s );
  }

  @Override
  public void clipRect( int x, int y, int width, int height )
  {
    m_graphics.clipRect( x, y, width, height );
  }

  @Override
  public void copyArea( int x, int y, int width, int height, int dx, int dy )
  {
    m_graphics.copyArea( x, y, width, height, dx, dy );
  }

  /**
   * @see java.awt.Graphics#create()
   */
  @Override
  public Graphics create( )
  {
    return new HighlightGraphics( (Graphics2D) m_graphics.create() );
  }

  /**
   * @see java.awt.Graphics#create(int, int, int, in@Override t)
   */
  @Override
  public Graphics create( int x, int y, int width, int height )
  {
    return new HighlightGraphics( (Graphics2D) m_graphics.create( x, y, width, height ) );
  }

  @Override
  public void dispose( )
  {
    m_graphics.dispose();
  }

  @Override
  public void draw( Shape s )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.draw( s );
  }

  /**
   * @see java.awt.Graphics2D#draw3DRect(int, int, int, int, boolean)
   */
  @Override
  public void draw3DRect( int x, int y, int width, int height, boolean raised )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.draw3DRect( x, y, width, height, raised );
  }

  @Override
  public void drawArc( int x, int y, int width, int height, int startAngle, int arcAngle )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.drawArc( x, y, width, height, startAngle, arcAngle );
  }

  /**
   * @see java.awt.Graphics#drawBytes(byte[], int, int, int, int)
   */
  @Override
  public void drawBytes( byte[] data, int offset, int length, int x, int y )
  {
    // m_graphics.setColor(COLOR_BORDER);
    m_graphics.drawBytes( data, offset, length, x, y );
  }

  /**
   * @see java.awt.Graphics#drawChars(char[], int, int, int, int)
   */
  @Override
  public void drawChars( char[] data, int offset, int length, int x, int y )
  {
    m_graphics.setColor( COLOR_TEXT );
    m_graphics.drawChars( data, offset, length, x, y );
  }

  @Override
  public void drawGlyphVector( GlyphVector g, float x, float y )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.setBackground( COLOR_BACKGROUND );
    m_graphics.drawGlyphVector( g, x, y );
  }

  @Override
  public boolean drawImage( Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer )
  {
    return m_graphics.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer );
  }

  @Override
  public boolean drawImage( Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer )
  {
    return m_graphics.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer );
  }

  @Override
  public boolean drawImage( Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer )
  {
    return m_graphics.drawImage( img, x, y, width, height, bgcolor, observer );
  }

  @Override
  public boolean drawImage( Image img, int x, int y, int width, int height, ImageObserver observer )
  {
    return m_graphics.drawImage( img, x, y, width, height, observer );
  }

  @Override
  public boolean drawImage( Image img, int x, int y, Color bgcolor, ImageObserver observer )
  {
    return m_graphics.drawImage( img, x, y, bgcolor, observer );
  }

  @Override
  public boolean drawImage( Image img, AffineTransform xform, ImageObserver obs )
  {
    return m_graphics.drawImage( img, xform, obs );
  }

  @Override
  public void drawImage( BufferedImage img, BufferedImageOp op, int x, int y )
  {
    m_graphics.drawImage( img, op, x, y );
  }

  @Override
  public void drawOval( int x, int y, int width, int height )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.setBackground( COLOR_BACKGROUND );
    m_graphics.drawOval( x, y, width, height );
  }

  @Override
  public void drawPolygon( int[] xPoints, int[] yPoints, int nPoints )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.setBackground( COLOR_BACKGROUND );
    m_graphics.drawPolygon( xPoints, yPoints, nPoints );
  }

  /**
   * @see java.awt.Graphics#drawPolygon(java.awt.Polygon)
   */
  @Override
  public void drawPolygon( Polygon p )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.setBackground( COLOR_BACKGROUND );
    m_graphics.drawPolygon( p );
  }

  @Override
  public void drawPolyline( int[] xPoints, int[] yPoints, int nPoints )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.setBackground( COLOR_BACKGROUND );
    m_graphics.drawPolyline( xPoints, yPoints, nPoints );
  }

  /**
   * @see java.awt.Graphics#drawRect(int, int, int, int)
   */
  @Override
  public void drawRect( int x, int y, int width, int height )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.setBackground( COLOR_BACKGROUND );
    m_graphics.drawRect( x, y, width, height );
  }

  @Override
  public void drawRenderableImage( RenderableImage img, AffineTransform xform )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.drawRenderableImage( img, xform );
  }

  @Override
  public void drawRenderedImage( RenderedImage img, AffineTransform xform )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.drawRenderedImage( img, xform );
  }

  @Override
  public void drawRoundRect( int x, int y, int width, int height, int arcWidth, int arcHeight )
  {
    m_graphics.setColor( COLOR_BORDER );
    m_graphics.setBackground( COLOR_BACKGROUND );
    m_graphics.drawRoundRect( x, y, width, height, arcWidth, arcHeight );
  }

  @Override
  public void drawString( String s, float x, float y )
  {
    m_graphics.setColor( COLOR_TEXT );
    m_graphics.drawString( s, x, y );
  }

  @Override
  public void drawString( String str, int x, int y )
  {
    m_graphics.setColor( COLOR_TEXT );
    m_graphics.drawString( str, x, y );
  }

  @Override
  public void drawString( AttributedCharacterIterator iterator, float x, float y )
  {
    m_graphics.setColor( COLOR_TEXT );
    m_graphics.drawString( iterator, x, y );
  }

  @Override
  public void drawString( AttributedCharacterIterator iterator, int x, int y )
  {
    m_graphics.setColor( COLOR_TEXT );
    m_graphics.drawString( iterator, x, y );
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object obj )
  {
    return m_graphics.equals( obj );
  }

  @Override
  public void fill( Shape s )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fill( s );
  }

  /**
   * @see java.awt.Graphics2D#fill3DRect(int, int, int, int, boolean)
   */
  @Override
  public void fill3DRect( int x, int y, int width, int height, boolean raised )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fill3DRect( x, y, width, height, raised );
  }

  @Override
  public void fillArc( int x, int y, int width, int height, int startAngle, int arcAngle )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fillArc( x, y, width, height, startAngle, arcAngle );
  }

  @Override
  public void fillOval( int x, int y, int width, int height )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fillOval( x, y, width, height );
  }

  @Override
  public void fillPolygon( int[] xPoints, int[] yPoints, int nPoints )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fillPolygon( xPoints, yPoints, nPoints );
  }

  /**
   * @see java.awt.Graphics#fillPolygon(java.awt.Polygon)
   */
  @Override
  public void fillPolygon( Polygon p )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fillPolygon( p );
  }

  @Override
  public void fillRect( int x, int y, int width, int height )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fillRect( x, y, width, height );
  }

  @Override
  public void fillRoundRect( int x, int y, int width, int height, int arcWidth, int arcHeight )
  {
    m_graphics.setColor( COLOR_FILL );
    m_graphics.fillRoundRect( x, y, width, height, arcWidth, arcHeight );
  }

  @Override
  public Color getBackground( )
  {
    return m_graphics.getBackground();
  }

  @Override
  public Shape getClip( )
  {
    return m_graphics.getClip();
  }

  @Override
  public Rectangle getClipBounds( )
  {
    return m_graphics.getClipBounds();
  }

  /**
   * @see java.awt.Graphics#getClipBounds(java.awt.Rectangle)
   */
  @Override
  public Rectangle getClipBounds( Rectangle r )
  {
    return m_graphics.getClipBounds( r );
  }

  @Override
  public Composite getComposite( )
  {
    return m_graphics.getComposite();
  }

  @Override
  public GraphicsConfiguration getDeviceConfiguration( )
  {
    return m_graphics.getDeviceConfiguration();
  }

  @Override
  public Font getFont( )
  {
    return m_graphics.getFont();
  }

  /**
   * @see java.awt.Graphics#getFontMetrics()
   */
  @Override
  public FontMetrics getFontMetrics( )
  {
    return m_graphics.getFontMetrics();
  }

  @Override
  public FontMetrics getFontMetrics( Font f )
  {
    return m_graphics.getFontMetrics( f );
  }

  @Override
  public FontRenderContext getFontRenderContext( )
  {
    return m_graphics.getFontRenderContext();
  }

  @Override
  public Paint getPaint( )
  {
    return m_graphics.getPaint();
  }

  @Override
  public Object getRenderingHint( Key hintKey )
  {
    return m_graphics.getRenderingHint( hintKey );
  }

  @Override
  public RenderingHints getRenderingHints( )
  {
    return m_graphics.getRenderingHints();
  }

  @Override
  public Stroke getStroke( )
  {
    return m_graphics.getStroke();
  }

  @Override
  public AffineTransform getTransform( )
  {
    return m_graphics.getTransform();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return m_graphics.hashCode();
  }

  @Override
  public boolean hit( Rectangle rect, Shape s, boolean onStroke )
  {
    return m_graphics.hit( rect, s, onStroke );
  }

  /**
   * @see java.awt.Graphics#hitClip(int, int, int, int)
   */
  @Override
  public boolean hitClip( int x, int y, int width, int height )
  {
    return m_graphics.hitClip( x, y, width, height );
  }

  @Override
  public void rotate( double theta )
  {
    m_graphics.rotate( theta );
  }

  @Override
  public void rotate( double theta, double x, double y )
  {
    m_graphics.rotate( theta, x, y );
  }

  @Override
  public void scale( double sx, double sy )
  {
    m_graphics.scale( sx, sy );
  }

  @Override
  public void setClip( int x, int y, int width, int height )
  {
    m_graphics.setClip( x, y, width, height );
  }

  @Override
  public void setClip( Shape clip )
  {
    m_graphics.setClip( clip );
  }

  @Override
  public void setComposite( Composite comp )
  {
    m_graphics.setComposite( comp );
  }

  @Override
  public void setFont( Font font )
  {
    m_graphics.setFont( font );
  }

  @Override
  public void setPaint( Paint paint )
  {
    m_graphics.setPaint( paint );
  }

  @Override
  public void setPaintMode( )
  {
    m_graphics.setPaintMode();
  }

  @Override
  public void setRenderingHint( Key hintKey, Object hintValue )
  {
    m_graphics.setRenderingHint( hintKey, hintValue );
  }

  @Override
  public void setRenderingHints( Map hints )
  {
    m_graphics.setRenderingHints( hints );
  }

  @Override
  public void setTransform( AffineTransform Tx )
  {
    m_graphics.setTransform( Tx );
  }

  @Override
  public void setXORMode( Color c1 )
  {
    m_graphics.setXORMode( c1 );
  }

  @Override
  public void shear( double shx, double shy )
  {
    m_graphics.shear( shx, shy );
  }

  /**
   * @see java.awt.Graphics#toString()
   */
  @Override
  public String toString( )
  {
    return m_graphics.toString();
  }

  @Override
  public void transform( AffineTransform Tx )
  {
    m_graphics.transform( Tx );
  }

  @Override
  public void translate( double tx, double ty )
  {
    m_graphics.translate( tx, ty );
  }

  @Override
  public void translate( int x, int y )
  {
    m_graphics.translate( x, y );
  }
}
