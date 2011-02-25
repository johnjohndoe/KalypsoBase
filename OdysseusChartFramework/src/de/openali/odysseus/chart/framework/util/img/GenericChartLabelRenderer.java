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

import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author kimwerner
 */
public class GenericChartLabelRenderer implements IChartLabelRenderer
{
  private TitleTypeBean m_titleBean;

  private ILineStyle m_borderLine = null;

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

  public GenericChartLabelRenderer( final TitleTypeBean titleTypeBean, final ILineStyle borderLine )
  {
    m_titleBean = titleTypeBean;
    m_borderLine = borderLine;
  }

  private Point calcSize( )
  {
    final Point size = calcSize( m_titleBean == null ? null : m_titleBean.getText() );
    final int border = isDrawBorder() ? m_borderLine.getWidth() : 0;
    return new Point( size.x + border * 2 + m_titleBean.getInsets().left + m_titleBean.getInsets().right, size.y + border * 2 + m_titleBean.getInsets().top + m_titleBean.getInsets().bottom );
  }

  private Point calcSize( final String text )
  {
    if( StringUtils.isEmpty( text ) )
      return new Point( 0, 0 );
    if( isImageURL( text ) )
      return getImageSize( text );

    final Device device = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( device, 1, 1 );
    final Transform transform = new Transform( device );
    transform.rotate( m_titleBean.getRotation() );
    final GC gc = new GC( image );
    try
    {
      m_titleBean.getTextStyle().apply( gc );
      return gc.textExtent( text, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
    }
    finally
    {
      transform.dispose();
      gc.dispose();
      image.dispose();
    }
  }

  private String fitToFixedWidth( final String line, final int width )
  {
    if( width < 1 )
      return line;
    final Point letterSize = calcSize( StringUtils.substring( line, 0, 5 ) + StringUtils.substring( line, line.length() - 5 ) );
    final int charAnz = width * 10 / letterSize.x;
    if( charAnz < line.length() )
      return StringUtils.abbreviateMiddle( line, "...", charAnz );
    return line;

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getBorderLine()
   */
  @Override
  public ILineStyle getBorderLine( )
  {
    return m_borderLine;
  }

  private Point getImageSize( final String text )
  {
    final Device device = PlatformUI.getWorkbench().getDisplay();
    final ImageData imageData = loadImage( device, text.substring( 4 ) );
    if( imageData == null )
      return new Point( 0, 0 );
    return new Point( imageData.width, imageData.height );
  }

  private int getLineInset( final GC gc, final int offset, final String line, final ALIGNMENT pos, final int width )
  {
    if( pos == ALIGNMENT.RIGHT )
      return width - gc.textExtent( line, SWT.DRAW_TAB ).x - m_titleBean.getInsets().right - offset;
    else if( pos == ALIGNMENT.LEFT )
      return m_titleBean.getInsets().left + offset;
    else if( pos == ALIGNMENT.CENTER )
      return offset + (width - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;
    else
      // all other cases are centered ? or throw an exception ?
      return offset + (width - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;
    // throw new IllegalArgumentException();

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getSize()
   */
  @Override
  public Point getSize( )
  {

    return calcSize();

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getTitleTypeBean()
   */
  @Override
  public TitleTypeBean getTitleTypeBean( )
  {
    return m_titleBean;
  }

  private Point getTopLeft( final ALIGNMENT posX, final ALIGNMENT posY, final int width, final int height )
  {
    int left;
    int top;

    switch( posX )
    {
      case RIGHT:
      {
        left = -width;
        break;
      }
      case LEFT:
      {
        left = 0;
        break;
      }
      case CENTER:
      {
        left = -width / 2;
        break;
      }

      default:
        left = -width / 2;
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
        top = -height / 2;
        break;
      }
      case BOTTOM:
      {
        top = -height;
        break;
      }
      default:
        top = -height / 2;
    }
    return new Point( left, top );

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#isDrawBorder()
   */
  @Override
  public boolean isDrawBorder( )
  {

    return m_borderLine != null && m_borderLine.isVisible();
  }

  private boolean isImageURL( final String text )
  {
    return text.startsWith( "URL:" );
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

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC,
   *      java.lang.String, org.eclipse.swt.graphics.Point)
   */
  @Override
  public void paint( final GC gc, final Point textAnchor )
  {
    paint( gc, new Rectangle( textAnchor.x, textAnchor.y, -1, -1 ) );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Rectangle)
   */
  @Override
  public void paint( final GC gc, final Rectangle fixedWidth )
  {
    if( m_titleBean == null || StringUtils.isEmpty( m_titleBean.getText() ) || fixedWidth == null )
      return;

    // save GC
    final Device device = gc.getDevice();
    final Font oldFont = gc.getFont();
    final Color oldFillCol = gc.getBackground();
    final Color oldTextCol = gc.getForeground();
    final int oldLineWidth = gc.getLineWidth();
    final int oldAlpha = gc.getAlpha();

    // get Font and Colors
    final Font newFont = new Font( device, m_titleBean.getTextStyle().toFontData() );
    final Color newFillCol = new Color( device, m_titleBean.getTextStyle().getFillColor() );
    final Color newTextCol = new Color( device, m_titleBean.getTextStyle().getTextColor() );

    // calculate top,left correction
    final Point size = getSize();
    final int width = size.x;
    final int height = size.y;
    final Point fixedSize = new Point( fixedWidth.width > 1 ? fixedWidth.width : width, fixedWidth.height > 1 ? fixedWidth.height : height );
    final Point topLeftCorrection = getTopLeft( m_titleBean.getTextAnchorX(), m_titleBean.getTextAnchorY(), width, height );

    final Transform newTransform = new Transform( device );

    try
    {
      // prepare GC
      gc.setFont( newFont );
      gc.setBackground( newFillCol );
      gc.setForeground( newTextCol );
      gc.setAlpha( m_titleBean.getTextStyle().getAlpha() );
      gc.setLineWidth( 1 );// TODO getBorderLine().getWidth()

      // apply top,left and rotation
      gc.getTransform( newTransform );
      newTransform.translate( fixedWidth.x, fixedWidth.y );
      newTransform.rotate( m_titleBean.getRotation() );
      gc.setTransform( newTransform );

      // draw BorderRect
      final Rectangle textRect = new Rectangle( topLeftCorrection.x, topLeftCorrection.y, fixedSize.x, fixedSize.y );
      gc.fillRectangle( textRect );
      if( isDrawBorder() )
      {
        // TODO: BorderStyle auswerten
        gc.drawRectangle( textRect );
      }
      if( isImageURL( m_titleBean.getText() ) )
      {
        // draw image
        final ImageData imageData = loadImage( device, m_titleBean.getText().substring( 4 ) );
        if( imageData != null )
        {
          final Image image = new Image( device, imageData );
          try
          {
            gc.drawImage( image, 0, 0 );
          }
          finally
          {
            image.dispose();
          }
        }
      }
      else
      // draw Text
      {
        final String[] lines = StringUtils.split( m_titleBean.getText(), "\n" );// TODO: maybe other split strategy
        final int lineHeight = gc.textExtent( "Pq" ).y;

        final Insets insets = m_titleBean.getInsets();
        final int border = (isDrawBorder() ? gc.getLineWidth() : 0) + insets.top;

        for( int i = 0; i < lines.length; i++ )
        {
          final String line = fitToFixedWidth( lines[i], fixedWidth.width );
          final int lineInset = getLineInset( gc, border, line, m_titleBean.getTextStyle().getAlignment(), fixedSize.x );
          gc.drawText( line, topLeftCorrection.x + lineInset, topLeftCorrection.y + border + i * lineHeight, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
        }
      }
    }
    finally
    {
      // restore GC
      gc.setLineWidth( oldLineWidth );
      gc.setFont( oldFont );
      gc.setBackground( oldFillCol );
      gc.setForeground( oldTextCol );
      gc.setAlpha( oldAlpha );
      newTransform.translate( -fixedWidth.x, -fixedWidth.y );
      newTransform.rotate( -m_titleBean.getRotation() );
      gc.setTransform( newTransform );

      // dispose Font,Transform and Colors
      newFont.dispose();
      newTransform.dispose();
      newFillCol.dispose();
      newTextCol.dispose();

    }

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setBorderLine(de.openali.odysseus.chart.framework.model.style.ILineStyle)
   */
  @Override
  public void setBorderLine( final ILineStyle borderLine )
  {
    m_borderLine = borderLine;

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setTitleTypeBean(de.openali.odysseus.chart.framework.util.img.TitleTypeBean)
   */
  @Override
  public void setTitleTypeBean( final TitleTypeBean titleTypeBean )
  {
    m_titleBean = titleTypeBean;

  }

}
