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

import java.awt.Graphics;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.URIUtil;
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
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.FeatureLinkUtils;
import org.kalypsodeegree_impl.model.feature.GMLWorkspace_Impl;

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
      throw new IllegalArgumentException( "Adding a new element violates maxOccurs" ); //$NON-NLS-1$
  }

  protected GM_Envelope getEnvelope( final Object object )
  {
    final GM_Envelope envelope = m_envelopeProvider.getEnvelope( object );
    return envelope;
  }

  protected void registerFeature( final Object object )
  {
    Assert.isNotNull( object );

    /* Only inline features needs to be unregistered from the workspace */
    if( object instanceof IXLinkedFeature )
      return;

    if( !(object instanceof Feature) )
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

    final Feature f = (Feature)object;
    final GMLWorkspace workspace = f.getWorkspace();
    if( workspace instanceof GMLWorkspace_Impl )
      ((GMLWorkspace_Impl)workspace).unregisterFeature( f );
  }

  private String findLinkPath( final Feature toLink )
  {
    final GMLWorkspace linkedWorkspace = toLink.getWorkspace();
    final GMLWorkspace sourceWorkspace = m_parentFeature.getWorkspace();

    /* Internal link, no uri */
    if( linkedWorkspace == sourceWorkspace )
      return StringUtils.EMPTY;

    final URL targetContext = linkedWorkspace.getContext();
    final URL sourceContext = sourceWorkspace.getContext();

    try
    {
      final URI targetURI = targetContext.toURI();
      final URI sourceURI = sourceContext.toURI();
      final URI relativeURI = URIUtil.makeRelative( targetURI, sourceURI );
      return relativeURI.toString();
    }
    catch( final URISyntaxException e )
    {
      e.printStackTrace();
      return targetContext.toString();
    }
  }

  /**
   * If this list does not has an owner within a valid workspace, links are not resolved and the returned arraxy will
   * contain <code>null</code> elements instead.
   */
  private Feature resolveFeature( final Object object )
  {
    if( object instanceof Feature && !(object instanceof IXLinkedFeature) )
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

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addLink(java.lang.String)
   */
  @Override
  public IXLinkedFeature addLink( final String href ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addLink(java.lang.String, org.kalypso.gmlschema.feature.IFeatureType)
   */
  @Override
  public IXLinkedFeature addLink( final String href, final IFeatureType featureType ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href, featureType );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addLink(java.lang.String, javax.xml.namespace.QName)
   */
  @Override
  public IXLinkedFeature addLink( final String href, final QName featureTypeName ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href, featureTypeName );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#addLink(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public <T extends Feature> IXLinkedFeature addLink( final T toAdd ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), toAdd );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#getOwner()
   */
  @Override
  public Feature getOwner( )
  {
    return m_parentFeature;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertLink(int, java.lang.String)
   */
  @Override
  public IXLinkedFeature insertLink( final int index, final String href ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( index, href, getPropertyType().getTargetFeatureType() );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertLink(int, java.lang.String, org.kalypso.gmlschema.feature.IFeatureType)
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public synchronized IXLinkedFeature insertLink( final int index, final String href, final IFeatureType featureType ) throws IllegalArgumentException, IllegalStateException
  {
    final IXLinkedFeature link = FeatureFactory.createXLink( m_parentFeature, m_parentFeatureTypeProperty, featureType, href );

    // REMARK: this should be checked on every add. Probably this will cause problems due to old buggy code.
    // So we at least check in this new method. Should be moved into the add methods.
    checkCanAdd( 1 );

    if( index < 0 )
      add( link );
    else
      add( index, link );

    return link;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertLink(int, java.lang.String, javax.xml.namespace.QName)
   */
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

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#insertLink(int, org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public <T extends Feature> IXLinkedFeature insertLink( final int index, final T toLink ) throws IllegalArgumentException, IllegalStateException
  {
    final String path = findLinkPath( toLink );
    final String id = toLink.getId();

    final String href = String.format( "%s#%s", path, id );

    return insertLink( index, href, toLink.getFeatureType() );
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#removeLink(org.kalypsodeegree.model.feature.Feature)
   */
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

  /**
   * @see org.kalypsodeegree.model.feature.FeatureList#toFeatures()
   */
  @Override
  public synchronized Feature[] toFeatures( )
  {
    final Feature[] features = new Feature[size()];
    for( int i = 0; i < features.length; i++ )
    {
      final Object object = get( i );

      features[i] = resolveFeature( object );
    }

    return features;
  }

  /* List interface */

  /**
   * @see java.util.List#addAll(java.util.Collection)
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public synchronized boolean addAll( final Collection c )
  {
    checkCanAdd( c.size() );

    for( final Object object : c )
      add( object );

    return !c.isEmpty();
  }

  /**
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public synchronized boolean addAll( final int index, final Collection c )
  {
    checkCanAdd( c.size() );

    for( final Object object : c )
      add( index, object );

    return !c.isEmpty();
  }

  /**
   * @see java.util.List#contains(java.lang.Object)
   */
  @Override
  public synchronized boolean contains( final Object item )
  {
    return indexOf( item ) != -1;
  }

  /**
   * @see java.util.List#containsAll(java.util.Collection)
   */
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

  /**
   * @see java.util.List#isEmpty()
   */
  @Override
  public synchronized boolean isEmpty( )
  {
    return size() == 0;
  }

  /**
   * @see java.util.List#iterator()
   */
  @Override
  public synchronized Iterator< ? > iterator( )
  {
    return listIterator();
  }

  /**
   * @see java.util.List#listIterator()
   */
  @Override
  public synchronized ListIterator< ? > listIterator( )
  {
    return listIterator( 0 );
  }

  /**
   * @see java.util.List#removeAll(java.util.Collection)
   */
  @Override
  public synchronized boolean removeAll( final Collection c )
  {
    boolean changed = false;

    for( final Object object : c )
      changed |= remove( object );

    return changed;
  }

  /**
   * @see java.util.List#retainAll(java.util.Collection)
   */
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

  /**
   * @see java.util.List#set(int, java.lang.Object)
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public Object set( int index, Object object )
  {
    final Object oldItem = remove( index );
    add( index, object );
    return oldItem;
  }

  /**
   * @see java.util.List#subList(int, int)
   *      NOT IMPLEMENTED
   */
  @Override
  public synchronized List< ? > subList( final int fromIndex, final int toIndex )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#toArray()
   */
  @Override
  public synchronized Object[] toArray( )
  {
    return toArray( new Object[size()] );
  }

  /**
   * @see java.util.List#toArray(T[])
   */
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

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#invalidate(java.lang.Object)
   */
  @Override
  public void invalidate( Object o )
  {
    // do nothing
  }

  /**
   * @see org.kalypsodeegree.model.sort.JMSpatialIndex#paint(java.awt.Graphics, org.kalypsodeegree.graphics.transformation.GeoTransform)
   */
  @Override
  public synchronized void paint( final Graphics g, final GeoTransform geoTransform )
  {
    // how to paint
  }

  /* IFeatureProperty interface */

  /**
   * @see org.kalypsodeegree.model.feature.IFeatureProperty#getName()
   */
  @Override
  public QName getName( )
  {
    return getPropertyType().getQName();
  }

  /**
   * @see org.kalypsodeegree.model.feature.IFeatureProperty#getValue()
   */
  @Override
  public Object getValue( )
  {
    return this;
  }

  /**
   * @see org.kalypsodeegree.model.feature.IFeatureProperty#setValue(java.lang.Object)
   */
  @Override
  public void setValue( final Object value )
  {
    throw new UnsupportedOperationException();
  }

  /* IFeatureRelation interface */

  /**
   * @see org.kalypsodeegree.model.feature.IFeatureRelation#getPropertyType()
   */
  @Override
  public IRelationType getPropertyType( )
  {
    return m_parentFeatureTypeProperty;
  }

}