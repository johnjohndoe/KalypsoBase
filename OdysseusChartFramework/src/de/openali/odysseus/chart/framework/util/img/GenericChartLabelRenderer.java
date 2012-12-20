/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package de.openali.odysseus.chart.framework.util.img;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author kimwerner
 */
public class GenericChartLabelRenderer implements IChartLabelRenderer
{
  private TitleTypeBean m_titleBean;

  private IAreaStyle m_borderStyle = null;

  private final int m_drawTransparent = SWT.DRAW_TRANSPARENT;

  public GenericChartLabelRenderer( )
  {
    m_titleBean = new TitleTypeBean( null );
  }

  public GenericChartLabelRenderer( final String label )
  {
    m_titleBean = new TitleTypeBean( label );
  }

  public GenericChartLabelRenderer( final TitleTypeBean titleTypeBean )
  {
    m_titleBean = titleTypeBean;
  }

  public GenericChartLabelRenderer( final TitleTypeBean titleTypeBean, final IAreaStyle borderStyle )
  {
    m_titleBean = titleTypeBean;
    m_borderStyle = borderStyle;
  }

  private Rectangle calcSize( final GC gc, final int degree )
  {
    if( m_titleBean == null )
      return new Rectangle( 0, 0, 0, 0 );

    final Point textSize = calcTextSize( gc, m_titleBean.getText() );
    final int border = isDrawBorder() ? m_borderStyle.getStroke().getWidth() : 0;
    final Point overAllSize = new Point( textSize.x + border * 2 + m_titleBean.getInsets().left + m_titleBean.getInsets().right, textSize.y + border * 2 + m_titleBean.getInsets().top
        + m_titleBean.getInsets().bottom );

    final Rectangle textRect = getTextRect( m_titleBean.getTextAnchorX(), m_titleBean.getTextAnchorY(), overAllSize );
    final double radian = Math.toRadians( degree );
    final double cosi = Math.cos( radian );
    final double sinu = Math.sin( radian );
    final double rotX = cosi * textRect.x + sinu * textRect.y;
    final double rotY = sinu * textRect.x + cosi * textRect.y;
    final double rotWidth = Math.abs( cosi * textRect.width ) + Math.abs( sinu * textRect.height );
    final double rotHeight = Math.abs( sinu * textRect.width ) + Math.abs( cosi * textRect.height );

    return new Rectangle( (int) Math.round( rotX ), (int) Math.round( rotY ), (int) Math.round( rotWidth ), (int) Math.round( rotHeight ) );
  }

