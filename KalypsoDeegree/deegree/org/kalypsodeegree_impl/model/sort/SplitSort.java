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

import gnu.trove.TIntProcedure;

import java.awt.Graphics;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.FeatureLinkUtils;
import org.kalypsodeegree_impl.model.feature.GMLWorkspace_Impl;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.infomatiq.jsi.Rectangle;

public class SplitSort implements FeatureList
{
  /* Items of this list */
  private final List<SplitSortItem> m_items = new ArrayList<>();

  /**
   * (Geo-)index for this FeatureList. Lazy instantiated for memory reasons, small list (and those that never query
   * shall not have the memory consuming index.
   */
  private SplitSortindex m_index = null;

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
  public SplitSort( final Feature parentFeature, final IRelationType parentFTP )
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
  public SplitSort( final Feature parentFeature, final IRelationType parentFTP, final IEnvelopeProvider envelopeProvider )
  {
    m_parentFeature = parentFeature;
    m_parentFeatureTypeProperty = parentFTP;
    m_envelopeProvider = envelopeProvider == null ? new DefaultEnvelopeProvider( parentFeature ) : envelopeProvider;
  }

  List<SplitSortItem> getItems( )
  {
    return m_items;
  }

  private void checkCanAdd( final int count )
  {
    if( m_parentFeatureTypeProperty == null )
      return;

    final int maxOccurs = m_parentFeatureTypeProperty.getMaxOccurs();

    if( maxOccurs != IPropertyType.UNBOUND_OCCURENCY && size() + count > maxOccurs )
      throw new IllegalArgumentException( "Adding a new element violates maxOccurs" ); //$NON-NLS-1$
  }

