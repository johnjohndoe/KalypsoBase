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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.PlatformObject;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IWorkspaceProvider;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;

/**
 * In order to use this workspace with support of xlinks, a {@link org.kalypsodeegree_impl.model.feature.IFeatureProviderFactory} must be set.
 * 
 * @see #setFeatureProviderFactory(IFeatureProviderFactory)
 * @author doemming
 */
public class GMLWorkspace_Impl extends PlatformObject implements GMLWorkspace
{
  /** id -> feature */
  final Map<String, Feature> m_indexMap = new HashMap<>();

  private final Feature m_rootFeature;

  /** The namespace context for which to resolve any namespace-prefixes inside this workspace. */
  private final NamespaceContext m_namespaceContext;

  private String m_schemaLocation;

  private final IGMLSchema m_schema;

  private final FeatureProviderFactoryCache m_factory;

  /** The url-context against which to resolve any references inside this workspace. */
  private final URL m_context;

  public GMLWorkspace_Impl( final IGMLSchema schema, final Feature feature, final URL context, final NamespaceContext namespaceContext, final String schemaLocation, final IFeatureProviderFactory factory )
  {
    m_schema = schema;
    m_context = context;
    m_namespaceContext = namespaceContext;
    m_schemaLocation = schemaLocation;
    m_rootFeature = feature;

    m_factory = new FeatureProviderFactoryCache( factory );

    if( m_rootFeature instanceof Feature_Impl )
      ((Feature_Impl)m_rootFeature).setWorkspace( this );

    try
    {
      // REMARK: In some old gml files the root feature has no root id set, so it is corrected here.
      // REMARK: In future each root feature must have an id.
      final String id = m_rootFeature.getId();
      if( StringUtils.isBlank( id ) )
        ((Feature_Impl)m_rootFeature).setId( "root" );

      registerFeature( m_rootFeature );
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
    }

    /* Hook registered listeners to myself */
    addModellListener( new GMLWorkspaceModellListener( this ) );
  }

  @Override
  public void dispose( )
  {
    m_listener.clear();
    m_factory.dispose();
  }

  @Override
  protected void finalize( ) throws Throwable
  {
    dispose();

    super.finalize();
  }

  @Override
  public Feature getFeature( final String id )
  {
    return m_indexMap.get( id );
  }

  @Override
  public Feature[] resolveLinks( final Feature srcFeature, final IRelationType linkProperty )
  {
    if( !linkProperty.isList() )
    {
      final Feature feature = srcFeature.getMember( linkProperty );
      if( feature == null )
        return new Feature[] {};

      return new Feature[] { feature };
    }

    final List<Feature> result = new ArrayList<>();
    final List< ? > linkList = (List< ? >)srcFeature.getProperty( linkProperty );

    for( final Object linkValue : linkList )
    {
      // TODO: same code as in resolveLink -> move to common place
      if( linkValue instanceof Feature )
      {
        result.add( (Feature)linkValue );
        continue;
      }

      // must be a reference
      final String linkID = (String)linkValue;
      result.add( getFeature( linkID ) );
    }

    return result.toArray( new Feature[result.size()] );
  }

  @Override
  public Feature getRootFeature( )
  {
    return m_rootFeature;
  }

  private final Collection<ModellEventListener> m_listener = new HashSet<>();

  /**
   * Every listener is registered only once.
   * 
   * @see org.kalypsodeegree.model.feature.event.ModellEventProvider#addModellListener(org.kalypsodeegree.model.feature.event.ModellEventListener)
   */
  @Override
  public void addModellListener( final ModellEventListener listener )
  {
    m_listener.add( listener );
  }

  @Override
  public void removeModellListener( final ModellEventListener listener )
  {
    m_listener.remove( listener );
  }

