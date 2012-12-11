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

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.jts.JTSUtilities;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.feature.FeatureLinkUtils;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.infomatiq.jsi.Rectangle;

public class SplitSort extends AbstractFeatureList
{
  /* Items of this list */
  protected final List<Object> m_items = new ArrayList<>();

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

  private synchronized void createItem( final Object data, final int index )
  {
    /* Create a new item */
    registerFeature( data );
    if( m_index != null )
      m_index.insert( data, index );
  }

  private synchronized void createIndex( )
  {
    if( m_index == null )
      m_index = new SplitSortindex( this );
  }

  // IMPLEMENTATION OF LIST INTERFACE

  @Override
  public synchronized boolean add( final Object item )
  {
    add( size(), item );
    return true;
  }

  @Override
  public synchronized void add( final int index, final Object item )
  {
    checkCanAdd( 1 );

    final int numIndexShifts = m_items.size() - index;
    if( m_index != null && numIndexShifts > 0 )
    {
      final TIntArrayList oldIndices = new TIntArrayList( numIndexShifts );
      final TIntArrayList newIndices = new TIntArrayList( numIndexShifts );
      for( int i = index; i < m_items.size(); i++ )
      {
        oldIndices.add( i );
        newIndices.add( i + 1 );
      }
      m_index.reindex( oldIndices, newIndices );
    }

    createItem( item, index );
    m_items.add( index, item );

    if( m_index != null )
      m_index.assertSize();
  }

  @Override
  public synchronized boolean addAll( final int index, final Collection c )
  {
    final int newItemCount = c.size();
    checkCanAdd( newItemCount );

    if( m_index != null )
    {
      final TIntArrayList oldIndices = new TIntArrayList( m_items.size() - index );
      final TIntArrayList newIndices = new TIntArrayList( m_items.size() - index );
      for( int i = index; i < m_items.size(); i++ )
      {
        oldIndices.add( i );
        newIndices.add( i + newItemCount );
      }
      m_index.reindex( oldIndices, newIndices );
    }

    final int count = 0;
    for( final Object object : c )
      createItem( object, index + count );

    final boolean result = m_items.addAll( index, c );

    if( m_index != null )
      m_index.assertSize();

    return result;
  }

  @Override
  public synchronized Object set( final int index, final Object item )
  {
    final Object oldItem = m_items.get( index );

    removeItem( oldItem, index );
    createItem( item, index );

    return m_items.set( index, item );
  }

  protected void removeItem( final Object item, final int index )
  {
    if( m_index != null )
      m_index.remove( index, item );

    // REMARK: it is a bit unclear what happens if we have a real Feature (not a link) multiple times in the same list.
    // We only unregister the it if the last occurrence is removed, assuming, that the feature is really no longer used.
    unregisterFeature( item );
  }

  @Override
  public synchronized Object get( final int index )
  {
    return m_items.get( index );
  }

  @Override
  public synchronized boolean remove( final Object object )
  {
    final int index = indexOf( object );
    if( index == -1 )
      return false;

    remove( index );
    return true;
  }

  @Override
  public synchronized Object remove( final int index )
  {
    final Object item = m_items.get( index );

    removeItem( item, index );

    final int numIndexShifts = m_items.size() - index;
    if( m_index != null && numIndexShifts > 0 )
    {
      final TIntArrayList oldIndices = new TIntArrayList( numIndexShifts );
      final TIntArrayList newIndices = new TIntArrayList( numIndexShifts );
      for( int i = index + 1; i < m_items.size(); i++ )
      {
        oldIndices.add( i );
        newIndices.add( i - 1 );
      }
      m_index.reindex( oldIndices, newIndices );
    }

    m_items.remove( index );

    if( m_index != null )
      m_index.assertSize();

    return true;
  }

  @Override
  public synchronized boolean removeAll( final Collection c )
  {
    final TIntArrayList removedItemIndices = new TIntArrayList( c.size() );
    for( final ListIterator<Object> listIterator = m_items.listIterator(); listIterator.hasNext(); )
    {
      final int index = listIterator.nextIndex();
      final Object object = listIterator.next();
      if( c.contains( object ) )
        removedItemIndices.add( index );
    }

    removeAll( removedItemIndices.toNativeArray() );

    return !removedItemIndices.isEmpty();
  }

