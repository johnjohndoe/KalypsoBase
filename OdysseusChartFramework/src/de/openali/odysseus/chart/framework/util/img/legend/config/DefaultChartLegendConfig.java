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
package de.openali.odysseus.chart.framework.util.img.legend.config;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author Dirk Kuch
 */
public class DefaultChartLegendConfig implements IChartLegendConfig
{
  private final ITextStyle m_style = StyleUtils.getDefaultTextStyle();

  private final int m_width;

  private final Point m_iconSize = new Point( 9, 9 );

  private final Point m_itemSpacer = new Point( 10, 8 );

  public DefaultChartLegendConfig( final int width )
  {
    m_width = width;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig#getIconSize()
   */
  @Override
  public Point getIconSize( )
  {
    return m_iconSize;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig#getTextStyle()
   */
  @Override
  public ITextStyle getTextStyle( )
  {
    return m_style;
  }

  @Override
  public Point getSpacer( )
  {
    return new Point( 2, 0 );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig#getItemSpacer()
   */
  @Override
  public Point getItemSpacer( )
  {
    return m_itemSpacer;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig#getMaximumWidth()
   */
  @Override
  public int getMaximumWidth( )
  {
    return m_width;
  }

}
