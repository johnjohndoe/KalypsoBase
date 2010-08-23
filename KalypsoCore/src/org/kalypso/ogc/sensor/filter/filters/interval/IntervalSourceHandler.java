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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;

/**
 * @author Dirk Kuch
 */
public final class IntervalSourceHandler
{
  public static final String SOURCE_EXTENDED = "source://extended";

  public static final String SOURCE_INITIAL_VALUE = "source://initialValue";

  private IntervalSourceHandler( )
  {
  }

  public static void intersectSources( final String[] sources, final double factor )
  {
    for( int i = 0; i < sources.length; i++ )
    {
      /* Faktor != 1: "verschmiert?source=Prio_X" */
      if( factor != 1.0 )
      {
        final String[] srcs = DataSourceHelper.getSources( sources[i] );

        final StringBuffer buffer = new StringBuffer();
        buffer.append( String.format( "filter://%s?", IntervalFilter.class.getName() ) );

        if( !ArrayUtils.isEmpty( srcs ) )
        {
          for( int srcIndex = 0; srcIndex < srcs.length; srcIndex++ )
          {
            final String src = srcs[srcIndex];
            buffer.append( String.format( "source_%d=%s&", srcIndex, src ) );
          }

          // mergedSources=...,n-1,n
          buffer.append( String.format( "%s=", DataSourceHelper.MERGED_SOURCES_ID ) );
          for( int srcIndex = 0; srcIndex < srcs.length; srcIndex++ )
          {
            buffer.append( String.format( "%d,", srcIndex ) );
          }
        }

        final String reference = StringUtilities.chomp( buffer.toString() );
        sources[i] = reference;
      }
    }
  }

  public static void mergeSources( final String[] baseSources, final String[] otherSources, final boolean baseSourceWasEmpty )
  {
    final Set<String> merged = new HashSet<String>();

    /** collect merged sources */
    for( final String source : baseSources )
    {
      Collections.addAll( merged, DataSourceHelper.getMergedSources( source ) );
    }
    for( final String source : otherSources )
    {
      Collections.addAll( merged, DataSourceHelper.getMergedSources( source ) );
    }

    for( int i = 0; i < otherSources.length; i++ )
    {
      final String reference = mergeSourceReference( baseSources[i], otherSources[i], baseSourceWasEmpty );

      // append mergedSources references
      if( !merged.isEmpty() )
        baseSources[i] = DataSourceHelper.appendMergedSourcesReference( reference, merged.toArray( new String[] {} ) );
      else
        baseSources[i] = reference;
    }
  }

  /**
   * @param srcFieldWasEmpty
   *          if values has been empty, take source reference of other
   */
  private static String mergeSourceReference( final String base, final String other, final boolean baseSourceWasEmpty )
  {
    // - wenn undefiniert: quelle kopieren
    // - wenn schon definiert: "verschmiert": nach ? kombinieren
    if( IDataSourceItem.SOURCE_UNKNOWN.equalsIgnoreCase( base ) || IntervalSourceHandler.SOURCE_INITIAL_VALUE.equalsIgnoreCase( base ) )
      return other;
    else if( base.startsWith( "filter://" ) )
    {
      final Set<String> sources = new LinkedHashSet<String>();

      if( !baseSourceWasEmpty )
        Collections.addAll( sources, DataSourceHelper.getSources( base ) );

      if( other.startsWith( "filter://" ) )
        Collections.addAll( sources, DataSourceHelper.getSources( other ) );
      else if( !IDataSourceItem.SOURCE_UNKNOWN.equals( other ) || !IntervalSourceHandler.SOURCE_INITIAL_VALUE.equals( other ) )
        sources.add( other );

      if( sources.isEmpty() )
      {
        return String.format( "filter://%s?source_0=%s", IntervalFilter.class.getName(), SOURCE_EXTENDED );
      }
      else
      {
        final StringBuffer buffer = new StringBuffer();
        buffer.append( String.format( "filter://%s?", IntervalFilter.class.getName() ) );

        final String[] sourceArray = sources.toArray( new String[] {} );
        for( int i = 0; i < sourceArray.length; i++ )
        {
          buffer.append( String.format( "source_%d=%s&", i, sourceArray[i] ) );
        }

        final String source = StringUtilities.chomp( buffer.toString() );
        return source;
      }
    }

    return base;
  }

}
