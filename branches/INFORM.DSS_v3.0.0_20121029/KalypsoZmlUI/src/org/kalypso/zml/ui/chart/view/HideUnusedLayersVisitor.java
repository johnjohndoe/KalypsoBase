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
package org.kalypso.zml.ui.chart.view;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.ui.chart.layer.themes.ZmlSelectionLayer;
import org.kalypso.zml.ui.chart.layer.visitor.NoDataLayerVisibilityVisitor;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.AbstractChartLayerVisitor;

/**
 * @author Dirk Kuch
 */
public class HideUnusedLayersVisitor extends AbstractChartLayerVisitor
{
  @Override
  public void visit( final IChartLayer layer )
  {
    // REMARK: only for zml layers, else we get conflict with the no-data visitor
    if( layer instanceof ZmlSelectionLayer )
      return;

    if( NoDataLayerVisibilityVisitor.NO_DATA_LAYER.equals( layer.getIdentifier() ) )
      return;

      layer.setVisible( isVisible( layer ) );
  }

  private boolean isVisible( final IChartLayer layer )
  {
    if( layer instanceof IZmlLayer )
    {
      final IZmlLayer zmlLayer = (IZmlLayer) layer;

      final IZmlLayerDataHandler handler = zmlLayer.getDataHandler();
      final IObservation observation = (IObservation) handler.getAdapter( IObservation.class );

      return Objects.isNotNull( observation );
    }
    else if( layer instanceof ZmlSelectionLayer )
      return true;

    final ILayerManager manager = layer.getLayerManager();
    final IChartLayer[] children = manager.getLayers();

    for( final IChartLayer child : children )
    {
      if( isVisible( child ) )
        return true;
    }

    return false;
  }
}