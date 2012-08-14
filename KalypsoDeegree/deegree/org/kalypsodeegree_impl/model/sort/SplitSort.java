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

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;

import java.awt.Graphics;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
import org.kalypsodeegree_impl.model.geometry.GM_Envelope_Impl;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

public class SplitSort implements FeatureList
{
  private SpatialIndex m_spatialIndex;

  /* Items of this list */
  private final List<Object> m_items = new ArrayList<>();

  /** Invalid objects, will be placed into the index when it is accessed next time. */
  private final Set<SplitSortItem> m_invalidItems = new HashSet<>();

  private final TIntObjectHashMap<SplitSortItem> m_itemIdHash = new TIntObjectHashMap<>();

  private final Map<Object, SplitSortItem> m_itemHash = new HashMap<>();

  private final Feature m_parentFeature;

  private final IRelationType m_parentFeatureTypeProperty;

  private final IEnvelopeProvider m_envelopeProvider;

  private int m_currentId = Integer.MIN_VALUE;

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
    m_spatialIndex = createIndex();
  }

  private SpatialIndex createIndex( )
  {
    final SpatialIndex spatialIndex = new RTree();
    spatialIndex.init( null );

    return spatialIndex;
  }

  /**
   * Recreate the index, if it is <code>null</code>.
   */
  private synchronized void checkIndex( )
  {
    for( final SplitSortItem invalidItem : m_invalidItems )
    {
      final Rectangle newEnvelope = getEnvelope( invalidItem );
      final Rectangle oldEnvelope = invalidItem.getEnvelope();

      final boolean envelopeChanged = invalidItem.setEnvelope( newEnvelope );

      /* Only update spatial index if envelope really changed */
      if( envelopeChanged )
      {
        final int id = invalidItem.getId();

        /* Remove from index */
        if( oldEnvelope != null )
        {
          final boolean success = m_spatialIndex.delete( newEnvelope, id );
          if( !success )
            System.out.println( "problem?" );
        }

        /* reinsert into index */
        if( newEnvelope != null )
          m_spatialIndex.add( newEnvelope, id );
      }
    }

    m_invalidItems.clear();
  }

  private synchronized SplitSortItem createItem( final Object data )
  {
    Assert.isNotNull( data );

    final SplitSortItem existingItem = m_itemHash.get( data );
    if( existingItem != null )
    {
      /* If item is already known, just increase the reference count and return it */
      existingItem.increaseRef();
      return existingItem;
    }

    /* Create a new item */
    final int id = m_currentId++;

    if( m_currentId == Integer.MAX_VALUE )
      System.out.println( "Problem! Max number of entries exceeded..." );

    final SplitSortItem newItem = new SplitSortItem( data, id );

    m_itemIdHash.put( id, newItem );
    m_itemHash.put( data, newItem );

    /* mark as invalid, item gets inserted on next access */
    m_invalidItems.add( newItem );

    return newItem;
  }

  private void removeDataObject( final Object object )
  {
    final SplitSortItem item = m_itemHash.get( object );

    Assert.isNotNull( item );

    final int counter = item.decreaseRef();
    if( counter > 0 )
    {
      /* if item has still references, just return */
      return;
    }

    /* really remove item and unregister it from all hashes */
    m_itemHash.remove( item );

    m_invalidItems.remove( item );

    final Rectangle envelope = item.getEnvelope();
    if( envelope != null )
    {
      final int id = item.getId();
      m_spatialIndex.delete( envelope, id );
    }

    // REMARK: it is a bit unclear what happens if we have a real Feature (not a link) multiple times in the same list.
    // We only unregister the it if the last occurrence is removed, assuming, that the feature is really no longer used.

    unregisterFeature( object );
  }

  private void checkCanAdd( final int count )
  {
    if( m_parentFeatureTypeProperty == null )
      return;

    final int maxOccurs = m_parentFeatureTypeProperty.getMaxOccurs();

    if( maxOccurs != IPropertyType.UNBOUND_OCCURENCY && size() + count > maxOccurs )
      throw new IllegalArgumentException( "Adding a new element violates maxOccurs" ); //$NON-NLS-1$
  }

  private Rectangle getEnvelope( final SplitSortItem item )
  {
    final Object data = item.getData();
    final GM_Envelope envelope = m_envelopeProvider.getEnvelope( data );

    return toRectangle( envelope );
  }

  private static final Rectangle toRectangle( final GM_Envelope envelope )
  {
    if( envelope == null )
      return null;

    final float x1 = (float) envelope.getMinX();
    final float y1 = (float) envelope.getMinY();
    final float x2 = (float) envelope.getMaxX();
    final float y2 = (float) envelope.getMaxY();

    return new Rectangle( x1, y1, x2, y2 );
  }

  private void unregisterFeature( final Object object )
  {
    /* Only inline features needs to be unregistered from the workspace */
    if( object instanceof IXLinkedFeature )
      return;
    if( !(object instanceof Feature) )
      return;

    final Feature f = (Feature) object;
    final GMLWorkspace workspace = f.getWorkspace();
    if( workspace instanceof GMLWorkspace_Impl )
      ((GMLWorkspace_Impl) workspace).unregisterFeature( f );
  }

  // IMPLEMENTATION OF LIST INTERFACE

  @Override
  public synchronized boolean add( final Object object )
  {
    checkCanAdd( 1 );

    createItem( object );

    m_items.add( object );

    return true;
  }

  @Override
  public synchronized void add( final int index, final Object object )
  {
    checkCanAdd( 1 );

    createItem( object );

    m_items.add( index, object );
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized boolean addAll( @SuppressWarnings("rawtypes") final Collection c )
  {
    checkCanAdd( c.size() );

    for( final Object object : c )
      createItem( object );

    return m_items.addAll( c );
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized boolean addAll( final int index, @SuppressWarnings("rawtypes") final Collection c )
  {
    checkCanAdd( c.size() );

    for( final Object object : c )
      createItem( object );

    return m_items.addAll( index, c );
  }

  @Override
  public synchronized Object set( final int index, final Object newObject )
  {
    final Object oldObject = m_items.set( index, newObject );

    removeDataObject( oldObject );

    createItem( newObject );

    return oldObject;
  }

  @Override
  public synchronized Object get( final int index )
  {
    return m_items.get( index );
  }

  @Override
  public synchronized boolean remove( final Object object )
  {
    if( m_items.remove( object ) )
    {
      removeDataObject( object );
      return true;
    }


    /* unknown object */
    return false;
  }

  @Override
  public synchronized Object remove( final int index )
  {
    final Object removedItem = m_items.remove( index );

    removeDataObject( removedItem );

    return removedItem;
  }

  @Override
  public synchronized boolean removeAll( @SuppressWarnings("rawtypes") final Collection c )
  {
    boolean changed = false;

    for( final Object object : c )
    {
      // REMARK: slower than removeAll on m_items,
      // but we have to make sure we really removed something, before remove the index item
      if( m_items.remove( object ) )
      {
        removeDataObject( object );
        changed = true;
      }
    }

    return changed;
  }

  @Override
  public synchronized boolean retainAll( @SuppressWarnings("rawtypes") final Collection c )
  {
    boolean modified = false;

    final int size = size();
    for( int i = 0; i < size; i++ )
    {
      final Object object = get( i );
      if( !c.contains( object ) )
      {
        remove( i-- );
        modified = true;
      }
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

    m_items.clear();
    m_invalidItems.clear();
    m_itemHash.clear();
    m_itemIdHash.clear();

    m_spatialIndex = createIndex();
    m_currentId = Integer.MIN_VALUE;
  }

  @Override
  public synchronized Object[] toArray( )
  {
    return m_items.toArray( new Object[m_items.size()] );
  }

  @Override
  public synchronized Object[] toArray( final Object[] a )
  {
    return m_items.toArray( a );
  }

  @Override
  public synchronized int indexOf( final Object item )
  {
    return m_items.indexOf( item );
  }

  @Override
  public synchronized int lastIndexOf( final Object item )
  {
    return m_items.lastIndexOf( item );
  }

  @Override
  public synchronized boolean contains( final Object item )
  {
    return m_itemHash.containsKey( item );
  }

  @Override
  public synchronized boolean containsAll( @SuppressWarnings("rawtypes") final Collection c )
  {
    for( final Object object : c )
    {
      if( !m_itemHash.containsKey( object ) )
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
    return Collections.unmodifiableList( m_items ).iterator();
  }

  /**
   * ATTENTION: Returns an unmodifiable iterator i.e. changing the list via the returned iterator results in an
   * exception.<br/>
   * The iterator is not synchronized, however.
   */
  @Override
  public synchronized ListIterator< ? > listIterator( )
  {
    return Collections.unmodifiableList( m_items ).listIterator();
  }

  /**
   * ATTENTION: Returns an unmodifiable iterator i.e. changing the list via the returned iterator results in an
   * exception.<br/>
   * The iterator is not synchronized, however.
   */
  @Override
  public synchronized ListIterator< ? > listIterator( final int index )
  {
    return Collections.unmodifiableList( m_items ).listIterator( index );
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
  public List< ? > query( final GM_Position pos, @SuppressWarnings("rawtypes") final List result )
  {
    final Rectangle envelope = new Rectangle( (float) pos.getX(), (float) pos.getY(), (float) pos.getX(), (float) pos.getY() );
    return query( envelope, result );
  }

  @Override
  public List< ? > query( final GM_Envelope queryEnv, @SuppressWarnings("rawtypes") final List result )
  {
    checkIndex();

    final Rectangle envelope = toRectangle( queryEnv );
    return query( envelope, result );
  }

  private synchronized List< ? > query( final Rectangle envelope, @SuppressWarnings("rawtypes") final List receiver )
  {
    @SuppressWarnings("unchecked")
    final List<Object> result = receiver == null ? new ArrayList<Object>() : receiver;

    final TIntProcedure ip = new TIntProcedure()
    {
      @Override
      public boolean execute( final int id )
      {
        final SplitSortItem item = getItemById( id );
        final Object data = item.getData();
        result.add( data );
        return true;
      }
    };

    final Rectangle searchRect = envelope == null ? m_spatialIndex.getBounds() : envelope;
    m_spatialIndex.intersects( searchRect, ip );

    return result;
  }

  SplitSortItem getItemById( final int id )
  {
    return m_itemIdHash.get( id );
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
    checkIndex();

    final Rectangle bounds = m_spatialIndex.getBounds();
    if( bounds == null )
      return null;

    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    return new GM_Envelope_Impl( bounds.minX, bounds.minY, bounds.maxX, bounds.maxY, crs );
  }

  @Override
  public synchronized void invalidate( final Object object )
  {
    final SplitSortItem item = m_itemHash.get( object );

    // REMARK: item == null happens often during loading, because item is not yet in list
    if( item != null )
      m_invalidItems.add( item );
  }

  // FEATURE LIST IMPLEMENTATION

  private Feature resolveFeature( final Object object )
  {
    if( object instanceof Feature && !(object instanceof IXLinkedFeature) )
      return (Feature) object;

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
      final Object object = m_items.get( i );

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
    for( final Object object : m_items )
    {
      if( object instanceof Feature && !(object instanceof IXLinkedFeature) )
        visitor.visit( (Feature) object );
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