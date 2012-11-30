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
package de.openali.odysseus.chart.ext.base.layer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainIntervalValueData;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;

/**
 * @author Gernot Belger
 */
public class DomainIntervallBarPainter implements IBarLayerPainter
{
  private final BarPaintManager m_paintManager;

  private final AbstractDomainIntervalValueData< ? , ? > m_dataContainer;

  private final DomainIntervalBarLayer m_layer;

  private final int m_screenHeight;

  private final String[] m_styleNames;

  public DomainIntervallBarPainter( final DomainIntervalBarLayer layer, final BarPaintManager paintManager, final AbstractDomainIntervalValueData< ? , ? > dataContainer, final int screenHeight, final String[] styleNames )
  {
    m_layer = layer;
    m_paintManager = paintManager;
    m_dataContainer = dataContainer;
    m_screenHeight = screenHeight;
    m_styleNames = styleNames;
  }

  @Override
  public void execute( final IProgressMonitor monitor )
  {
    if( m_dataContainer == null )
      return;

    m_dataContainer.open();

    final Object[] domainStartComponent = m_dataContainer.getDomainDataIntervalStart();
    final Object[] domainEndComponent = m_dataContainer.getDomainDataIntervalEnd();
    final Object[] targetComponent = m_dataContainer.getTargetValues();

    for( int i = 0; i < domainStartComponent.length; i++ )
    {
      final Object startValue = domainStartComponent[i];
      final Object endValue = domainEndComponent[i];
      final Object targetValue = targetComponent[i];

      final Point p1 = m_layer.getCoordinateMapper().logicalToScreen( startValue, targetValue );
      final Point p2 = m_layer.getCoordinateMapper().logicalToScreen( endValue, targetValue );
      final Rectangle rect = new Rectangle( p1.x, p1.y, p2.x - p1.x, m_screenHeight - p2.y );

      final EditInfo info = m_layer.getEditInfo( i );
      final BarRectangle barRect = new BarRectangle( rect, m_styleNames, info );

      m_paintManager.addRectangle( barRect, null );
    }
  }
}