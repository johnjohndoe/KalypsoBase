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

import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author kimwerner
 */

public class ChartImageLayerManagerEventListener implements ILayerManagerEventListener
{

  private final ChartImageComposite m_chartImageComposite;

  public ChartImageLayerManagerEventListener( final ChartImageComposite chartImageComposite )
  {
    super();
    m_chartImageComposite = chartImageComposite;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onActivLayerChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onActivLayerChanged( final IChartLayer layer )
  {
    // do nothing
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerAdded(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerAdded( final IChartLayer layer )
  {
    m_chartImageComposite.invalidate();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerContentChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerContentChanged( final IChartLayer layer )
  {
    m_chartImageComposite.invalidate();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerMoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerMoved( final IChartLayer layer )
  {
    m_chartImageComposite.invalidate();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerRemoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerRemoved( final IChartLayer layer )
  {
    m_chartImageComposite.invalidate();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void onLayerVisibilityChanged( final IChartLayer layer )
  {
    m_chartImageComposite.invalidate();
  }
}
