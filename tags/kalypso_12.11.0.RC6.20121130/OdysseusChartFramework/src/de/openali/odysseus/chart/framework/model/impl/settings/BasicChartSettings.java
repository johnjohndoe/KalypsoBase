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
package de.openali.odysseus.chart.framework.model.impl.settings;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import de.openali.odysseus.chart.framework.OdysseusChartExtensions;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.ChartPlotFrame;
import de.openali.odysseus.chart.framework.util.img.ChartPlotFrameEdge;
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
  private String m_description = ""; //$NON-NLS-1$

  private String m_renderer;

  private ITextStyle m_textStyle = null;

  private final ChartPlotFrame m_plotFrame = new ChartPlotFrame();

  private final List<TitleTypeBean> m_title = new ArrayList<>();

  private CHART_DATA_LOADER_STRATEGY m_strategy = CHART_DATA_LOADER_STRATEGY.eAsynchrone;

  private Insets m_chartInsets = new Insets( 0, 0, 0, 0 );

  private Insets m_plotInsets = new Insets( 0, 0, 0, 0 );

  @Override
  public void addPlotFrameStyle( final POSITION position, final ILineStyle lineStyle )
  {
    m_plotFrame.setFrame( new ChartPlotFrameEdge( lineStyle ), position );
  }

  @Override
  public void addTitles( final TitleTypeBean... titles )
  {
    Collections.addAll( m_title, titles );
  }

  @Override
  public CHART_DATA_LOADER_STRATEGY getDataLoaderStrategy( )
  {
    return m_strategy;
  }

  @Override
  public String getDescription( )
  {
    return m_description;
  }

  @Override
  public IChartLegendRenderer getLegendRenderer( )
  {
    if( m_renderer == null )
      m_renderer = CompactChartLegendRenderer.ID;

    return OdysseusChartExtensions.getRenderers( m_renderer );
  }

  @Override
  public ChartPlotFrame getPlotFrame( )
  {
    return m_plotFrame;
  }

  @Override
  public ITextStyle getTextStyle( )
  {
    if( m_textStyle == null )
      m_textStyle = StyleUtils.getDefaultTextStyle();

    return m_textStyle;
  }

  @Override
  public TitleTypeBean[] getTitles( )
  {
    return m_title.toArray( new TitleTypeBean[] {} );
  }

  @Override
  public void setDataLoaderStrategy( final CHART_DATA_LOADER_STRATEGY strategy )
  {
    m_strategy = strategy;
  }

  @Override
  public void setDescription( final String description )
  {
    m_description = description;
  }

  @Override
  public void setLegendRenderer( final String renderer )
  {
    m_renderer = renderer;
  }

  public void setTextStyle( final ITextStyle textStyle )
  {
    m_textStyle = textStyle;
  }

  @Override
  public void setTitle( final String title, final ALIGNMENT position, final ITextStyle textStyle, final Insets insets )
  {
    final TitleTypeBean titleType = m_title.isEmpty() ? new TitleTypeBean( null ) : m_title.get( 0 );
    titleType.setLabel( title );
    titleType.setInsets( insets );
    titleType.setPositionHorizontal( position );
    titleType.setTextStyle( textStyle );
    m_title.clear();
    m_title.add( titleType );
  }

  @Override
  public Insets getChartInsets( )
  {
    return m_chartInsets;
  }

  @Override
  public void setChartInsets( final Insets chartInsets )
  {
    Assert.isNotNull( chartInsets );

    m_chartInsets = chartInsets;
  }

  @Override
  public Insets getPlotInsets( )
  {
    return m_plotInsets;
  }

  @Override
  public void setPlotInsets( final Insets plotInsets )
  {
    Assert.isNotNull( plotInsets );

    m_plotInsets = plotInsets;
  }
}
