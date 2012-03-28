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

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.AbstractChartLayerVisitor;

/**
 * @author Dirk Kuch
 */
public class ZmlChartLegendLayersVisitor extends AbstractChartLayerVisitor
{
  private final Set<IChartLayer> m_layers = new LinkedHashSet<IChartLayer>();

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor#visit(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void visit( final IChartLayer layer )
  {
    if( isValid( layer ) )
    {
      m_layers.add( layer );

      final ILayerManager layerManager = layer.getLayerManager();
      layerManager.accept( this );
    }
  }

  private boolean isValid( final IChartLayer layer )
  {
    if( !layer.isVisible() )
      return false;

    if( !layer.isLegend() )
      return false;

    if( !(layer instanceof IZmlLayer) )
    {
      final ILayerManager manager = layer.getLayerManager();
      final IChartLayer[] children = manager.getLayers();
      for( final IChartLayer child : children )
      {
        if( isValid( child ) )
          return true;
      }

      return false;
    }

    final IZmlLayer zml = (IZmlLayer) layer;
    final IZmlLayerDataHandler dataHandler = zml.getDataHandler();
    if( dataHandler == null )
      return false;

    final IObservation observation = dataHandler.getObservation();
    if( Objects.isNull( observation ) )
      return false;

    // w/q relation defined?
    final IAxis valueAxis = dataHandler.getValueAxis();
    if( valueAxis == null )
      return false;

    final IAxis[] axes = observation.getAxes();
    if( !ArrayUtils.contains( axes, valueAxis ) )
      return false;

    return true;
  }

  public IChartLayer[] getLayers( )
  {
    return m_layers.toArray( new IChartLayer[] {} );
  }

}