  private Point calcTextSize( final GC gc, final String text )
  {
    if( StringUtils.isEmpty( text ) )
      return new Point( 0, 0 );

    if( isImageURL( text ) )
      return getImageSize( text );

    m_titleBean.getTextStyle().apply( gc );

    return gc.textExtent( text, m_drawTransparent | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
  }

  final private Rectangle checkSize( final Rectangle boundsRect, final Rectangle textRect, final ALIGNMENT alignment )
  {
    final int delta = textRect.width - boundsRect.width;
    final int height = boundsRect.height < 0 ? textRect.height : boundsRect.height;
    if( boundsRect.width < 0 || delta <= 0 )
      return new Rectangle( textRect.x, textRect.y, textRect.width, height );

    switch( alignment )
    {
      case LEFT:
      {
        return new Rectangle( textRect.x, textRect.y, textRect.width - delta, height );
      }
      case RIGHT:
      {
        return new Rectangle( textRect.x + delta, textRect.y, textRect.width - delta, height );
      }
      case CENTER:
      {
        return new Rectangle( textRect.x + delta / 2, textRect.y, textRect.width - delta, height );
      }
      default:
        return textRect;
    }
  }

  private String fitToFixedWidth( final GC gc, final String line, final int width )
  {
    if( width < 1 )
      return line;

    final int lineWidth = calcTextSize( gc, line ).x;
    final int charWidth = lineWidth / line.length();
    if( lineWidth <= width )
      return line;
    int maxChar = width / charWidth;
    if( maxChar < 6 )
      return StringUtils.abbreviate( line, 5 );
    String s = StringUtils.abbreviateMiddle( line, "..", maxChar ); //$NON-NLS-1$
    while( calcTextSize( gc, s ).x > width )
    {
      maxChar -= 1;
      s = StringUtils.abbreviateMiddle( line, "..", maxChar ); //$NON-NLS-1$
    }
    return s;
  }

  @Override
  public IAreaStyle getBorderStyle( )
  {
    return m_borderStyle;
  }

  private Point getImageSize( final String imageURL )
  {
    final Device device = ChartUtilities.getDisplay();
    final ImageData imageData = loadImage( device, imageURL.substring( 4 ) );
    if( imageData == null )
      return new Point( 0, 0 );
    return new Point( imageData.width, imageData.height );
  }

  private int getLineInset( final ALIGNMENT posX, final int lineWidth, final int width )
  {
    int left;
    switch( posX )
    {
      case RIGHT:
      {
        left = width - lineWidth;
        break;
      }
      case LEFT:
      {
        left = 0;
        break;
      }
      case CENTER:
      {
        left = (width - lineWidth) / 2;
        break;
      }

      default:
        left = (lineWidth - width) / 2;
    }

    return left;

  }

  private Point getRendererAnchor( final ALIGNMENT posX, final ALIGNMENT posY, final Rectangle rect )
  {
    int left;
    int top;
    final int width = rect.width < 0 ? 0 : rect.width;
    final int height = rect.height < 0 ? 0 : rect.height;
    switch( posX )
    {
      case RIGHT:
      {
        left = rect.x + width;
        break;
      }
      case LEFT:
      {
        left = rect.x;
        break;
      }
      case CENTER:
      {
        left = rect.x + width / 2;
        break;
      }

      default:
        left = rect.x + width / 2;
    }
    switch( posY )
    {
      case TOP:
      {
        top = rect.y;
        break;
      }

      case CENTER:
      {
        top = rect.y + height / 2;
        break;
      }
      case BOTTOM:
      {
        top = rect.y + height;
        break;
      }
      default:
        top = rect.y + height / 2;
    }
    return new Point( left, top );

  }

  @Override
  public Rectangle getSize( )
  {
    final Device device = ChartUtilities.getDisplay();
    final Image image = new Image( device, 1, 1 );
    final GC gc = new GC( image );
    try
    {
      m_titleBean.getTextStyle().apply( gc );

      return calcSize( gc, m_titleBean.getRotation() );
    }
    finally
    {
      gc.dispose();
      image.dispose();
    }
  }

  private Rectangle getTextRect( final ALIGNMENT posX, final ALIGNMENT posY, final Point size )
  {
    int left;
    int top;
    switch( posX )
    {
      case RIGHT:
      {
        left = -size.x;
        break;
      }
      case LEFT:
      {
        left = 0;
        break;
      }
      case CENTER:
      {
        left = -size.x / 2;
        break;
      }

      default:
        left = -size.x / 2;
    }
    switch( posY )
    {
      case TOP:
      {
        top = 0;
        break;
      }

      case CENTER:
      {
        top = -size.y / 2;
        break;
      }
      case BOTTOM:
      {
        top = -size.y;
        break;
      }
      default:
        top = -size.y / 2;
    }
    return new Rectangle( left, top, size.x, size.y );

  }

  @Override
  public TitleTypeBean getTitleTypeBean( )
  {
    return m_titleBean;
  }

  @Override
  public boolean isDrawBorder( )
  {
    return getBorderStyle() != null && getBorderStyle().getStroke().isVisible();
  }

  private boolean isImageURL( final String text )
  {// FIXME: WHAT IS THIS?!
    //Falls ein Image (Icon) als Titel gezeichnet werden soll, wird es zurzeit mit einer URL referenziert.
    //TODO:ein ImageObject statt eines String ¸bergeben
    return text.startsWith( "URL:" ); //$NON-NLS-1$
  }

  private ImageData loadImage( final Device dev, final String text )
  {
    if( text == null || text.length() == 0 )
      return null;

    InputStream inputStream = null;
    Image image = null;
    try
    {
      final URL imageURL = new URL( text );
      inputStream = imageURL.openStream();
      image = new Image( dev, inputStream );
      final ImageData imageData = image.getImageData();
      final int maxHeight = getTitleTypeBean().getTextStyle().getHeight();
      if( maxHeight < imageData.height )
      {
        final double scale = imageData.height / maxHeight;
        return imageData.scaledTo( new Double( imageData.width / scale ).intValue(), maxHeight );
      }

      return imageData;
    }
    catch( final MalformedURLException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch( final IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally
    {
      if( image != null )
        image.dispose();
      IOUtils.closeQuietly( inputStream );
    }
    return null;
  }

  @Override
  public void paint( final GC gc, final Point textAnchor )
  {
    paint( gc, new Rectangle( textAnchor.x, textAnchor.y, -1, -1 ) );
  }

  @Override
  public void paint( final GC gc, final Rectangle boundsRect )
  {
    if( gc == null || m_titleBean == null || StringUtils.isEmpty( m_titleBean.getText() ) || boundsRect == null )
      return;

    // save GC
    final Font oldFont = gc.getFont();
    final Color oldFillCol = gc.getBackground();
    final Color oldTextCol = gc.getForeground();
    final int oldLineWidth = gc.getLineWidth();
    final int oldAlpha = gc.getAlpha();
    // create Transform
    final Device device = gc.getDevice();
    final Transform oldTransform = new Transform( device );
    final Transform newTransform = new Transform( device );
    final Point rendererAnchor = getRendererAnchor( m_titleBean.getPositionHorizontal(), m_titleBean.getPositionVertical(), boundsRect );
    final Rectangle titleRect = calcSize( gc, 0 );
    final Rectangle fitRect = checkSize( boundsRect, titleRect, m_titleBean.getPositionHorizontal() );
    try
    {
      // get transform from gc
      gc.getTransform( oldTransform );
      gc.getTransform( newTransform );
      // move to renderer AnchorPoint
      newTransform.translate( rendererAnchor.x, rendererAnchor.y );
      // rotate TextRectangle
      newTransform.rotate( m_titleBean.getRotation() );
      // mirror Text
      if( m_titleBean.isMirrorHorizontal() || m_titleBean.isMirrorVertical() )
      {
        newTransform.translate( fitRect.x + fitRect.width / 2, fitRect.y + fitRect.height / 2 );
        newTransform.scale( m_titleBean.isMirrorHorizontal() ? -1 : 1, m_titleBean.isMirrorVertical() ? -1 : 1 );
        newTransform.translate( -(fitRect.x -1 + fitRect.width / 2), -(fitRect.y +1 + fitRect.height / 2) );
      }
      gc.setTransform( newTransform );

      // TODO: discuss with Gernot: usnig same element for image and text is cool, but ugly. Why not define a 'titleBean' and a 'imageBean' separately?
      if( isImageURL( m_titleBean.getText() ) )
        paintAsImage( gc, device, fitRect );
      else
        paintAsText( gc, boundsRect );
    }
    finally
    {
      // restore GC
      gc.setLineWidth( oldLineWidth );
      gc.setFont( oldFont );
      gc.setBackground( oldFillCol );
      gc.setForeground( oldTextCol );
      gc.setAlpha( oldAlpha );
      // restore gc

      gc.setTransform( oldTransform );
      oldTransform.dispose();
      newTransform.dispose();
    }
  }

  private void paintAsImage( final GC gc, final Device device, final Rectangle fitRect )
  {
    final ImageData imageData = loadImage( device, m_titleBean.getText().substring( 4 ) );
    if( imageData != null )
    {
      final Image image = new Image( device, imageData );
      try
      {
        gc.drawImage( image, fitRect.x, fitRect.y );
      }
      finally
      {
        image.dispose();
      }
    }
  }

  private void paintAsText( final GC gc, final Rectangle boundsRect )
  {
    // HOTFIX: recalculate sizes here, because transformation changes the calculated text size
    // -> leads to abbreviated texts in all tooltips. Seems to work with mirror/rotation etc.
    final Rectangle titleRect2 = calcSize( gc, 0 );
    final Rectangle fitRect2 = checkSize( boundsRect, titleRect2, m_titleBean.getPositionHorizontal() );

    final int borderWidth = getBorderStyle() == null ? 0 : getBorderStyle().getStroke().getWidth();

    if( isDrawBorder() )
    {
      final Rectangle borderLineCentered = RectangleUtils.inflateRect( fitRect2, (borderWidth + 1) / 2 );
      getBorderStyle().apply( gc );
      if( getBorderStyle().isFillVisible() )
        gc.fillRectangle( borderLineCentered );
      gc.drawRectangle( borderLineCentered );
    }

    final Rectangle innerBorder = RectangleUtils.inflateRect( fitRect2, borderWidth );
    final Rectangle textRect = RectangleUtils.inflateRect( innerBorder, getTitleTypeBean().getInsets() );
    final String[] lines = StringUtils.split( m_titleBean.getText(), "\n" );// TODO: maybe other split strategy //$NON-NLS-1$

    final int lineHeight = textRect.height / lines.length;

    int top = textRect.y;

    final int flags = m_drawTransparent | SWT.DRAW_DELIMITER | SWT.DRAW_TAB;

    m_titleBean.getTextStyle().apply( gc );
    for( final String line : lines )
    {
      // FIXME: NO, WHY?!
      final String fitLine = fitToFixedWidth( gc, line, textRect.width );

      final Point lineSize = gc.textExtent( fitLine, flags );
      final int lineInset = getLineInset( m_titleBean.getPositionHorizontal(), lineSize.x, textRect.width );
      gc.drawText( fitLine, textRect.x + lineInset, top, flags );
      top += lineHeight;
    }
  }

  @Override
  public void setBorderStyle( final IAreaStyle borderStyle )
  {
    m_borderStyle = borderStyle;
  }

  @Override
  public void setTitleTypeBean( final TitleTypeBean titleTypeBean )
  {
    m_titleBean = titleTypeBean;
  }
}