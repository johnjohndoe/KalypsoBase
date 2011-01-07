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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
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

  public DefaultTextLayer( final ILayerProvider provider, final String id, final IChartLabelRenderer labelRenderer, final TitleTypeBean... titleTypeBeans )
  {
    super( provider );

    m_titleTypeBeans = titleTypeBeans;
    m_labelRenderer = labelRenderer;
    setId( id );
  }

  public DefaultTextLayer( final ILayerProvider provider, final String id, final TitleTypeBean... titleTypeBeans )
  {
    this( provider, id, new GenericChartLabelRenderer(), titleTypeBeans );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    final ICoordinateMapper cm = getCoordinateMapper();
    if( m_titleTypeBeans == null || m_labelRenderer == null || cm == null )
      return;

    for( final TitleTypeBean bean : m_titleTypeBeans )
    {
      m_labelRenderer.eatBean( bean );
      final Point position = cm.numericToScreen( bean.getTextAnchorX().doubleValue(), bean.getTextAnchorY().doubleValue() );

      m_labelRenderer.paint( gc, new Point( position.x, position.y ) );
    }

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange(de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#createLegendEntries()
   */
  @Override
  protected ILegendEntry[] createLegendEntries( )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
