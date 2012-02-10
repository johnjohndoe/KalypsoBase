/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.kalypso.contribs.eclipse.swt.udig;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

/**
 * A Graphics object that wraps SWT's GC object
 * 
 * @author jeichar
 * @since 0.3
 */
public class SWTGraphics implements ViewportGraphics
{

  /** The <code>TRANSPARENT</code> color */
  public final static int TRANSPARENT = 0x220000 | 0x2200 | 0x22;

  static final AffineTransform AFFINE_TRANSFORM = new AffineTransform();

  private Transform swtTransform;

  private GC gc = null;

  private Color fore = null;

  private Color back = null;

  private final Display display;

  private Font font = null;

  /**
   * Construct <code>SWTGraphics</code>.
   * 
   * @param Image
   *          image
   * @param display
   *          the display to use with the
   * @param display
   *          The display object
   */
  public SWTGraphics( final Image image, final Display display )
  {
    this( new GC( image ), display );

  }

  /**
   * Construct <code>SWTGraphics</code>.
   * 
   * @param gc
   *          The GC object
   * @param display
   *          The display object
   */
  public SWTGraphics( final GC gc, final Display display )
  {
    AWTSWTImageUtils.checkAccess();
    this.display = display;
    setGraphics( gc, display );
  }

  void setGraphics( final GC gc, final Display display )
  {
    AWTSWTImageUtils.checkAccess();
    this.gc = gc;
    // this.display=display;
    if( back != null )
    {
      back.dispose();
    }
    back = new Color( display, 255, 255, 255 );
    gc.setBackground( back );

    gc.setAdvanced( true );
  }

  @Override
  public GC getGC( )
  {
    AWTSWTImageUtils.checkAccess();
    return gc;
  }

  @Override
  public void dispose( )
  {
    AWTSWTImageUtils.checkAccess();
    if( fore != null )
    {
      fore.dispose();
    }
    if( back != null )
    {
      back.dispose();
    }
    if( swtTransform != null )
    {
      swtTransform.dispose();
    }
    gc.dispose();
  }

  @Override
  public void drawPath( final Path path )
  {
    AWTSWTImageUtils.checkAccess();
    gc.drawPath( path );
  }

  /**
   * @see net.refractions.udig.project.render.ViewportGraphics#draw(java.awt.Shape)
   */
  @Override
  public void draw( final Shape s )
  {
    AWTSWTImageUtils.checkAccess();
    final Path path = AWTSWTImageUtils.convertToPath( s, display );
    if( path != null )
    {
      gc.drawPath( path );
      path.dispose();
    }

  }

  /**
   * Converts the shape to a path object. Remember to dispose of the path object when done.
   * 
   * @param shape
   * @return the shape converted to a {@link Path} object.
   * @deprecated Use {@link AWTSWTImageUtils#convertToPath(Shape,Device)} instead
   */
  @Deprecated
  public static Path convertToPath( final Shape shape, final Device device )
  {
    return AWTSWTImageUtils.convertToPath( shape, device );
  }

  /**
   * @deprecated Use {@link AWTSWTImageUtils#createPath(PathIterator,Device)} instead
   */
  @Deprecated
  public static Path createPath( final PathIterator p, final Device device )
  {
    return AWTSWTImageUtils.createPath( p, device );
  }

  /**
   * @see net.refractions.udig.project.render.ViewportGraphics#draw(java.awt.Shape)
   */
  @Override
  public void fill( final Shape s )
  {
    final Color tmp = prepareForFill();
    final Path path = AWTSWTImageUtils.convertToPath( s, display );
    gc.fillPath( path );
    path.dispose();
    gc.setBackground( tmp );
  }

  private Color prepareForFill( )
  {
    AWTSWTImageUtils.checkAccess();
    final Color tmp = gc.getBackground();
    if( fore == null )
    {
      gc.setBackground( gc.getForeground() );
    }
    else
    {
      gc.setBackground( fore );
    }
    return tmp;
  }

  @Override
  public void fillPath( final Path path )
  {
    final Color tmp = prepareForFill();
    gc.fillPath( path );
    gc.setBackground( tmp );
  }

  @Override
  public void drawRect( final int x, final int y, final int width, final int height )
  {
    final Color tmp = prepareForFill();

    gc.drawRectangle( x, y, width, height );
    gc.setBackground( tmp );
  }

