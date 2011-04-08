/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.ui.chart.layer.provider;

import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.kalypso.zml.core.diagram.data.ZmlObservationDataHandler;
import org.kalypso.zml.ui.chart.layer.themes.ZmlSinglePointLayer;
import org.kalypso.zml.ui.core.provider.observation.DefaultRequestHandler;
import org.kalypso.zml.ui.core.provider.observation.SynchronousObservationProvider;

import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

/**
 * @author Dirk Kuch
 */
public class ZmlSinglePointProvider extends AbstractLayerProvider implements ILayerProvider
{
  public static final String ID = "org.kalypso.zml.ui.chart.layer.provider.ZmlSinglePointProvider"; //$NON-NLS-1$

  /**
   * @see de.openali.odysseus.chart.factory.provider.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  public IChartLayer getLayer( final URL context )
  {
    final IParameterContainer parameters = getParameterContainer();
    final String href = parameters.getParameterValue( "href", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    final ZmlSinglePointLayer layer = new ZmlSinglePointLayer( this, getStyleSet() );

    if( !StringUtils.isEmpty( href ) )
    {
      try
      {
        final SynchronousObservationProvider provider = new SynchronousObservationProvider( context, href, new DefaultRequestHandler() );

        final ZmlObservationDataHandler handler = new ZmlObservationDataHandler( layer, href );
        handler.setObservation( provider.getObservation() );

        layer.setDataHandler( handler );
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
      }
    }

    return layer;

  }

}
