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
package org.kalypso.simulation.core.ant.copyobservation;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.metadata.ICopyObservationMetaDataConstants;
import org.kalypso.simulation.core.ant.copyobservation.source.ObservationSource;

/**
 * @author Dirk Kuch
 */
public final class CopyObservationHelper implements ICopyObservationMetaDataConstants
{
  private CopyObservationHelper( )
  {
  }

  public static Map<String, String> getSourceMetadataSettings( final ObservationSource source, final int count )
  {
    final Map<String, String> map = new HashMap<String, String>();

    final String reference = source.getObservation().getHref();
    final String filter = XMLUtilities.encapsulateInCDATA( source.getFilter() );

    final DateRange sourceDateRange = source.getDateRange();

    final String sourceFrom = getFrom( sourceDateRange );
    final String sourceTo = getTo( sourceDateRange );

    map.put( getMetaDataKey( MD_TIME_SERIES_SOURCE, count ), reference );
    map.put( getMetaDataKey( MD_TIME_SERIES_FILTER, count ), filter );
    map.put( getMetaDataKey( MD_TIME_SERIES_SRC_DATE_RANGE_FROM, count ), sourceFrom );
    map.put( getMetaDataKey( MD_TIME_SERIES_SRC_DATE_RANGE_TO, count ), sourceTo );

    return map;
  }

  private static String getTo( final DateRange dateRange )
  {
    if( dateRange == null )
      return "";

    if( dateRange.getFrom() == null )
      return "";

    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();

    return DateUtilities.printDateTime( dateRange.getTo(), timeZone );
  }

  private static String getFrom( final DateRange dateRange )
  {
    if( dateRange == null )
      return "";

    if( dateRange.getFrom() == null )
      return "";

    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();

    return DateUtilities.printDateTime( dateRange.getFrom(), timeZone );
  }

  private static String getMetaDataKey( final String base, final Integer count )
  {
    return base + "_" + count.toString();
  }
}