  @Override
  public void removeAll( final int[] allIndices )
  {
    /* clone in order not to change the input array */
    final TIntArrayList removedItemIndices = new TIntArrayList( allIndices );

    // sort indices in ascending order
    removedItemIndices.sort();

    // build array of all shifts
    final int removeCount = removedItemIndices.size();
    if( removeCount == 0 )
      return;

    // remove all items from index and workspace
    removedItemIndices.forEach( new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        final Object item = m_items.get( index );
        removeItem( item, index );
        return true;
      }
    } );

    // reindex
    final int firstIndex = removedItemIndices.get( 0 ); // smallest index of a removed item
    final int numIndexShifts = m_items.size() - firstIndex - removeCount + 1;

    if( numIndexShifts > 0 )
    {
      final TIntArrayList oldIndices = new TIntArrayList( numIndexShifts );
      final TIntArrayList newIndices = new TIntArrayList( numIndexShifts );
      int j = firstIndex; // new index for remaining items
      // loop from the item after the first removed item to end of list
      for( int i = firstIndex + 1; i < m_items.size(); i++ )
      {
        if( removedItemIndices.contains( i ) )
          continue;

        oldIndices.add( i );
        newIndices.add( j++ ); // the new indices will be in consecutive order
      }

      if( m_index != null )
        m_index.reindex( oldIndices, newIndices );
    }

    // delete items
    // find out if this is a contiguous set of removed items
    final int lastIndex = removedItemIndices.get( removeCount - 1 ); // largest index of a removed item
    if( lastIndex - firstIndex == removeCount - 1 )
    {
      // optimization for deleting a range of items
      m_items.subList( firstIndex, lastIndex + 1 ).clear();
    }
    else
    {
      removedItemIndices.forEachDescending( new TIntProcedure()
      {
        @Override
        public boolean execute( final int value )
        {
          m_items.remove( value );
          return true;
        }
      } );
    }

    if( m_index != null )
      m_index.assertSize();
  }

  @Override
  public synchronized int size( )
  {
    return m_items.size();
  }

  @Override
  public synchronized void clear( )
  {
    for( final Object element : m_items )
      unregisterFeature( element );

    if( m_index != null )
    {
      m_index.dispose();
      m_index = null;
    }

    m_items.clear();
  }

  @Override
  public synchronized int indexOf( final Object object )
  {
    if( m_items.size() >= SplitSortindex.INITIAL_CAPACITY )
      createIndex();

    if( m_index != null )
      return m_index.indexOf( object );
    else
      return m_items.indexOf( object );
  }

  @Override
  public synchronized int lastIndexOf( final Object object )
  {
    return m_items.lastIndexOf( object );
  }

  /**
   * Overwritten in order to use index to improve performance.
   */
  @Override
  public synchronized int indexOfLink( final Feature targetFeature )
  {
    if( m_items.size() >= SplitSortindex.INITIAL_CAPACITY )
      createIndex();

    if( m_index == null || targetFeature == null )
      return super.indexOfLink( targetFeature );

    /* string reference? */
    final int indexOfId = indexOf( targetFeature.getId() );
    if( indexOfId != -1 )
      return indexOfId;

    /* search by location */
    final GM_Envelope envelope = getEnvelope( targetFeature );
    if( envelope == null )
      return super.indexOfLink( targetFeature );

    final Rectangle searchRect = GeometryUtilities.toRectangle( envelope );

    final int[] resultIndex = new int[] { -1 };
    final TIntProcedure searcher = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        final Object element = get( index );

        if( FeatureLinkUtils.isSameOrLinkTo( targetFeature, element ) )
        {
          resultIndex[0] = index;
          return false;
        }

        return true;
      }
    };

    query( searchRect, searcher );

    return resultIndex[0];
  }

  /**
   * ATTENTION: Returns an unmodifiable iterator i.e. changing the list via the returned iterator results in an
   * exception.<br/>
   * The iterator is not synchronized, however.
   */
  @Override
  public synchronized ListIterator< ? > listIterator( final int index )
  {
    return new SplitSortIterator( this, index );
  }

  // JMSpatialIndex implementation

  @Override
  public List< ? > query( final GM_Position pos, final List result )
  {
    final Rectangle envelope = JTSUtilities.toRectangle( pos.getX(), pos.getY() );
    return query( envelope, result, false );
  }

  @Override
  public List< ? > query( final GM_Envelope queryEnv, final List result )
  {
    final Rectangle envelope = GeometryUtilities.toRectangle( queryEnv );
    return query( envelope, result, false );
  }

  @Override
  @SuppressWarnings( { "unchecked" } )
  public <T extends Feature> List<T> queryResolved( final GM_Position pos, final List<T> result )
  {
    final Rectangle envelope = JTSUtilities.toRectangle( pos.getX(), pos.getY() );
    return (List<T>)query( envelope, result, true );
  }

  @Override
  @SuppressWarnings( { "unchecked" } )
  public <T extends Feature> List<T> queryResolved( final GM_Envelope queryEnv, final List<T> result )
  {
    final Rectangle envelope = GeometryUtilities.toRectangle( queryEnv );
    return (List<T>)query( envelope, result, true );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  private List< ? > query( final Rectangle envelope, final List receiver, final boolean resolve )
  {
    final List<Object> result = receiver == null ? new ArrayList<>() : receiver;

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        result.add( resolve ? getResolved( index ) : get( index ) );
        return true;
      }
    };

    query( envelope, ip );

    return result;
  }

  private synchronized void query( final Rectangle envelope, final TIntProcedure ip )
  {
    createIndex();

    final Rectangle searchRect = envelope == null ? m_index.getBounds() : envelope;
    m_index.intersects( searchRect, ip );
  }

  @Override
  public synchronized GM_Envelope getBoundingBox( )
  {
    createIndex();

    final Rectangle bounds = m_index.getBounds();

    // REMARK: we assume that all elements of the split sort are always in Kalypso CRS
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    return GeometryUtilities.toEnvelope( bounds, crs );
  }

  @Override
  public synchronized void invalidate( final Object object )
  {
    if( m_index != null )
      m_index.invalidate( object );
  }
}