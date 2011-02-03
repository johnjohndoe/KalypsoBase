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
package de.openali.odysseus.chart.framework.model.impl.utils;

import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.impl.IChartBehaviour;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class ChartModelLayerEventListener extends AbstractLayerManagerEventListener
{

  private final ChartModel m_model;

  public ChartModelLayerEventListener( final ChartModel model )
  {
    m_model = model;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onActivLayerChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onActivLayerChanged( final IChartLayer layer )
  {
    if( !layer.isActive() )
      return;

    final ILayerManager layerManager = m_model.getLayerManager();
    final IChartLayer[] layers = layerManager.getLayers();

    for( final IChartLayer other : layers )
    {
      if( layer != other && other.isActive() )
        other.setActive( false );
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.IChartModel#onLayerAdded(org.kalypso.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerAdded( final IChartLayer layer )
  {
    if( m_model.getBehaviour().isHideUnusedAxes() )
      hideUnusedAxes();

    if( m_model.getBehaviour().isSetAutoscale() )
      autoscale( layer );
  }

  private void autoscale( final IChartLayer layer )
  {
    final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
    if( coordinateMapper == null )
      m_model.autoscale();
    else
      m_model.autoscale( coordinateMapper.getDomainAxis(), coordinateMapper.getTargetAxis() );
  }

  private void hideUnusedAxes( )
  {
    final IChartBehaviour behaviour = m_model.getBehaviour();
    final IAxis[] axes = m_model.getMapperRegistry().getAxes();

    behaviour.hideUnusedAxis( axes );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerContentChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerContentChanged( final IChartLayer layer )
  {
    if( m_model.getBehaviour().isHideUnusedAxes() )
    {
      final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
      if( coordinateMapper != null )
      {
        m_model.getBehaviour().hideUnusedAxis( coordinateMapper.getTargetAxis() );
        m_model.getBehaviour().hideUnusedAxis( coordinateMapper.getDomainAxis() );
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.kalypso.chart.framework.model.IChartModel#onLayerRemoved(org.kalypso.chart.framework.model.layer.IChartLayer )
   */
  @Override
  public void onLayerRemoved( final IChartLayer layer )
  {
    if( m_model.getBehaviour().isHideUnusedAxes() )
      hideUnusedAxes();

    if( m_model.getBehaviour().isSetAutoscale() )
      autoscale( layer );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerVisibilityChanged( final IChartLayer layer )
  {
    if( m_model.getBehaviour().isHideUnusedAxes() )
    {
      final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
      if( coordinateMapper != null )
      {
        m_model.getBehaviour().hideUnusedAxis( coordinateMapper.getTargetAxis() );
        m_model.getBehaviour().hideUnusedAxis( coordinateMapper.getDomainAxis() );
      }
    }
  }
}
