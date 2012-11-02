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
package org.kalypsodeegree_impl.model.sort;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntFunction;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

/**
 * Spatially index and hash elements for {@link SplitSort}.<br/>
 * Put into a separate class, so it can get separately instantiated. This is necessary for memory saving reasons.
 * 
 * @author Gernot Belger
 */
class SplitSortindex
{
  static final int INITIAL_CAPACITY = 16;

  private final Map<Object, TIntArrayList> m_itemIndex = new HashMap<>( INITIAL_CAPACITY );

  private final TIntObjectHashMap<Rectangle> m_itemEnvelopes = new TIntObjectHashMap<>( INITIAL_CAPACITY );

  private final TIntHashSet m_invalidIndices = new TIntHashSet( INITIAL_CAPACITY );

  private final SpatialIndex m_spatialIndex = new RTree();

  private final SplitSort m_parent;

  private int m_size = 0;

  public SplitSortindex( final SplitSort parent )
  {
    m_parent = parent;
    initIndex();
  }

  private void initIndex( )
  {
    m_spatialIndex.init( null );
    for( int i = 0; i < m_parent.size(); i++ )
      insert( m_parent.get( i ), i );
  }

  private void hashItem( final Object item, final int index )
  {
    TIntArrayList indexSet = m_itemIndex.get( item );
    if( indexSet == null )
    {
      indexSet = new TIntArrayList( 2 );
      m_itemIndex.put( item, indexSet );
    }
    indexSet.add( index );
    m_size++;
  }

  private void unhashItem( final Object item, final int index )
  {
    final TIntArrayList indexSet = m_itemIndex.get( item );
    if( indexSet == null )
    {
      throw new IllegalStateException();
    }
    final int size = indexSet.size();
    if( size == 1 )
      m_itemIndex.remove( item );
    else
      indexSet.remove( indexSet.indexOf( index ) );
    m_size--;
  }

  /**
   * Recreate the index, if it is <code>null</code>.<br/>
   */
  private synchronized void checkIndex( )
  {
    final TIntProcedure tp = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        revalidateItem( index );
        return true;
      }
    };
    m_invalidIndices.forEach( tp );
    m_invalidIndices.clear();
    Assert.isTrue( m_size == m_parent.size() );
    // Assert.isTrue( m_itemEnvelopes.size() == m_spatialIndex.size() );
  }

  protected synchronized void revalidateItem( final int index )
  {
    final Object item = m_parent.get( index );
    final GM_Envelope envelope = m_parent.getEnvelope( item );

    final Rectangle oldEnvelope = m_itemEnvelopes.remove( index );
    final Rectangle newEnvelope = GeometryUtilities.toRectangle( envelope );
    final boolean envelopeChanged = !ObjectUtils.equals( oldEnvelope, newEnvelope );

    /* Only update spatial index if envelope really changed */
    if( envelopeChanged )
    {
      /* Remove from index */
      if( oldEnvelope != null )
      {
        final boolean removed = m_spatialIndex.delete( oldEnvelope, index );
        Assert.isTrue( removed );
      }

      /* reinsert into index */
      if( newEnvelope != null )
      {
        /* remember the envelope of this item */
        m_itemEnvelopes.put( index, newEnvelope );

        /* put into index */
        m_spatialIndex.add( newEnvelope, index );
      }
    }
  }

  public void insert( final Object item, final int index )
  {
    /* mark as invalid, item gets inserted on next access */
    m_invalidIndices.add( index );
    hashItem( item, index );
  }

  /**
   * Fixes indices after elements are inserted / removed from the middle of the list.
   */
  void reindex( final int startIndex, final int offset )
  {
    // FIXME: iteration must be in reverse order of offset > 0
    for( int oldIndex = startIndex; oldIndex < m_parent.size(); oldIndex++ )
    {
      final Rectangle envelope = m_itemEnvelopes.get( oldIndex );
      final int newIndex = oldIndex + offset;

      /* fix spatial index */
      if( envelope != null )
      {
        final boolean removed = m_spatialIndex.delete( envelope, oldIndex );
        Assert.isTrue( removed );

        m_spatialIndex.add( envelope, newIndex );

        // FIXME: if offset is positive, valid envelopes will be overwritten!
        m_itemEnvelopes.put( newIndex, envelope );
      }
      else
      {
        // map newIndex to 'null' in this case i.e. remove from list
        m_itemEnvelopes.remove( newIndex );
      }
    }

    // VERIFY / FIXED: remove obsolete indices from m_itemEnvelopes
    // FIXME: will not work for positive offsets
    for( int index = m_parent.size() + offset; index < m_parent.size(); index++ )
      m_itemEnvelopes.remove( index );

    /* Fix invalid item indices */
    final int[] invalidIndices = m_invalidIndices.toArray();
    m_invalidIndices.clear();

    for( final int invalidIndex : invalidIndices )
    {
      if( invalidIndex < startIndex )
        m_invalidIndices.add( invalidIndex );
      else
        m_invalidIndices.add( invalidIndex + offset );
    }

    /* Fix item hash */
    final TIntFunction tf = new TIntFunction()
    {
      @Override
      public int execute( final int value )
      {
        if( value < startIndex )
          return value;
        else
          // VERIFY: use 'offset' instead of -1 -> difference to multiple reindex below!
          return value + offset;
      }
    };

    final Collection<TIntArrayList> values = m_itemIndex.values();
    for( final TIntArrayList indexSet : values )
    {
      indexSet.transformValues( tf );
    }
  }

