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
package org.kalypso.contribs.eclipse.swt.canvas;

import java.awt.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Content area of an {@link ImageCanvas2}. An {@link ImageCanvas2} can contain a tuple of {@link IContentArea}'s
 * 
 * @author Dirk Kuch
 */
public abstract class DefaultContentArea implements IContentArea
{
  protected int m_spacing = 10;

  private Image m_image;

  private String m_text;

  private int m_textPosition;

  private Font m_textFont;

  private Color m_textColor;

  private MouseAdapter m_mouseAdapter;

  private String m_tooltip;

  private org.eclipse.swt.graphics.Point m_textExtent;

  private Image m_hoverImage;

  private boolean m_hover;

  private boolean m_enabled = true;

  private Image m_disabledImage;

  public DefaultContentArea( )
  {
  }

  public DefaultContentArea( final Integer spacing )
  {
    m_spacing = spacing;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#draw(org.eclipse.swt.events.PaintEvent)
   */
  @Override
  public void draw( final PaintEvent e )
  {
    final Point point = getContentAreaAnchorPoint();

    if( isEnabled() )
    {
      if( m_image != null )
      {
        /* draw image */
        if( m_hover && m_hoverImage != null )
        {
          e.gc.drawImage( m_hoverImage, point.x, point.y );
        }
        else
        {
          e.gc.drawImage( m_image, point.x, point.y );
        }
      }
    }
    else if( m_disabledImage != null )
    {
      /* draw disabled image */
      e.gc.drawImage( m_disabledImage, point.x, point.y );
    }

    /* draw text */
    if( m_text != null )
    {
      final Color oldColor = e.gc.getForeground();
      final Font oldFont = e.gc.getFont();

      e.gc.setForeground( m_textColor );
      e.gc.setFont( m_textFont );

      m_textExtent = e.gc.textExtent( m_text );

      if( m_image != null )
      {
        drawImageText( point, e );
      }
      else
      {
        drawTextOnly( point, e );
      }

      e.gc.setForeground( oldColor );
      e.gc.setFont( oldFont );
    }
  }

  private void drawTextOnly( final Point point, final PaintEvent e )
  {

    e.gc.drawText( m_text, point.x, point.y, true );

  }

  private void drawImageText( final Point point, final PaintEvent e )
  {
    final Rectangle imageBounds = m_image.getBounds();

    if( m_textPosition == SWT.BOTTOM )
    {
      int x;
      final int y = point.y + imageBounds.height + m_spacing;

      if( m_textExtent.x - imageBounds.width > 5 )
      {
        x = point.x + (imageBounds.width - m_textExtent.x) / 2;
      }
      else
      {
        x = point.x;
      }

      e.gc.drawText( m_text, x, y, true );
    }
    else if( m_textPosition == SWT.RIGHT )
    {
      final int x = point.x + imageBounds.width + m_spacing;
      final int y = point.y + (imageBounds.height / 2) - (m_textExtent.y / 2);

      e.gc.drawText( m_text, x, y, true );
    }
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#getContentAreaAnchorPoint()
   */
  @Override
  public abstract Point getContentAreaAnchorPoint( );

  @Override
  public void setImage( final Image image )
  {
    m_image = image;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#setHoverImage(org.eclipse.swt.graphics.Image)
   */
  @Override
  public void setHoverImage( final Image image )
  {
    m_hoverImage = image;
  }

  /**
   * @param textPosition
   *          at the moment only SWT.BOTTOM & SWT.RIGHT supported
   */

  @Override
  public void setText( final String text, final Font textFont, final Color textColor, final int textPosition )
  {
    m_text = text;
    m_textFont = textFont;
    m_textColor = textColor;
    m_textPosition = textPosition;
  }

  public void setMouseListener( final MouseAdapter mouseAdapter )
  {
    m_mouseAdapter = mouseAdapter;
  }

  public void setTooltip( final String tooltip )
  {
    m_tooltip = tooltip;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#getBoundingBox()
   */
  @Override
  public Rectangle getBoundingBox( )
  {
    final Point anchor = getContentAreaAnchorPoint();

    int width = 0;
    int height = 0;

    if( m_image != null )
    {
      final Rectangle imgBounds = m_image.getBounds();
      width += imgBounds.width;
      height += imgBounds.height;
    }

    if( m_textExtent != null )
    {
      width += m_textExtent.x;
      height += m_textExtent.y;
    }

    /* add spacing */
    if( m_image != null && m_textExtent != null )
    {
      if( SWT.RIGHT == m_textPosition )
      {
        width += m_spacing;
      }
      else if( SWT.BOTTOM == m_textPosition )
      {
        height += m_spacing;
      }
    }

    return new Rectangle( anchor.x, anchor.y, width, height );
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#hasMouseListener()
   */
  @Override
  public boolean hasMouseListener( )
  {
    if( m_mouseAdapter == null )
      return false;

    return true;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#getTooltip()
   */
  @Override
  public String getTooltip( )
  {
    return m_tooltip;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDoubleClick( final MouseEvent e )
  {
    if( isEnabled() )
      if( m_mouseAdapter != null )
      {
        m_mouseAdapter.mouseDoubleClick( e );
      }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDown( final MouseEvent e )
  {
    if( isEnabled() )
      if( m_mouseAdapter != null )
      {
        m_mouseAdapter.mouseDown( e );
      }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
    if( isEnabled() )
      if( m_mouseAdapter != null )
      {
        m_mouseAdapter.mouseUp( e );
      }
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#hover()
   */
  @Override
  public boolean hover( final boolean hover )
  {
    if( m_hover != hover && m_enabled )
    {
      m_hover = hover;

      return true;
    }

    return false;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#isEnabled()
   */
  @Override
  public boolean isEnabled( )
  {
    return m_enabled;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#setEnabled(boolean)
   */
  @Override
  public void setEnabled( final boolean state )
  {
    m_enabled = state;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.canvas.IContentArea#setDisabledImage(org.eclipse.swt.graphics.Image)
   */
  @Override
  public void setDisabledImage( final Image image )
  {
    m_disabledImage = image;
  }
}
