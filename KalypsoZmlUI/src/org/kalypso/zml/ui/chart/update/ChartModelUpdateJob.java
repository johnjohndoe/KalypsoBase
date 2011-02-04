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
package org.kalypso.zml.ui.chart.update;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;

/**
 * @author Dirk Kuch
 */
public class ChartModelUpdateJob extends UIJob
{

  private final Set<IChartLayerVisitor> m_visitors = new LinkedHashSet<IChartLayerVisitor>();

  private final IChartModel m_model;

  public ChartModelUpdateJob( final IChartModel model )
  {
    super( "Aktualisiere Diagramm" );
    m_model = model;
  }

  /**
   * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus runInUIThread( final IProgressMonitor monitor )
  {
    IChartLayerVisitor[] visitors;
    synchronized( this )
    {
      visitors = m_visitors.toArray( new IChartLayerVisitor[] {} );
      m_visitors.clear();
    }

    final ILayerManager layerManager = m_model.getLayerManager();
    layerManager.accept( visitors );

    m_model.autoscale();

    final IChartLayer[] layers = layerManager.getLayers();

    return Status.OK_STATUS;
  }

  public void add( final IChartLayerVisitor visitor )
  {
    m_visitors.add( visitor );
  }

}
