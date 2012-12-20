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

import java.awt.Graphics;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Assert;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.FeatureLinkUtils;
import org.kalypsodeegree_impl.model.feature.GMLWorkspace_Impl;
import org.kalypsodeegree_impl.model.feature.XLinkedFeature_Impl;

/**
 * @author kurzbach
 */
public abstract class AbstractFeatureList implements FeatureList
{
  private final Feature m_parentFeature;

  private final IRelationType m_parentFeatureTypeProperty;

  private final IEnvelopeProvider m_envelopeProvider;

  /**
   * @param parentFeature
   *          The parent feature. May be <code>null</code>, if this list has no underlying workspace. Make sure
   *          parentFTP is also null then.
   * @param parentFTP
   *          The feature type of the parent. May be <code>null</code>, if this list has no underlying workspace. Make
   *          sure parentFeature is also null then.
   */
  public AbstractFeatureList( final Feature parentFeature, final IRelationType parentFTP )
  {
    this( parentFeature, parentFTP, null );
  }

  /**
   * @param parentFeature
   *          The parent feature. May be null, if this list has no underlying workspace. Make sure parentFTP is also
   *          null then.
   * @param parentFTP
   *          The feature type of the parent. May be null, if this list has no underlying workspace. Make sure
   *          parentFeature is also null then.
   * @param envelopeProvider
   *          The provider returns envelops. If <code>null</code>, the default one is used.
   */
  public AbstractFeatureList( final Feature parentFeature, final IRelationType parentFTP, final IEnvelopeProvider envelopeProvider )
  {
    m_parentFeature = parentFeature;
    m_parentFeatureTypeProperty = parentFTP;
    m_envelopeProvider = envelopeProvider == null ? new DefaultEnvelopeProvider( parentFeature ) : envelopeProvider;
  }

  protected void checkCanAdd( final int count )
  {
    if( m_parentFeatureTypeProperty == null )
      return;

    final int maxOccurs = m_parentFeatureTypeProperty.getMaxOccurs();

    if( maxOccurs != IPropertyType.UNBOUND_OCCURENCY && size() + count > maxOccurs )
      return;
    //throw new IllegalArgumentException( "Adding a new element violates maxOccurs" ); //$NON-NLS-1$
  }

  protected GM_Envelope getEnvelope( final Object object )
  {
    final GM_Envelope envelope = m_envelopeProvider.getEnvelope( object );
    return envelope;
  }

  protected void registerFeature( final Object object )
  {
    Assert.isNotNull( object );

    /* Only inline features needs to be registered with the workspace */
    if( object instanceof IXLinkedFeature )
      return;

    if( !(object instanceof Feature) )
      return;

    if( m_parentFeature == null )
      return;

    final Feature f = (Feature)object;
    final GMLWorkspace workspace = f.getWorkspace();
    if( workspace instanceof GMLWorkspace_Impl )
      ((GMLWorkspace_Impl)workspace).registerFeature( f );
  }

  protected void unregisterFeature( final Object object )
  {
    Assert.isNotNull( object );

    /* Only inline features needs to be unregistered from the workspace */
    if( object instanceof IXLinkedFeature )
      return;

    if( !(object instanceof Feature) )
      return;

    /* REMARK: If the split sort is used with features with another workspace than the parent feature */
    /* REMARK: or without a parent feature, we cannot unregister it. */
    if( m_parentFeature == null )
      return;

    final Feature f = (Feature)object;
    final GMLWorkspace workspace = f.getWorkspace();
    if( workspace instanceof GMLWorkspace_Impl )
      ((GMLWorkspace_Impl)workspace).unregisterFeature( f );
  }

  /**
   * If this list does not has an owner within a valid workspace, links are not resolved and the returned arraxy will
   * contain <code>null</code> elements instead.
   */
  protected Feature resolveFeature( final Object object )
  {
    if( object instanceof IXLinkedFeature )
      return ((IXLinkedFeature)object).getFeature();

    if( object instanceof Feature )
      return (Feature)object;

    final Feature owner = getOwner();
    if( owner == null )
      return null;

    final GMLWorkspace workspace = owner.getWorkspace();
    if( workspace == null )
      return null;

    return FeatureHelper.getFeature( workspace, object );
  }

