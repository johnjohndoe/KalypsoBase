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
package org.kalypso.zml.ui.chart.legend;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;

/**
 * @author Dirk Kuch
 */
public class ZmlChartLegendLayersVisitor implements IChartLayerVisitor
{
  private final Set<IChartLayer> m_layers = new LinkedHashSet<IChartLayer>();

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor#visit(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public boolean visit( final IChartLayer layer )
  {
    if( isValid( layer ) )
    {
      m_layers.add( layer );

      final ILayerManager layerManager = layer.getLayerManager();
      layerManager.accept( this );
    }

    return true;
  }

  private boolean isValid( final IChartLayer layer )
  {
    if( !layer.isVisible() )
      return false;

    if( !layer.isLegend() )
      return false;

    if( !(layer instanceof IZmlLayer) )
      return true;

    final IZmlLayer zml = (IZmlLayer) layer;
    final IZmlLayerDataHandler dataHandler = zml.getDataHandler();
    if( dataHandler == null )
      return false;

    if( dataHandler.getObservation() == null )
      return false;

    // w/q relation defined?
    if( dataHandler.getValueAxis() == null )
      return false;

    return true;
  }

  public IChartLayer[] getLayers( )
  {
    return m_layers.toArray( new IChartLayer[] {} );
  }

}
