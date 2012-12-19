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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.kalypso.contribs.javax.xml.namespace.QNameUnique;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.FilteredFeatureVisitor;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.FeatureTypeFilter;
import org.kalypsodeegree_impl.model.feature.visitors.CollectorVisitor;

/**
 * Eine gefilterte FeatureListe. Die Liste zeigt nach aussen nur die Features, die einem bestimmten IFeatureType
 * entsprechen. Andererseits ist die Liste aber durch die originale Liste gebackupd, d.h. alle Änderungen dieser Liste
 * ändern auch die Originalliste.
 * 
 * @author belger
 */
public class FilteredFeatureList implements FeatureList
{
  private final FeatureList m_original;

  private final FeatureTypeFilter m_predicate;

  public FilteredFeatureList( final FeatureList original, final QName filterQName, final boolean acceptIfSubstituting )
  {
    m_original = original;

    final QNameUnique uniqueFilterQName = QNameUnique.create( filterQName );

    m_predicate = new FeatureTypeFilter( uniqueFilterQName, uniqueFilterQName.asLocal(), acceptIfSubstituting );
  }

  /**
   * not implemented
   */
  @Override
  public <T extends Feature> T[] toFeatures( final T[] features )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature[] toFeatures( )
  {
    final CollectorVisitor collector = new CollectorVisitor( m_predicate );
    m_original.accept( collector, FeatureVisitor.DEPTH_INFINITE_LINKS );
    return collector.getResults( true );
  }

  @Override
  public void accept( final FeatureVisitor visitor )
  {
    accept( visitor, FeatureVisitor.DEPTH_INFINITE );
  }

  @Override
  public void accept( final FeatureVisitor visitor, final int depth )
  {
    final FilteredFeatureVisitor filterVisitor = new FilteredFeatureVisitor( visitor, m_predicate );
    m_original.accept( filterVisitor );
  }

  @Override
  public int size( )
  {
    return toFeatures().length;
  }

  @Override
  public void clear( )
  {
    final Feature[] features = toFeatures();
    for( final Feature element : features )
    {
      m_original.remove( element );
    }
  }

  @Override
  public boolean isEmpty( )
  {
    return toFeatures().length == 0;
  }

  @Override
  public Object[] toArray( )
  {
    return toFeatures();
  }

  @Override
  public Object get( final int index )
  {
    return toFeatures()[index];
  }

  @Override
  public Object remove( final int index )
  {
    final Object object = get( index );
    if( m_original.remove( object ) )
      return object;

    return null;
  }

