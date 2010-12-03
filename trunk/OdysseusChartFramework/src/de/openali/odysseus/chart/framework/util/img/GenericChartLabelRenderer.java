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

  private ALIGNMENT m_anchorX = ALIGNMENT.LEFT;

  private ALIGNMENT m_anchorY = ALIGNMENT.TOP;

  private ALIGNMENT m_alignmentX = ALIGNMENT.LEFT;

  private ALIGNMENT m_alignmentY = ALIGNMENT.CENTERED_VERTICAL;

  private boolean m_drawBorder = false;

  private ITextStyle m_style;

  private String m_label;

  private Insets m_insets = new Insets( 2, 2, 2, 2 );

  private Point m_size = null;

  private int m_angle = 0;

  private Point calcSize( )
  {
    if( getLabel() == null || getTextStyle() == null || getLabel().trim() == "" )
      return new Point( 0, 0 );
    final Device device = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( device, 1, 1 );
    final Transform transform = new Transform( device );
    transform.rotate( getRotation() );
    final GC gc = new GC( image );
    try
    {
      getTextStyle().apply( gc );
      final Point size = gc.textExtent( m_label, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
      final int border = isDrawBorder() ? 1 : 0;// TODO getBorderLine().getWidth()
      return new Point( size.x + border * 2 + getInsets().left + getInsets().right, size.y + border * 2 + getInsets().top + getInsets().bottom );
    }
    finally
    {
      transform.dispose();
      gc.dispose();
      image.dispose();
    }
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
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC,
   *      java.lang.String, org.eclipse.swt.graphics.Point)
   */
  @Override
  public void paint( final GC gc, final Point textAnchor )
  {
    if( getLabel() == null || getTextStyle() == null || getLabel().trim() == "" || textAnchor == null )
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
    final int top = getTopLeft( getTextAnchorY() );
    final int left = getTopLeft( getTextAnchorX() );
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
      newTransform.translate( textAnchor.x, textAnchor.y );
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
      final int border = (isDrawBorder() ? gc.getLineWidth() : 0) + getInsets().top;
      for( int i = 0; i < lines.length; i++ )
      {
        final int lineInset = getLineInset( gc, border, lines[i], getAlignmentX() );
        gc.drawText( lines[i], left + lineInset, top + border + i * lineHeight, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
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
      newTransform.translate( -textAnchor.x, -textAnchor.y );
      newTransform.rotate( -getRotation() );
      gc.setTransform( newTransform );

      // dispose Font,Transform and Colors
      newFont.dispose();
      newTransform.dispose();
      newFillCol.dispose();
      newTextCol.dispose();

    }

  }

  private int getTopLeft( final ALIGNMENT pos )
  {
    switch( pos )
    {
      case RIGHT:
        return -getSize().x;
      case LEFT:
        return 0;
      case CENTERED_HORIZONTAL:
        return -getSize().x / 2;
            case BOTTOM:
        return -getSize().y;
      case TOP:
        return 0;
      case CENTERED_VERTICAL:
        return -getSize().y / 2;
    }
    throw new IllegalArgumentException();

  }

  private int getLineInset( final GC gc, final int offset, final String line, final ALIGNMENT pos )
  {
    if( pos == ALIGNMENT.RIGHT )
      return getSize().x - gc.textExtent( line, SWT.DRAW_TAB ).x - getInsets().right - offset;
    else if( pos == ALIGNMENT.LEFT )
      return m_insets.left + offset;
    else if( pos == ALIGNMENT.CENTERED_HORIZONTAL )
      return offset + (getSize().x - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;
    else if( pos == ALIGNMENT.CENTER )
      return offset + (getSize().x - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;
    else if( pos == ALIGNMENT.TICK_CENTERED )
      return offset + (getSize().x - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;

    throw new IllegalArgumentException();

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
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getDrawBorder()
   */
  @Override
  public boolean isDrawBorder( )
  {

    return m_drawBorder;
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
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getRotation()
   */
  @Override
  public int getRotation( )
  {
    return m_angle;
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

}
