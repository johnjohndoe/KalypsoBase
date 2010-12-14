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

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author kimwerner
 */
public class TitleTypeBean
{
  private String m_text = "";

  private ALIGNMENT m_alignmentHorizontal = ALIGNMENT.CENTER;

  private ALIGNMENT m_alignmentVertical = ALIGNMENT.CENTER;

  private ALIGNMENT m_textAnchorX = ALIGNMENT.CENTER;

  private ALIGNMENT m_textAnchorY = ALIGNMENT.CENTER;

  private ITextStyle m_textStyle;

  private int m_rotation;

  private Insets m_insets = new Insets( 0, 0, 0, 0 );

  public TitleTypeBean( final String text )
  {
    m_text = text;
  }

  public TitleTypeBean( final String text, final ALIGNMENT alignmentHorizontal, final ITextStyle textStyle, final Insets insets )
  {
    super();
    m_text = text;
    m_alignmentHorizontal = alignmentHorizontal;
    m_textStyle = textStyle;
    m_insets = insets == null ? new Insets( 0, 0, 0, 0 ) : insets;
  }

  public ALIGNMENT getAlignmentHorizontal( )
  {
    return m_alignmentHorizontal;
  }

  public ALIGNMENT getAlignmentVertical( )
  {
    return m_alignmentVertical;
  }

  public Insets getInsets( )
  {
    return m_insets;
  }

  public ALIGNMENT getTextAnchorX( )
  {
    return m_textAnchorX;
  }

  public ALIGNMENT getTextAnchorY( )
  {
    return m_textAnchorY;
  }

  public int getRotation( )
  {
    return m_rotation;
  }

  public String getText( )
  {
    return m_text;
  }

  public ITextStyle getTextStyle( )
  {
    if( m_textStyle == null )
      m_textStyle = StyleUtils.getDefaultTextStyle();

    return m_textStyle;
  }

  public void setAlignmentHorizontal( final ALIGNMENT alignmentHorizontal )
  {
    m_alignmentHorizontal = alignmentHorizontal;
  }

  public void setAlignmentVertical( final ALIGNMENT alignmentVertical )
  {
    m_alignmentVertical = alignmentVertical;
  }

  public void setInsets( final Insets insets )
  {
    m_insets = insets;
  }

  public void setLabel( final String text )
  {
    m_text = text;
  }

  public void setTextAnchorX( final ALIGNMENT normalizedPositionX )
  {
    m_textAnchorX = normalizedPositionX;
  }

  public void setTextAnchorY( final ALIGNMENT normalizedPositionY )
  {
    m_textAnchorY = normalizedPositionY;
  }

  public void setRotation( final int rotation )
  {
    m_rotation = rotation;
  }

  public void setText( final String text )
  {
    m_text = text;
  }

  public void setTextStyle( final ITextStyle textStyle )
  {
    m_textStyle = textStyle;
  }

}
