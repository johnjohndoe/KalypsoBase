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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import de.openali.odysseus.chart.ext.base.data.IAxisContentProvider;
import de.openali.odysseus.chart.framework.util.img.GenericChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;

/**
 * @author kimwerner
 */
public class OrdinalAxisRenderer extends ExtendedAxisRenderer
{

  private final IAxisContentProvider m_contentProvider;

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IChartLabelRenderer tickLabelRenderer, final IChartLabelRenderer axisTitleRenderer, final IAxisContentProvider contentProvider, final int minWidth, final int maxWidth )
  {
    super( id, contentProvider, axisTitleRenderer, tickLabelRenderer, new OrdinalAxisTickCalculator( contentProvider, minWidth, maxWidth ), config );
    m_contentProvider = contentProvider;
  }

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IChartLabelRenderer tickLabelRenderer, final IAxisContentProvider contentProvider, final int minWidth, final int maxWidth )
  { 
    this( id, config, tickLabelRenderer, new GenericChartLabelRenderer(), contentProvider, minWidth, maxWidth );
  }

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config, final IAxisContentProvider contentProvider, final int minWidth, final int maxWidth )
  {
    this( id, config, new GenericChartLabelRenderer(),  new GenericChartLabelRenderer(), contentProvider, minWidth, maxWidth );
  }

  public OrdinalAxisRenderer( final String id, final AxisRendererConfig config , final IAxisContentProvider contentProvider)
  {
    this( id, config, new GenericChartLabelRenderer(), contentProvider, 0, 0 );
  }

  public final Object getContent( final int index )
  {
    return m_contentProvider.getContent( index );
  }
}
