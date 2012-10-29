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
package org.kalypso.chart.ext.observation;

import java.net.URL;

import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * Layer provider which provides a {@link TupleResultLineChartLayer} on a feature based observation.
 * <p>
 * The following arguments are supported:
 * <ul>
 * <li>See TupleResultDomainValueData for further properties</li>
 * <li>featureKey: String. Key, where to get the feature from the ChartDataProvider.</li>
 * <li>propertyName: QName. If non null, the observation feature is found at that property of the given feature. Else the given feature must be an observation itself.</li>
 * <li>FIXME missing properties</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class TupleResultLineLayerProvider extends AbstractLayerProvider
{
  @Override
  public IChartLayer getLayer( final URL context )
  {
    final IStyleSet styleSet = getStyleSet();

    final TupleResultDomainValueData< ? , ? > dataContainer = TupleResultDomainValueData.fromContainer( getParameterContainer(), context, getModel() );

    return new TupleResultLineLayer( this, dataContainer, styleSet );
  }
}