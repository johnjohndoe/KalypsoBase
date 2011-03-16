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
package org.kalypso.ogc.sensor.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.repository.utils.RepositoryItems;

/**
 * @author Dirk Kuch
 */
public class CopyObservationMetadataHelper extends MetadataHelper
{
  public static String getFilter( final MetadataList mdl, final String baseIdentifier )
  {
    final Integer index = findCopyObservationIndex( mdl, baseIdentifier );
    if( index == null )
    {
      /** fallback */
      final String header = getCountedHeaderItem( MD_TIME_SERIES_FILTER, 0 );
      return mdl.getProperty( header );
    }

    final String header = getCountedHeaderItem( MD_TIME_SERIES_FILTER, index );

    return mdl.getProperty( header );
  }

  private static Integer findCopyObservationIndex( final MetadataList mdl, final String baseIdentifier )
  {
    final String plainBaseIdentifier = RepositoryItems.getPlainId( baseIdentifier );

    for( final Object obj : mdl.keySet() )
    {
      final String key = (String) obj;
      if( key.startsWith( MD_TIME_SERIES_SOURCE ) )
      {
        final String reference = RepositoryItems.getPlainId( mdl.getProperty( key ) );

        if( plainBaseIdentifier.equals( reference ) )
        {
          final String index = key.substring( key.lastIndexOf( "_" ) + 1 );

          return Integer.valueOf( index );
        }
      }
    }

    return null;
  }

  /**
   * @return only IRepository item sources will be returned - filters (like interval, interpolation) will be ignored
   */
  public static String[] getCopyObservationSources( final MetadataList mdl )
  {
    final List<String> sources = new ArrayList<String>();

    final Set<Object> keys = mdl.keySet();
    for( final Object object : keys )
    {
      final String key = (String) object;
      if( key.startsWith( MD_TIME_SERIES_SOURCE ) )
      {
        if( DataSourceHelper.isFiltered( key ) )
          continue;

        sources.add( mdl.getProperty( key ) );
      }

    }

    return sources.toArray( new String[] {} );
  }

  public static DateRange getSourceDateRange( final MetadataList mdl, final String baseItem )
  {
    final Integer index = findCopyObservationIndex( mdl, baseItem );
    if( index == null )
    {
      /** fallback */
      return getDefaultSourceDateRange( mdl );
    }
    final String from = getCountedHeaderItem( MD_TIME_SERIES_SRC_DATE_RANGE_FROM, index );
    final String to = getCountedHeaderItem( MD_TIME_SERIES_SRC_DATE_RANGE_TO, index );

    return getDateRange( mdl, from, to );
  }

  /**
   * REMARK: at the moment we only support the handling of one observation source in the user interface
   */
  public static DateRange getDefaultSourceDateRange( final MetadataList mdl )
  {
    final String from = getCountedHeaderItem( MD_TIME_SERIES_SRC_DATE_RANGE_FROM, 0 );
    final String to = getCountedHeaderItem( MD_TIME_SERIES_SRC_DATE_RANGE_TO, 0 );

    final DateRange defaultDateRange = getDateRange( mdl, from, to );
    return defaultDateRange;
  }
}
