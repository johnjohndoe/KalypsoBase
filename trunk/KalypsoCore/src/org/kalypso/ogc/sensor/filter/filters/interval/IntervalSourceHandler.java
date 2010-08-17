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

import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;

/**
 * @author Dirk Kuch
 */
public final class IntervalSourceHandler
{
  private IntervalSourceHandler( )
  {

  }

  public static void intersectSources( final String[] sources, final double factor )
  {
    int srcIndex = 0;
    for( int i = 0; i < sources.length; i++ )
    {
      final String source = sources[i];

      /* Faktor != 1: "verschmiert?source=Prio_X" */
      if( factor != 1.0 )
      {
        // FIXME
        // &verschmiert=true
        final String[] srcs = DataSourceHelper.getSources( source );

        final StringBuffer buffer = new StringBuffer();
        buffer.append( String.format( "filter://%s?", IntervalFilter.class.getName() ) );

        for( final String src : srcs )
        {
          buffer.append( String.format( "source_%d=%s&", srcIndex++, src ) );
        }

        sources[i] = StringUtilities.chomp( buffer.toString() );
      }
    }
  }

  public static void mergeSources( final String[] baseSources, final String[] otherSources, final boolean baseSourceWasEmpty )
  {
    for( int i = 0; i < otherSources.length; i++ )
    {
      final String reference = mergeSourceReference( baseSources[i], otherSources[i], baseSourceWasEmpty );
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
    // - wenn schon definiert: "verschimiert": nach ? kombinieren
    if( IDataSourceItem.SOURCE_UNKNOWN.equalsIgnoreCase( base ) )
      return other;
    else if( base.startsWith( "filter://" ) )
    {
      final Set<String> sources = new LinkedHashSet<String>();

      if( !baseSourceWasEmpty )
        Collections.addAll( sources, DataSourceHelper.getSources( base ) );

      if( other.startsWith( "filter://" ) )
        Collections.addAll( sources, DataSourceHelper.getSources( other ) );
      else if( !IDataSourceItem.SOURCE_UNKNOWN.equals( other ) )
        sources.add( other );

      if( sources.isEmpty() )
      {
        return String.format( "filter://%s?source_0=%s", IntervalFilter.class.getName(), IDataSourceItem.SOURCE_UNKNOWN );
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