  /* FeatureList interface */

  @Override
  public void accept( final FeatureVisitor visitor )
  {
    accept( visitor, FeatureVisitor.DEPTH_INFINITE );
  }

  @Override
  public synchronized void accept( final FeatureVisitor visitor, final int depth )
  {
    for( final Object object : this )
    {
      if( object instanceof Feature && !(object instanceof IXLinkedFeature) )
        visitor.visit( (Feature)object );
      else if( depth == FeatureVisitor.DEPTH_INFINITE_LINKS )
      {
        final Feature linkedFeature = resolveFeature( object );
        if( linkedFeature != null )
          visitor.visit( linkedFeature );
      }
    }
  }

  @Override
  public IXLinkedFeature addLink( final String href ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href );
  }

  @Override
  public IXLinkedFeature addLink( final String href, final IFeatureType featureType ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href, featureType );
  }

  @Override
  public IXLinkedFeature addLink( final String href, final QName featureTypeName ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href, featureTypeName );
  }

  @Override
  public <T extends Feature> IXLinkedFeature addLink( final T toAdd ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), toAdd );
  }

  @Override
  public Feature getOwner( )
  {
    return m_parentFeature;
  }

  @Override
  public Feature getResolved( final int index )
  {
    return resolveFeature( get( index ) );
  }

  @Override
  public synchronized int indexOfLink( final Feature targetFeature )
  {
    if( targetFeature instanceof IXLinkedFeature )
      throw new IllegalArgumentException( "targetFeature may only be an inline feature" ); //$NON-NLS-1$

    final int size = size();
    for( int i = 0; i < size; i++ )
    {
      final Object element = get( i );

      if( FeatureLinkUtils.isSameOrLinkTo( targetFeature, element ) )
        return i;
    }

    return -1;
  }
  
  @Override
  public IXLinkedFeature insertLink( final int index, final String href ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( index, href, getPropertyType().getTargetFeatureType() );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public synchronized IXLinkedFeature insertLink( final int index, final String href, final IFeatureType featureType ) throws IllegalArgumentException, IllegalStateException
  {
    final IXLinkedFeature link = new XLinkedFeature_Impl( m_parentFeature, m_parentFeatureTypeProperty, featureType, href );

    // REMARK: backwards compatibility; insert local href as string instead of xlink
    // else, old client code that not correctly resolves the links will break
    final Object linkOrString;
    if( href == null )
    {
      // Protect against NPE, should really not happen
      linkOrString = null;
    }
    else if( link.getUri() == null )
    {
      if( href.startsWith( "#" ) ) //$NON-NLS-1$
        linkOrString = href.substring( 1 );
      else
        linkOrString = href;
    }
    else
      linkOrString = link;

    if( index < 0 )
      add( linkOrString );
    else
      add( index, linkOrString );

    return link;
  }

  @Override
  public IXLinkedFeature insertLink( final int index, final String href, final QName featureTypeName ) throws IllegalArgumentException, IllegalStateException
  {
    final IFeatureType featureType = GMLSchemaUtilities.getFeatureTypeQuiet( featureTypeName );
    if( featureType == null )
    {
      final String message = String.format( "Unknown feature type: %s", featureTypeName ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    return insertLink( index, href, featureType );
  }

  @Override
  public <T extends Feature> IXLinkedFeature insertLink( final int index, final T toLink ) throws IllegalArgumentException, IllegalStateException
  {
    final String path = FeatureLinkUtils.findLinkPath( toLink, m_parentFeature );
    return insertLink( index, path, toLink.getFeatureType() );
  }

  @Override
  public synchronized int removeLinks( final Feature[] targetFeatures )
  {
    final TIntArrayList indicesToRemove = new TIntArrayList( targetFeatures.length );

    /* find indices of all elements that should be removed */
    for( final Feature targetFeature : targetFeatures )
    {
      if( targetFeature instanceof IXLinkedFeature )
        throw new IllegalArgumentException( "targetFeature may only be an inline feature" ); //$NON-NLS-1$

      final int index = indexOfLink( targetFeature );
      if( index != -1 )
        indicesToRemove.add( index );
    }

    final int[] allIndices = indicesToRemove.toNativeArray();
    removeAll( allIndices );

    return allIndices.length;
  }

  /**
   * Default implementation that simply iterates through all elements and call {@link #remove(int)}.
   */
  @Override
  public void removeAll( final int[] allIndices )
  {
    for( final int i : allIndices )
      remove( i );
  }

  @Override
  public synchronized boolean removeLink( final Feature targetFeature )
  {
    if( targetFeature instanceof IXLinkedFeature )
      throw new IllegalArgumentException( "targetFeature may only be an inline feature" ); //$NON-NLS-1$

    for( int i = 0; i < size(); i++ )
    {
      final Object element = get( i );

      if( FeatureLinkUtils.isSameOrLinkTo( targetFeature, element ) )
      {
        remove( i );
        return true;
      }
    }

    /* not found */
    return false;
  }

  @Override
  public boolean containsLinkTo( final Feature targetFeature )
  {
    return indexOfLink( targetFeature ) != -1;
  }

  @Override
  public synchronized Feature[] toFeatures( )
  {
    return toFeatures( new Feature[size()] );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public synchronized <T extends Feature> T[] toFeatures( T[] features )
  {
    final int newLength = size();
    final Class< ? extends Feature[]> newType = features.getClass();
    if( features.length != newLength )
    {
      features = ((Object)newType == (Object)Object[].class) ? (T[])new Object[newLength] : (T[])Array.newInstance( newType.getComponentType(), newLength );
    }

    for( int i = 0; i < newLength; i++ )
    {
      final Object object = get( i );
      features[i] = (T)resolveFeature( object );
    }

    return features;
  }

  /* List interface */

  @Override
  @SuppressWarnings( "unchecked" )
  public synchronized boolean addAll( final Collection c )
  {
    checkCanAdd( c.size() );

    for( final Object object : c )
      add( object );

    return !c.isEmpty();
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public synchronized boolean addAll( final int index, final Collection c )
  {
    checkCanAdd( c.size() );

    for( final Object object : c )
      add( index, object );

    return !c.isEmpty();
  }

  @Override
  public synchronized boolean contains( final Object item )
  {
    return indexOf( item ) != -1;
  }

  @Override
  public synchronized boolean containsAll( final Collection c )
  {
    for( final Object object : c )
    {
      if( !contains( object ) )
        return false;
    }

    return true;
  }

  @Override
  public synchronized boolean isEmpty( )
  {
    return size() == 0;
  }

  @Override
  public synchronized Iterator< ? > iterator( )
  {
    return listIterator();
  }

  @Override
  public synchronized ListIterator< ? > listIterator( )
  {
    return listIterator( 0 );
  }

  @Override
  public synchronized boolean removeAll( final Collection c )
  {
    boolean changed = false;

    for( final Object object : c )
      changed |= remove( object );

    return changed;
  }

  @Override
  public synchronized boolean retainAll( final Collection c )
  {
    boolean modified = false;

    final int size = size();
    for( int i = 0; i < size; i++ )
    {
      final Object object = get( i );
      if( !c.contains( object ) )
        modified |= remove( object );
    }

    return modified;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public Object set( final int index, final Object object )
  {
    final Object oldItem = remove( index );
    add( index, object );
    return oldItem;
  }

  /**
   * NOT IMPLEMENTED
   */
  @Override
  public synchronized List< ? > subList( final int fromIndex, final int toIndex )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized Object[] toArray( )
  {
    return toArray( new Object[size()] );
  }

  @Override
  public synchronized Object[] toArray( Object[] a )
  {
    if( a == null || a.length != size() )
      a = new Object[size()];

    for( int i = 0; i < a.length; i++ )
      a[i] = get( i );

    return a;
  }

  /* JMSpatialIndex interface */

  @Override
  public void invalidate( final Object o )
  {
    // do nothing
  }

  @Override
  public synchronized void paint( final Graphics g, final GeoTransform geoTransform )
  {
    // how to paint
  }

  /* IFeatureProperty interface */

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

  /* IFeatureRelation interface */

  @Override
  public IRelationType getPropertyType( )
  {
    return m_parentFeatureTypeProperty;
  }

}