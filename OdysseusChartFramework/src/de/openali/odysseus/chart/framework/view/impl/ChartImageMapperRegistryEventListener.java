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
package de.openali.odysseus.chart.framework.view.impl;

import de.openali.odysseus.chart.framework.model.event.impl.AbstractMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;

/**
 * @author kimwerner
 */
public class ChartImageMapperRegistryEventListener extends AbstractMapperRegistryEventListener
{
  /**
   * @see de.openali.odysseus.chart.framework.axis.IMapperRegistryEventListener#onMapperAdded(de.openali.odysseus.chart.framework.axis.IAxis)
   *      adds an AxisComponent for any newly added axis and reports Axis and its AxisComponent to the AxisRegistry
   */

  private final ChartImageComposite m_chartImageComposite;

  public ChartImageMapperRegistryEventListener( ChartImageComposite chartImageComposite )
  {
    super();
    m_chartImageComposite = chartImageComposite;
  }

  @Override
  public void onMapperAdded( final IMapper mapper )
  {
    m_chartImageComposite.invalidate();
  }

  /**
   * @see de.openali.odysseus.chart.framework.impl.model.event.AbstractMapperRegistryEventListener#onMapperRangeChanged(de.openali.odysseus.chart.framework.model.mapper.IMapper)
   */
  @Override
  public void onMapperChanged( final IMapper mapper )
  {
    m_chartImageComposite.invalidate();
  }

  /**
   * @see de.openali.odysseus.chart.framework.axis.IMapperRegistryEventListener#onMapperRemoved(de.openali.odysseus.chart.framework.axis.IAxis)
   *      TODO: not implemented yet (or is it? - right now there's no way to remove an axis, so this should be checked
   *      in the future)
   */
  @Override
  public void onMapperRemoved( final IMapper mapper )
  {
    m_chartImageComposite.invalidate();
  }
}
