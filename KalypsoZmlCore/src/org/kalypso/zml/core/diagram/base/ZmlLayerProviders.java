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
package org.kalypso.zml.core.diagram.base;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.KalypsoZmlCoreExtensions;
import org.kalypso.zml.core.base.request.IRequestStrategy;

import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

/**
 * @author Dirk Kuch
 */
public final class ZmlLayerProviders
{
  private ZmlLayerProviders( )
  {
  }

  public static IAxis getValueAxis( final IObsProvider provider, final String type )
  {
    if( provider == null )
      return null;

    final IObservation observation = provider.getObservation();
    if( observation == null )
      return null;

    final IAxis[] axes = observation.getAxes();
    return AxisUtils.findAxis( axes, type );
  }

  public static Date getMetadataDate( final IParameterContainer parameters, final String key, final MetadataList metadata )
  {
    final String parameter = parameters.getParameterValue( key, "" ); //$NON-NLS-1$

    return getMetadataDate( parameter, metadata );
  }

  private static final Pattern PATTERN_METADATE_DATE = new Pattern( "^metadata\\:" ); //$NON-NLS-1$

  public static Date getMetadataDate( final String key, final MetadataList metadata )
  {
    final RETokenizer tokenizer = new RETokenizer( PATTERN_METADATE_DATE, key );
    final String mdKey = tokenizer.nextToken();

    // FIXME: error handling if property is missing
    final String property = metadata.getProperty( mdKey );
    if( StringUtils.isBlank( property ) )
      return null;

    final Calendar calendar = DatatypeConverter.parseDate( property );
    return calendar.getTime();
  }

  public static IRequestStrategy getRequestStrategy( final IZmlLayer layer, final IParameterContainer container )
  {
    final String id = container.getParameterValue( "request.strategy", "request.strategy.prognose" ); //$NON-NLS-1$ //$NON-NLS-2$
    final IRequestStrategy strategy = KalypsoZmlCoreExtensions.getInstance().findStrategy( id );
    strategy.init( layer, container );

    return strategy;
  }
}
