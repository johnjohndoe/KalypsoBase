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
package de.openali.odysseus.chart.framework.model.impl;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.CompactChartLegendRenderer;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer;

/**
 * Basic chart model parameters like title, styling, description, ...
 * 
 * @author Dirk Kuch
 */
public class BasicChartSettings implements IBasicChartSettings
{
  private String m_description = "";

  private String m_renderer;

  private ITextStyle m_textStyle = null;

  private final List<TitleTypeBean> m_title = new ArrayList<TitleTypeBean>();

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setTitle(java.lang.String)
   */
  @Override
  public void addTitles( final TitleTypeBean... titles )
  {
    Collections.addAll( m_title, titles );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getLegendRenderer()
   */
  @Override
  public IChartLegendRenderer getLegendRenderer( )
  {
    if( m_renderer == null )
      m_renderer = CompactChartLegendRenderer.ID;

    return OdysseusChartFramework.getDefault().getRenderers( m_renderer );
  }

  @Override
  public ITextStyle getTextStyle( )
  {
    if( m_textStyle == null )
      m_textStyle = StyleUtils.getDefaultTextStyle();

    return m_textStyle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getTitle()
   */
  @Override
  public TitleTypeBean[] getTitles( )
  {
    return m_title.toArray( new TitleTypeBean[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setDescription(java.lang.String)
   */
  @Override
  public void setDescription( final String description )
  {
    m_description = description;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setLegendRenderer(java.lang.String)
   */
  @Override
  public void setLegendRenderer( final String renderer )
  {
    m_renderer = renderer;
  }

  public void setTextStyle( final ITextStyle textStyle )
  {
    m_textStyle = textStyle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setTitle(java.lang.String,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.LABEL_POSITION,
   *      de.openali.odysseus.chart.framework.model.style.ITextStyle, java.awt.Insets)
   */
  @Override
  public void setTitle( final String title, final ALIGNMENT position, final ITextStyle textStyle, final Insets insets )
  {
    m_title.clear();
    m_title.add( new TitleTypeBean( title, position, ALIGNMENT.CENTER, textStyle, insets ) );
  }
}