  /**
   * @see net.refractions.udig.project.render.ViewportGraphics#fillRect(int, int, int, int)
   */
  @Override
  public void fillRect( final int x, final int y, final int width, final int height )
  {
    final Color tmp = prepareForFill();
    gc.fillRectangle( new Rectangle( x, y, width, height ) );

    gc.setBackground( tmp );
  }

  /**
   * @see net.refractions.udig.project.render.ViewportGraphics#setColor(java.awt.Color)
   */
  @Override
  public void setColor( final java.awt.Color c )
  {
    AWTSWTImageUtils.checkAccess();
    final Color color = new Color( display, c.getRed(), c.getGreen(), c.getBlue() );
    gc.setForeground( color );
    gc.setAlpha( c.getAlpha() );
    if( fore != null )
    {
      fore.dispose();
    }
    fore = color;
  }

  /**
   * This is hard because - background doesn't mean what we think it means.
   * 
   * @see net.refractions.udig.project.render.ViewportGraphics#setBackground(java.awt.Color)
   */
  @Override
  public void setBackground( final java.awt.Color c )
  {
    AWTSWTImageUtils.checkAccess();
    final Color color = new Color( display, c.getRed(), c.getGreen(), c.getBlue() );
    gc.setBackground( color );
    if( back != null )
    {
      back.dispose();
    }
    back = color;
  }

  /**
   * @see net.refractions.udig.project.render.ViewportGraphics#setStroke(int, int)
   */
  @Override
  public void setStroke( final int style, final int width )
  {
    AWTSWTImageUtils.checkAccess();

    gc.setLineWidth( width );
    switch( style )
    {
      case LINE_DASH:
      {
        gc.setLineStyle( SWT.LINE_DASH );
        break;
      }
      case LINE_DASHDOT:
      {
        gc.setLineStyle( SWT.LINE_DASHDOT );
        break;
      }
      case LINE_DASHDOTDOT:
      {
        gc.setLineStyle( SWT.LINE_DASHDOTDOT );
        break;
      }
      case LINE_DOT:
      {
        gc.setLineStyle( SWT.LINE_DOT );
        break;
      }
      case LINE_SOLID:
      {
        gc.setLineStyle( SWT.LINE_SOLID );
        break;
      }

      case LINE_SOLID_ROUNDED:
      {
        gc.setLineCap( SWT.CAP_ROUND );
        gc.setLineJoin( SWT.JOIN_ROUND );
        gc.setLineStyle( SWT.LINE_SOLID );
        break;
      }
      default:
      {
        gc.setLineStyle( SWT.LINE_SOLID );
        break;
      }
    }
  }

  /**
   * @see net.refractions.udig.project.render.ViewportGraphics#setClip(java.awt.Rectangle)
   */
  @Override
  public void setClip( final java.awt.Rectangle r )
  {
    AWTSWTImageUtils.checkAccess();
    gc.setClipping( r.x, r.y, r.width, r.height );
  }

  /**
   * @see net.refractions.udig.project.render.ViewportGraphics#translate(java.awt.Point)
   */
  @Override
  public void translate( final Point offset )
  {
    AWTSWTImageUtils.checkAccess();
    if( swtTransform == null )
    {
      swtTransform = new Transform( display );
    }
    swtTransform.translate( offset.x, offset.y );
    gc.setTransform( swtTransform );
  }

  @Override
  public void clearRect( final int x, final int y, final int width, final int height )
  {
    AWTSWTImageUtils.checkAccess();
    gc.fillRectangle( x, y, width, height );
  }

  @Override
  public void drawImage( final RenderedImage rimage, final int x, final int y )
  {
    AWTSWTImageUtils.checkAccess();
    drawImage( rimage, x, y, x + rimage.getWidth(), y + rimage.getHeight(), 0, 0, rimage.getWidth(), rimage.getHeight() );
  }

  /**
   * @deprecated Use {@link AWTSWTImageUtils#createDefaultImage(Display,int,int)} instead
   */
  @Deprecated
  public static Image createDefaultImage( final Display display, final int width, final int height )
  {
    return AWTSWTImageUtils.createDefaultImage( display, width, height );
  }

  /**
   * @deprecated Use {@link AWTSWTImageUtils#createImageDescriptor(RenderedImage,boolean)} instead
   */
  @Deprecated
  public static ImageDescriptor createImageDescriptor( final RenderedImage image, final boolean transparent )
  {
    return AWTSWTImageUtils.createImageDescriptor( image, transparent );
  }

