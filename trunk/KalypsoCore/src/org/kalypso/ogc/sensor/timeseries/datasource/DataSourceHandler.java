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

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.repository.IRepositoryItem;

/**
 * Handles data sources of meta data lists
 *
 * @author Dirk Kuch
 */
public class DataSourceHandler
{
  /**
   * Cache of data sources
   */
  private Map<String, Integer> m_sources = null;

  private final MetadataList m_metadata;

  public DataSourceHandler( final MetadataList metadata )
  {
    m_metadata = metadata;
  }

  private synchronized Map<String, Integer> getSources( )
  {
    if( m_sources == null )
      m_sources = initSources();

    return m_sources;
  }

  /**
   * @return An unmodifiable map<src item index, src item identifier> of all known sources.
   */
  public synchronized Map<String, Integer> getDataSources( )
  {
    return Collections.unmodifiableMap( getSources() );
  }

  private Map<String, Integer> initSources( )
  {
    final SortedMap<String, Integer> sources = new TreeMap<>();

    for( final Object key : m_metadata.keySet() )
    {
      final String header = (String) key;
      if( header.startsWith( IDataSourceItem.MD_DATA_SOURCE_ITEM ) )
      {
        final Integer count = MetadataHelper.getCount( header );
        final String identifier = m_metadata.getProperty( header );
        sources.put( identifier, count );
      }
    }

    return sources;
  }

  public synchronized int addDataSource( final IRepositoryItem item )
  {
    return addDataSource( item.getIdentifier(), item.getRepository().getLabel() );
  }

  public synchronized int addDataSource( final String identifier, final String repository )
  {
    final Map<String, Integer> sources = getSources();
    if( sources.containsKey( identifier ) )
      return sources.get( identifier );

    // TODO: performance
    int count = getSources().size(); // append new items at the end of the list!
    while( m_metadata.getProperty( MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM, count ) ) != null )
    {
      count++;
    }

    final String sourceHeader = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM, count );
    final String repositoryHeader = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM_REPOSITORY, count );

    m_metadata.put( sourceHeader, identifier );
    m_metadata.put( repositoryHeader, repository );

    /* update cache */
    sources.put( identifier, count );

    return count;
  }

  public String getDataSourceIdentifier( final int pos )
  {
    if( pos < 0 )
      return null;

    final String header = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM, pos );
    final String source = m_metadata.getProperty( header );

    // TODO: check, what to do in this case?
    if( Objects.isNull( source ) )
      return null;

    if( containsFilter( source ) )
    {
      final String[] parts = StringUtils.split( source, "\\?" ); //$NON-NLS-1$
      return parts[0];
    }

    return source;
  }

  /**
   * @return source reference contains filter string, like source://id?<ns=... ...>
   */
  private boolean containsFilter( final String source )
  {
    if( source.contains( "?" ) ) //$NON-NLS-1$
    {
      final String[] parts = StringUtils.split( source, "\\?" ); //$NON-NLS-1$
      return parts[1].startsWith( "<" ); //$NON-NLS-1$
    }

    return false;
  }

// public String getDataSourceRepository( final int pos )
// {
// final String header = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM_REPOSITORY, pos );
//
// return m_metadata.getProperty( header );
// }

  public synchronized void removeAllDataSources( )
  {
    final String[] keys = m_metadata.keySet().toArray( new String[] {} );
    for( final String key : keys )
    {
      if( key.startsWith( IDataSourceItem.MD_DATA_SOURCE_ITEM ) )
        m_metadata.remove( key );
      else if( key.startsWith( IDataSourceItem.MD_DATA_SOURCE_ITEM_REPOSITORY ) )
        m_metadata.remove( key );
    }

    /* also clear cache */
    m_sources = null;
  }

  public boolean containsDataSourceReferences( )
  {
    final String[] keys = m_metadata.keySet().toArray( new String[] {} );
    for( final String key : keys )
    {
      if( key.startsWith( IDataSourceItem.MD_DATA_SOURCE_ITEM ) )
        return true;
    }

    return false;
  }
}