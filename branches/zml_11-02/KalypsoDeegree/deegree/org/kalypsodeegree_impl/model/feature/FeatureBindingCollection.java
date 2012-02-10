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
package org.kalypsodeegree_impl.model.feature;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;

/**
 * TODO: replaces FeatureWrapperCollection.... refaktor and use this stuff instead<br>
 * 
 * @author Gernot Belger
 * @author Dirk Kuch
 */
@SuppressWarnings( { "unchecked" })
public class FeatureBindingCollection<FWCls extends Feature> implements IFeatureBindingCollection<FWCls>
{
  /**
   * The feature wrapped by this object
   */
  private final Feature m_parentFeature;

  /**
   * the list of the feature properties
   */
  private FeatureList m_featureLst;

  /**
   * The {@link QName} of the list property of the feature(-collection)
   */
  private final QName m_featureMemberProp;

  /**
   * The class of the DEFAULT feature wrapper in this collection
   */
  private final Class<FWCls> m_defaultWrapperClass;

  private final boolean m_followXlinks;

  /**
   * Same as {@link #FeatureBindingCollection(Feature, Class, QName, false)}.
   */
  public FeatureBindingCollection( final Feature parentFeature, final Class<FWCls> fwClass, final QName featureMemberProp )
  {
    this( parentFeature, fwClass, featureMemberProp, false );
  }

  /**
   * Creates a new {@link FeatureWrapperCollection} wrapping the provided feature
   * 
   * @param featureCol
   *          the feature or feature collection with a list property to wrap
   * @param fwClass
   *          the base class representing the property feature in the list
   * @param featureMemberProp
   *          the list property linking the feature and its properties
   * @param followXlinks
   *          Experimental: If <code>true</code>, we resolve xlinked features as well in this list.
   */
  public FeatureBindingCollection( final Feature parentFeature, final Class<FWCls> fwClass, final QName featureMemberProp, final boolean followXlinks )
  {
    m_defaultWrapperClass = fwClass;
    m_parentFeature = parentFeature;
    m_featureMemberProp = featureMemberProp;
    m_followXlinks = followXlinks;
  }

  @Override
  public FeatureList getFeatureList( )
  {
    if( m_featureLst == null )
      m_featureLst = (FeatureList) m_parentFeature.getProperty( m_featureMemberProp );

    return m_featureLst;
  }

  @Override
  public void add( final int index, final FWCls element )
  {
    getFeatureList().add( index, element );
  }

  @Override
  public boolean add( final FWCls o )
  {
    return getFeatureList().add( o );
  }

  @Override
  public FWCls addNew( final QName newChildType )
  {
    return addNew( newChildType, m_defaultWrapperClass );
  }

  @Override
  public <T extends FWCls> T addNew( final QName newChildType, final Class<T> classToAdapt )
  {
    Feature feature = null;
    try
    {
      feature = FeatureHelper.createFeatureForListProp( getFeatureList(), newChildType, -1 );
      final T wrapper = getAdaptedFeature( feature, classToAdapt );
      if( wrapper == null )
        throw new IllegalArgumentException( "Feature not adaptable. FeatureType: " + newChildType + ", TypeToAdapt: " + m_defaultWrapperClass + ", Feature: " + feature );

      return wrapper;
    }
    catch( final GMLSchemaException e )
    {
      throw new IllegalArgumentException( "feature:" + feature + " class=" + m_defaultWrapperClass + " featureQName=" + newChildType, e );
    }
  }

  @Override
  public FWCls addNew( final QName newChildType, final String newFeatureId )
  {
    return addNew( newChildType, newFeatureId, m_defaultWrapperClass );
  }

  @Override
  public <T extends FWCls> T addNew( final QName newChildType, final String newFeatureId, final Class<T> classToAdapt )
  {
    Feature feature = null;
    try
    {
      feature = FeatureHelper.createFeatureWithId( newChildType, m_parentFeature, m_featureMemberProp, newFeatureId );
      final T wrapper = (T) feature.getAdapter( classToAdapt );
      if( wrapper == null )
        throw new IllegalArgumentException( "Feature not adaptable. FeatureType: " + newChildType + ", TypeToAdapt: " + m_defaultWrapperClass + ", Feature: " + feature );
      return wrapper;
    }
    catch( final Exception e )
    {
      throw new IllegalArgumentException( "Feature=" + feature + " class=" + m_defaultWrapperClass, e );
    }
  }

  public FWCls addNew( final int index, final QName newChildType )
  {
    return addNew( index, newChildType, m_defaultWrapperClass );
  }

