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

import de.openali.odysseus.chart.ext.base.layer.DefaultTickRasterLayer;
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
    if( isVisible( layer ) )
    {
      if( layer instanceof DefaultTickRasterLayer )
      {
        layer.setVisible( m_visibility );
        m_visibility = false;
      }

      layer.getLayerManager().accept( this );
    }
  }

  private boolean isVisible( final IChartLayer layer )
  {
    if( !layer.isVisible() )
      return false;

    final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
    if( coordinateMapper == null )
      return true;

    final IAxis domainAxis = coordinateMapper.getDomainAxis();
    final IAxis targetAxis = coordinateMapper.getTargetAxis();
    if( Objects.isNull( domainAxis, targetAxis ) )
      return true;

    if( !domainAxis.isVisible() )
      return false;

    if( !targetAxis.isVisible() )
      return false;

    return true;
  }
}
