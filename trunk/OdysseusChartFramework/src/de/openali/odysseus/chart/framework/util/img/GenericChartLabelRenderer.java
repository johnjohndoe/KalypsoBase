/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

/**
 * @author kimwerner
 */
public class GenericChartLabelRenderer implements IChartLabelRenderer
{
  private static final Insets DEFAULT_INSETS = new Insets( 2, 2, 2, 2 );

  private ALIGNMENT m_anchorX = ALIGNMENT.LEFT;

  private ALIGNMENT m_anchorY = ALIGNMENT.TOP;

  private ALIGNMENT m_alignmentX = ALIGNMENT.LEFT;

  private ALIGNMENT m_alignmentY = ALIGNMENT.CENTERED_VERTICAL;

  private boolean m_drawBorder = false;

  private ITextStyle m_style;

  private String m_label;

  private Insets m_insets = DEFAULT_INSETS;

  private Point m_size = null;

  private int m_angle = 0;

  private Point calcSize( final String text )
  {
    if( StringUtils.isEmpty( text ) || getTextStyle() == null )
      return new Point( 0, 0 );

    final Device device = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( device, 1, 1 );
    final Transform transform = new Transform( device );
    transform.rotate( getRotation() );
    final GC gc = new GC( image );
    try
    {
      getTextStyle().apply( gc );
      return gc.textExtent( text, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
    }
    finally
    {
      transform.dispose();
      gc.dispose();
      image.dispose();
    }
  }

  private Point calcSize( )
  {
    final Point size = calcSize( getLabel() );
    final int border = isDrawBorder() ? 1 : 0;// TODO getBorderLine().getWidth()
    return new Point( size.x + border * 2 + getInsets().left + getInsets().right, size.y + border * 2 + getInsets().top + getInsets().bottom );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#eatBean(de.openali.odysseus.chart.framework.util.img.TitleTypeBean)
   */
  @Override
  public void eatBean( final TitleTypeBean titleTypeBean )
  {

    if( titleTypeBean == null )
      return;
    setAlignment( titleTypeBean.getAlignmentHorizontal(), titleTypeBean.getAlignmentVertical() );
    setTextAnchor( titleTypeBean.getTextAnchorX(), titleTypeBean.getTextAnchorY() );
    setLabel( titleTypeBean.getText() );
    setTextStyle( titleTypeBean.getTextStyle() );
    setRotation( titleTypeBean.getRotation() );
    setInsets( titleTypeBean.getInsets() );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getLinePosition()
   */
  @Override
  public ALIGNMENT getAlignmentX( )
  {

    return m_alignmentX;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getLinePosition()
   */
  @Override
  public ALIGNMENT getAlignmentY( )
  {

    return m_alignmentY;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getInsets()
   */
  @Override
  public Insets getInsets( )
  {
    return m_insets;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getLabel()
   */
  @Override
  public String getLabel( )
  {
    return m_label;
  }

  private int getLineInset( final GC gc, final int offset, final String line, final ALIGNMENT pos, final int width )
  {
    if( pos == ALIGNMENT.RIGHT )
      return width - gc.textExtent( line, SWT.DRAW_TAB ).x - getInsets().right - offset;
    else if( pos == ALIGNMENT.LEFT )
      return m_insets.left + offset;
    else if( pos == ALIGNMENT.CENTERED_HORIZONTAL )
      return offset + (width - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;
    else if( pos == ALIGNMENT.CENTER )
      return offset + (width - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;
    else if( pos == ALIGNMENT.TICK_CENTERED )
      return offset + (width - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;
    else if( pos == ALIGNMENT.INTERVALL_CENTERED )
      return offset + (width - gc.textExtent( line, SWT.DRAW_TAB ).x);

    throw new IllegalArgumentException();

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getRotation()
   */
  @Override
  public int getRotation( )
  {
    return m_angle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getSize(java.lang.String)
   */
  @Override
  public Point getSize( )
  {
    if( m_size == null )
    {
      m_size = calcSize();

    }
    return m_size;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getTextAnchor()
   */
  @Override
  public ALIGNMENT getTextAnchorX( )
  {
    return m_anchorX;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getTextAnchor()
   */
  @Override
  public ALIGNMENT getTextAnchorY( )
  {
    return m_anchorY;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getTextStyle()
   */
  @Override
  public ITextStyle getTextStyle( )
  {

    return m_style;
  }

  private int getTopLeft( final ALIGNMENT pos, final Rectangle rect )
  {
    final int width = rect.width > 1 ? Math.min( rect.width, getSize().x ) : getSize().x;

    switch( pos )
    {
      case RIGHT:
        return -width;
      case LEFT:
        return 0;
      case CENTERED_HORIZONTAL:
        return -width / 2;
      case BOTTOM:
        return -getSize().y;
      case TOP:
        return 0;
      case CENTERED_VERTICAL:
        return -getSize().y / 2;
      case TICK_CENTERED:
        return -width / 2;
      case INTERVALL_CENTERED:
        return rect.width < 0 ? 0 : (rect.width - width) / 2;

    }
    throw new IllegalArgumentException();

  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getDrawBorder()
   */
  @Override
  public boolean isDrawBorder( )
  {

    return m_drawBorder;
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
    if( StringUtils.isEmpty( getLabel() ) || getTextStyle() == null || fixedWidth == null )
      return;

    // save GC
    final Device device = gc.getDevice();
    final Font oldFont = gc.getFont();
    final Color oldFillCol = gc.getBackground();
    final Color oldTextCol = gc.getForeground();
    final int oldLineWidth = gc.getLineWidth();
    final int oldAlpha = gc.getAlpha();

    // get Font and Colors
    final Font newFont = new Font( device, getTextStyle().toFontData() );
    final Color newFillCol = new Color( device, getTextStyle().getFillColor() );
    final Color newTextCol = new Color( device, getTextStyle().getTextColor() );

    // calculate top,left
    final int top = getTopLeft( getTextAnchorY(), fixedWidth );
    final int left = getTopLeft( getTextAnchorX(), fixedWidth );
// final int midX = left + getSize().x / 2;
// final int midY = top + getSize().y / 2;

    final Transform newTransform = new Transform( device );

    try
    {
      // prepare GC
      gc.setFont( newFont );
      gc.setBackground( newFillCol );
      gc.setForeground( newTextCol );
      gc.setAlpha( getTextStyle().getAlpha() );
      gc.setLineWidth( 1 );// TODO getBorderLine().getWidth()

      // apply top,left and rotation
      gc.getTransform( newTransform );
      newTransform.translate( fixedWidth.x, fixedWidth.y );
      newTransform.rotate( getRotation() );
      gc.setTransform( newTransform );

      // draw BorderRect
      final Rectangle textRect = new Rectangle( left, top, getSize().x, getSize().y );
      gc.fillRectangle( textRect );
      if( isDrawBorder() )
      {
        gc.drawRectangle( textRect );
      }
      // draw Text
      final String[] lines = StringUtils.split( getLabel(), "\n" );// TODO: maybe other split strategy
      final int lineHeight = gc.textExtent( "Pq" ).y;

      final Insets insets = getInsets();
      final int border = (isDrawBorder() ? gc.getLineWidth() : 0) + insets.top;

      for( int i = 0; i < lines.length; i++ )
      {
        final String line = fitToFixedWidth( lines[i], fixedWidth.width );
        final int lineInset = getLineInset( gc, border, line, getAlignmentX(), fixedWidth.width < 0 ? getSize().x : Math.min( fixedWidth.width, getSize().x ) );
        gc.drawText( line, left + lineInset, top + border + i * lineHeight, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
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
      newTransform.rotate( -getRotation() );
      gc.setTransform( newTransform );

      // dispose Font,Transform and Colors
      newFont.dispose();
      newTransform.dispose();
      newFillCol.dispose();
      newTextCol.dispose();

    }

  }

  private final String fitToFixedWidth( final String line, final int width )
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
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setLinePosition(de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT)
   */
  @Override
  public void setAlignment( final ALIGNMENT alignmentX, final ALIGNMENT alignmentY )
  {
    m_size = null;
    m_alignmentX = alignmentX;
    m_alignmentY = alignmentY;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setDrawBorder(boolean)
   */
  @Override
  public void setDrawBorder( final boolean drawBorder )
  {
    m_size = null;
    m_drawBorder = drawBorder;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.LabelRenderer.IChartLabelRenderer#setInsets(java.awt.Insets)
   */
  @Override
  public void setInsets( final Insets insets )
  {
    m_size = null;
    if( insets == null )
      m_insets = DEFAULT_INSETS;
    else
      m_insets = insets;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setLabel(java.lang.String)
   */
  @Override
  public void setLabel( final String label )
  {
    if( label != null && label.equals( m_label ) )
      return;
    m_size = null;
    m_label = label;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setRotation(int,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT)
   */
  @Override
  public void setRotation( final int degree )
  {
    m_size = null;
    m_angle = degree;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setTextAnchor(de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT)
   */
  @Override
  public void setTextAnchor( final ALIGNMENT positionX, final ALIGNMENT positionY )
  {
    m_size = null;
    m_anchorX = positionX;
    m_anchorY = positionY;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.LabelRenderer.IChartLabelRenderer#setTextStyle(de.openali.odysseus.chart.framework.model.style.ITextStyle)
   */
  @Override
  public void setTextStyle( final ITextStyle textStyle )
  {
    m_size = null;
    m_style = textStyle;

  }

}
