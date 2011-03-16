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
package org.kalypso.ogc.sensor.timeseries.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * Helper code for multiple data source references - lke: filter://smeFilterClass?source_1=blub&source_2=blub2&...
 * 
 * @author Dirk Kuch
 */
public final class DataSourceHelper
{
  public static final String MERGED_SOURCES_ID = "mergedSources";

  public static final String FILTER_SOURCE = "filter://";

  private DataSourceHelper( )
  {
  }

  /**
   * @param reference
   *          filter://smeFilterClass?source_1=SRC_1&source_2=SRC_2&...
   * @return [SRC_1, SRC_2]
   */
  public static String[] getSources( final String reference )
  {
    if( !isFiltered( reference ) )
      return new String[] { reference };

    final String[] referenceParts = reference.split( "\\?" );
    if( referenceParts.length != 2 )
      return new String[] {};

    final List<String> sources = new ArrayList<String>();

    final String[] parts = referenceParts[1].split( "\\&" );
    for( final String part : parts )
    {
      if( part.contains( MERGED_SOURCES_ID ) )
        continue;

      final String[] sourceParts = part.split( "\\=" );
      if( sourceParts.length == 1 )
        sources.add( IDataSourceItem.SOURCE_UNKNOWN );
      else
        sources.add( sourceParts[1] );
    }

    return sources.toArray( new String[] {} );
  }

  public static boolean isFiltered( final String reference )
  {
    return reference.startsWith( FILTER_SOURCE );
  }

  /**
   * @return merged sources (filter://smeFilterClass?source_1=SRC_1&source_2=SRC_2&mergedSources=1,2)
   */
  public static String[] getMergedSources( final String reference )
  {
    final String[] referenceParts = reference.split( "\\?" );
    if( referenceParts.length != 2 )
      return new String[] {};

    final Integer[] sources = findMergedSourceIds( reference );

    final List<String> merged = new ArrayList<String>();
    final String[] parts = referenceParts[1].split( "\\&" );

    for( final String part : parts )
    {
      if( part.contains( MERGED_SOURCES_ID ) )
        continue;

      final String[] sourceParts = part.split( "\\=" );
      if( sourceParts.length != 2 )
        continue;

      final String sourceReference = sourceParts[0];
      final String sourceLetter = sourceReference.substring( sourceReference.lastIndexOf( '_' ) + 1 );

      final Integer index = Integer.valueOf( sourceLetter );
      if( ArrayUtils.contains( sources, index ) )
        merged.add( sourceParts[1] );
    }

    return merged.toArray( new String[] {} );
  }

  /**
   * @return mergedSource ids
   */
  private static Integer[] findMergedSourceIds( final String reference )
  {
    final String[] referenceParts = reference.split( "\\?" );
    if( referenceParts.length != 2 )
      return new Integer[] {};

    final String[] parts = referenceParts[1].split( "\\&" );
    for( final String part : parts )
    {
      if( part.contains( MERGED_SOURCES_ID ) )
      {
        final String[] idParts = part.split( "=" );
        if( referenceParts.length != 2 )
          return new Integer[] {};

        final List<Integer> ids = new ArrayList<Integer>();

        final String[] numbers = idParts[1].split( "," );
        for( final String number : numbers )
        {
          ids.add( Integer.valueOf( number ) );
        }

        return ids.toArray( new Integer[] {} );
      }

    }

    return new Integer[] {};
  }

  /**
   * @return extended reference by &mergedSources=x,y,z tag
   */
  public static String appendMergedSourcesReference( final String reference, final String[] mergedSources )
  {
    final String[] sources = getSources( reference );
    final Set<Integer> merged = new TreeSet<Integer>();

    for( final String source : sources )
    {
      if( source.trim().isEmpty() )
        continue;

      if( ArrayUtils.contains( mergedSources, source ) )
      {
        final int index = findSourceIndex( reference, source );
        if( index != -1 )
          merged.add( index );
      }
    }

    if( merged.isEmpty() )
      return reference;

    final StringBuffer buffer = new StringBuffer( String.format( "%s&%s=", removeMergedSourcesReference( reference ), MERGED_SOURCES_ID ) );

    final Integer[] indexes = merged.toArray( new Integer[] {} );
    for( final Integer index : indexes )
    {
      buffer.append( String.format( "%d,", index ) );
    }

    return StringUtils.chop( buffer.toString() );
  }

  private static String removeMergedSourcesReference( final String reference )
  {
    if( !reference.contains( MERGED_SOURCES_ID ) )
      return reference;

    final String[] referenceParts = reference.split( "\\?" );
    if( referenceParts.length != 2 )
      return reference;

    final StringBuffer buffer = new StringBuffer();
    buffer.append( String.format( "%s?", referenceParts[0] ) );

    final String[] sources = getSources( reference );
    for( int i = 0; i < sources.length; i++ )
    {
      final String source = sources[i];
      buffer.append( String.format( "source_%d=%s&", i, source ) );
    }

    return StringUtils.chop( buffer.toString() );
  }

  /**
   * @return source index of an source
   */
  private static int findSourceIndex( final String reference, final String source )
  {
    final String[] referenceParts = reference.split( "\\?" );
    if( referenceParts.length != 2 )
      return -1;

    final String[] parts = referenceParts[1].split( "\\&" );
    for( final String part : parts )
    {
      if( part.contains( MERGED_SOURCES_ID ) )
        continue;

      final String[] sourceParts = part.split( "\\=" );
      if( sourceParts.length == 1 )
        continue;
      else if( source.equals( sourceParts[1] ) )
      {
        final String sourceReference = sourceParts[0];
        final String sourceLetter = sourceReference.substring( sourceReference.lastIndexOf( '_' ) + 1 );

        return Integer.valueOf( sourceLetter );
      }
    }

    return -1;
  }

  public static boolean hasDataSources( final ITupleModel model )
  {
    final IAxis[] axes = model.getAxes();

    return AxisUtils.findDataSourceAxis( axes ) != null;
  }
}