  @Override
  public void add( final int index, final Object element )
  {
    // geht nicht?
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf( final Object o )
  {
    final Object[] objects = toArray();
    for( int i = 0; i < objects.length; i++ )
    {
      if( o == objects[i] )
        return i;
    }
    return -1;
  }

  @Override
  public int lastIndexOf( final Object o )
  {
    final Object[] objects = toArray();
    for( int i = objects.length - 1; i > -1; i-- )
    {
      if( o == objects[i] )
        return i;
    }

    return -1;
  }

  @Override
  public boolean add( final Object o )
  {
    if( !m_predicate.matchesType( (Feature)o ) )
      throw new IllegalArgumentException();

    return m_original.add( o );
  }

  @Override
  public boolean contains( final Object o )
  {
    return m_original.contains( o );
  }

  @Override
  public boolean remove( final Object o )
  {
    if( m_predicate.matchesType( (Feature)o ) )
      return m_original.remove( o );

    return false;
  }

  @Override
  public void removeAll( final int[] indices )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll( final int index, final Collection c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll( final Collection c )
  {
    for( final Iterator< ? > cIt = c.iterator(); cIt.hasNext(); )
    {
      add( cIt.next() );
    }

    return !c.isEmpty();
  }

  @Override
  public boolean containsAll( final Collection c )
  {
    for( final Iterator< ? > cIt = c.iterator(); cIt.hasNext(); )
    {
      final Object f = cIt.next();
      if( !contains( f ) )
        return false;
    }

    return true;
  }

  @Override
  public boolean removeAll( final Collection c )
  {
    for( final Iterator< ? > cIt = c.iterator(); cIt.hasNext(); )
    {
      final Object f = cIt.next();
      remove( f );
    }

    return !c.isEmpty();
  }

  @Override
  public boolean retainAll( final Collection c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator iterator( )
  {
    return listIterator();
  }

  @Override
  public List subList( final int fromIndex, final int toIndex )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator listIterator( )
  {
    return listIterator( 0 );
  }

  @Override
  public ListIterator listIterator( final int index )
  {
    if( index < 0 || index > size() )
      throw new IndexOutOfBoundsException();

    return new ArrayIterator( index, toFeatures() );
  }

  @Override
  public Object set( final int index, final Object element )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray( final Object[] a )
  {
    final Feature[] toFeatures = toFeatures();
    if( a == null || a.length < toFeatures.length )
      return toFeatures;

    System.arraycopy( toFeatures, 0, a, 0, toFeatures.length );

    return a;
  }

  @Override
  public List query( final GM_Envelope env, final List result )
  {
    return filterList( m_original.query( env, result ), result );
  }

  @Override
  public <T extends Feature> List<T> queryResolved( final GM_Envelope env, final List<T> result )
  {
    return filterList( m_original.queryResolved( env, result ), result );
  }

  private <T> List<T> filterList( final List<T> originalList, final List<T> result )
  {
    final int oldlength = result == null ? 0 : result.size();

    // only remove new elements, which do not match type
    final List<T> sublist = originalList.subList( oldlength, originalList.size() );
    final List<T> lListActualResult = new ArrayList<>();
    lListActualResult.addAll( originalList.subList( 0, oldlength ) );
    for( final T lObjNext : sublist )
    {
      final Feature f = FeatureHelper.resolveLinkedFeature( m_original.getOwner().getWorkspace(), lObjNext );
      if( m_predicate.matchesType( f ) )
      {
        lListActualResult.add( lObjNext );
        // removing elements from ArrayList is SLOW!
        // instead of removing elements from an existing list one by one,
        // it's better to create a new ArrayList on the fly, then
        // sIt.remove();
      }
    }

    return lListActualResult;
  }

  @Override
  public List query( final GM_Position env, final List result )
  {
    return filterList( m_original.query( env, result ), result );
  }

  @Override
  public <T extends Feature> List<T> queryResolved( final GM_Position env, final List<T> result )
  {
    return filterList( m_original.queryResolved( env, result ), result );
  }

  @Override
  public void paint( final Graphics g, final GeoTransform geoTransform )
  {
    m_original.paint( g, geoTransform );
  }

  @Override
  public GM_Envelope getBoundingBox( )
  {
    // zu gross!
    return m_original.getBoundingBox();
  }

  @Override
  public Feature getOwner( )
  {
    return m_original.getOwner();
  }

  @Override
  public IRelationType getPropertyType( )
  {
    return m_original.getPropertyType();
  }

  @Override
  public void invalidate( final Object o )
  {
    m_original.invalidate( o );
  }

  @Override
  public QName getName( )
  {
    return getPropertyType().getQName();
  }

  @Override
  public Object getValue( )
  {
    return this;
  }

  @Override
  public void setValue( final Object value )
  {
    throw new UnsupportedOperationException();
  }

  // TODO (for all addLink, insertLink implementations): should we only allow elements that may go into this list?

  @Override
  public int indexOfLink( final Feature targetFeature )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Feature> IXLinkedFeature addLink( final T toAdd ) throws IllegalArgumentException
  {
    return m_original.addLink( toAdd );
  }

  @Override
  public IXLinkedFeature addLink( final String href ) throws IllegalArgumentException
  {
    return m_original.addLink( href );
  }

  @Override
  public IXLinkedFeature addLink( final String href, final QName featureTypeName ) throws IllegalArgumentException
  {
    return m_original.addLink( href, featureTypeName );
  }

  @Override
  public IXLinkedFeature addLink( final String href, final IFeatureType featureType ) throws IllegalArgumentException
  {
    return m_original.addLink( href, featureType );
  }

  @Override
  public <T extends Feature> IXLinkedFeature insertLink( final int index, final T toLink ) throws IllegalArgumentException
  {
    return m_original.insertLink( index, toLink );
  }

  @Override
  public IXLinkedFeature insertLink( final int index, final String href ) throws IllegalArgumentException
  {
    return m_original.insertLink( index, href );
  }

  @Override
  public IXLinkedFeature insertLink( final int index, final String href, final QName featureTypeName ) throws IllegalArgumentException
  {
    return m_original.insertLink( index, href, featureTypeName );
  }

  @Override
  public IXLinkedFeature insertLink( final int index, final String href, final IFeatureType featureType ) throws IllegalArgumentException
  {
    return m_original.insertLink( index, href, featureType );
  }

  @Override
  public boolean removeLink( final Feature targetFeature )
  {
    return m_original.removeLink( targetFeature );
  }

  @Override
  public int removeLinks( final Feature[] targetFeatures )
  {
    return m_original.removeLinks( targetFeatures );
  }

  @Override
  public boolean containsLinkTo( final Feature targetFeature )
  {
    for( final Feature property : toFeatures() )
    {
      // FIXME: cannot work!
      if( property.equals( targetFeature ) )
        return true;
    }
    return false;
  }

  @Override
  public Feature getResolved( final int index )
  {
    return (Feature)get( index );
  }
}