  /**
   * Create a buffered image that can be be converted to SWTland later
   * 
   * @deprecated Use {@link AWTSWTImageUtils#createBufferedImage(int,int)} instead
   */
  @Deprecated
  public static BufferedImage createBufferedImage( final int w, final int h )
  {
    return AWTSWTImageUtils.createBufferedImage( w, h );
  }

  /**
   * @deprecated Use {@link AWTSWTImageUtils#createSWTImage(RenderedImage,boolean)} instead
   */
  @Deprecated
  public static Image createSWTImage( final RenderedImage image, final boolean transparent )
  {
    return AWTSWTImageUtils.createSWTImage( image, transparent );
  }

  /**
   * @deprecated Use {@link AWTSWTImageUtils#createImageData(RenderedImage,boolean)} instead
   */
  @Deprecated
  public static ImageData createImageData( final RenderedImage image, final boolean transparent )
  {
    return AWTSWTImageUtils.createImageData( image, transparent );
  }

  @Override
  public void drawString( final String string, final int x, final int y, final int alignx, final int aligny )
  {
    AWTSWTImageUtils.checkAccess();
    final org.eclipse.swt.graphics.Point text = gc.stringExtent( string );
    final int w = text.x;
    final int h = text.y;

    final int x2 = alignx == 0 ? x - w / 2 : alignx > 0 ? x - w : x;
    final int y2 = aligny == 0 ? y + h / 2 : aligny > 0 ? y + h : y;

    gc.drawString( string, x2, y2, true );
  }

  @Override
  public void setTransform( final AffineTransform transform )
  {
    AWTSWTImageUtils.checkAccess();
    final double[] matrix = new double[6];
    transform.getMatrix( matrix );
    if( swtTransform == null )
    {
      swtTransform = new Transform( display, (float) matrix[0], (float) matrix[1], (float) matrix[2], (float) matrix[3], (float) matrix[4], (float) matrix[5] );
    }
    else
    {
      swtTransform.setElements( (float) matrix[0], (float) matrix[1], (float) matrix[2], (float) matrix[3], (float) matrix[4], (float) matrix[5] );
    }

    gc.setTransform( swtTransform );
  }

  @Override
  public int getFontHeight( )
  {
    AWTSWTImageUtils.checkAccess();
    return gc.getFontMetrics().getHeight();
  }

  @Override
  public int stringWidth( final String str )
  {
    AWTSWTImageUtils.checkAccess();
    return -1;
  }

  @Override
  public int getFontAscent( )
  {
    AWTSWTImageUtils.checkAccess();
    return gc.getFontMetrics().getAscent();
  }

  @Override
  public Rectangle2D getStringBounds( final String str )
  {
    AWTSWTImageUtils.checkAccess();
    final org.eclipse.swt.graphics.Point extent = gc.textExtent( str );

    return new java.awt.Rectangle( 0, 0, extent.x, extent.y );
  }

