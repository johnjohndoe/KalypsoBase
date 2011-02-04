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
package de.openali.odysseus.chart.framework.model.layer.impl;

import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerState;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author kimwerner
 */
public class ChartLayerState implements IChartLayerState
{
  private List<IChartLayerState> m_childStates;

  private boolean m_isVisible;

  private boolean m_isActive;

  private String m_layerID;

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayerState#getID()
   */
  @Override
  public String getID( )
  {
    return m_layerID;
  }

  private void setState( final IChartLayer chartLayer )
  {
    setID( chartLayer.getId() );
    setVisible( chartLayer.isVisible() );
    setActive( chartLayer.isActive() );
  }

  public boolean isActive( )
  {
    return m_isActive;
  }

  public boolean isVisible( )
  {
    return m_isVisible;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayerState#restoreState(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void restoreState( final IChartLayer chartLayer )
  {
    chartLayer.setActive( isActive() );
    chartLayer.setVisible( isVisible() );
    final ILayerManager layerManager = chartLayer.getLayerManager();
    if( layerManager == null || m_childStates == null )
      return;
    for( final IChartLayerState childState : m_childStates )
    {
      final IChartLayer layer = layerManager.findLayer( childState.getID() );
      if( layer != null )
      {
        childState.restoreState( layer );
      }
    }
  }

  private void setActive( final boolean isActive )
  {
    m_isActive = isActive;
  }

  private void setVisible( final boolean isVisible )
  {
    m_isVisible = isVisible;
  }

  private void setID( final String layerID )
  {
    m_layerID = layerID;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayerState#storeState(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void storeState( final IChartLayer chartLayer )
  {
    setState( chartLayer );
    final ILayerManager layerManager = chartLayer.getLayerManager();
    if( layerManager == null )
      return;
    m_childStates = new ArrayList<IChartLayerState>();
    for( final IChartLayer childLayer : layerManager.getLayers() )
    {
      final ChartLayerState layerState = new ChartLayerState();
      layerState.storeState( childLayer );
      m_childStates.add( layerState );
    }
  }
}
