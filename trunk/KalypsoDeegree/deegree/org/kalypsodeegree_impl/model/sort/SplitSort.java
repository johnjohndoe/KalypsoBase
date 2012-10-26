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
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.infomatiq.jsi.Rectangle;

public class SplitSort extends AbstractFeatureList
{
  /* Items of this list */
  private final List<Object> m_items = new ArrayList<>();

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
  public synchronized boolean add( final Object object )
  {
    checkCanAdd( 1 );
    createItem( object, size() );
    final boolean result = m_items.add( object );
    return result;
  }

  @Override
  public synchronized void add( final int index, final Object object )
  {
    checkCanAdd( 1 );

    if( m_index != null )
      m_index.reindex( index, 1 );

    createItem( object, index );
    m_items.add( index, object );
  }

  @Override
  public synchronized boolean addAll( final int index, final Collection c )
  {
    checkCanAdd( c.size() );

    if( m_index != null )
      m_index.reindex( index, c.size() );

    final int count = 0;
    for( final Object object : c )
      createItem( object, index + count );

    final boolean result = m_items.addAll( index, c );
    return result;
  }

  @Override
  public synchronized Object set( final int index, final Object newObject )
  {
    final Object oldItem = m_items.get( index );

    removeItem( oldItem, index );
    createItem( newObject, index );
    m_items.set( index, newObject );
    return oldItem;
  }

  private void removeItem( final Object data, final int index )
  {
    if( m_index != null )
      m_index.remove( index, data );

    // REMARK: it is a bit unclear what happens if we have a real Feature (not a link) multiple times in the same list.
    // We only unregister the it if the last occurrence is removed, assuming, that the feature is really no longer used.
    unregisterFeature( data );
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

    final Object item = m_items.get( index );
    removeItem( item, index );

    if( m_index != null )
      m_index.reindex( index + 1, -1 );

    m_items.remove( index );
    return true;
  }

  @Override
  public synchronized Object remove( final int index )
  {
    final Object item = m_items.get( index );
    removeItem( item, index );

    if( m_index != null )
      m_index.reindex( index + 1, -1 );

    m_items.remove( index );
    return item;
  }

  @Override
  public synchronized boolean removeAll( final Collection c )
  {
    final TIntArrayList indices = new TIntArrayList( c.size() );

    for( final Object object : c )
    {
      final int index = indexOf( object );
      if( index != -1 )
      {
        indices.add( index + 1 ); // reindex starting one after removed element
        removeItem( object, index );
      }
    }
    indices.sort();

    final int[] startIndices = indices.toNativeArray();

    // build array of all shifts
    final int startIndex = startIndices[0]; // smallest index
    final int numIndexShifts = m_items.size() - startIndex;
    final int[] indexShifts = new int[numIndexShifts];
    // loop from startIndex (i=0) to m_itemEnvelopes.size()-1 (i=numIndexShifts-1)
    for( int j = 0, i = 0; i < numIndexShifts; i++ )
    {
      if( j < (startIndices.length - 1) && i + startIndex >= startIndices[j + 1] )
        j++; // advance to next index
      indexShifts[i] = -1 * (j + 1);
    }

    if( m_index != null )
      m_index.reindex( startIndex, indexShifts );

    // remove all items
    // correct startIndices by - 1 to find the indices of items to be removed
    if( startIndices[startIndices.length - 1] - startIndices[0] == startIndices.length - 1 )
    {
      // optimization for deleting a range of items
      m_items.subList( startIndices[0] - 1, startIndices[startIndices.length - 1] ).clear();
    }
    else
    {
      final ListIterator<Object> listIterator = m_items.listIterator( startIndices[0] - 1 );
      int i = 0; // loop over startIndices
      while( listIterator.hasNext() )
      {
        if( listIterator.nextIndex() < startIndices[i] - i - 1 )
          continue;
        // this is the item at startIndices[i] - 1
        listIterator.next();
        listIterator.remove();
        i++;
      }
    } // else
//      m_items.remove( startIndices[0] - 1 );
//      for( int i = 1; i < startIndices.length; i++ )
//        m_items.remove( startIndices[i] - i - 1 );
    return !indices.isEmpty();
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

    m_index.dispose();
    m_index = null;
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
    final Rectangle envelope = new Rectangle( (float)pos.getX() - 1.0f, (float)pos.getY() - 1.0f, (float)pos.getX() + 1.0f, (float)pos.getY() + 1.0f );
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
    final Rectangle envelope = new Rectangle( (float)pos.getX() - 1.0f, (float)pos.getY() - 1.0f, (float)pos.getX() + 1.0f, (float)pos.getY() + 1.0f );
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
  private synchronized List< ? > query( final Rectangle envelope, final List receiver, final boolean resolve )
  {
    createIndex();

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

    final Rectangle searchRect = envelope == null ? m_index.getBounds() : envelope;
    m_index.intersects( searchRect, ip );

    return result;
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