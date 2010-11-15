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

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author kimwerner
 */
public class TitleTypeBean implements IChartLabelRenderer
{
  private String m_text = "";

  private ALIGNMENT m_position = ALIGNMENT.TICK_CENTERED;

  private ITextStyle m_textStyle;

  private Insets m_insets = new Insets( 0, 0, 0, 0 );

  public TitleTypeBean( final String text )
  {
    m_text = text;
  }

  public TitleTypeBean( final String text, final ALIGNMENT position, final ITextStyle textStyle, final Insets insets )
  {
    super();
    m_text = text;
    m_position = position;
    m_textStyle = textStyle;
    m_insets = insets;
  }

  @Override
  public Insets getInsets( )
  {
    return m_insets;
  }

  @Override
  public ALIGNMENT getAlignment( )
  {
    return m_position;
  }

// public Point getSize( )
// {
// if( m_size == null )
// {
// final Device dev = PlatformUI.getWorkbench().getDisplay();
// final Image image = new Image( dev, 1, 1 );
// final GC gc = new GC( image );
// final Font font = new Font( dev, getTextStyle().toFontData() );
// gc.setFont( font );
// try
// {
// m_size = gc.textExtent( getText(), SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
// m_size.x += m_insets.bottom + m_insets.top;
// m_size.y += m_insets.left + m_insets.right;
// }
// finally
// {
// gc.dispose();
// image.dispose();
// font.dispose();
// }
// }
// return m_size;
// }

// private int getStart( final int width )
// {
// final Point textWidth = getSize();
// switch( m_position )
// {
// case LEFT:
// return m_insets.left;
//
// case RIGHT:
// return width - textWidth.x - m_insets.right;
//
// }
// // all centered
// return Math.max( 0, (width - textWidth.x) / 2 );
// }

  public String getText( )
  {
    return m_text;
  }

  @Override
  public ITextStyle getTextStyle( )
  {
    if( m_textStyle == null )
      m_textStyle = StyleUtils.getDefaultTextStyle();

    return m_textStyle;
  }

  @Override
  public void setInsets( final Insets insets )
  {
    m_insets = insets;
  }

  @Override
  public void setPosition( final ALIGNMENT position )
  {
    m_position = position;
  }

  @Override
  public void setLabel( final String text )
  {
    m_text = text;
  }

  @Override
  public void setTextStyle( final ITextStyle textStyle )
  {
    m_textStyle = textStyle;
  }

}
