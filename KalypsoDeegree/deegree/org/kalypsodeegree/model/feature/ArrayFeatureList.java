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
package org.kalypsodeegree.model.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GM_Envelope_Impl;
import org.kalypsodeegree_impl.model.sort.AbstractFeatureList;
import org.kalypsodeegree_impl.model.sort.IEnvelopeProvider;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * A FeatureList backed by a simple ArrayList of Features. No spatial index is created.
 * Queries require an iteration over all features.
 * 
 * @author Stefan Kurzbach
 */
public class ArrayFeatureList extends AbstractFeatureList
{
  public static final int INITIAL_CAPACITY = 4;

  private final ArrayList<Object> m_items = new ArrayList<>( 4 );

  public ArrayFeatureList( Feature parentFeature, IRelationType parentFTP )
  {
    super( parentFeature, parentFTP );
  }

  public ArrayFeatureList( Feature parentFeature, IRelationType parentFTP, final IEnvelopeProvider envelopeProvider )
  {
    super( parentFeature, parentFTP, envelopeProvider );
  }

  /* List interface */

  /**
   * @see java.util.List#add(int, java.lang.Object)
   */
  @Override
  public void add( int index, Object object )
  {
    checkCanAdd( 1 );
    registerFeature( object );
    m_items.add( index, object );
  }

  /**
   * @see java.util.List#add(java.lang.Object)
   */
  @Override
  public boolean add( Object object )
  {
    checkCanAdd( 1 );
    registerFeature( object );
    return m_items.add( object );
  }

  /**
   * @see java.util.List#clear()
   */
  @Override
  public void clear( )
  {
    for( final Object element : m_items )
      unregisterFeature( element );
    m_items.clear();
  }

  /**
   * @see java.util.List#get(int)
   */
  @Override
  public Object get( int index )
  {
    return m_items.get( index );
  }

  /**
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Override
  public int indexOf( Object object )
  {
    return m_items.indexOf( object );
  }

  /**
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  @Override
  public int lastIndexOf( Object object )
  {
    return m_items.lastIndexOf( object );
  }

  /**
   * @see java.util.List#listIterator(int)
   */
  @Override
  public ListIterator listIterator( int index )
  {
    return m_items.listIterator( index );
  }

  /**
   * @see java.util.List#remove(int)
   */
  @Override
  public Object remove( int index )
  {
    final Object object = m_items.remove( index );
    unregisterFeature( object );
    return object;
  }

  /**
   * @see java.util.List#remove(java.lang.Object)
   */
  @Override
  public boolean remove( Object object )
  {
    final int index = indexOf( object );
    if( index == -1 )
      return false;
    remove( index );
    return true;
  }

  /**
   * @see java.util.List#size()
   */
  @Override
  public int size( )
  {
    return m_items.size();
  }

  /**
   * @see java.util.List#size()
   */
  @Override
  public synchronized Object[] toArray( Object[] a )
  {
    if( a == null || a.length != size() )
      a = new Object[size()];

    System.arraycopy( m_items, 0, a, 0, a.length );

    return a;
  }

  /* JMSpatialIndex interface */

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#getBoundingBox()
   */
  @Override
  public GM_Envelope getBoundingBox( )
  {
    final GM_Envelope[] boundingBoxes = new GM_Envelope[size()];
    for( int i = 0; i < boundingBoxes.length; i++ )
    {
      boundingBoxes[i] = getEnvelope( m_items.get( i ) );
    }
    return GeometryUtilities.mergeEnvelopes( boundingBoxes );
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Envelope, java.util.List)
   */
  @Override
  public List query( GM_Envelope envelope, List receiver )
  {
    final List<Object> result = receiver == null ? new ArrayList<>() : receiver;
    if( envelope == null )
    {
      result.addAll( m_items );
      return result;
    }

    for( Object object : m_items )
    {
      final GM_Envelope itemEnv = getEnvelope( object );
      if( envelope.intersects( itemEnv ) )
        result.add( object );
    }

    return result;
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Position, java.util.List)
   */
  @Override
  public List query( GM_Position pos, List receiver )
  {
    final GM_Envelope envelope = new GM_Envelope_Impl( pos.getX() - 1.0, pos.getY() - 1.0, pos.getX() + 1.0, pos.getY() + 1.0, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
    return query( envelope, receiver );
  }

}