  @Override
  public void fireModellEvent( final ModellEvent event )
  {
    // use array instead of iterator, because the listener list may change in
    // response to this event (lead to a ConcurrentodificationException)
    final ModellEventListener[] objects = m_listener.toArray( new ModellEventListener[m_listener.size()] );
    for( final ModellEventListener element : objects )
    {
      try
      {
        element.onModellChange( event );
      }
      catch( final Throwable t )
      {
        KalypsoDeegreePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }
  }

  @Override
  public URL getContext( )
  {
    return m_context;
  }

  @Override
  public void accept( final FeatureVisitor fv, final int depth )
  {
    accept( fv, m_rootFeature, depth );
  }

  @Override
  public void accept( final FeatureVisitor fv, final Feature feature, final int depth )
  {
    accept( fv, feature, depth, feature.getFeatureType().getProperties() );
  }

  @Override
  public void accept( final FeatureVisitor fv, final Feature feature, final int depth, final IPropertyType[] ftps )
  {
    final boolean recurse = fv.visit( feature );

    if( recurse && depth != FeatureVisitor.DEPTH_ZERO )
    {
      for( final IPropertyType element : ftps )
      {
        if( element instanceof IRelationType )
        {
          final Object value = feature.getProperty( element );
          if( value == null )
            continue;

          if( value instanceof IXLinkedFeature )
          {
            if( depth == FeatureVisitor.DEPTH_INFINITE_LINKS )
            {
              final Feature f = getFeature( (String)value );
              accept( fv, f, depth );
            }
          }
          else if( value instanceof Feature )
          {
            final Feature f = (Feature)value;
            accept( fv, f, depth );
          }
          else if( value instanceof List )
            accept( fv, (List< ? >)value, depth );
          else if( value instanceof String && depth == FeatureVisitor.DEPTH_INFINITE_LINKS )
          {
            final Feature f = getFeature( (String)value );
            accept( fv, f, depth );
          }
        }
      }
    }
  }

  @Override
  public void accept( final FeatureVisitor fv, final List< ? > features, final int depth )
  {
    for( final Object next : features )
    {
      if( next instanceof String && depth == FeatureVisitor.DEPTH_INFINITE_LINKS )
      {
        final Feature f = getFeature( (String)next );
        accept( fv, f, depth );
      }
      else if( next instanceof IXLinkedFeature && depth == FeatureVisitor.DEPTH_INFINITE_LINKS )
      {
        final Feature f = ((IXLinkedFeature)next).getFeature();
        accept( fv, f, depth );
      }
      else if( !(next instanceof IXLinkedFeature) && next instanceof Feature )
        accept( fv, (Feature)next, depth );
    }
  }

  @Override
  public Object getFeatureFromPath( final String featurePath )
  {
    try
    {
      if( featurePath == null )
        return null;

      final FeaturePath fPath = new FeaturePath( featurePath );
      return fPath.getFeature( this );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String getSchemaLocationString( )
  {
    return m_schemaLocation;
  }

  /**
   * Creates a new feature and registers it with this workspace.
   */
  @Override
  public Feature createFeature( final Feature parent, final IRelationType parentRelation, final IFeatureType type )
  {
    return createFeature( parent, parentRelation, type, 0 );
  }

  /**
   * Creates a new feature and registers it with this workspace.
   */
  @Override
  public Feature createFeature( final Feature parent, final IRelationType parentRelation, final IFeatureType type, final int depth )
  {
    if( type.isAbstract() )
    {
      // TODO: throw an exception?
      // throw new IllegalArgumentException( "Cannot create feature from abstract type: " + type );
      System.out.println( "Creating feature from abstract type: " + type );
    }

    // TODO: @andreas: merge createFeature method with the addFeatureAsComposite method (add the IRelationType )
    final String newId = createFeatureId( type );
    final Feature newFeature = FeatureFactory.createFeature( parent, parentRelation, newId, type, true, depth );

    // TODO: because we nowadys do recurse, another feature might be created meanwhile
    // so there is a chance, that an id is used twice
    registerFeature( newFeature );

    return newFeature;
  }

  String createFeatureId( final IFeatureType type )
  {
    // REMARK: Performance Bufix
    // The commented code (see below) caused a serious performance
    // problem to long lists of homogenous features.

    // SLOW: do not comment in!
    // int no = 0;
    // while( m_indexMap.containsKey( id + Integer.toString( no ) ) )
    // no++;
    // return id + Integer.toString( no );

    // We now create random numbered feature ids,
    // which normally should lead to only one try for finding a new id
    final String name = type.getQName().getLocalPart();
    while( true )
    {
      final long rnd = Math.round( Math.random() * m_indexMap.size() );
      final long time = System.currentTimeMillis();
      final String id = name + time + rnd;
      if( !m_indexMap.containsKey( id ) )
        return id;
    }
  }

  @Override
  public void addFeatureAsComposition( final Feature parent, final IRelationType propName, final int pos, final Feature newFeature ) throws Exception
  {
    final Object prop = parent.getProperty( propName );

    if( prop instanceof List )
    {
      final List list = (List< ? >)prop;
      // when pos = -1 -> append to end of the list
      if( pos == -1 )
        list.add( newFeature );
      else
        list.add( pos, newFeature );
    }
    else
      parent.setProperty( propName, newFeature );

    m_indexMap.put( newFeature.getId(), newFeature );

    // register also features in subtree of new feature
    registerFeature( newFeature );
    return;
  }

  @Override
  public boolean removeLinkedAsAggregationFeature( final Feature parentFeature, final IRelationType linkProp, final String childFeatureId )
  {
    final Object prop = parentFeature.getProperty( linkProp );

    if( linkProp.isList() )
      return ((List< ? >)prop).remove( childFeatureId );

    if( childFeatureId.equals( parentFeature.getProperty( linkProp ) ) )
    {
      parentFeature.setProperty( linkProp, null );
      return true;
    }

    return false;
  }

  @Override
  public boolean removeLinkedAsCompositionFeature( final Feature parentFeature, final IRelationType linkProp, final Feature childFeature )
  {
    boolean result = false;
    final Object prop = parentFeature.getProperty( linkProp );
    if( linkProp.isList() )
    {
      final List< ? > list = (List< ? >)prop;
      result = list.remove( childFeature );
    }
    else
    {
      if( parentFeature.getProperty( linkProp ) == childFeature )
      {
        parentFeature.setProperty( linkProp, null );
        result = true;
      }
    }

    if( result )
    {
      unregisterFeature( childFeature );
    }
    return result;
  }

  /**
   * Unregisters a feature from this workspace.<br/>
   * Does not recurse into sub features.
   */
  void unregister( final Feature feature )
  {
    final String id = feature.getId();
    m_indexMap.remove( id );
  }

  /**
   * Registers a feature in this workspace.<br/>
   * Does not recurse into sub features.
   */
  void register( final Feature f )
  {
    m_indexMap.put( f.getId(), f );
  }

  @Override
  public void accept( final FeatureVisitor fv, final String featurePath, final int depth )
  {
    final Object featureFromPath = getFeatureFromPath( featurePath );
    if( featureFromPath instanceof Feature )
      accept( fv, (Feature)featureFromPath, depth );
    else if( featureFromPath instanceof FeatureList )
      accept( fv, (FeatureList)featureFromPath, depth );
    else
      throw new IllegalArgumentException( "FeaturePath is neither Feature nor FeatureList: " + featurePath );
  }

  @Override
  public boolean contains( final Feature feature )
  {
    if( feature == null )
      return false;
    return m_indexMap.containsKey( feature.getId() );
  }

  @Override
  public boolean isBrokenLink( final Feature parentFeature, final IPropertyType ftp, final int pos )
  {
    final Object property = parentFeature.getProperty( ftp );
    if( property == null )
      return false;
    if( property instanceof List )
    {
      final Object object = ((List< ? >)property).get( pos );
      if( object instanceof Feature )
        return false;
      return !m_indexMap.containsKey( object );
    }
    if( property instanceof Feature )
      return false;
    return !m_indexMap.containsKey( property );
  }

  @Override
  public IGMLSchema getGMLSchema( )
  {
    return m_schema;
  }

  @Override
  public IFeatureProviderFactory getFeatureProviderFactory( )
  {
    return m_factory.getFactory();
  }

  @Override
  public NamespaceContext getNamespaceContext( )
  {
    return m_namespaceContext;
  }

  @Override
  public void setSchemaLocation( final String schemaLocation )
  {
    m_schemaLocation = schemaLocation;
  }

  protected GMLWorkspace getLinkedWorkspace( final String uri )
  {
    final IWorkspaceProvider provider = m_factory.getFeatureProvider( this, uri );
    if( provider == null )
      return null;

    return provider.getWorkspace();
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == getClass() )
      return this;

    return super.getAdapter( adapter );
  }

  /**
   * Registers a feature and its sub features into this workspace.<br/>
   * ONLY intended to be used within this implementatino, NOT by clients.
   */
  public void registerFeature( final Feature feature )
  {
    accept( new RegisterVisitor( this ), feature, FeatureVisitor.DEPTH_INFINITE );
  }

  /**
   * Unregisters a feature and its sub features from this workspace.<br/>
   * ONLY intended to be used within this implementatino, NOT by clients.
   */
  public void unregisterFeature( final Feature feature )
  {
    accept( new UnRegisterVisitor( this ), feature, FeatureVisitor.DEPTH_INFINITE );
  }
}