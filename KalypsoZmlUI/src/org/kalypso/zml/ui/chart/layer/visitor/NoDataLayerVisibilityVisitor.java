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

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;

import de.openali.odysseus.chart.ext.base.layer.DefaultTextLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class NoDataLayerVisibilityVisitor implements IChartLayerVisitor
{
  public static final String NO_DATA_LAYER = "noData";

  boolean m_visible = true;

  IChartLayer m_noDataLayer;

  public NoDataLayerVisibilityVisitor( )
  {

  }

  // <Parameter name="hideOnMultiSelect" value="true" />
  @Override
  public void visit( final IChartLayer layer )
  {
    if( layer instanceof DefaultTextLayer )
    {
      if( NO_DATA_LAYER.equals( layer.getIdentifier() ) )
      {
        m_noDataLayer = layer;
      }
    }
    else if( layer instanceof IZmlLayer )
    {
      final boolean visible = isVisible( (IZmlLayer) layer );
      if( visible )
      {
        m_visible = false;
        return;
      }
    }

    layer.getLayerManager().accept( this );
  }

  /**
   * @return no_data_layer is visible
   */
  private boolean isVisible( final IZmlLayer layer )
  {
    if( !layer.isVisible() )
      return false;

    final IZmlLayerDataHandler handler = layer.getDataHandler();
    if( Objects.isNull( handler ) )
      return false;

    final IObservation observation = handler.getObservation();
    if( Objects.isNull( observation ) )
      return false;

    try
    {
      final ITupleModel model = observation.getValues( null );
      if( model.isEmpty() )
        return false;

      final ICoordinateMapper mapper = layer.getCoordinateMapper();
      final IAxis axis = mapper.getTargetAxis();
      final String type = axis.getIdentifier();

      return Objects.isNotNull( AxisUtils.findAxis( model.getAxes(), type ) );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public void doFinialize( )
  {
    if( Objects.isNull( m_noDataLayer ) )
      return;

    m_noDataLayer.setVisible( m_visible );
  }
}
