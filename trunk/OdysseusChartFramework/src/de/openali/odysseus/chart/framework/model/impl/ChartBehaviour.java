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

import org.apache.commons.lang.ArrayUtils;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;

/**
 * chart behavior settings - like hide unused axes, ...
 * 
 * @author Dirk Kuch
 */
public class ChartBehaviour implements IChartBehaviour
{
  /**
   * if set to true, all axes are sized automatically to fit all data into a layer
   */
  private boolean m_autoscale = false;

  private boolean m_hideLegend = true;

  private boolean m_hideTitle = false;

  private boolean m_hideUnusedAxes = true;

  private final IChartModel m_model;

  public ChartBehaviour( final IChartModel model )
  {
    m_model = model;
  }

  @Override
  public void hideUnusedAxis( final IAxis... axes )
  {
    // if axis has no layers, hide axis
    final ILayerManager layerManager = m_model.getLayerManager();

    for( final IAxis axis : axes )
    {
      final IChartLayer[] layers = layerManager.getLayers( axis );
      if( ArrayUtils.isEmpty( layers ) )
      {
        axis.setVisible( false );
        break;
      }

      for( final IChartLayer layer : layers )
      {
        if( layer.isVisible() )
        {
          axis.setVisible( true );
          break;
        }
      }
    }

  }

  @Override
  public boolean isHideLegend( )
  {
    return m_hideLegend;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#isHideTitle()
   */
  @Override
  public boolean isHideTitle( )
  {
    return m_hideTitle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#isHideUnusedAxes()
   */
  @Override
  public boolean isHideUnusedAxes( )
  {
    return m_hideUnusedAxes;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.impl.IChartBehaviour#isSetAutoscale()
   */
  @Override
  public boolean isSetAutoscale( )
  {
    return m_autoscale;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.impl.IChartBehaviour#setAutoscale(boolean)
   */
  @Override
  public void setAutoscale( final boolean autoscale )
  {
    m_autoscale = autoscale;
  }

  @Override
  public void setHideLegend( final boolean hide )
  {
    m_hideLegend = hide;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setHideTitle(boolean)
   */
  @Override
  public void setHideTitle( final boolean hide )
  {
    m_hideTitle = hide;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#setHideUnusedAxes(boolean)
   */
  @Override
  public void setHideUnusedAxes( final boolean hide )
  {
    if( hide == m_hideUnusedAxes )
      return;

    m_hideUnusedAxes = hide;

    m_model.getMapperRegistry().accept( new IAxisVisitor()
    {
      @Override
      public void visit( final IAxis axis )
      {
        if( hide )
          hideUnusedAxis( axis );
        else
          axis.setVisible( true );
      }
    } );
  }
}
