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

import org.apache.commons.lang.StringUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.provider.IObsProvider;

import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

/**
 * @author Dirk Kuch
 */
public final class LayerProviderUtils
{
  private LayerProviderUtils( )
  {
  }

  public static IAxis getValueAxis( final IObsProvider provider, final String type )
  {
    final IAxis[] axes = provider.getObservation().getAxisList();
    for( final IAxis axis : axes )
    {
      if( axis.getType().equals( type ) )
        return axis;
    }

    return null;
  }

  public static Date getMetadataDate( final IParameterContainer parameters, final String key, final MetadataList metadata )
  {
    final String parameter = parameters.getParameterValue( key, "" ); //$NON-NLS-1$

    return getMetadataDate( parameter, metadata );
  }

  public static Date getMetadataDate( final String key, final MetadataList metadata )
  {
    final Pattern pattern = new Pattern( "^metadata\\:" ); //$NON-NLS-1$
    final RETokenizer tokenizer = new RETokenizer( pattern, key );

    final String mdKey = tokenizer.nextToken();

    // FIXME: error handling if property is missing
    final String property = metadata.getProperty( mdKey );
    if( StringUtils.isBlank( property ) )
      return null;

    final Calendar calendar = DatatypeConverter.parseDate( property );
    return calendar.getTime();
  }
}
