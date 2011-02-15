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
package de.openali.odysseus.chart.framework.model.layer.manager.visitors;

import java.util.HashSet;
import java.util.Set;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class FindAxisLayerVisitor implements IChartLayerVisitor
{
  Set<IChartLayer> m_layers = new HashSet<IChartLayer>();

  private final IAxis m_axis;

  private final boolean m_recursive;

  public FindAxisLayerVisitor( final IAxis axis, final boolean recursive )
  {
    m_axis = axis;
    m_recursive = recursive;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor#visit(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void visit( final IChartLayer layer )
  {
    final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
    if( coordinateMapper != null )
    {
      if( coordinateMapper.getDomainAxis() == m_axis )
        m_layers.add( layer );
      else if( coordinateMapper.getTargetAxis() == m_axis )
        m_layers.add( layer );
    }

    if( m_recursive )
      layer.getLayerManager().accept( this );
  }

  public IChartLayer[] getLayers( )
  {
    return m_layers.toArray( new IChartLayer[] {} );
  }

}
