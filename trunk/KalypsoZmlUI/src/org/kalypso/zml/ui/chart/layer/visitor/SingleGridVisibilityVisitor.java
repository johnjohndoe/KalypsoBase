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
package org.kalypso.zml.ui.chart.layer.visitor;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;

import de.openali.odysseus.chart.ext.base.layer.DefaultTickRasterLayer;
import de.openali.odysseus.chart.factory.layer.Layers;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class SingleGridVisibilityVisitor implements IChartLayerVisitor
{
  private boolean m_visibility = true;

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor#visit(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void visit( final IChartLayer layer )
  {
    if( Layers.isVisible( layer ) )
      layer.getLayerManager().accept( this );

    if( layer instanceof DefaultTickRasterLayer )
    {
      if( isValid( layer ) )
      {
        layer.setVisible( m_visibility );
        m_visibility = false;
      }
      else
        layer.setVisible( false );
    }
  }

  private boolean isValid( final IChartLayer layer )
  {
    final ICoordinateMapper mapper = layer.getCoordinateMapper();

    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();
    if( Objects.isNull( domainAxis, targetAxis ) )
      return false;

    /** contains data? or perhaps it's an empty layer? */
    final ILayerContainer plainLayer = layer.getParent();
    final IZmlLayer zmlLayer = findZmlLayer( plainLayer );
    if( zmlLayer == null )
      return false;

    final IZmlLayerDataHandler handler = zmlLayer.getDataHandler();
    if( handler.getObservation() == null )
      return false;

    return true;
  }

  private IZmlLayer findZmlLayer( final ILayerContainer plainLayer )
  {
    final IChartLayer[] layers = plainLayer.getLayerManager().getLayers();
    for( final IChartLayer layer : layers )
    {
      if( layer instanceof IZmlLayer )
        return (IZmlLayer) layer;
    }

    return null;
  }

}
