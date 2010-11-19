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

  private ALIGNMENT m_centerX = ALIGNMENT.LEFT;

  private ALIGNMENT m_centerY = ALIGNMENT.TOP;

  private ALIGNMENT m_linePosition = ALIGNMENT.LEFT;

  private boolean m_drawBorder = false;

  private ITextStyle m_style;

  private String m_label;

  private Insets m_insets = new Insets( 2, 2, 2, 2 );

  private Point m_size = null;

  private int m_angle = 0;

  private Point calcSize( )
  {
    if( m_label == null || m_style == null || m_label.trim() == "" )
      return new Point( 0, 0 );

    final Image image = new Image( PlatformUI.getWorkbench().getDisplay(), 1, 1 );
    final GC gc = new GC( image );
    try
    {
      m_style.apply( gc );
      final Point size = gc.textExtent( m_label, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
      final int border = m_drawBorder ? 1 : 0;// TODO getBorderLine().getWidth()
      return new Point( size.x + border * 2 + m_insets.left + m_insets.right, size.y + border * 2 + m_insets.top + m_insets.bottom );
    }
    finally
    {
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
  public void paint( final GC gc, final Point anchor )
  {
    if( m_label == null || m_style == null || m_label.trim() == "" )
      return;

    // save GC
    final Device device = gc.getDevice();
    final Font oldFont = gc.getFont();
    final Color oldFillCol = gc.getBackground();
    final Color oldTextCol = gc.getForeground();
    final int oldLineWidth = gc.getLineWidth();
    final int oldAlpha = gc.getAlpha();
    final Transform oldTransform = new Transform( device );
    gc.getTransform( oldTransform );

    // get Font and Colors
    final Font newFont = new Font( device, m_style.toFontData() );
    final Color newFillCol = new Color( device, m_style.getFillColor() );
    final Color newTextCol = new Color( device, m_style.getTextColor() );
    final Transform newTransform = new Transform( device );

    // calculate top,left
    final int top = getTopLeft( anchor, m_anchorY );
    final int left = getTopLeft( anchor, m_anchorX );
    final int midX = left + getSize().x / 2;
    final int midY = top + getSize().y / 2;
    final Rectangle boundsRect = new Rectangle( left, top, getSize().x, getSize().y );
    try
    {
      // prepare GC
      gc.setFont( newFont );
      gc.setBackground( newFillCol );
      gc.setForeground( newTextCol );
      gc.setAlpha( m_style.getAlpha() );
      gc.setLineWidth( 1 );// TODO getBorderLine().getWidth()

      // apply top,left and rotation
      newTransform.translate( midX, midY );
      newTransform.rotate( m_angle );
      newTransform.translate( -midX, -midY );

      // draw BorderRect
      gc.fillRectangle( boundsRect );
      if( m_drawBorder )
      {
        gc.drawRectangle( boundsRect );
      }
      // draw Text
      final String[] lines = StringUtils.split( m_label, "\n" );
      final int lineHeight = gc.textExtent( "Pq" ).y;
      final int offset = m_drawBorder ? 1 : 0 + m_insets.top;
      for( int i = 0; i < lines.length; i++ )
      {
        gc.drawText( lines[i], getLineInset( gc, left, lines[i], m_linePosition ), top + offset + i * lineHeight, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
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

      // dispose Font and Colors
      newFont.dispose();
      newFillCol.dispose();
      newTextCol.dispose();

    }

  }

  private int getTopLeft( final Point anchor, final ALIGNMENT pos )
  {
    switch( pos )
    {
      case RIGHT:
        return anchor.x - getSize().x;
      case LEFT:
        return anchor.x;
      case CENTERED_HORIZONTAL:
        return anchor.x - getSize().x / 2;
      case BOTTOM:
        return anchor.y - getSize().y;
      case TOP:
        return anchor.y;
      case CENTERED_VERTICAL:
        return anchor.y + -getSize().y / 2;
    }
    throw new IllegalArgumentException();

  }

  private int getLineInset( final GC gc, final int left, final String line, final ALIGNMENT pos )
  {
    if( pos == ALIGNMENT.RIGHT )
      return left + getSize().x - gc.textExtent( line, SWT.DRAW_TAB ).x - m_insets.right;
    else if( pos == ALIGNMENT.LEFT )
      return left + m_insets.left;
    else if( pos == ALIGNMENT.CENTERED_HORIZONTAL )
      return left + (getSize().x - gc.textExtent( line, SWT.DRAW_TAB ).x) / 2;

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
  public void setLinePosition( final ALIGNMENT position )
  {
    m_size = null;
    m_linePosition = position;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setRotation(int,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT)
   */
  @Override
  public void setRotation( final int angle, final ALIGNMENT centerX, final ALIGNMENT centerY )
  {
    m_size = null;
    m_angle = angle;
    m_centerX = centerX;
    m_centerY = centerY;

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
