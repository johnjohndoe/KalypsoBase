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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;
import de.openali.odysseus.chart.framework.util.img.GenericChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

/**
 * @author kimwerner
 */
public class DefaultTextLayer extends AbstractChartLayer
{
  private final IChartLabelRenderer m_labelRenderer;

  private final TitleTypeBean[] m_titleTypeBeans;

  public DefaultTextLayer( final ILayerProvider provider, final String id, final TitleTypeBean... titleTypeBeans )
  {
    this( provider, id, new GenericChartLabelRenderer(), titleTypeBeans );
  }

  private DefaultTextLayer( final ILayerProvider provider, final String id, final IChartLabelRenderer labelRenderer, final TitleTypeBean... titleTypeBeans )
  {
    super( provider,new StyleSet() );

    m_titleTypeBeans = titleTypeBeans;
    m_labelRenderer = labelRenderer;

    setIdentifier( id );
  }

  @Override
  public void paint( final GC gc, ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    final ICoordinateMapper cm = getCoordinateMapper();
    if( m_titleTypeBeans == null || m_labelRenderer == null || cm == null )
      return;

    for( final TitleTypeBean bean : m_titleTypeBeans )
    {
      m_labelRenderer.setTitleTypeBean( bean );

      final double anchorX = bean.getTextAnchorX().doubleValue();
      final double anchorY = bean.getTextAnchorY().doubleValue();

      final double screenPosX = getDomainAxis().getScreenHeight() * anchorX;
      final double screenPosY = getTargetAxis().getScreenHeight() * anchorY;

      m_labelRenderer.paint( gc, new Point( (int) screenPosX, (int) screenPosY ) );
    }
  }

  @Override
  public IDataRange<Double> getDomainRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IDataRange<Double> getTargetRange( IDataRange<Double> domainIntervall )
  {
    // TODO Auto-generated method stub
    return null;
  }
}