  private void registerFeature( final Object object )
  {
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

  private void unregisterFeature( final Object object )
  {
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

  private synchronized SplitSortItem createItem( final Object data, final int index )
  {
    Assert.isNotNull( data );

    /* Create a new item */

    final SplitSortItem newItem = new SplitSortItem( data );

    if( m_index != null )
      m_index.insert( newItem, index );

    registerFeature( data );

    return newItem;
  }

  Rectangle getEnvelope( final SplitSortItem item )
  {
    final Object data = item.getData();
    final GM_Envelope envelope = m_envelopeProvider.getEnvelope( data );

    return GeometryUtilities.toRectangle( envelope );
  }

  private void removeDataObject( final SplitSortItem item, final int index )
  {
    if( m_index != null )
      m_index.remove( item, index );

    // REMARK: it is a bit unclear what happens if we have a real Feature (not a link) multiple times in the same list.
    // We only unregister the it if the last occurrence is removed, assuming, that the feature is really no longer used.
    unregisterFeature( item.getData() );
  }

  private synchronized void createIndex( )
  {
    if( m_index == null )
      m_index = new SplitSortindex( this );
  }

  // IMPLEMENTATION OF LIST INTERFACE

  @Override
  public synchronized boolean add( final Object object )
  {
    checkCanAdd( 1 );

    final SplitSortItem item = createItem( object, size() );

    m_items.add( item );

    return true;
  }

  @Override
  public synchronized void add( final int index, final Object object )
  {
    checkCanAdd( 1 );

    if( m_index != null )
      m_index.reindex( index, 1 );

    final SplitSortItem newItem = createItem( object, index );

    m_items.add( index, newItem );
  }

  @Override
  public synchronized boolean addAll( final Collection c )
  {
    checkCanAdd( c.size() );

    for( final Object object : c )
      add( object );

    return !c.isEmpty();
  }

  @Override
  public synchronized boolean addAll( final int index, final Collection c )
  {
    checkCanAdd( c.size() );

    if( m_index != null )
      m_index.reindex( index, c.size() );

    final Collection<SplitSortItem> items = new ArrayList<>( c.size() );
    final int count = 0;
    for( final Object object : c )
    {
      final SplitSortItem newItem = createItem( object, index + count );
      items.add( newItem );
    }

    return m_items.addAll( index, items );
  }

  @Override
  public synchronized Object set( final int index, final Object newObject )
  {
    final SplitSortItem oldItem = m_items.get( index );

    if( m_index != null )
      removeDataObject( oldItem, index );

    final SplitSortItem newItem = createItem( newObject, index );

    m_items.set( index, newItem );

    return oldItem.getData();
  }

  @Override
  public synchronized Object get( final int index )
  {
    return m_items.get( index ).getData();
  }

  @Override
  public synchronized boolean remove( final Object object )
  {
    final int index = indexOf( object );
    if( index == -1 )
      return false;

    final SplitSortItem item = m_items.get( index );

    removeDataObject( item, index );

    // FIXME: reindexing is very slow, we need another idea how to speed this up
    if( m_index != null )
      m_index.reindex( index + 1, -1 );

    m_items.remove( index );

    return true;
  }

  @Override
  public synchronized Object remove( final int index )
  {
    final SplitSortItem item = m_items.get( index );

    removeDataObject( item, index );

    if( m_index != null )
      m_index.reindex( index + 1, -1 );

    m_items.remove( index );

    return item.getData();
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
  public synchronized int size( )
  {
    return m_items.size();
  }

  @Override
  public synchronized boolean isEmpty( )
  {
    return size() == 0;
  }

  @Override
  public synchronized void clear( )
  {
    for( final Object element : m_items )
      unregisterFeature( element );

    m_index = null;

    m_items.clear();
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

  @Override
  public synchronized int indexOf( final Object object )
  {
    if( m_index != null )
    {
      // TODO: if we have many objects, should we create the index now?

      final int index = m_index.indexOf( object );
      if( index != 0 )
        return index;
    }

    // REMARK: linear search is needed, because we cannot assure the valid index, especially after an object has been
    // removed that was contained inside the list twice.

    /* linear search */
    for( int i = 0; i < size(); i++ )
    {
      final SplitSortItem item = m_items.get( i );
      if( item.getData().equals( object ) )
        return i;
    }

    return -1;
  }

  @Override
  public synchronized int lastIndexOf( final Object object )
  {
    for( int i = size() - 1; i >= 0; i-- )
    {
      final SplitSortItem item = m_items.get( i );
      if( item.getData().equals( object ) )
        return i;
    }

    return -1;
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

  /**
   * ATTENTION: Returns an unmodifiable iterator i.e. changing the list via the returned iterator results in an
   * exception.<br/>
   * The iterator is not synchronized, however.
   */
  @Override
  public synchronized Iterator< ? > iterator( )
  {
    return listIterator();
  }

  /**
   * ATTENTION: Returns an unmodifiable iterator i.e. changing the list via the returned iterator results in an
   * exception.<br/>
   * The iterator is not synchronized, however.
   */
  @Override
  public synchronized ListIterator< ? > listIterator( )
  {
    return listIterator( 0 );
  }

  /**
   * ATTENTION: Returns an unmodifiable iterator i.e. changing the list via the returned iterator results in an
   * exception.<br/>
   * The iterator is not synchronized, however.
   */
  @Override
  public synchronized ListIterator< ? > listIterator( final int index )
  {
    return new SplitSortIterator( this, index );
  }

  /**
   * NOT IMPLEMENTED
   */
  @Override
  public synchronized List< ? > subList( final int fromIndex, final int toIndex )
  {
    throw new UnsupportedOperationException();
  }

  // JMSpatialIndex implementation

  @Override
  public List< ? > query( final GM_Position pos, final List result )
  {
    final Rectangle envelope = new Rectangle( (float)pos.getX() - 1.0f, (float)pos.getY() - 1.0f, (float)pos.getX() + 1.0f, (float)pos.getY() + 1.0f );
    return query( envelope, result );
  }

  @Override
  public List< ? > query( final GM_Envelope queryEnv, final List result )
  {
    final Rectangle envelope = GeometryUtilities.toRectangle( queryEnv );
    return query( envelope, result );
  }

  private synchronized List< ? > query( final Rectangle envelope, @SuppressWarnings( "rawtypes" ) final List receiver )
  {
    // FIXME: better handling of lists with very view elements: in this case, linear search would be better and would avoid the momory consuming spatial index
    createIndex();

    @SuppressWarnings( "unchecked" ) final List<Object> result = receiver == null ? new ArrayList<>() : receiver;

    final List<SplitSortItem> items = m_items;

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int index )
      {
        final SplitSortItem item = items.get( index );
        final Object data = item.getData();
        result.add( data );
        return true;
      }
    };

    final Rectangle searchRect = envelope == null ? m_index.getBounds() : envelope;
    m_index.intersects( searchRect, ip );

    return result;
  }

  @Override
  public synchronized void paint( final Graphics g, final GeoTransform geoTransform )
  {
    // how to paint the RTree?
    // m_spatialIndex.paint( g, geoTransform );
  }

  @Override
  public synchronized GM_Envelope getBoundingBox( )
  {
    createIndex();

    final Rectangle bounds = m_index.getBounds();

    // REMARK: we assume that all elements of the split sort are always in Kalypso CRS
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    return GeometryUtilities.toEnvelope( bounds, crs );
  }

  @Override
  public synchronized void invalidate( final Object object )
  {
    if( m_index != null )
      m_index.invalidate( object );
  }

  // FEATURE LIST IMPLEMENTATION

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

  /**
   * If this list does not has an owner within a valid workspace, links are not resolved and the returned arraxy will
   * contain <code>null</code> elements instead.
   */
  @Override
  public synchronized Feature[] toFeatures( )
  {
    final Feature[] features = new Feature[m_items.size()];
    for( int i = 0; i < features.length; i++ )
    {
      final Object object = get( i );

      features[i] = resolveFeature( object );
    }

    return features;
  }

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
  public Feature getOwner( )
  {
    return m_parentFeature;
  }

  @Override
  public IRelationType getPropertyType( )
  {
    return m_parentFeatureTypeProperty;
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

  @Override
  public <T extends Feature> IXLinkedFeature addLink( final T toAdd ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), toAdd );
  }

  @Override
  public IXLinkedFeature addLink( final String href ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href );
  }

  @Override
  public IXLinkedFeature addLink( final String href, final QName featureTypeName ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href, featureTypeName );
  }

  @Override
  public IXLinkedFeature addLink( final String href, final IFeatureType featureType ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( size(), href, featureType );
  }

  @Override
  public <T extends Feature> IXLinkedFeature insertLink( final int index, final T toLink ) throws IllegalArgumentException, IllegalStateException
  {
    final String path = findLinkPath( toLink );
    final String id = toLink.getId();

    final String href = String.format( "%s#%s", path, id );

    return insertLink( index, href, toLink.getFeatureType() );
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

  @Override
  public IXLinkedFeature insertLink( final int index, final String href ) throws IllegalArgumentException, IllegalStateException
  {
    return insertLink( index, href, getPropertyType().getTargetFeatureType() );
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
}