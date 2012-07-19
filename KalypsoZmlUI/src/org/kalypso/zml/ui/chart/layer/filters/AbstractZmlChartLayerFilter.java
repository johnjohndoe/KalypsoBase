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
package org.kalypso.zml.ui.chart.layer.filters;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;

import de.openali.odysseus.chart.framework.model.layer.AbstractChartFilter;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlChartLayerFilter extends AbstractChartFilter implements IZmlChartLayerFilter
{
  private ContainerAccessor m_accessor;

  @Override
  public void init( final MetadataList metadata, final IAxis[] axes )
  {
    m_accessor = new ContainerAccessor( metadata, axes );
  }

  public ContainerAccessor getAccessor( )
  {
    return m_accessor;
  }

  @Override
  public final boolean isFiltered( final Object object )
  {
    if( !(object instanceof IObservationValueContainer) )
      return false;

    return filter( (IObservationValueContainer) object );
  }

  protected abstract boolean filter( IObservationValueContainer container );
}
