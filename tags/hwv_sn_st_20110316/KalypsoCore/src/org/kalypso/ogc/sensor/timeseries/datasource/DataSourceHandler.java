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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.utils.RepositoryItems;

/**
 * Handles data sources of meta data lists
 * 
 * @author Dirk Kuch
 */
public class DataSourceHandler
{
  private final MetadataList m_metadata;

  public DataSourceHandler( final MetadataList metadata )
  {
    m_metadata = metadata;
  }

  public synchronized boolean hasDataSource( final IRepositoryItem repositoryItem )
  {
    return hasDataSource( repositoryItem.getIdentifier() );
  }

  public synchronized boolean hasDataSource( final String identifier )
  {
    final int index = getDataSourceIndex( identifier );
    if( index == -1 )
      return false;

    return true;
  }

  /**
   * @return -1 if index not exists
   */
  public synchronized int getDataSourceIndex( final String identifier )
  {
    final String plainIdentifier = RepositoryItems.getPlainId( identifier );

    final Map<Integer, String> dataSourceIndex = getDataSources();
    final Set<Entry<Integer, String>> entries = dataSourceIndex.entrySet();

    for( final Entry<Integer, String> entry : entries )
    {
      final String plainSource = RepositoryItems.getPlainId( entry.getValue() );

      if( plainIdentifier.equalsIgnoreCase( plainSource ) )
        return entry.getKey();
    }

    return -1;
  }

  /**
   * @return map<src item index, src item identifier>
   */
  public synchronized Map<Integer, String> getDataSources( )
  {
    final Map<Integer, String> sources = new TreeMap<Integer, String>();

    for( final Object key : m_metadata.keySet() )
    {
      final String header = (String) key;
      if( header.startsWith( IDataSourceItem.MD_DATA_SOURCE_ITEM ) )
      {
        final Integer count = MetadataHelper.getCount( header );
        sources.put( count, m_metadata.getProperty( header ) );
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
    if( hasDataSource( identifier ) )
      return getDataSourceIndex( identifier );

    int count = getDataSources().size(); // append new items at the end of the list!
    while( m_metadata.getProperty( MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM, count ) ) != null )
    {
      count++;
    }

    final String sourceHeader = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM, count );
    final String repositoryHeader = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM_REPOSITORY, count );

    m_metadata.put( sourceHeader, identifier );
    m_metadata.put( repositoryHeader, repository );

    return count;
  }

  public String getDataSourceIdentifier( final int pos )
  {
    final String header = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM, pos );
    final String source = m_metadata.getProperty( header );

    // TODO: check, what to do in this case?
    if( source == null )
      return null;

    if( containsFilter( source ) )
    {
      final String[] parts = source.split( "\\?" );
      return parts[0];
    }

    return source;
  }

  /**
   * @return source reference contains filter string, like source://id?<ns=... ...>
   */
  private boolean containsFilter( final String source )
  {
    if( source.contains( "?" ) )
    {
      final String[] parts = source.split( "\\?" );
      return parts[1].startsWith( "<" );
    }

    return false;
  }

  public String getDataSourceRepository( final int pos )
  {
    final String header = MetadataHelper.getCountedHeaderItem( IDataSourceItem.MD_DATA_SOURCE_ITEM_REPOSITORY, pos );

    return m_metadata.getProperty( header );
  }

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
  }

// /* add virtual sources to meta data */
// for( final IRepositoryItem item : m_items )
// {
// final IRepository repository = item.getRepository();
// DatasourceItemHelper.addDataSource( mdl, item.getIdentifier(), repository.getLabel() );
// }

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
