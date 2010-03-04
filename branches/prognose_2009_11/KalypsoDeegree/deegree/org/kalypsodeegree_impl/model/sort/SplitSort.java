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

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Envelope;

public class SplitSort implements FeatureList
{
  private final List<Object> m_items = new ArrayList<Object>();

  private final Feature m_parentFeature;

  private final IRelationType m_parentFeatureTypeProperty;

  private final IEnvelopeProvider m_envelopeProvider;

  private SpatialIndexExt m_index;

  /**
   * The constructor.
   * 
   * @param parentFeature
   *          The parent feature. May be null, if this list has no underlying workspace. Make sure parentFTP is also
   *          null then.
   * @param parentFTP
   *          The feature type of the parent. May be null, if this list has no underlying workspace. Make sure
   *          parentFeature is also null then.
   */
  public SplitSort( final Feature parentFeature, final IRelationType parentFTP )
  {
    this( parentFeature, parentFTP, null );
  }

  /**
   * The constructor.
   * 
   * @param parentFeature
   *          The parent feature. May be null, if this list has no underlying workspace. Make sure parentFTP is also
   *          null then.
   * @param parentFTP
   *          The feature type of the parent. May be null, if this list has no underlying workspace. Make sure
   *          parentFeature is also null then.
   * @param envelopeProvider
   *          The provider returns evenlopes. If null, the default one is used.
   */
  public SplitSort( final Feature parentFeature, final IRelationType parentFTP, final IEnvelopeProvider envelopeProvider )
  {
    m_parentFeature = parentFeature;
    m_parentFeatureTypeProperty = parentFTP;
    m_envelopeProvider = envelopeProvider == null ? new DefaultEnvelopeProvider( parentFeature ) : envelopeProvider;

    // Index is initially invalid. This is necessary, as loading the features adds them to this list,
    // but the features often have no envelope; so we rather wait for the first query.
    m_index = null;
  }

  private SpatialIndexExt createIndex( final Envelope env )
  {
    return new SplitSortSpatialIndex( env );
    // m_index = new QuadTreeIndex( env );
  }

  /**
   * Recreate the index, if it is <code>null</code>.
   */
  private void checkIndex( )
  {
    // HM: still dangerous: the calculation of the whole bbox is not synchronized, as we access 'getEnvelope' here
    // However, this is not thread safe...

    // What is the solution? Maybe never get the envelopes again? In that case we need to maintain an own Map
    // object->envelope?
    // But: we need the recalculate all envelopes if the whole index is invalidated...

    if( m_index == null )
    {
      // Recalculate the bounding box
      Envelope bbox = null;
      for( final Object item : m_items )
      {
        final Envelope env = getEnvelope( item );
        if( env != null )
          if( bbox == null )
            bbox = new Envelope( env );
          else
            bbox.expandToInclude( env );
      }

      synchronized( this )
      {
        // create index
        m_index = createIndex( bbox );
        // insert all elements
        for( final Object item : m_items )
        {
          final Envelope env = getEnvelope( item );
          m_index.insert( env, item );
        }
      }
    }
  }

  /**
   * @see java.util.List#add(java.lang.Object)
   */
  public boolean add( final Object object )
  {
    // TODO: not necessary if m_index is null, but calling this inside the synchronized block causes dead locks
    final Envelope env = getEnvelope( object );

    synchronized( this )
    {
      if( m_index != null )
        m_index.insert( env, object );
      return m_items.add( object );
    }
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Envelope,
   *      java.util.List)
   */
  public List< ? > query( final GM_Envelope queryEnv, final List result )
  {
    checkIndex();

    synchronized( this )
    {
      final Envelope env = JTSAdapter.export( queryEnv );

      final List< ? > list = m_index.query( env );
      if( result == null )
        return list;

      result.addAll( list );
      return result;
    }
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Position,
   *      java.util.List)
   */
  public List< ? > query( final GM_Position pos, final List result )
  {
    return query( GeometryFactory.createGM_Envelope( pos, pos, null ), result );
  }

