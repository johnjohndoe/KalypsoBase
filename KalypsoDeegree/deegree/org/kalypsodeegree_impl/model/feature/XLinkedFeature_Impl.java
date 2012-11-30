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

import javax.xml.namespace.QName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.deegree.model.spatialschema.GeometryException;
import org.eclipse.core.runtime.PlatformObject;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.gml.binding.commons.NamedFeatureHelper;

/**
 * A Feature implementation which delegates all calls to another feature, proved by a feature provider.
 * <p>
 * Everything is delegated to the provided feature, except #getParent (because this freature stil lives in its own structure).
 * </p>
 * <p>
 * Cannot be used as workspace root.
 * </p>
 * 
 * @author Gernot Belger
 */
public class XLinkedFeature_Impl extends PlatformObject implements IXLinkedFeature
{
  private final Feature m_parentFeature;

  private final IRelationType m_parentRelation;

  private final String m_uri;

  private final String m_featureId;

  private final IFeatureType m_basicFeatureType;

  private IFeatureType m_featureType;

  public XLinkedFeature_Impl( final Feature parentFeature, final IRelationType parentRelation, final IFeatureType featureType, final String href )
  {
    m_parentFeature = parentFeature;
    m_parentRelation = parentRelation;
    m_basicFeatureType = featureType;

    final String trimmed_href = href.trim();

    final int indexOf = trimmed_href.indexOf( '#' );
    if( indexOf == -1 || indexOf == trimmed_href.length() - 1 )
    {
      m_uri = null;
      m_featureId = null;
    }
    else if( indexOf == 0 )
    {
      m_uri = null;
      m_featureId = trimmed_href.substring( 1 );
    }
    else
    {
      m_uri = trimmed_href.substring( 0, indexOf );
      m_featureId = trimmed_href.substring( indexOf + 1 );
    }

    if( m_parentFeature == null )
      throw new IllegalArgumentException( "XLinked Feature must have parent feature: " + m_parentFeature );
  }

  /** Returns the linked feature. */
  @Override
  public final Feature getFeature( )
  {
    final GMLWorkspace workspace = m_parentFeature.getWorkspace();

    // FIXME: nonsense, just cast!
    final GMLWorkspace_Impl workspaceImpl = (GMLWorkspace_Impl)workspace.getAdapter( GMLWorkspace_Impl.class );
    if( workspaceImpl == null || m_featureId == null )
    {
      // REMARK: This may happen while loading the gml, so we ignore it and all access to
      // getFeature() should check for null
      return null;
    }

    final GMLWorkspace linkedWorkspace = m_uri == null ? workspaceImpl : workspaceImpl.getLinkedWorkspace( m_uri );
    if( linkedWorkspace == null )
      throw new IllegalStateException( String.format( "Could not resolve xlinked workspace: %s", m_uri ) );

    final Feature feature = linkedWorkspace.getFeature( m_featureId );
    if( feature == null )
      throw new IllegalStateException( "No feature found at: " + m_uri + "#" + m_featureId );

    /* The first time we access the real feature, get our real feature type. */
    if( m_featureType == null )
      m_featureType = feature.getFeatureType();

    return feature;
  }

  @Override
  public String getId( )
  {
    // return null in order to let the workspace generate internal ids
    return null;

    // do not access the provider/feature, because else we already access the remote workspace while loading
    // the old one, this leading to dead-locks
    // final IFeatureProvider provider = getProvider( getWorkspace() );
    // return provider.getId();
  }

  @Override
  public IFeatureType getFeatureType( )
  {
    /* As long as the feature was not accessed, we only know the target feature type of our defining property. */
    if( m_featureType != null )
      return m_featureType;

    return m_basicFeatureType;
  }

  /**
   * @return array of properties, properties with maxoccurency>0 (as defined in applicationschema) will be embedded in
   *         java.util.List-objects
   * @see org.kalypsodeegree.model.feature.Feature#getProperties()
   */
  @Override
  @Deprecated
  public Object[] getProperties( )
  {
    final Feature feature = getFeature();
    return feature.getProperties();
  }

  /**
   * format of name if "namespace:name" or just "name" - both will work
   * 
   * @return array of properties, properties with maxoccurency>0 (as defined in applicationschema) will be embedded in
   *         java.util.List-objects
   * @see org.kalypsodeegree.model.feature.Feature#getProperty(java.lang.String)
   */
  @Override
  public Object getProperty( final IPropertyType pt )
  {
    final Feature feature = getFeature();
    if( feature == null )
      return null;

    return feature.getProperty( pt );
  }

  @Override
  public GM_Envelope getEnvelope( )
  {
    try
    {
      return getBoundedBy();
    }
    catch( final GeometryException e )
    {
      e.printStackTrace();

      return null;
    }
  }

  @Override
  public void setProperty( final IPropertyType pt, final Object value )
  {
    final Feature feature = getFeature();
    if( feature != null )
      feature.setProperty( pt, value );
  }

  /**
   * @deprecated use getProperty(IPropertyType)
   * @see org.kalypsodeegree.model.feature.Feature#getProperty(java.lang.String)
   */
  @Override
  @Deprecated
  public Object getProperty( final String propNameLocalPart )
  {
    final Feature feature = getFeature();
    if( feature == null )
      return null;
    return feature.getProperty( propNameLocalPart );
  }

  @Override
  public Object getProperty( final QName propQName )
  {
    final Feature feature = getFeature();
    if( feature == null )
      return null;
    return feature.getProperty( propQName );
  }

  /**
   * Returns the workspace of the linked feature.
   * 
   * @see org.kalypsodeegree.model.feature.Feature#getWorkspace()
   */
  @Override
  public GMLWorkspace getWorkspace( )
  {
    return m_parentFeature.getWorkspace();
  }

