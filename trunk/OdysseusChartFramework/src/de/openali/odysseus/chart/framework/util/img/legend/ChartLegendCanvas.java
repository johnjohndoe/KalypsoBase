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
package de.openali.odysseus.chart.framework.util.img.legend;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.util.img.legend.config.DefaultChartLegendConfig;
import de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer;

/**
 * @author Dirk Kuch
 */
public class ChartLegendCanvas implements IChartLegendCanvas
{
  private final IChartModel m_model;

  private final IChartLegendConfig m_config;

  public ChartLegendCanvas( final IChartModel model, final Rectangle size )
  {
    this( model, new DefaultChartLegendConfig( size.width ) );
  }

  public ChartLegendCanvas( final IChartModel model, final IChartLegendConfig config )
  {
    Assert.isNotNull( model );
    m_model = model;
    m_config = config;
  }

  public Point getSize( )
  {
    if( m_model == null )
      return new Point( 0, 0 );

    if( m_model.isHideLegend() )
      return new Point( 0, 0 );

    IChartLegendRenderer legendRenderer = m_model.getLegendRenderer();
    if( legendRenderer == null )
      return new Point( 0, 0 );

    return legendRenderer.calculateSize( this, m_config );
  }

  public Image createImage( )
  {
    if( m_model == null )
      return null;

    if( m_model.isHideLegend() )
      return null;

    IChartLegendRenderer legendRenderer = m_model.getLegendRenderer();
    if( legendRenderer == null )
      return null;

    return legendRenderer.createImage( this, m_config );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.IChartLegendCanvas#getModel()
   */
  @Override
  public IChartModel getModel( )
  {
    return m_model;
  }
}
