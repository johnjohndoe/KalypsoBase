/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.chart.layer.visitor;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.zml.core.diagram.base.AbstractExternalChartModelVisitor;

import de.openali.odysseus.chart.ext.base.layer.DefaultTextLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class SetVisibilityChartModelVisitor extends AbstractExternalChartModelVisitor
{
  public static final String NO_DATA_LAYER = "noData";

  private final String[] m_ignoreTypes;

  private final boolean m_empty;

  public SetVisibilityChartModelVisitor( final String[] ignoreTypes, final boolean empty )
  {
    m_ignoreTypes = ignoreTypes;
    m_empty = empty;
  }

  /**
   * @see org.kalypso.zml.core.diagram.base.AbstractExternalChartModelVisitor#accept(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  protected void accept( final IChartLayer layer )
  {
    if( layer instanceof DefaultTextLayer )
    {
      if( NO_DATA_LAYER.equals( layer.getId() ) )
        layer.setVisible( m_empty );

    }
    else
    {

// final AbstractChartLayer abstractLayer = (AbstractChartLayer) layer;
// final ILayerProvider provider = abstractLayer.getProvider();
// if( provider == null )
// return;
      final String axisType = getTargetAxis( layer );
      if( ArrayUtils.contains( m_ignoreTypes, axisType ) )
        layer.setVisible( false );
      else
        layer.setVisible( true );
    }
  }

  private String getTargetAxis( final IChartLayer layer )
  {
    final ICoordinateMapper mapper = layer.getCoordinateMapper();
    final IAxis targetAxis = mapper.getTargetAxis();
    final String axisId = targetAxis.getId();

    return axisId;
  }
}
