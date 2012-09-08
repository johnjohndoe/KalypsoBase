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
package org.kalypso.chart.ext.observation.layer.provider;

import java.net.URL;

import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.ext.observation.layer.TupleResultLineLayer;

import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

/**
 * @author Gernot Belger
 */
public class DefaultTupleResultLayerProvider extends AbstractLayerProvider
{
  @Override
  public IChartLayer getLayer( final URL context )
  {
    return new TupleResultLineLayer( this, getDataContainer(), getStyleSet() );
  }

  public TupleResultDomainValueData< ? , ? > getDataContainer( )
  {
    final IParameterContainer pc = getParameterContainer();

    final String href = pc.getParameterValue( "href", null ); //$NON-NLS-1$

    final String observationId = pc.getParameterValue( "observationId", null ); //$NON-NLS-1$
    final String domainComponentName = pc.getParameterValue( "domainComponentId", null ); //$NON-NLS-1$
    final String targetComponentName = pc.getParameterValue( "targetComponentId", null ); //$NON-NLS-1$

    if( href != null && observationId != null && domainComponentName != null && targetComponentName != null )
    {
      return new TupleResultDomainValueData<>( getContext(), href, observationId, domainComponentName, targetComponentName );
    }

    return null;
  }
}
