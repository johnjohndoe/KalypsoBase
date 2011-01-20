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
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.ui.chart.layer.themes.ZmlLayerFactory;
import org.kalypso.zml.ui.chart.layer.themes.ZmlLineLayer;
import org.kalypso.zml.ui.core.provider.observation.IRequestHandler;
import org.kalypso.zml.ui.core.provider.observation.SynchronousObservationProvider;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * @author Dirk Kuch
 */
public class ZmlLineLayerProvider extends AbstractLayerProvider implements ILayerProvider
{
  public static final String ID = "org.kalypso.zml.ui.chart.layer.provider.ZmlLineLayerProvider";

  /**
   * @see de.openali.odysseus.chart.factory.provider.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  public IChartLayer getLayer( final URL context ) throws ConfigurationException
  {
    try
    {
      final IStyleSet styleSet = getStyleSet();
      final String targetAxisId = getTargetAxisId();

      final ZmlLayerFactory factory = ZmlLayerFactory.getInstance();

      final ZmlLineLayer layer = factory.createLineLayer( this, styleSet );
      final ZmlObsProviderDataHandler handler = new ZmlObsProviderDataHandler( layer, targetAxisId );

      final IParameterContainer parameters = getParameterContainer();
      final String href = parameters.getParameterValue( "href", "" ); //$NON-NLS-1$
      if( !StringUtils.isEmpty( href ) )
      {
        final SynchronousObservationProvider provider = new SynchronousObservationProvider( context, href, getRequestHandler() );
        handler.setObsProvider( provider );
      }

      return layer;
    }
    catch( final Throwable t )
    {
      throw new ConfigurationException( "Configuring of .kod line layer theme failed.", t );
    }
  }

  protected IRequestHandler getRequestHandler( )
  {
    return new LineLayerRequestHandler( getParameterContainer() );
  }

}