  @Override
  public String toString( )
  {
    final Feature feature = getFeature();
    return feature == null ? "null" : FeatureHelper.getAnnotationValue( feature, IAnnotation.ANNO_LABEL );
  }

  @Override
  public void setProperty( final QName propQName, final Object value )
  {
    final Feature feature = getFeature();
    if( feature != null )
      feature.setProperty( propQName, value );
  }

  @Override
  public String getHref( )
  {
    if( m_uri == null )
      return "#" + m_featureId;

    return m_uri + "#" + m_featureId;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( !(obj instanceof IXLinkedFeature) )
      return false;

    final XLinkedFeature_Impl other = (XLinkedFeature_Impl)obj;
    return new EqualsBuilder().append( m_uri, other.m_uri ).append( m_featureId, other.m_featureId ).isEquals();
  }

  @Override
  public int hashCode( )
  {
    return new HashCodeBuilder().append( m_uri ).append( m_featureId ).toHashCode();
  }

  @Override
  public IRelationType getParentRelation( )
  {
    return m_parentRelation;
  }

  @Override
  public String getUri( )
  {
    return m_uri;
  }

  @Override
  public String getFeatureId( )
  {
    return m_featureId;
  }

  @Override
  public GM_Envelope getBoundedBy( ) throws GeometryException
  {
    final Feature feature = getFeature();
    if( feature == null )
      return null;

    return feature.getBoundedBy();
  }

  @Override
  public GM_Object getDefaultGeometryPropertyValue( )
  {
    return getFeature().getDefaultGeometryPropertyValue();
  }

  @Override
  public GM_Object[] getGeometryPropertyValues( )
  {
    return getFeature().getGeometryPropertyValues();
  }

  @Override
  public Feature getOwner( )
  {
    return m_parentFeature;
  }

  @Override
  public QName getQualifiedName( )
  {
    return getFeatureType().getQName();
  }

  @Override
  public void setEnvelopesUpdated( )
  {
    final Feature feature = getFeature();
    if( feature != null )
      feature.setEnvelopesUpdated();
  }

  @Override
  public void setFeatureType( final IFeatureType ft )
  {
    m_featureType = ft;
  }

  @Override
  public String getName( )
  {
    return NamedFeatureHelper.getName( getFeature() );
  }

  @Override
  public void setName( final String name )
  {
    NamedFeatureHelper.setName( getFeature(), name );
  }

  @Override
  public String getDescription( )
  {
    return NamedFeatureHelper.getDescription( getFeature() );
  }

  @Override
  public void setDescription( final String desc )
  {
    NamedFeatureHelper.setDescription( getFeature(), desc );
  }

  @Override
  public GM_Object getLocation( )
  {
    final Object property = getFeature().getProperty( NamedFeatureHelper.GML_LOCATION );
    if( property instanceof GM_Object )
      return (GM_Object)property;

    return null;
  }

  @Override
  public void setLocation( final GM_Object location )
  {
    getFeature().setProperty( NamedFeatureHelper.GML_LOCATION, location );
  }

  @Override
  public Feature getMember( final QName relation )
  {
    return getFeature().getMember( relation );
  }

  @Override
  public Feature getMember( final IRelationType relation )
  {
    return getFeature().getMember( relation );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final Feature target )
  {
    return getFeature().setLink( relationName, target );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href )
  {
    return getFeature().setLink( relation, href );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final String href )
  {
    return getFeature().setLink( relationName, href );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final QName featureType )
  {
    return getFeature().setLink( relation, href, featureType );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final IFeatureType featureType )
  {
    return getFeature().setLink( relation, href, featureType );
  }

  @Override
  public IXLinkedFeature setLink( final QName relation, final String href, final QName featureType )
  {
    return getFeature().setLink( relation, href, featureType );
  }

  @Override
  public IXLinkedFeature setLink( final QName relation, final String href, final IFeatureType featureType )
  {
    return getFeature().setLink( relation, href, featureType );
  }

  @Override
  public Feature createSubFeature( final IRelationType relation, final QName featureTypeName )
  {
    return getFeature().createSubFeature( relation, featureTypeName );
  }

  @Override
  public Feature createSubFeature( final IRelationType relation )
  {
    return getFeature().createSubFeature( relation );
  }

  @Override
  public Feature createSubFeature( final QName relationName, final QName featureTypeName )
  {
    return getFeature().createSubFeature( relationName, featureTypeName );
  }

  @Override
  public Feature createSubFeature( final QName relationName )
  {
    return getFeature().createSubFeature( relationName );
  }

  @Override
  public Feature resolveMember( final IRelationType relation )
  {
    return getFeature().resolveMember( relation );
  }

  @Override
  public Feature resolveMember( final QName relation )
  {
    return getFeature().resolveMember( relation );
  }

  @Override
  public Feature[] resolveMembers( final IRelationType relation )
  {
    return getFeature().resolveMembers( relation );
  }

  @Override
  public Feature[] resolveMembers( final QName relation )
  {
    return getFeature().resolveMembers( relation );
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final QName relationName )
  {
    return getFeature().getMemberList( relationName );
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final QName relationName, final Class<T> type )
  {
    return getFeature().getMemberList( relationName, type );
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final IRelationType relation )
  {
    return getFeature().getMemberList( relation );
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final IRelationType relation, final Class<T> type )
  {
    return getFeature().getMemberList( relation, type );
  }

  @Override
  public int removeMember( final QName relationName, final Object toRemove )
  {
    return getFeature().removeMember( relationName, toRemove );
  }

  @Override
  public int removeMember( final IRelationType relation, final Object toRemove )
  {
    return getFeature().removeMember( relation, toRemove );
  }
}