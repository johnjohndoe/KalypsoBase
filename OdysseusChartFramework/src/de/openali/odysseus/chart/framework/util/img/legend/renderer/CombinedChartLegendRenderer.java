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
package de.openali.odysseus.chart.framework.util.img.legend.renderer;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.util.img.legend.IChartLegendCanvas;
import de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig;

/**
 * @about if an chart legend only consists of one row, so the compact chart legend renderer will be used - otherwise the
 *        block chart legend render will be used
 * @author Dirk Kuch
 */
public class CombinedChartLegendRenderer implements IChartLegendRenderer
{

  private static final String ID = "de.openali.odysseus.chart.legend.render.combined"; //$NON-NLS-1$

  private IChartLegendRenderer m_lastRenderer;

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer#createImage(de.openali.odysseus.chart.framework.util.img.legend.IChartLegendCanvas,
   *      de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig)
   */
  @Override
  public Image createImage( final IChartLegendCanvas canvas, final IChartLegendConfig config )
  {
    return m_lastRenderer.createImage( canvas, config );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer#calculateSize(de.openali.odysseus.chart.framework.util.img.legend.IChartLegendCanvas,
   *      de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig)
   */
  @Override
  public Point calculateSize( final IChartLegendCanvas canvas, final IChartLegendConfig config )
  {
    m_lastRenderer = new CompactChartLegendRenderer();
    final Point size = m_lastRenderer.calculateSize( canvas, config );
    if( m_lastRenderer.rowSize() == 1 )
      return size;

    m_lastRenderer = new BlockChartLegendRenderer();
    return m_lastRenderer.calculateSize( canvas, config );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer#getIdentifier()
   */
  @Override
  public String getIdentifier( )
  {
    return ID;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer#rowSize()
   */
  @Override
  public int rowSize( )
  {
    return -1;
  }

}
