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

import gnu.trove.TIntFunction;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntProcedure;
import gnu.trove.TObjectIntHashMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

/**
 * Index elements for {@link SplitSort}.<br/>
 * Put into a spearate class, so it can get separately instantiated. This is necessary for memory saving reasons.
 * 
 * @author Gernot Belger
 */
class SplitSortindex
{
  private final TObjectIntHashMap<Object> m_itemIndex = new TObjectIntHashMap<>();

  private final TIntHashSet m_invalidIndices = new TIntHashSet();

  private final SpatialIndex m_spatialIndex = new RTree();

  private final SplitSort m_parent;

  public SplitSortindex( final SplitSort parent )
  {
    m_parent = parent;

    m_spatialIndex.init( null );

    initIndex();
  }

  private void initIndex( )
  {
    final List<SplitSortItem> items = m_parent.getItems();
    for( int i = 0; i < items.size(); i++ )
    {
      final SplitSortItem item = items.get( i );
      insert( item, i );
    }
  }

  /**
   * Recreate the index, if it is <code>null</code>.<br/>
   */
  private synchronized void checkIndex( )
  {
    final TIntHashSet x = m_invalidIndices;

    final TIntProcedure tp = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        revalidateItem( index );
        return true;
      }
    };

    x.forEach( tp );

    m_invalidIndices.clear();
  }

  protected synchronized void revalidateItem( final int index )
  {
    final List<SplitSortItem> items = m_parent.getItems();

    final SplitSortItem invalidItem = items.get( index );

    final GM_Envelope envelope = m_parent.getEnvelope( invalidItem.getData() );
    final Rectangle newEnvelope = GeometryUtilities.toRectangle( envelope );

    final Rectangle oldEnvelope = invalidItem.getEnvelope();

    final boolean envelopeChanged = invalidItem.setEnvelope( newEnvelope );

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
        m_spatialIndex.add( newEnvelope, index );
    }
  }

  public void insert( final SplitSortItem item, final int index )
  {
    /* mark as invalid, item gets inserted on next access */
    m_invalidIndices.add( index );

    /* hash object against its id for fast lookup */
    m_itemIndex.put( item.getData(), index );
  }

  /**
   * Fixes indices after elements are inserted / removed from the middle of the list.
   */
  void reindex( final int startIndex, final int offset )
  {
    // FIXME: slow!

    final List<SplitSortItem> items = m_parent.getItems();

    for( int oldIndex = startIndex; oldIndex < items.size(); oldIndex++ )
    {
      final SplitSortItem item = items.get( oldIndex );

      final int newIndex = oldIndex + offset;

      /* fix spatial index */
      final Rectangle envelope = item.getEnvelope();
      if( envelope != null )
      {
        final boolean removed = m_spatialIndex.delete( envelope, oldIndex );
        Assert.isTrue( removed );

        m_spatialIndex.add( envelope, newIndex );
      }
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
          return value + offset;
      }
    };
    m_itemIndex.transformValues( tf );

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
  }

  public void reindex( final int[] startIndices, final int[] offsets )
  {
    final List<SplitSortItem> items = m_parent.getItems();

    // sort startIndices and corresponding offsets in ascending order
    final Integer[] sortOrder = new Integer[offsets.length];
    for( int i = 0; i < sortOrder.length; i++ )
    {
      sortOrder[i] = i;
    }
    Arrays.sort( sortOrder, new Comparator<Integer>()
    {
      @Override
      public int compare( final Integer i1, final Integer i2 )
      {
        return startIndices[i1] - startIndices[i2];
      }
    } );

    // build corresponding cumulative sum of offsets
    final int[] cumsum = new int[offsets.length];
    cumsum[sortOrder[0]] = offsets[sortOrder[0]];
    for( int j = 1; j < offsets.length; j++ )
    {
      cumsum[sortOrder[j]] = offsets[sortOrder[j]] + cumsum[sortOrder[j - 1]];
    }

    final int startIndex = startIndices[sortOrder[0]]; // smallest index
    final int numIndexShifts = items.size() - startIndex;
    final int[] indexShifts = new int[numIndexShifts];
    // loop from startIndex (i=0) to items.size()-1 (i=numIndexShifts-1)
    for( int j = 0, i = 0; i < numIndexShifts; i++ )
    {
      if( j < (sortOrder.length - 1) && i + startIndex >= startIndices[sortOrder[j + 1]] )
        j++; // advance to next cumsum
      indexShifts[i] = cumsum[sortOrder[j]];
    }

    for( int oldIndex = startIndex; oldIndex < items.size(); oldIndex++ )
    {
      final SplitSortItem item = items.get( oldIndex );
      final int newIndex = oldIndex + indexShifts[oldIndex - startIndex];

      /* fix spatial index */
      final Rectangle envelope = item.getEnvelope();
      if( envelope != null )
      {
        final boolean removed = m_spatialIndex.delete( envelope, oldIndex );
        //Assert.isTrue( removed );

        m_spatialIndex.add( envelope, newIndex );
      }
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
    m_itemIndex.transformValues( tf );

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
  }

  void remove( final SplitSortItem item, final int index )
  {
    Assert.isNotNull( item );

    final Object object = item.getData();

    /* really remove item and unregister it from all hashes */
    m_invalidIndices.remove( index );

    // FIXME: check, what happens if object is contained in this list more than once? Is there a way to avoid this?
    m_itemIndex.remove( object );

    final Rectangle envelope = item.getEnvelope();
    if( envelope != null )
      m_spatialIndex.delete( envelope, index );
  }

  public int indexOf( final Object object )
  {
    final int index = m_itemIndex.get( object );
    // REMARK: strange, trove returns '0' for unknown element, but '0' may be a valid value of our mapping...
    if( index == 0 && !m_itemIndex.containsKey( object ) )
      return -1;

    return index;
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

}