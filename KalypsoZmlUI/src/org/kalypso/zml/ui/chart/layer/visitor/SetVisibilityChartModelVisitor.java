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
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.provider.IObsProvider;

import de.openali.odysseus.chart.ext.base.layer.DefaultTextLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class SetVisibilityChartModelVisitor implements IChartLayerVisitor
{
  public static final String NO_DATA_LAYER = "noData";

  private final String[] m_ignoreTypes;

  private final IObsProvider[] m_providers;

  public SetVisibilityChartModelVisitor( final IObsProvider[] providers, final String[] currentIgnoreTypes )
  {
    m_providers = providers;
    m_ignoreTypes = currentIgnoreTypes;
  }

  /**
   * @see org.kalypso.zml.core.diagram.base.AbstractExternalChartModelVisitor#accept(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void visit( final IChartLayer layer )
  {
    if( layer instanceof DefaultTextLayer )
    {
      if( NO_DATA_LAYER.equals( layer.getId() ) )
      {
        layer.setVisible( isVisible() );
      }
    }
    else
    {
      final ILayerManager layerManager = layer.getLayerManager();
      final IChartLayer[] children = layerManager.getLayers();

      final String axisType = getTargetAxis( layer );
      if( ArrayUtils.contains( m_ignoreTypes, axisType ) )
      {
        layer.setVisible( false );
        setVisibility( children, false );
      }
      else
      {
        layer.setVisible( true );
        setVisibility( children, true );
      }
    }
  }

  /**
   * @return no_data_layer is visible
   */
  private boolean isVisible( )
  {
    if( ArrayUtils.isEmpty( m_providers ) )
      return true;

    // TODO instead provider.isLoaded() -> provider.isValid()
    for( final IObsProvider provider : m_providers )
    {
      if( provider.isValid() )
      {
        try
        {
          final IObservation observation = provider.getObservation();
          final ITupleModel model = observation.getValues( null );
          if( model.size() > 0 )
            return false;
        }
        catch( final Throwable t )
        {
          t.printStackTrace();
        }
      }
    }

    return true;
  }

  private void setVisibility( final IChartLayer[] layers, final boolean visibility )
  {
    for( final IChartLayer layer : layers )
    {
      layer.setVisible( visibility );
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
