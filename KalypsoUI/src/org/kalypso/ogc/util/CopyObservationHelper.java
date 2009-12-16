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
package org.kalypso.ogc.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.timeseries.ICopyObservationTimeSeriesConstants;

/**
 * @author Dirk Kuch
 */
public final class CopyObservationHelper implements ICopyObservationTimeSeriesConstants
{
  private CopyObservationHelper( )
  {

  }

  public static Map<String, String> getSourceMetadataSettings( final ObservationSource source, final int count )
  {
    Map<String, String> map = new HashMap<String, String>();

    String reference = source.getObservation().getIdentifier();
    String filter = XMLUtilities.encapsulateInCDATA( source.getFilter() );

    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();

    DateRange sourceDateRange = source.getSourceDateRange();
    String sourceFrom = sourceDateRange.getFrom() == null ? "" : DateUtilities.printDateTime( sourceDateRange.getFrom(), timeZone );
    String sourceTo = sourceDateRange.getTo() == null ? "" : DateUtilities.printDateTime( sourceDateRange.getTo(), timeZone );

    DateRange forecastDateRange = source.getForecastDateRange();
    String foreCastFrom = forecastDateRange.getFrom() == null ? "" : DateUtilities.printDateTime( forecastDateRange.getFrom(), timeZone );
    String foreCastTo = forecastDateRange.getTo() == null ? "" : DateUtilities.printDateTime( forecastDateRange.getTo(), timeZone );

    map.put( getMetaDataKey( MD_TIME_SERIES_SOURCE, count ), reference );
    map.put( getMetaDataKey( MD_TIME_SERIES_FILTER, count ), filter );
    map.put( getMetaDataKey( MD_TIME_SERIES_SRC_DATE_RANGE_FROM, count ), sourceFrom );
    map.put( getMetaDataKey( MD_TIME_SERIES_SRC_DATE_RANGE_TO, count ), sourceTo );
    map.put( getMetaDataKey( MD_TIME_SERIES_FORECAST_DATE_RANGE_FROM, count ), foreCastFrom );
    map.put( getMetaDataKey( MD_TIME_SERIES_FORECAST_DATE_RANGE_TO, count ), foreCastTo );

    return map;
  }

  private static String getMetaDataKey( final String base, final Integer count )
  {
    return base + "_" + count.toString();
  }
}
