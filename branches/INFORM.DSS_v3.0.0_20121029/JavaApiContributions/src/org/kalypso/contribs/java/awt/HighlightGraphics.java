/** This file is part of Kalypso
 *
 *  Copyright (c) 2008 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
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
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
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
  private final Graphics2D m_graphics;

  private final DefaultHighlightColors m_colors = new DefaultHighlightColors();

  public HighlightGraphics( final Graphics2D graphics )
  {
    m_graphics = graphics;
  }

  @Override
  public void setColor( final Color c )
  {
    // color is controled inside
  }

  @Override
  public void setBackground( final Color color )
  {
    // color is controled inside
  }

  @Override
  public void setStroke( final Stroke s )
  {
    // stroke is controled inside
  }

  @Override
  public void drawLine( final int x1, final int y1, final int x2, final int y2 )
  {
    // m_graphics.setStroke( STROKE_NORMAL );
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.drawLine( x1, y1, x2, y2 );

    // m_graphics.setStroke( STROKE_DASHED );
    m_graphics.setColor( m_colors.getLineColor() );
    m_graphics.drawLine( x1, y1, x2, y2 );

  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y, final ImageObserver observer )
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
  public void addRenderingHints( final Map< ? , ? > hints )
  {
    m_graphics.addRenderingHints( hints );
  }

  @Override
  public void clearRect( final int x, final int y, final int width, final int height )
  {
    m_graphics.clearRect( x, y, width, height );
  }

  @Override
  public void clip( final Shape s )
  {
    m_graphics.clip( s );
  }

  @Override
  public void clipRect( final int x, final int y, final int width, final int height )
  {
    m_graphics.clipRect( x, y, width, height );
  }

  @Override
  public void copyArea( final int x, final int y, final int width, final int height, final int dx, final int dy )
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
   * @see java.awt.Graphics#create(int, int, int, int)
   */
  @Override
  public Graphics create( final int x, final int y, final int width, final int height )
  {
    return new HighlightGraphics( (Graphics2D) m_graphics.create( x, y, width, height ) );
  }

  @Override
  public void dispose( )
  {
    m_graphics.dispose();
  }

  @Override
  public void draw( final Shape s )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.draw( s );
  }

  /**
   * @see java.awt.Graphics2D#draw3DRect(int, int, int, int, boolean)
   */
  @Override
  public void draw3DRect( final int x, final int y, final int width, final int height, final boolean raised )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.draw3DRect( x, y, width, height, raised );
  }

  @Override
  public void drawArc( final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.drawArc( x, y, width, height, startAngle, arcAngle );
  }

  /**
   * @see java.awt.Graphics#drawBytes(byte[], int, int, int, int)
   */
  @Override
  public void drawBytes( final byte[] data, final int offset, final int length, final int x, final int y )
  {
    // m_graphics.setColor(m_colors.getBorderColor());
    m_graphics.drawBytes( data, offset, length, x, y );
  }

  /**
   * @see java.awt.Graphics#drawChars(char[], int, int, int, int)
   */
  @Override
  public void drawChars( final char[] data, final int offset, final int length, final int x, final int y )
  {
    m_graphics.setColor( m_colors.getTextColor() );
    m_graphics.drawChars( data, offset, length, x, y );
  }

  @Override
  public void drawGlyphVector( final GlyphVector g, final float x, final float y )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.setBackground( m_colors.getBackgroundColor() );
    m_graphics.drawGlyphVector( g, x, y );
  }

  @Override
  public boolean drawImage( final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2, final Color bgcolor, final ImageObserver observer )
  {
    return m_graphics.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer );
  }

  @Override
  public boolean drawImage( final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2, final ImageObserver observer )
  {
    return m_graphics.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer );
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y, final int width, final int height, final Color bgcolor, final ImageObserver observer )
  {
    return m_graphics.drawImage( img, x, y, width, height, bgcolor, observer );
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y, final int width, final int height, final ImageObserver observer )
  {
    return m_graphics.drawImage( img, x, y, width, height, observer );
  }

  @Override
  public boolean drawImage( final Image img, final int x, final int y, final Color bgcolor, final ImageObserver observer )
  {
    return m_graphics.drawImage( img, x, y, bgcolor, observer );
  }

  @Override
  public boolean drawImage( final Image img, final AffineTransform xform, final ImageObserver obs )
  {
    return m_graphics.drawImage( img, xform, obs );
  }

  @Override
  public void drawImage( final BufferedImage img, final BufferedImageOp op, final int x, final int y )
  {
    m_graphics.drawImage( img, op, x, y );
  }

  @Override
  public void drawOval( final int x, final int y, final int width, final int height )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.setBackground( m_colors.getBackgroundColor() );
    m_graphics.drawOval( x, y, width, height );
  }

  @Override
  public void drawPolygon( final int[] xPoints, final int[] yPoints, final int nPoints )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.setBackground( m_colors.getBackgroundColor() );
    m_graphics.drawPolygon( xPoints, yPoints, nPoints );
  }

  /**
   * @see java.awt.Graphics#drawPolygon(java.awt.Polygon)
   */
  @Override
  public void drawPolygon( final Polygon p )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.setBackground( m_colors.getBackgroundColor() );
    m_graphics.drawPolygon( p );
  }

  @Override
  public void drawPolyline( final int[] xPoints, final int[] yPoints, final int nPoints )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.setBackground( m_colors.getBackgroundColor() );
    m_graphics.drawPolyline( xPoints, yPoints, nPoints );
  }

  /**
   * @see java.awt.Graphics#drawRect(int, int, int, int)
   */
  @Override
  public void drawRect( final int x, final int y, final int width, final int height )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.setBackground( m_colors.getBackgroundColor() );
    m_graphics.drawRect( x, y, width, height );
  }

  @Override
  public void drawRenderableImage( final RenderableImage img, final AffineTransform xform )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.drawRenderableImage( img, xform );
  }

  @Override
  public void drawRenderedImage( final RenderedImage img, final AffineTransform xform )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.drawRenderedImage( img, xform );
  }

  @Override
  public void drawRoundRect( final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight )
  {
    m_graphics.setColor( m_colors.getBorderColor() );
    m_graphics.setBackground( m_colors.getBackgroundColor() );
    m_graphics.drawRoundRect( x, y, width, height, arcWidth, arcHeight );
  }

  @Override
  public void drawString( final String s, final float x, final float y )
  {
    m_graphics.setColor( m_colors.getTextColor() );
    m_graphics.drawString( s, x, y );
  }

  @Override
  public void drawString( final String str, final int x, final int y )
  {
    m_graphics.setColor( m_colors.getTextColor() );
    m_graphics.drawString( str, x, y );
  }

  @Override
  public void drawString( final AttributedCharacterIterator iterator, final float x, final float y )
  {
    m_graphics.setColor( m_colors.getTextColor() );
    m_graphics.drawString( iterator, x, y );
  }

  @Override
  public void drawString( final AttributedCharacterIterator iterator, final int x, final int y )
  {
    m_graphics.setColor( m_colors.getTextColor() );
    m_graphics.drawString( iterator, x, y );
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    return m_graphics.equals( obj );
  }

  @Override
  public void fill( final Shape s )
  {
    m_graphics.setColor( m_colors.getFillColor() );
    m_graphics.fill( s );
  }

  /**
   * @see java.awt.Graphics2D#fill3DRect(int, int, int, int, boolean)
   */
  @Override
  public void fill3DRect( final int x, final int y, final int width, final int height, final boolean raised )
  {
    m_graphics.setColor( m_colors.getFillColor() );
    m_graphics.fill3DRect( x, y, width, height, raised );
  }

  @Override
  public void fillArc( final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle )
  {
    m_graphics.setColor( m_colors.getFillColor() );
    m_graphics.fillArc( x, y, width, height, startAngle, arcAngle );
  }

  @Override
  public void fillOval( final int x, final int y, final int width, final int height )
  {
    m_graphics.setColor( m_colors.getFillColor() );
    m_graphics.fillOval( x, y, width, height );
  }

  @Override
  public void fillPolygon( final int[] xPoints, final int[] yPoints, final int nPoints )
  {
    m_graphics.setColor( m_colors.getFillColor() );
    m_graphics.fillPolygon( xPoints, yPoints, nPoints );
  }

  /**
   * @see java.awt.Graphics#fillPolygon(java.awt.Polygon)
   */
  @Override
  public void fillPolygon( final Polygon p )
  {
    m_graphics.setColor( m_colors.getFillColor() );
    m_graphics.fillPolygon( p );
  }

  @Override
  public void fillRect( final int x, final int y, final int width, final int height )
  {
    m_graphics.setColor( m_colors.getFillColor() );
    m_graphics.fillRect( x, y, width, height );
  }

  @Override
  public void fillRoundRect( final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight )
  {
    m_graphics.setColor( m_colors.getFillColor() );
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
  public Rectangle getClipBounds( final Rectangle r )
  {
    return m_graphics.getClipBounds( r );
  }

  // /**
  // *
  // * @see java.awt.Graphics#getClipRect()
  // */
  // public Rectangle getClipRect()
  // {
  // return m_graphics.getClipRect();
  // }

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
  public FontMetrics getFontMetrics( final Font f )
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
  public Object getRenderingHint( final Key hintKey )
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
  public boolean hit( final Rectangle rect, final Shape s, final boolean onStroke )
  {
    return m_graphics.hit( rect, s, onStroke );
  }

  /**
   * @see java.awt.Graphics#hitClip(int, int, int, int)
   */
  @Override
  public boolean hitClip( final int x, final int y, final int width, final int height )
  {
    return m_graphics.hitClip( x, y, width, height );
  }

  @Override
  public void rotate( final double theta )
  {
    m_graphics.rotate( theta );
  }

  @Override
  public void rotate( final double theta, final double x, final double y )
  {
    m_graphics.rotate( theta, x, y );
  }

  @Override
  public void scale( final double sx, final double sy )
  {
    m_graphics.scale( sx, sy );
  }

  @Override
  public void setClip( final int x, final int y, final int width, final int height )
  {
    m_graphics.setClip( x, y, width, height );
  }

  @Override
  public void setClip( final Shape clip )
  {
    m_graphics.setClip( clip );
  }

  @Override
  public void setComposite( final Composite comp )
  {
    m_graphics.setComposite( comp );
  }

  @Override
  public void setFont( final Font font )
  {
    m_graphics.setFont( font );
  }

  @Override
  public void setPaint( final Paint paint )
  {
    m_graphics.setPaint( paint );
  }

  @Override
  public void setPaintMode( )
  {
    m_graphics.setPaintMode();
  }

  @Override
  public void setRenderingHint( final Key hintKey, final Object hintValue )
  {
    m_graphics.setRenderingHint( hintKey, hintValue );
  }

  @Override
  public void setRenderingHints( final Map< ? , ? > hints )
  {
    m_graphics.setRenderingHints( hints );
  }

  @Override
  public void setTransform( final AffineTransform Tx )
  {
    m_graphics.setTransform( Tx );
  }

  @Override
  public void setXORMode( final Color c1 )
  {
    m_graphics.setXORMode( c1 );
  }

  @Override
  public void shear( final double shx, final double shy )
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
  public void transform( final AffineTransform Tx )
  {
    m_graphics.transform( Tx );
  }

  @Override
  public void translate( final double tx, final double ty )
  {
    m_graphics.translate( tx, ty );
  }

  @Override
  public void translate( final int x, final int y )
  {
    m_graphics.translate( x, y );
  }
}
