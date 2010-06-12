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
package org.kalypsodeegree.model.feature;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * this featurelist cascades serveral lists, so it is possible to merge other lists without resorting or copying
 * listcontents <br>
 * this featurelist is <b>readonly </b>
 * 
 * @deprecated Only used for relation editing stuff, which also should be used no more...
 * @author doemming
 */
@Deprecated
public class CascadingFeatureList implements FeatureList
{
  private final FeatureList[] m_lists;

  public CascadingFeatureList( final FeatureList[] lists )
  {
    m_lists = lists;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#toFeatures()
   */
  @Override
  public Feature[] toFeatures( )
  {
    final List<Feature> result = new ArrayList<Feature>();
    for( final FeatureList element : m_lists )
      result.addAll( Arrays.asList( element.toFeatures() ) );

    return result.toArray( new Feature[result.size()] );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#accept(org.kalypsodeegree.model.feature.FeatureVisitor)
   */
  @Override
  public void accept( final FeatureVisitor visitor )
  {
    accept( visitor, FeatureVisitor.DEPTH_INFINITE );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#accept(org.kalypsodeegree.model.feature.FeatureVisitor, int)
   */
  @Override
  public void accept( final FeatureVisitor visitor, final int depth )
  {
    throw new NotImplementedException();
  }

  /**
   * @see java.util.Collection#size()
   */
  @Override
  public int size( )
  {
    int result = 0;
    for( final FeatureList element : m_lists )
    {
      result += element.size();
    }
    return result;
  }

  /**
   * @see java.util.Collection#clear()
   */
  @Override
  public void clear( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.Collection#isEmpty()
   */
  @Override
  public boolean isEmpty( )
  {
    return size() == 0;
  }

  /**
   * @see java.util.Collection#toArray()
   */
  @Override
  public Object[] toArray( )
  {
    return toFeatures();
  }

  /**
   * @see java.util.List#get(int)
   */
  @Override
  public Object get( final int index )
  {
    int c = 0;
    for( final FeatureList element : m_lists )
    {
      final int size = element.size();
      if( c + size < index )
      {
        c += size;
      }
      else
        return element.get( index - c );
    }
    return null;
  }

  /**
   * @see java.util.List#remove(int)
   */
  @Override
  public Object remove( final int index )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#add(int, java.lang.Object)
   */
  @Override
  public void add( final int index, final Object element )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Override
  public int indexOf( final Object o )
  {
    int c = 0;
    for( final FeatureList element : m_lists )
    {
      final int index = element.indexOf( o );
      if( index >= 0 )
        return index + c;
      c += element.size();
    }
    return -1;
  }

  /**
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  @Override
  public int lastIndexOf( final Object o )
  {
    int result = -1;
    int c = 0;
    for( final FeatureList element : m_lists )
    {
      final int index = element.indexOf( o );
      if( index >= 0 )
      {
        result = index + c;
      }
      c += element.size();
    }
    return result;
  }

  /**
   * @see java.util.Collection#add(java.lang.Object)
   */
  @Override
  public boolean add( final Object o )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.Collection#contains(java.lang.Object)
   */
  @Override
  public boolean contains( final Object o )
  {
    for( final FeatureList element : m_lists )
    {
      if( element.contains( o ) )
        return true;
    }
    return false;
  }

  /**
   * @see java.util.Collection#remove(java.lang.Object)
   */
  @Override
  public boolean remove( final Object o )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  @Override
  public boolean addAll( final int index, final Collection c )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  @Override
  public boolean addAll( final Collection c )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.Collection#containsAll(java.util.Collection)
   */
  @Override
  public boolean containsAll( final Collection c )
  {
    Collection left = c;// new ArrayList();
    for( int i = 0; i < m_lists.length; i++ )
    {
      final Collection stillLeft = new ArrayList();
      for( final Iterator iter = left.iterator(); iter.hasNext(); )
      {
        final Object object = iter.next();
        if( !m_lists[i].contains( object ) )
        {
          stillLeft.add( object );
        }
      }
      if( stillLeft.isEmpty() )
        return true;
      left = stillLeft;
    }
    return false;
  }

  /**
   * @see java.util.Collection#removeAll(java.util.Collection)
   */
  @Override
  public boolean removeAll( final Collection c )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.Collection#retainAll(java.util.Collection)
   */
  @Override
  public boolean retainAll( final Collection c )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.Collection#iterator()
   */
  @Override
  public Iterator< ? > iterator( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#subList(int, int)
   */
  @Override
  public List< ? > subList( final int fromIndex, final int toIndex )
  {
    // could be implemented
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#listIterator()
   */
  @Override
  public ListIterator< ? > listIterator( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#listIterator(int)
   */
  @Override
  public ListIterator< ? > listIterator( final int index )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#set(int, java.lang.Object)
   */
  @Override
  public Object set( final int index, final Object element )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.Collection#toArray(java.lang.Object[])
   */
  @Override
  public Object[] toArray( final Object[] a )
  {
    try
    {
      final Object[] objects = toArray();
      if( objects.length != a.length )
        throw new ArrayStoreException( "wrong length" );
      for( int i = 0; i < objects.length; i++ )
      {
        a[i] = objects[i];
      }
    }
    catch( final Exception e )
    {
      throw new ArrayStoreException( e.getMessage() );
    }
    return a;
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Envelope,
   *      java.util.List)
   */
  @Override
  public List< ? > query( final GM_Envelope env, List result )
  {
    if( result == null )
    {
      result = new ArrayList();
    }
    for( final FeatureList element : m_lists )
    {
      result = element.query( env, result );
    }
    return result;
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#query(org.kalypsodeegree.model.geometry.GM_Position,
   *      java.util.List)
   */
  @Override
  public List< ? > query( final GM_Position env, List result )
  {
    if( result == null )
      result = new ArrayList<Object>();

    for( final FeatureList element : m_lists )
      result = element.query( env, result );

    return result;
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#paint(java.awt.Graphics,
   *      org.kalypsodeegree.graphics.transformation.GeoTransform)
   */
  @Override
  public void paint( final Graphics g, final GeoTransform geoTransform )
  {
    for( final FeatureList element : m_lists )
    {
      element.paint( g, geoTransform );
    }
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#getBoundingBox()
   */
  @Override
  public GM_Envelope getBoundingBox( )
  {
    GM_Envelope result = null;
    for( final FeatureList element : m_lists )
    {
      final GM_Envelope boundingBox = element.getBoundingBox();
      if( result == null )
      {
        result = boundingBox;
      }
      else
      {
        result = result.getMerged( boundingBox );
      }
    }
    return result;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#getParentFeature()
   * @return null, as this are mixed lists
   */
  @Override
  public Feature getParentFeature( )
  {
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#getParentFeatureTypeProperty()
   * @return null, as this are mixed lists
   */
  @Override
  public IRelationType getParentFeatureTypeProperty( )
  {
    return null;
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#invalidate()
   */
  @Override
  public void invalidate( )
  {
    for( final FeatureList list : m_lists )
    {
      list.invalidate();
    }
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#invalidate(java.lang.Object)
   */
  @Override
  public void invalidate( final Object o )
  {
    for( final FeatureList list : m_lists )
    {
      list.invalidate( o );
    }
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#first()
   */
  @Override
  public Object first( )
  {
    if( m_lists.length == 0 )
      return null;

    return m_lists[0].first();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#searchFeatures(org.kalypsodeegree.model.geometry.GM_Object)
   */
  @Override
  public List<Feature> searchFeatures( final GM_Object geometry )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName)
   */
  @Override
  public Feature addNew( final QName newChildType )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName, java.lang.String)
   */
  @Override
  public Feature addNew( final QName newChildType, final String newFeatureId )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName, java.lang.Class)
   */
  @Override
  public <T extends Feature> T addNew( final QName newChildType, final Class<T> classToAdapt )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addNew(javax.xml.namespace.QName, java.lang.String,
   *      java.lang.Class)
   */
  @Override
  public <T extends Feature> T addNew( final QName newChildType, final String newFeatureId, final Class<T> classToAdapt )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addRef(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public <T extends Feature> boolean addRef( final T toAdd ) throws IllegalArgumentException
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName)
   */
  @Override
  public Feature insertNew( final int index, final QName newChildType )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.String)
   */
  @Override
  public Feature insertNew( final int index, final QName newChildType, final String newFeatureId )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.Class)
   */
  @Override
  public <T extends Feature> T insertNew( final int index, final QName newChildType, final Class<T> classToAdapt )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.String,
   *      java.lang.Class)
   */
  @Override
  public <T extends Feature> T insertNew( final int index, final QName newChildType, final String newFeatureId, final Class<T> classToAdapt )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertNew(int, javax.xml.namespace.QName, java.lang.String,
   *      java.lang.Class, java.lang.Object[])
   */
  @Override
  public <T extends Feature> T insertNew( final int index, final QName newChildType, final String newFeatureId, final Class<T> classToAdapt, final Object[] properties )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertRef(int, org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public <T extends Feature> boolean insertRef( final int index, final T toAdd ) throws IllegalArgumentException
  {
    throw new UnsupportedOperationException();
  }
}