// FIXME: this complicated method should not be duplicated! Has already lead to bugs, because they work in a different way
  // Idea: give interface that provides mapping between old/new indices
  public void reindex( final int startIndex, final int[] indexShifts )
  {
    for( int oldIndex = startIndex; oldIndex < startIndex + indexShifts.length; oldIndex++ )
    {
      // FIXME: different behaviour to single reindex method; probably bugfix above was not necessary with this remove
      final Rectangle envelope = m_itemEnvelopes.remove( oldIndex );

      /* fix spatial index */
      if( envelope != null )
      {
        final boolean removed = m_spatialIndex.delete( envelope, oldIndex );
        Assert.isTrue( removed );

        final int newIndex = oldIndex + indexShifts[oldIndex - startIndex];
        m_spatialIndex.add( envelope, newIndex );
        m_itemEnvelopes.put( newIndex, envelope );
      }
      else
      {
        // FIXME: see above
      }
    }

    // FIXME: remove obsolete from m_itemEnvelopes

    /* Fix invalid item indices */
    final int[] invalidIndices = m_invalidIndices.toArray();
    m_invalidIndices.clear();

    for( final int oldIndex : invalidIndices )
    {
      if( oldIndex < startIndex )
        m_invalidIndices.add( oldIndex );
      else
        m_invalidIndices.add( oldIndex + indexShifts[oldIndex - startIndex] );
    }

    /* Fix item hash */
    final TIntFunction tf = new TIntFunction()
    {
      @Override
      public int execute( final int oldIndex )
      {
        if( oldIndex < startIndex )
          return oldIndex;
        else
          return oldIndex + indexShifts[oldIndex - startIndex];
      }
    };

    final Collection<TIntArrayList> values = m_itemIndex.values();
    for( final TIntArrayList indexSet : values )
    {
      indexSet.transformValues( tf );
    }
  }

  void remove( final int index, final Object item )
  {
    Assert.isTrue( index >= 0 );

    /* really remove item and unregister it from all hashes */
    m_invalidIndices.remove( index );

    final Rectangle envelope = m_itemEnvelopes.remove( index );
    if( envelope != null )
    {
      final boolean removed = m_spatialIndex.delete( envelope, index );
      Assert.isTrue( removed );
    }

    unhashItem( item, index );
  }

  public Rectangle getBounds( )
  {
    checkIndex();
    return m_spatialIndex.getBounds();
  }

  public void intersects( final Rectangle searchRect, final TIntProcedure ip )
  {
    checkIndex();
    m_spatialIndex.intersects( searchRect, ip );
  }

  public void invalidate( final Object object )
  {
    final int index = indexOf( object );
    if( index == -1 )
      return;
    m_invalidIndices.add( index );
  }

  public int indexOf( final Object object )
  {
    final TIntArrayList indexSet = m_itemIndex.get( object );
    if( indexSet == null )
      return -1;
    return indexSet.getQuick( 0 );
  }

  public void dispose( )
  {
    m_itemEnvelopes.clear();
    m_invalidIndices.clear();
    m_itemIndex.clear();
  }

}