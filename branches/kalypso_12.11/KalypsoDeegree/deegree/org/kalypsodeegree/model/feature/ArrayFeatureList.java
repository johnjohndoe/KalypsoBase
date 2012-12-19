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
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
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
  public static final int DEFAULT_INITIAL_CAPACITY = 4;

  private final ArrayList<Object> m_items;

  public ArrayFeatureList( final Feature parentFeature, final IRelationType parentFTP )
  {
    this( parentFeature, parentFTP, null );
  }

  public ArrayFeatureList( final Feature parentFeature, final IRelationType parentFTP, final IEnvelopeProvider envelopeProvider )
  {
    this( parentFeature, parentFTP, envelopeProvider, DEFAULT_INITIAL_CAPACITY );
  }

  public ArrayFeatureList( final Feature parentFeature, final IRelationType parentFTP, final IEnvelopeProvider envelopeProvider, final int initialCapacity )
  {
    super( parentFeature, parentFTP, envelopeProvider );
    m_items = new ArrayList<>( initialCapacity );
  }

  /* List interface */

  @Override
  public void add( final int index, final Object object )
  {
    checkCanAdd( 1 );
    registerFeature( object );
    m_items.add( index, object );
  }

  /**
   * @see java.util.List#add(java.lang.Object)
   */
  @Override
  public boolean add( final Object object )
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
  public Object get( final int index )
  {
    return m_items.get( index );
  }

  /**
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Override
  public int indexOf( final Object object )
  {
    return m_items.indexOf( object );
  }

  /**
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  @Override
  public int lastIndexOf( final Object object )
  {
    return m_items.lastIndexOf( object );
  }

  /**
   * @see java.util.List#listIterator(int)
   */
  @Override
  public ListIterator listIterator( final int index )
  {
    return m_items.listIterator( index );
  }

  /**
   * @see java.util.List#remove(int)
   */
  @Override
  public Object remove( final int index )
  {
    final Object object = m_items.remove( index );
    unregisterFeature( object );
    return object;
  }

  /**
   * @see java.util.List#remove(java.lang.Object)
   */
  @Override
  public boolean remove( final Object object )
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
  public List query( final GM_Envelope envelope, final List receiver )
  {
    return queryInternal( envelope, receiver, false );
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Position, java.util.List)
   */
  @Override
  public List query( final GM_Position pos, final List receiver )
  {
    final GM_Envelope envelope = GeometryFactory.createGM_Envelope( pos.getX() - 1.0, pos.getY() - 1.0, pos.getX() + 1.0, pos.getY() + 1.0, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
    return queryInternal( envelope, receiver, false );
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Envelope, java.util.List)
   */
  @SuppressWarnings( { "unchecked" } )
  @Override
  public <T extends Feature> List<T> queryResolved( final GM_Envelope envelope, final List<T> receiver )
  {
    return (List<T>)queryInternal( envelope, receiver, true );
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Position, java.util.List)
   */
  @SuppressWarnings( { "unchecked" } )
  @Override
  public <T extends Feature> List<T> queryResolved( final GM_Position pos, final List<T> receiver )
  {
    final GM_Envelope envelope = GeometryFactory.createGM_Envelope( pos.getX() - 1.0, pos.getY() - 1.0, pos.getX() + 1.0, pos.getY() + 1.0, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
    return (List<T>)queryInternal( envelope, receiver, true );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  private List< ? > queryInternal( final GM_Envelope envelope, final List receiver, final boolean resolve )
  {
    final List<Object> result = (receiver == null) ? new ArrayList<>() : receiver;
    for( final Object object : m_items )
    {
      if( envelope == null || envelope.intersects( getEnvelope( object ) ) )
        result.add( resolve ? resolveFeature( object ) : object );
    }
    return result;
  }

}