  @Override
  public <T extends FWCls> T addNew( final int index, final QName newChildType, final Class<T> classToAdapt )
  {
    try
    {
      final Feature feature = FeatureHelper.createFeatureForListProp( getFeatureList(), newChildType, index );
      final T wrapper = (T) feature.getAdapter( classToAdapt );
      if( wrapper == null )
        throw new IllegalArgumentException( "Feature not adaptable. FeatureType: " + newChildType + ", TypeToAdapt: " + m_defaultWrapperClass + ", Feature: " + feature );

      return wrapper;
    }
    catch( final GMLSchemaException e )
    {
      throw new IllegalArgumentException( e );
    }
  }

  @Override
  public boolean addAll( final Collection< ? extends FWCls> c )
  {
// return getFeatureList().addAll( FeatureHelper.toFeatureList( c ) );
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll( final int index, final Collection< ? extends FWCls> c )
  {
// return getFeatureList().addAll( index, FeatureHelper.toFeatureList( c ) );
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear( )
  {
    getFeatureList().clear();
  }

  @Override
  public boolean contains( final Object o )
  {
    return indexOf( o ) != -1;
  }

  @Override
  public boolean containsAll( final Collection< ? > c )
  {
    throw new UnsupportedOperationException();
    /*
     * TODO: see comment at 'contains' / return featureList.containsAll(c);
     */
    /*
     * TOASK i do not understand that one. and featureList.containsAll(c) will only work if featureList contains real
     * features or references
     */
  }

  @Override
  public FWCls get( final int index )
  {
    final Object property = getFeatureList().get( index );
    final Feature f = FeatureHelper.getFeature( m_parentFeature.getWorkspace(), property );
    return getAdaptedFeature( f, m_defaultWrapperClass );
  }

  @Override
  public int indexOf( final Object o )
  {
    if( o instanceof Feature )
    {
      // The following line does not work, because the feature list
      // may
      // contain strings (i.e. references to features)
      // return featureList.indexOf(((IFeatureWrapper) o)
      // .getWrappedFeature());

      // We do trust in the equals implementation of
      // AbstractFeatureBinder
      /*
       * TODO may be we should put equals() into the iwrapper interface because all ifeaturewrapper must not extends
       * AbtractFeatureBinder, and this will therefore work, i guess, only for AbstractFeatureBinder childs. i will have
       * a look during the move IfeatureWrapper to kalypso deegree
       */

      // Why backwards??: because of remove below...
      for( int i = size() - 1; i >= 0; i-- )
      {
        final FWCls cls = get( i );
        if( cls == null )
        {
          // FIXME: this is no good! This fixing of a bad model should not happen here, this is too dangerous!
          // bad link removing it
          System.out.println( "removing bad link:" + getFeatureList().get( i ) );
          remove( i );
          continue;
        }

        if( cls.equals( o ) )
          return i;
      }
    }

    return -1;
  }

  @Override
  public boolean isEmpty( )
  {
    return getFeatureList().isEmpty();
  }

  @Override
  public Iterator<FWCls> iterator( )
  {
    final Feature parentFeature = m_parentFeature;
    final Class<FWCls> defaultWrapperClass = m_defaultWrapperClass;

    return new Iterator<FWCls>()
    {
      private final Iterator< ? > m_it = getFeatureList().iterator();

      private final GMLWorkspace m_workspace = parentFeature.getWorkspace();

      @Override
      public synchronized boolean hasNext( )
      {
        return m_it.hasNext();
      }

      @Override
      public synchronized FWCls next( )
      {
        final Object next = m_it.next();
        final Feature f = FeatureHelper.getFeature( m_workspace, next );
        if( f == null )
          throw new RuntimeException( "Feature does not exists: " + next.toString() );

        final FWCls wrapper = getAdaptedFeature( f, defaultWrapperClass );
        if( wrapper == null )
          throw new RuntimeException( "Feature " + f + " could not be adapted: " + f.getId() );
        return wrapper;
      }

      @Override
      public void remove( )
      {
        m_it.remove();
      }

    };
  }

  @Override
  public int lastIndexOf( final Object o )
  {
    if( o instanceof Feature )
      return getFeatureList().lastIndexOf( (o) );

    return -1;
  }

  @Override
  public ListIterator<FWCls> listIterator( )
  {
    return listIterator( 0 );
  }

  @Override
  public ListIterator<FWCls> listIterator( final int index )
  {
    final Feature parentFeature = m_parentFeature;
    final Class<FWCls> defaultWrapperClass = m_defaultWrapperClass;

    return new ListIterator<FWCls>()
    {
      private final ListIterator<Object> m_lit = getFeatureList().listIterator( index );

      @Override
      public void add( final FWCls o )
      {
        m_lit.add( o );
      }

      @Override
      public boolean hasNext( )
      {
        return m_lit.hasNext();
      }

      @Override
      public boolean hasPrevious( )
      {
        return m_lit.hasPrevious();
      }

      @Override
      public FWCls next( )
      {
        final Feature f = FeatureHelper.getFeature( parentFeature.getWorkspace(), m_lit.next() );
        final Object wrapper = f.getAdapter( defaultWrapperClass );
        return (FWCls) wrapper;
      }

      @Override
      public int nextIndex( )
      {
        return m_lit.nextIndex();
      }

      @Override
      public FWCls previous( )
      {
        final Feature f = (Feature) m_lit.previous();
        final Object wrapper = f.getAdapter( defaultWrapperClass );
        return (FWCls) wrapper;
      }

      @Override
      public int previousIndex( )
      {
        return m_lit.previousIndex();
      }

      @Override
      public void remove( )
      {
        m_lit.remove();
      }

      @Override
      public void set( final FWCls o )
      {
        m_lit.set( o );
      }

    };
  }

  @Override
  public FWCls remove( final int index )
  {
    final FWCls wrapper = FeatureHelper.getFeature( m_parentFeature.getWorkspace(), getFeatureList().remove( index ), m_defaultWrapperClass );
    return wrapper;
  }

  @Override
  public boolean remove( final Object o )
  {
    if( o instanceof Feature )
    {
      boolean removed = getFeatureList().remove( (o) );
      if( !removed )
      {
        removed = getFeatureList().remove( (o) );
      }
      return removed;
    }
    else if( o instanceof String )
      return getFeatureList().remove( o );
    else
      return getFeatureList().remove( o );
  }

  @Override
  public boolean removeAll( final Collection< ? > c )
  {
    boolean ret = false;
    for( final Object o : c )
    {
      ret = ret || remove( o );
    }
    return ret;
  }

  @Override
  public boolean retainAll( final Collection< ? > c )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public FWCls set( final int index, final FWCls element )
  {
    final FWCls r = get( index );

    getFeatureList().set( index, element );
    return r;
  }

  @Override
  public int size( )
  {
    return getFeatureList().size();
  }

  @Override
  public List<FWCls> subList( final int fromIndex, final int toIndex )
  {
    return null;
  }

  @Override
  public Object[] toArray( )
  {
    final Object[] objs = new Object[size()];
    for( int i = 0; i < objs.length; i++ )
    {
      final Object fObj = getFeatureList().get( i );
      final Feature feature = FeatureHelper.getFeature( getFeatureList().getParentFeature().getWorkspace(), fObj );
      if( feature == null )
        throw new RuntimeException( "Type not known:" + fObj );
      final Object object = getAdaptedFeature( feature, m_defaultWrapperClass );
      if( object != null )
      {
        objs[i] = object;
      }
    }
    return objs;
  }

  @Override
  public <T> T[] toArray( final T[] a )
  {
    final int size = size();
    final Class< ? > compType = a.getClass().getComponentType();
    if( !compType.isAssignableFrom( m_defaultWrapperClass ) )
      throw new ArrayStoreException();

    final T[] result;
    if( a.length < size )
      result = (T[]) Array.newInstance( compType, size );
    else
      result = a;

    for( int i = size - 1; i >= 0; i-- )
      result[i] = (T) get( i );

    if( a.length > size )
      result[size] = null;

    return result;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return getFeatureList().hashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof FeatureBindingCollection )
      return getFeatureList().equals( ((FeatureBindingCollection< ? >) obj).getFeatureList() );

    if( obj instanceof IFeatureBindingCollection )
    {
      final IFeatureBindingCollection< ? > frs = (IFeatureBindingCollection< ? >) obj;
      final int size = size();
      if( size != frs.size() )
        return false;

      for( int i = size - 1; i >= 0; i-- )
      {
        if( !get( i ).equals( frs.get( 0 ) ) )
          return false;
      }
      return true;
    }
    else
      return super.equals( obj );
  }

  /**
   * @see org.kalypso.kalypsosimulationmodel.core.IFeatureWrapperCollection#addRef(org.kalypsodeegree.model.feature.binding.IFeatureWrapper)
   */
  @Override
  public boolean addRef( final FWCls toAdd )
  {
    final String gmlID = toAdd.getId();
    // TODO: this can cause major performance leaks
    if( getFeatureList().contains( gmlID ) )
      return false;
    return getFeatureList().add( gmlID );
  }

  /**
   * @see org.kalypsodeegree.model.feature.IFeatureBindingCollection#query(org.kalypsodeegree.model.geometry.GM_Surface,
   *      javax.xml.namespace.QName, boolean)
   */
  @Override
  public List<FWCls> query( final GM_Surface< ? > selectionSurface, final QName qname, final boolean containedOnly )
  {
    final List< ? > selectedFeature = getFeatureList().query( selectionSurface.getEnvelope(), null );
    final List<FWCls> selFW = new ArrayList<FWCls>( selectedFeature.size() );
    final GMLWorkspace workspace = m_parentFeature.getWorkspace();

    for( final Object linkOrFeature : selectedFeature )
    {
      final FWCls feature = FeatureHelper.getFeature( workspace, linkOrFeature, m_defaultWrapperClass );
      if( feature != null )
      {
        final IPropertyType pt = feature.getFeatureType().getProperty( qname );
        if( pt != null )
        {
          try
          {
            final Object property = feature.getProperty( qname );
            if( property instanceof GM_Object )
            {
              final GM_Object gmo = (GM_Object) property;
              if( containedOnly )
              {
                if( selectionSurface.contains( gmo ) )
                {
                  selFW.add( feature );
                }
              }
              else
              {
                if( selectionSurface.intersects( gmo ) )
                {
                  selFW.add( feature );
                }
              }
            }
          }
          catch( final Exception e )
          {
            e.printStackTrace();
          }
        }
      }
    }

    return selFW;
  }

  /**
   * @see org.kalypsodeegree.model.feature.IFeatureBindingCollection#query(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  @Override
  public List<FWCls> query( final GM_Envelope envelope )
  {
    final List< ? > selectedFeature = getFeatureList().query( envelope, null );
    final List<FWCls> selFW = new ArrayList<FWCls>( selectedFeature.size() );
    for( final Object linkOrFeature : selectedFeature )
    {
      final Feature feature = FeatureHelper.getFeature( m_parentFeature.getWorkspace(), linkOrFeature );
      final FWCls adaptedFeature = getAdaptedFeature( feature, m_defaultWrapperClass );
      if( adaptedFeature != null )
        selFW.add( adaptedFeature );
    }
    return selFW;
  }

  /**
   * @see org.kalypso.kalypsosimulationmodel.core.IFeatureWrapperCollection#query(org.kalypsodeegree.model.geometry.GM_Position)
   */
  @Override
  public List<FWCls> query( final GM_Position position )
  {
    final List< ? > selectedFeature = getFeatureList().query( position, null );
    final List<FWCls> selFW = new ArrayList<FWCls>( selectedFeature.size() );
    final GMLWorkspace workspace = m_parentFeature.getWorkspace();

    for( final Object linkOrFeature : selectedFeature )
    {
      final FWCls feature = FeatureHelper.getFeature( workspace, linkOrFeature, m_defaultWrapperClass );
      if( feature != null )
      {
        selFW.add( feature );
      }
    }
    return selFW;
  }

  /**
   * @see org.kalypsodeegree.model.feature.binding.IFeatureWrapperCollection#cloneInto(org.kalypsodeegree.model.feature.binding.IFeatureWrapper2)
   */
  @Override
  public void cloneInto( final FWCls toClone ) throws Exception
  {
    final IRelationType relationType = getFeatureList().getParentFeatureTypeProperty();
    FeatureHelper.cloneFeature( m_parentFeature, relationType, toClone );
  }

  <T extends FWCls> T getAdaptedFeature( final Feature feature, final Class<T> classToAdapt )
  {
    if( feature == null )
      return null;

    final Object adapted = feature.getAdapter( classToAdapt );
    if( adapted != null )
      return classToAdapt.cast( adapted );

    if( !m_followXlinks )
      return null;

    // EXPERIMENTAL: if this function is on, we even try to adapt the linked feature.
    if( feature instanceof XLinkedFeature_Impl )
    {
      final XLinkedFeature_Impl xlinkedFeature = (XLinkedFeature_Impl) feature;
      final Feature linkedFeature = xlinkedFeature.getFeature();
      if( linkedFeature == null )
        return null;

      final Object adaptedLink = linkedFeature.getAdapter( m_defaultWrapperClass );
      return classToAdapt.cast( adaptedLink );
    }

    return null;
  }

  /**
   * @see org.kalypsodeegree.model.feature.binding.IFeatureWrapperCollection#getBoundingBox()
   */
  @Override
  public GM_Envelope getBoundingBox( )
  {
    return getFeatureList().getBoundingBox();
  }

  @Override
  public Feature getParentFeature( )
  {
    return m_parentFeature;
  }
}
