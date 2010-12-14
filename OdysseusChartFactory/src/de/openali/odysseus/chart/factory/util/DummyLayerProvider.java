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
package de.openali.odysseus.chart.factory.util;

import java.net.URL;
import java.util.Map;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * @author burtscher1
 */
public class DummyLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  public IChartLayer getLayer( URL context )
  {
    return new DummyLayer();
  }

  /**
   * @see de.openali.odysseus.chart.factory.provider.ILayerProvider#init(de.openali.odysseus.chart.framework.model.IChartModel,
   *      java.lang.String, java.lang.String, java.util.Map, de.openali.odysseus.chart.framework.model.style.IStyleSet,
   *      de.openali.odysseus.chart.factory.config.parameters.IParameterContainer, java.net.URL)
   */
  @SuppressWarnings("unused")
  public void init( IChartModel model, String domainAxisId, String targetAxisId, Map<String, String> mapperMap, IStyleSet styleSet, IParameterContainer parameters,  URL context )
  {
    // nothing to do
  }
}
