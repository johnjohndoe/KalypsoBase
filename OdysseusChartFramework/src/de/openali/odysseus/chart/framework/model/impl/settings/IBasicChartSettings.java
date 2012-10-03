/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.img.ChartPlotFrame;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer;

/**
 * @author Dirk Kuch
 */
public interface IBasicChartSettings
{
  void addPlotFrameStyle( final POSITION position, final ILineStyle lineStyle );

  ChartPlotFrame getPlotFrame( );

  void addTitles( TitleTypeBean... titles );

  CHART_DATA_LOADER_STRATEGY getDataLoaderStrategy( );

  String getDescription( );

  IChartLegendRenderer getLegendRenderer( );

  ITextStyle getTextStyle( );

  TitleTypeBean[] getTitles( );

  void setDataLoaderStrategy( CHART_DATA_LOADER_STRATEGY convert );

  void setDescription( String description );

  void setLegendRenderer( String renderer );

  void setTitle( String title, ALIGNMENT position, ITextStyle textStyle, Insets insets );

  Insets getChartInsets( );

  void setChartInsets( Insets insets );

  Insets getPlotInsets( );

  void setPlotInsets( Insets insets );
}