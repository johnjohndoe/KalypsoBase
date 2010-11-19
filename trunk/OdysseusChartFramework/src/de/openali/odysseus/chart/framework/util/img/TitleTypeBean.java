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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author kimwerner
 */
public class TitleTypeBean implements IChartLabelRenderer
{
  private String m_text = "";

  private ALIGNMENT m_alignment = ALIGNMENT.CENTER;

  private ITextStyle m_textStyle;

  private Insets m_insets = new Insets( 0, 0, 0, 0 );

  public TitleTypeBean( final String text )
  {
    m_text = text;
  }

  public TitleTypeBean( final String text, final ALIGNMENT alignment, final ITextStyle textStyle, final Insets insets )
  {
    super();
    m_text = text;
    m_alignment = alignment;
    m_textStyle = textStyle;
    m_insets = insets;
  }

  public Insets getInsets( )
  {
    return m_insets;
  }

  public ALIGNMENT getAlignment( )
  {
    return m_alignment;
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

  @Override
  public void setInsets( final Insets insets )
  {
    m_insets = insets;
  }

  public void setAlignment( final ALIGNMENT alignment )
  {
    m_alignment = alignment;
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

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.graphics.Point)
   */
  @Override
  public void paint( final GC gc, final Point anchor )
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setTextAnchor(de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT, de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT)
   */
  @Override
  public void setTextAnchor( final ALIGNMENT positionX, final ALIGNMENT positionY )
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setRotation(int, de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT, de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT)
   */
  @Override
  public void setRotation( final int angle, final ALIGNMENT centerX, final ALIGNMENT centerY )
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setLinePosition(de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT)
   */
  @Override
  public void setLinePosition( final ALIGNMENT position )
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#setDrawBorder(boolean)
   */
  @Override
  public void setDrawBorder( final boolean drawBorder )
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer#getSize()
   */
  @Override
  public Point getSize( )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