  @Override
  public void drawLine( final int x1, final int y1, final int x2, final int y2 )
  {
    AWTSWTImageUtils.checkAccess();
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * @see net.refractions.udig.ui.graphics.ViewportGraphics#drawImage(java.awt.Image, int, int) Current version can only
   *      draw Image if the image is an RenderedImage
   */
  @Override
  public void drawImage( final java.awt.Image awtImage, final int x, final int y )
  {
    AWTSWTImageUtils.checkAccess();
    final RenderedImage rimage = (RenderedImage) awtImage;
    drawImage( rimage, x, y );
  }

  public void drawImage( final RenderedImage rimage, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2 )
  {
    AWTSWTImageUtils.checkAccess();
    assert rimage != null;
    Image swtImage = null;
    try
    {
      if( rimage instanceof BufferedImage )
      {
        swtImage = AWTSWTImageUtils.convertToSWTImage( (BufferedImage) rimage );
      }
      else
      {
        swtImage = AWTSWTImageUtils.createSWTImage( rimage );
      }
      if( swtImage != null )
      {
        gc.drawImage( swtImage, sx1, sy1, Math.abs( sx2 - sx1 ), Math.abs( sy2 - sy1 ), dx1, dy1, Math.abs( dx2 - dx1 ), Math.abs( dy2 - dy1 ) );
        swtImage.dispose();
      }
    }
    finally
    {
      if( swtImage != null )
      {
        swtImage.dispose();
      }
    }

  }

  /**
   * @see net.refractions.udig.ui.graphics.ViewportGraphics#drawImage(java.awt.Image, int, int, int, int, int, int, int,
   *      int)
   */
  @Override
  public void drawImage( final java.awt.Image awtImage, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2 )
  {
    AWTSWTImageUtils.checkAccess();
    final RenderedImage rimage = (RenderedImage) awtImage;
    drawImage( rimage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2 );
  }

  @Override
  public void drawImage( final Image swtImage, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2 )
  {
    AWTSWTImageUtils.checkAccess();

    gc.drawImage( swtImage, sx1, sy1, Math.abs( sx2 - sx1 ), Math.abs( sy2 - sy1 ), dx1, dy1, Math.abs( dx2 - dx1 ), Math.abs( dy2 - dy1 ) );

  }

  @Override
  public void drawImage( final Image swtImage, final int x, final int y )
  {
    gc.drawImage( swtImage, x, y );
  }

  @Override
  public AffineTransform getTransform( )
  {
    AWTSWTImageUtils.checkAccess();
    if( swtTransform == null )
    {
      return AFFINE_TRANSFORM;
    }

    final float[] matrix = new float[6];
    swtTransform.getElements( matrix );
    return new AffineTransform( matrix );
  }

  @Override
  public void drawOval( final int x, final int y, final int width, final int height )
  {
    gc.drawOval( x, y, width, height );
  }

  @Override
  public void fillOval( final int x, final int y, final int width, final int height )
  {
    gc.fillOval( x, y, width, height );
  }

  /**
   * Creates an image descriptor that from the source image.
   * 
   * @param image
   *          source image
   * @return an image descriptor that from the source image.
   * @deprecated Use {@link AWTSWTImageUtils#createImageDescriptor(BufferedImage)} instead
   */
  @Deprecated
  public static ImageDescriptor createImageDescriptor( final BufferedImage image )
  {
    return AWTSWTImageUtils.createImageDescriptor( image );
  }

  /**
   * Converts a BufferedImage to an SWT Image. You are responsible for disposing the created image. This method is
   * faster than creating a SWT image from a RenderedImage so use this method if possible.
   * 
   * @param image
   *          source image.
   * @return a swtimage showing the source image.
   * @deprecated Use {@link AWTSWTImageUtils#convertToSWTImage(BufferedImage)} instead
   */
  @Deprecated
  public static Image convertToSWTImage( final BufferedImage image )
  {
    return AWTSWTImageUtils.convertToSWTImage( image );
  }

  /**
   * Creates an ImageData from the 0,0,width,height section of the source BufferedImage.
   * <p>
   * This method is faster than creating the ImageData from a RenderedImage so use this method if possible.
   * </p>
   * 
   * @param image
   *          source image.
   * @return an ImageData from the 0,0,width,height section of the source BufferedImage
   * @deprecated Use {@link AWTSWTImageUtils#createImageData(BufferedImage)} instead
   */
  @Deprecated
  public static ImageData createImageData( final BufferedImage image )
  {
    return AWTSWTImageUtils.createImageData( image );
  }

  /**
   * Converts a RenderedImage to an SWT Image. You are responsible for disposing the created image. This method is
   * slower than calling {@link #createSWTImage(BufferedImage, int, int)}.
   * 
   * @param image
   *          source image.
   * @param width
   *          the width of the final image
   * @param height
   *          the height of the final image
   * @return a swtimage showing the 0,0,width,height rectangle of the source image.
   * @deprecated Use {@link AWTSWTImageUtils#createSWTImage(RenderedImage)} instead
   */
  @Deprecated
  public static Image createSWTImage( final RenderedImage image )
  {
    return AWTSWTImageUtils.createSWTImage( image );
  }

  /**
   * Creates an ImageData from the source RenderedImage.
   * <p>
   * This method is slower than using {@link AWTSWTImageUtils#createImageData(BufferedImage, int, int)}.
   * </p>
   * 
   * @param image
   *          source image.
   * @return an ImageData from the source RenderedImage.
   * @deprecated Use {@link AWTSWTImageUtils#createImageData(RenderedImage)} instead
   */
  @Deprecated
  public static ImageData createImageData( final RenderedImage image )
  {
    return AWTSWTImageUtils.createImageData( image );
  }

  @Override
  public Shape getClip( )
  {
    final Rectangle clipping = gc.getClipping();
    return new java.awt.Rectangle( clipping.x, clipping.y, clipping.width, clipping.height );
  }

  @Override
  public void setClipBounds( final java.awt.Rectangle newBounds )
  {
    gc.setClipping( new Rectangle( newBounds.x, newBounds.y, newBounds.width, newBounds.height ) );
  }

  @Override
  public java.awt.Color getBackgroundColor( )
  {
    AWTSWTImageUtils.checkAccess();
    return AWTSWTImageUtils.swtColor2awtColor( gc, gc.getBackground() );
  }

  @Override
  public java.awt.Color getColor( )
  {
    AWTSWTImageUtils.checkAccess();
    return AWTSWTImageUtils.swtColor2awtColor( gc, gc.getForeground() );
  }

  /**
   * @deprecated Use {@link AWTSWTImageUtils#swtColor2awtColor(GC,Color)} instead
   */
  @Deprecated
  public static java.awt.Color swtColor2awtColor( final GC gc, final Color swt )
  {
    return AWTSWTImageUtils.swtColor2awtColor( gc, swt );
  }

  @Override
  public void drawRoundRect( final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight )
  {
    AWTSWTImageUtils.checkAccess();
    gc.drawRoundRectangle( x, y, width, height, arcWidth, arcHeight );
  }

  /**
   * @deprecated Use {@link AWTSWTImageUtils#checkAccess()} instead
   */
  @Deprecated
  static void checkAccess( )
  {
    AWTSWTImageUtils.checkAccess();
  }

  @Override
  public void fillRoundRect( final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight )
  {
    final Color tmp = prepareForFill();
    gc.fillRoundRectangle( x, y, width, height, arcWidth, arcHeight );
    gc.setBackground( tmp );
  }

  @Override
  public void setLineDash( final int[] dash )
  {
    gc.setLineDash( dash );
  }

  @Override
  public void setLineWidth( final int width )
  {
    gc.setLineWidth( width );
  }

  /**
   * Takes an AWT Font.
   * 
   * @param style
   * @return
   * @deprecated Use {@link AWTSWTImageUtils#toFontStyle(java.awt.Font)} instead
   */
  @Deprecated
  public static int toFontStyle( final java.awt.Font f )
  {
    return AWTSWTImageUtils.toFontStyle( f );
  }

  @Override
  public void setFont( final java.awt.Font f )
  {
    Font swtFont;

    final int size = f.getSize() * getDPI() / 72;
    final int style = AWTSWTImageUtils.toFontStyle( f );

    swtFont = new Font( gc.getDevice(), f.getFamily(), size, style );
    if( font != null )
    {
      font.dispose();
    }
    font = swtFont;
    gc.setFont( font );
  }

  @Override
  public int getDPI( )
  {
    return gc.getDevice().getDPI().y;
  }

  @Override
  public void fillGradientRectangle( final int x, final int y, final int width, final int height, final java.awt.Color startColor, final java.awt.Color endColor, final boolean isVertical )
  {
    final Color color1 = new Color( display, startColor.getRed(), startColor.getGreen(), startColor.getBlue() );
    final Color color2 = new Color( display, endColor.getRed(), endColor.getGreen(), endColor.getBlue() );
    gc.setForeground( color1 );
    gc.setBackground( color2 );

    gc.fillGradientRectangle( x, y, width, height, isVertical );
    color1.dispose();
    color2.dispose();
  }

  /**
   * Converts SWT FontData to a AWT Font
   * 
   * @param fontData
   *          the font data
   * @return the equivalent AWT font
   * @deprecated Use {@link AWTSWTImageUtils#swtFontToAwt(FontData)} instead
   */
  @Deprecated
  public static java.awt.Font swtFontToAwt( final FontData fontData )
  {
    return AWTSWTImageUtils.swtFontToAwt( fontData );
  }

  /**
   * Converts an AWTFont to a SWT Font
   * 
   * @param font
   *          and AWT Font
   * @param fontRegistry
   * @return the equivalent SWT Font
   * @deprecated Use {@link AWTSWTImageUtils#awtFontToSwt(java.awt.Font,FontRegistry)} instead
   */
  @Deprecated
  public static org.eclipse.swt.graphics.Font awtFontToSwt( final java.awt.Font font, final FontRegistry fontRegistry )
  {
    return AWTSWTImageUtils.awtFontToSwt( font, fontRegistry );
  }
}
