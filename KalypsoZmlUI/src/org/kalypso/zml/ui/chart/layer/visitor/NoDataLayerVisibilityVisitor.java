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
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.provider.IObsProvider;

import de.openali.odysseus.chart.ext.base.layer.DefaultTextLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;

/**
 * @author Dirk Kuch
 */
public class NoDataLayerVisibilityVisitor implements IChartLayerVisitor
{
  public static final String NO_DATA_LAYER = "noData";

  private final IObsProvider[] m_providers;

  public NoDataLayerVisibilityVisitor( final IObsProvider[] providers )
  {
    m_providers = providers;
  }

  // <Parameter name="hideOnMultiSelect" value="true" />

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
          if( Objects.isNull( observation ) )
            continue;

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
}
