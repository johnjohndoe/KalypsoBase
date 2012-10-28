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
package de.openali.odysseus.chart.framework.model.impl.visitors;

import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author kimwerner
 * @author Dirk Kuch
 */
public class FindLayerTooltipVisitor implements IChartLayerVisitor2
{
  final IChartComposite m_chart;

  final Point m_point;

  private EditInfo m_info;

  public FindLayerTooltipVisitor( final IChartComposite chart, final Point point )
  {
    m_chart = chart;
    m_point = point;
  }

  @Override
  public boolean getVisitDirection( )
  {
    // anticyclic to the paint direction, so tooltip of top layers is shown first.
    return true;
  }

  @Override
  public boolean visit( final IChartLayer chartLayer ) throws CancelVisitorException
  {
    if( !(chartLayer instanceof ITooltipChartLayer) )
      return true;

    if( !chartLayer.isVisible() )
      return false;

    final ITooltipChartLayer layer = (ITooltipChartLayer)chartLayer;
    final EditInfo info = layer.getHover( m_point );
    if( Objects.isNotNull( info ) )
    {
      m_info = info;

      throw new CancelVisitorException();
    }

    return true;
  }

  public EditInfo getEditInfo( )
  {
    return m_info;
  }
}
