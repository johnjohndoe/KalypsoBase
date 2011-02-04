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

import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.IChartModelState;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerState;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.impl.ChartLayerState;

/**
 * @author kimwerner
 */
public class ChartModelState implements IChartModelState
{

  private final List<IChartLayerState> m_layerStates = new ArrayList<IChartLayerState>();

  @Override
  public void restoreState( final IChartModel model )
  {
    final ILayerManager layerManager = model.getLayerManager();
    if( layerManager == null || m_layerStates.size() == 0 )
      return;
    for( final IChartLayerState layerState : m_layerStates )
    {
      final IChartLayer chartLayer = layerManager.findLayer( layerState.getID() );
      if( chartLayer != null )
        layerState.restoreState( chartLayer );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModelState#storeState(de.openali.odysseus.chart.framework.model.IChartModel)
   */
  @Override
  public void storeState( final IChartModel model )
  {
    final ILayerManager layerManager = model == null ? null : model.getLayerManager();
    if( layerManager == null )
      return;
    for( final IChartLayer chartLayer : layerManager.getLayers() )
    {
      final IChartLayerState layerState = new ChartLayerState();
      layerState.storeState( chartLayer );
      m_layerStates.add( layerState );
    }
  }
}
