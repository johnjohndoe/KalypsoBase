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
package org.kalypso.ogc.sensor.filter.filters.interval;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;

/**
 * @author Dirk Kuch
 */
public final class IntervalSourceHandler
{
  public static final String SOURCE_EXTENDED = "source://extended"; // $NON-NLS-1$

  public static final String SOURCE_INITIAL_VALUE = IDataSourceItem.SOURCE_UNKNOWN;

  public static final String SOURCE_INTERVAL_FITLER = DataSourceHelper.FILTER_SOURCE + IntervalFilter.FILTER_ID;

  private IntervalSourceHandler( )
  {
  }

  /**
   * @param srcFieldWasEmpty
   *          if values has been empty, take source reference of other
   */
  public static String mergeSourceReference( final String base, final String other )
  {
    // - wenn undefiniert: quelle kopieren
    // - wenn schon definiert: "verschmiert": nach ? kombinieren
    if( IDataSourceItem.SOURCE_UNKNOWN.equalsIgnoreCase( base ) || isInitialValue( base ) )
      return other;
    else if( DataSourceHelper.isFiltered( base ) )
    {
      final Set<String> sources = new LinkedHashSet<String>();

      if( !isInitialValue( base ) )
        Collections.addAll( sources, DataSourceHelper.getSources( base ) );

      if( DataSourceHelper.isFiltered( other ) )
      {
        final String[] otherSources = DataSourceHelper.getSources( other );
        for( final String source : otherSources )
        {
          if( SOURCE_INITIAL_VALUE.equalsIgnoreCase( source ) )
            continue;

          sources.add( source );
        }
      }
      else if( !IDataSourceItem.SOURCE_UNKNOWN.equals( other ) && !isInitialValue( other ) )
        sources.add( other );

      if( sources.isEmpty() )
      {
        return String.format( "%s%s?source_0=%s", DataSourceHelper.FILTER_SOURCE, IntervalFilter.FILTER_ID, SOURCE_EXTENDED ); //$NON-NLS-1$
      }
      else
      {
        final StringBuffer buffer = new StringBuffer();
        buffer.append( String.format( "%s%s?", DataSourceHelper.FILTER_SOURCE, IntervalFilter.FILTER_ID ) ); //$NON-NLS-1$

        final String[] sourceArray = sources.toArray( new String[] {} );
        for( int i = 0; i < sourceArray.length; i++ )
        {
          buffer.append( String.format( "source_%d=%s&", i, sourceArray[i] ) ); //$NON-NLS-1$
        }

        return StringUtils.chop( buffer.toString() );
      }
    }

    return base;
  }

  private static boolean isInitialValue( final String base )
  {
    return SOURCE_INITIAL_VALUE.equalsIgnoreCase( base );
  }
}
