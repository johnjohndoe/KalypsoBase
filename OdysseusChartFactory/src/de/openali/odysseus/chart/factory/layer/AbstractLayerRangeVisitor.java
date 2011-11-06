/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package de.openali.odysseus.chart.factory.layer;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.AbstractChartLayerVisitor;

/**
 * @author Gernot Belger
 *
 */
public abstract class AbstractLayerRangeVisitor extends AbstractChartLayerVisitor
{
  private Double m_min = null;

  private Double m_max = null;

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor#visit(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public final void visit( final IChartLayer layer )
  {
    if( !layer.isVisible() )
      return;

    final IDataRange<Number> dr = getLayerRange( layer );
    if( dr != null )
    {
      if( m_max == null )
        m_max = dr.getMax().doubleValue();
      else
        m_max = Math.max( m_max, dr.getMax().doubleValue() );

      if( m_min == null )
        m_min = dr.getMin().doubleValue();
      else
        m_min = Math.min( m_min, dr.getMin().doubleValue() );
    }
  }

  protected abstract IDataRange<Number> getLayerRange( final IChartLayer layer );

  public final IDataRange<Number> getRange( )
  {
    if( m_min == null || m_max == null )
      return null;

    return new DataRange<Number>( m_min, m_max );
  }
}