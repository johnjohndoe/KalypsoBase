/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.model.sort;

import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.infomatiq.jsi.Rectangle;

public class SplitSort extends AbstractFeatureList
{
  /* Items of this list */
  private final List<SplitSortItem> m_items = new ArrayList<>();

  /**
   * (Geo-)index for this FeatureList. Lazy instantiated for memory reasons, small list (and those that never query
   * shall not have the memory consuming index.
   */
  private SplitSortindex m_index = null;

  public SplitSort( final Feature parentFeature, final IRelationType parentFTP )
  {
    super( parentFeature, parentFTP, null );
  }

  public SplitSort( final Feature parentFeature, final IRelationType parentFTP, final IEnvelopeProvider envelopeProvider )
  {
    super( parentFeature, parentFTP, envelopeProvider );
  }

  List<SplitSortItem> getItems( )
  {
    return m_items;
  }

  private synchronized SplitSortItem createItem( final Object data, final int index )
  {
    /* Create a new item */
    registerFeature( data );

    final SplitSortItem newItem = new SplitSortItem( data );

    if( m_index != null )
      m_index.insert( newItem, index );

    return newItem;
  }

  private void removeDataObject( final SplitSortItem item, final int index )
  {
    if( m_index != null )
      m_index.remove( item, index );

    // REMARK: it is a bit unclear what happens if we have a real Feature (not a link) multiple times in the same list.
    // We only unregister the it if the last occurrence is removed, assuming, that the feature is really no longer used.
    unregisterFeature( item.getData() );
  }

  private synchronized void createIndex( )
  {
    if( m_index == null )
      m_index = new SplitSortindex( this );
  }

  // IMPLEMENTATION OF LIST INTERFACE

  /**
   * @see java.util.List#add(java.lang.Object)
   */
  @Override
  public synchronized boolean add( final Object object )
  {
    checkCanAdd( 1 );

    final SplitSortItem item = createItem( object, size() );

    m_items.add( item );

    return true;
  }

  /**
   * @see java.util.List#add(int, java.lang.Object)
   */
  @Override
  public synchronized void add( final int index, final Object object )
  {
    checkCanAdd( 1 );

    if( m_index != null )
      m_index.reindex( index, 1 );

    final SplitSortItem newItem = createItem( object, index );

    m_items.add( index, newItem );
  }

  /**
   * @see org.kalypsodeegree_impl.model.sort.AbstractFeatureList#addAll(int, java.util.Collection)
   */
  @Override
  public synchronized boolean addAll( final int index, final Collection c )
  {
    checkCanAdd( c.size() );

    if( m_index != null )
      m_index.reindex( index, c.size() );

    final Collection<SplitSortItem> items = new ArrayList<>( c.size() );
    final int count = 0;
    for( final Object object : c )
    {
      final SplitSortItem newItem = createItem( object, index + count );
      items.add( newItem );
    }

    return m_items.addAll( index, items );
  }

  /**
   * @see org.kalypsodeegree_impl.model.sort.AbstractFeatureList#set(int, java.lang.Object)
   */
  @Override
  public synchronized Object set( final int index, final Object newObject )
  {
    final SplitSortItem oldItem = m_items.get( index );

    if( m_index != null )
      removeDataObject( oldItem, index );

    final SplitSortItem newItem = createItem( newObject, index );

    m_items.set( index, newItem );

    return oldItem.getData();
  }

  /**
   * @see java.util.List#get(int)
   */
  @Override
  public synchronized Object get( final int index )
  {
    return m_items.get( index ).getData();
  }

  /**
   * @see java.util.List#remove(java.lang.Object)
   */
  @Override
  public synchronized boolean remove( final Object object )
  {
    final int index = indexOf( object );
    if( index == -1 )
      return false;

    final SplitSortItem item = m_items.get( index );

    removeDataObject( item, index );

    // FIXME: reindexing is very slow, we need another idea how to speed this up
    if( m_index != null )
      m_index.reindex( index + 1, -1 );

    m_items.remove( index );

    return true;
  }

  /**
   * @see java.util.List#remove(int)
   */
  @Override
  public synchronized Object remove( final int index )
  {
    final SplitSortItem item = m_items.get( index );

    removeDataObject( item, index );

    if( m_index != null )
      m_index.reindex( index + 1, -1 );

    m_items.remove( index );

    return item.getData();
  }

  /**
   * @see java.util.List#size()
   */
  @Override
  public synchronized int size( )
  {
    return m_items.size();
  }

  /**
   * @see java.util.List#clear()
   */
  @Override
  public synchronized void clear( )
  {
    for( final Object element : m_items )
      unregisterFeature( element );

    m_index = null;

    m_items.clear();
  }

  /**
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Override
  public synchronized int indexOf( final Object object )
  {
    if( m_index != null )
    {
      // TODO: if we have many objects, should we create the index now?

      final int index = m_index.indexOf( object );
      if( index != 0 )
        return index;
    }

    // REMARK: linear search is needed, because we cannot assure the valid index, especially after an object has been
    // removed that was contained inside the list twice.

    /* linear search */
    for( int i = 0; i < size(); i++ )
    {
      final SplitSortItem item = m_items.get( i );
      if( item.getData().equals( object ) )
        return i;
    }

    return -1;
  }

  /**
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  @Override
  public synchronized int lastIndexOf( final Object object )
  {
    for( int i = size() - 1; i >= 0; i-- )
    {
      final SplitSortItem item = m_items.get( i );
      if( item.getData().equals( object ) )
        return i;
    }

    return -1;
  }

  /**
   * ATTENTION: Returns an unmodifiable iterator i.e. changing the list via the returned iterator results in an
   * exception.<br/>
   * The iterator is not synchronized, however.
   * 
   * @see java.util.List#listIterator(int)
   */
  @Override
  public synchronized ListIterator< ? > listIterator( final int index )
  {
    return new SplitSortIterator( this, index );
  }

  // JMSpatialIndex implementation

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Position, java.util.List)
   */
  @Override
  public List< ? > query( final GM_Position pos, final List result )
  {
    final Rectangle envelope = new Rectangle( (float)pos.getX() - 1.0f, (float)pos.getY() - 1.0f, (float)pos.getX() + 1.0f, (float)pos.getY() + 1.0f );
    return query( envelope, result );
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Envelope, java.util.List)
   */
  @Override
  public List< ? > query( final GM_Envelope queryEnv, final List result )
  {
    final Rectangle envelope = GeometryUtilities.toRectangle( queryEnv );
    return query( envelope, result );
  }

  /**
   *
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  protected synchronized List< ? > query( final Rectangle envelope, final List receiver )
  {
    createIndex();

    final List<Object> result = receiver == null ? new ArrayList<>() : receiver;

    final List<SplitSortItem> items = m_items;

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        final SplitSortItem item = items.get( index );
        final Object data = item.getData();
        result.add( data );
        return true;
      }
    };

    final Rectangle searchRect = envelope == null ? m_index.getBounds() : envelope;
    m_index.intersects( searchRect, ip );

    return result;
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#getBoundingBox()
   */
  @Override
  public synchronized GM_Envelope getBoundingBox( )
  {
    createIndex();

    final Rectangle bounds = m_index.getBounds();

    // REMARK: we assume that all elements of the split sort are always in Kalypso CRS
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    return GeometryUtilities.toEnvelope( bounds, crs );
  }

  /**
   * @see org.kalypsodeegree_impl.model.sort.AbstractFeatureList#invalidate(java.lang.Object)
   */
  @Override
  public synchronized void invalidate( final Object object )
  {
    if( m_index != null )
      m_index.invalidate( object );
  }

}