  /**
   * This is slow: TODO: better comment
   * 
   * @see java.util.List#remove(java.lang.Object)
   */
  public boolean remove( final Object object )
  {
    final Envelope env = getEnvelope( object );

    synchronized( this )
    {
      // TODO: slow!
      final boolean removed = m_items.remove( object );

      if( m_index != null && env != null )
        m_index.remove( env, object );

      return removed;
    }
  }

  private Envelope getEnvelope( final Object object )
  {
    final GM_Envelope envelope = m_envelopeProvider.getEnvelope( object );
    return JTSAdapter.export( envelope );
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform)
   */
  public void paint( final Graphics g, final GeoTransform geoTransform )
  {
    checkIndex();

    synchronized( this )
    {
      m_index.paint( g, geoTransform );
    }
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#getBoundingBox()
   */
  // TODO: slow; check if we can improve it by maintaining the bbox in this class
  public GM_Envelope getBoundingBox( )
  {
    checkIndex();

    synchronized( this )
    {
      final Envelope bbox = m_index.getBoundingBox();
      if( bbox == null )
        return null;
      return JTSAdapter.wrap( bbox, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
    }
  }

  /**
   * @see java.util.List#size()
   */
  public int size( )
  {
    synchronized( this )
    {
      return m_items.size();
    }
  }

  /**
   * @see java.util.List#clear()
   */
  public void clear( )
  {
    synchronized( this )
    {
      m_items.clear();
      m_index = null;
    }
  }

  /**
   * @see java.util.List#isEmpty()
   */
  public boolean isEmpty( )
  {
    return size() == 0;
  }

  /**
   * @see java.util.List#toArray()
   */
  public Object[] toArray( )
  {
    synchronized( this )
    {
      return m_items.toArray( new Object[m_items.size()] );
    }
  }

  /**
   * @see java.util.List#get(int)
   */
  public Object get( final int index )
  {
    synchronized( this )
    {
      return m_items.get( index );
    }
  }

  /**
   * @see java.util.List#remove(int)
   */
  public Object remove( final int index )
  {
    synchronized( this )
    {
      final Object removedItem = m_items.remove( index );
      if( m_index != null )
    	// FIXME: We remove with null envelope here, else we would break the synchronized code by calling getEnvelope() here
        m_index.remove( null, removedItem );
      return removedItem;
    }
  }

  /**
   * @deprecated SLOW (as we are using ArrayList internally) TODO: better comment, make deprecated in interface
   * @see java.util.List#add(int, java.lang.Object)
   */
  @Deprecated
  public void add( final int index, final Object item )
  {
    final Envelope env = getEnvelope( item );

    synchronized( this )
    {
      m_items.add( index, item );
      if( m_index != null )
        m_index.insert( env, item );
    }
  }

  /**
   * @deprecated SLOW TODO: better comment, make deprecated in interface
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Deprecated
  public int indexOf( final Object item )
  {
    synchronized( this )
    {
      return m_items.indexOf( item );
    }
  }

  /**
   * @deprecated SLOW TODO: better comment, make deprecated in interface
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  @Deprecated
  public int lastIndexOf( final Object item )
  {
    synchronized( this )
    {
      return m_items.lastIndexOf( item );
    }
  }

  /**
   * @see java.util.List#contains(java.lang.Object)
   */
  public boolean contains( final Object item )
  {
    final Envelope env = getEnvelope( item );
    checkIndex();

    synchronized( this )
    {
      // TODO: slow, as the index may be recalculated, check if possible to avoid this
      return m_index.contains( env, item );
    }
  }

  /**
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  public boolean addAll( final int index, final Collection c )
  {
    // First get all envelope, in order no to break the synchronize code by calling getEnvelope inside
    final Map<Object, Envelope> newItems = new HashMap<Object, Envelope>();
    for( final Object item : c )
    {
      final Envelope envelope = getEnvelope( item );
      newItems.put( item, envelope );
    }

    synchronized( this )
    {
      final boolean added = m_items.addAll( index, c );
      if( m_index != null )
        for( final Entry<Object, Envelope> entry : newItems.entrySet() )
          m_index.insert( entry.getValue(), entry.getKey() );
      return added;
    }
  }

  /**
   * @see java.util.List#addAll(java.util.Collection)
   */
  public boolean addAll( final Collection c )
  {
    // First get all envelope, in order no to break the synchronize code by calling getEnvelope inside
    final Map<Object, Envelope> newItems = new HashMap<Object, Envelope>();
    for( final Object item : c )
    {
      final Envelope envelope = getEnvelope( item );
      newItems.put( item, envelope );
    }

    synchronized( this )
    {
      final boolean added = m_items.addAll( c );
      if( m_index != null )
        for( final Entry<Object, Envelope> entry : newItems.entrySet() )
          m_index.insert( entry.getValue(), entry.getKey() );
      return added;
    }
  }

  /**
   * @see java.util.List#containsAll(java.util.Collection)
   */
  public boolean containsAll( final Collection c )
  {
    // First get all envelope, in order no to break the synchronize code by calling getEnvelope inside
    final Map<Object, Envelope> newItems = new HashMap<Object, Envelope>();
    for( final Object item : c )
    {
      final Envelope envelope = getEnvelope( item );
      newItems.put( item, envelope );
    }

    checkIndex();

    synchronized( this )
    {
      // TODO: see contains()
      for( final Entry<Object, Envelope> entry : newItems.entrySet() )
        if( !m_index.contains( entry.getValue(), entry.getKey() ) )
          return false;
      return true;
    }
  }

  /**
   *  SLOW: TODO: better comment, make deprecated in interface! FAST TODO: check the comments
   * @see java.util.List#removeAll(java.util.Collection)
   */
  public boolean removeAll( final Collection c )
  {

    boolean result = false;
    synchronized( this )
    {
      for( final Object lObj : c )
      {
        final Envelope env = getEnvelope( lObj );
        if( m_index != null && env != null )
          m_index.remove( env, lObj );
      }
      result = m_items.removeAll( c );
    }
    
    // VERY SLOW! removes elements from ArrayList one by one
    // which causes re-creation and copying of the whole array every time
//    boolean result = false;
//    for( final Object object : c )
//      result |= remove( object );

    return result;
  }

  /**
   * NOT IMPLEMENTED
   * 
   * @see java.util.List#retainAll(java.util.Collection)
   */
  public boolean retainAll( final Collection c )
  {
    // TODO: implement
    throw new UnsupportedOperationException();
  }

  /**
   * ATTENTION: do not remove object via this iterator, it will break the geo-index<br>
   * TODO: wrap iterator in order to maintain the index's consistency
   * 
   * @see java.util.List#iterator()
   */
  public Iterator iterator( )
  {
    // TODO: what about synchronization?

    return m_items.iterator();
  }

  /**
   * NOT IMPLEMENTED
   * 
   * @see java.util.List#subList(int, int)
   */
  public List subList( final int fromIndex, final int toIndex )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * ATTENTION: do not remove object via this iterator, it will break the geo-index<br>
   * TODO: wrap iterator in order to maintain the index's consistency
   * 
   * @see java.util.List#listIterator()
   */
  public ListIterator listIterator( )
  {
    // TODO: what about synchronization?
    return m_items.listIterator();
  }

  /**
   * ATTENTION: do not remove object via this iterator, it will break the geo-index<br>
   * TODO: wrap iterator in order to maintain the index's consistency
   * 
   * @see java.util.List#listIterator(int)
   */
  public ListIterator listIterator( final int index )
  {
    // TODO: what about synchronization?
    return m_items.listIterator( index );
  }

  /**
   * @see java.util.List#set(int, java.lang.Object)
   */
  public Object set( final int index, final Object newItem )
  {
    final Envelope newEnv = getEnvelope( newItem );

    synchronized( this )
    {
      final Object oldItem = m_items.set( index, newItem );
      if( m_index != null )
      {
        // remove with null envelope, in order not to break the synchronization code by calling getEnvelope
        m_index.remove( null, oldItem );
        m_index.insert( newEnv, newItem );
      }
      return oldItem;
    }
  }

  /**
   * @see java.util.List#toArray(java.lang.Object[])
   */
  public Object[] toArray( final Object[] a )
  {
    synchronized( this )
    {
      return m_items.toArray( a );
    }
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#toFeatures()
   */
  public Feature[] toFeatures( )
  {
    synchronized( this )
    {
      // FIXME: this will probably not work, as the list may contain non-features
      return m_items.toArray( new Feature[m_items.size()] );
    }
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#accept(org.kalypsodeegree.model.feature.FeatureVisitor)
   */
  public void accept( final FeatureVisitor visitor )
  {
    accept( visitor, FeatureVisitor.DEPTH_INFINITE );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#accept(org.kalypsodeegree.model.feature.FeatureVisitor, int)
   */
  public void accept( final FeatureVisitor visitor, final int depth )
  {
    // TODO: not synchronized: Problem?
    final Feature parentFeature = getParentFeature();
    final GMLWorkspace workspace = parentFeature == null ? null : parentFeature.getWorkspace();
    for( final Object object : m_items )
      if( workspace != null && depth == FeatureVisitor.DEPTH_INFINITE_LINKS )
      {
        final Feature linkedFeature = FeatureHelper.resolveLinkedFeature( workspace, object );
        visitor.visit( linkedFeature );
      }
      else if( object instanceof Feature )
        visitor.visit( (Feature) object );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#getParentFeature()
   */
  public Feature getParentFeature( )
  {
    return m_parentFeature;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#getParentFeatureTypeProperty()
   */
  public IRelationType getParentFeatureTypeProperty( )
  {
    return m_parentFeatureTypeProperty;
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#invalidate()
   */
  public void invalidate( )
  {
    synchronized( this )
    {
      m_index = null;
    }
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#invalidate(java.lang.Object)
   */
  public void invalidate( final Object o )
  {
    synchronized( this )
    {
      if( m_index != null )
      {
        final Envelope envelope = getEnvelope( o );
        m_index.remove( null, o );

        m_index.insert( envelope, o );
      }
    }
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#first()
   */
  public Object first( )
  {
    synchronized( this )
    {
      if( size() == 0 )
        return null;
      return m_items.get( 0 );
    }
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#searchFeatures(org.kalypsodeegree.model.geometry.GM_Object)
   */
  @Override
  public List<Feature> searchFeatures( final GM_Object geometry )
  {
    final Feature parentFeature = getParentFeature();
    final GMLWorkspace workspace = parentFeature == null ? null : parentFeature.getWorkspace();

    final List< ? > query = query( geometry.getEnvelope(), null );

    final List<Feature> result = new LinkedList<Feature>();
    for( final Object object : query )
    {
      final Feature feature = FeatureHelper.resolveLinkedFeature( workspace, object );

      final GM_Object[] geometryPropertyValues = feature.getGeometryPropertyValues();
      for( final GM_Object gmObject : geometryPropertyValues )
      {
        if( gmObject != null && gmObject.intersects( geometry ) )
        {
          result.add( feature );
          break;
        }
      }
    }

    return result;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName)
   */
  @Override
  public Feature addNew( final QName newChildType )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName, java.lang.String)
   */
  @Override
  public Feature addNew( final QName newChildType, final String newFeatureId )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName, java.lang.Class)
   */
  @Override
  public <T extends Feature> T addNew( final QName newChildType, final Class<T> classToAdapt )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName, java.lang.String,
   *      java.lang.Class)
   */
  @Override
  public <T extends Feature> T addNew( final QName newChildType, final String newFeatureId, final Class<T> classToAdapt )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addRef(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public <T extends Feature> boolean addRef( final T toAdd ) throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName)
   */
  @Override
  public Feature insertNew( final int index, final QName newChildType )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.String)
   */
  @Override
  public Feature insertNew( final int index, final QName newChildType, final String newFeatureId )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.Class)
   */
  @Override
  public <T extends Feature> T insertNew( final int index, final QName newChildType, final Class<T> classToAdapt )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.String,
   *      java.lang.Class)
   */
  @Override
  public <T extends Feature> T insertNew( final int index, final QName newChildType, final String newFeatureId, final Class<T> classToAdapt )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.String,
   *      java.lang.Class, java.lang.Object[])
   */
  @Override
  public <T extends Feature> T insertNew( final int index, final QName newChildType, final String newFeatureId, final Class<T> classToAdapt, final Object[] properties )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertRef(int, org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public <T extends Feature> boolean insertRef( final int index, final T toAdd ) throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return false;
  }

}