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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.feature.IFeaturePropertyHandler;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.gml.binding.commons.NamedFeatureHelper;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Implementation of ogc feature
 * 
 * @author doemming
 */
public class Feature_Impl extends PlatformObject implements Feature
{
  private static final GM_Envelope INVALID_ENV = GeometryFactory.createGM_Envelope( 0, 0, 0, 0, null );

  /**
   * all property-values are stored here in sequential order (as defined in application-schema) properties with
   * maxOccurency = 1 are stored direct properties with maxOccurency > 1 are stored in a list properties with
   * maxOccurency = "unbounded" should use FeatureCollections
   */
  private final Object[] m_properties;

  private final IFeatureType m_featureType;

  private String m_id;

  private Object m_parent = null;

  private final IRelationType m_parentRelation;

  private GM_Envelope m_envelope = Feature_Impl.INVALID_ENV;

  protected Feature_Impl( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    if( ft == null )
      throw new UnsupportedOperationException( "must provide a featuretype" );

    m_parent = parent;

    m_parentRelation = parentRelation;
    m_featureType = ft;
    m_id = id;
    m_properties = propValues;
  }

  @Override
  public String getId( )
  {
    return m_id;
  }

  @Override
  public IFeatureType getFeatureType( )
  {
    return m_featureType;
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
    return m_properties;
  }

  /**
   * Accesses a property value of this feature.
   * 
   * @return Value of the given properties. Properties with maxoccurency > 0 (as defined in applicationschema) will be
   *         embedded in java.util.List-objects
   * @see org.kalypsodeegree.model.feature.Feature#getProperty(java.lang.String)
   */
  @Override
  public Object getProperty( final IPropertyType pt )
  {
    if( pt == null )
      throw new IllegalArgumentException( "pt may not null" );

    final int pos = m_featureType.getPropertyPosition( pt );
    final IFeaturePropertyHandler fsh = getPropertyHandler();
    if( pos == -1 && !fsh.isFunctionProperty( pt ) )
    {
      final String msg = String.format( "Unknown property (%s) for type: %s", pt, m_featureType );
      throw new IllegalArgumentException( msg );
    }

    final Object currentValue = pos == -1 ? null : m_properties[pos];

    return fsh.getValue( this, pt, currentValue );
  }

  @Deprecated
  @Override
  public GM_Envelope getEnvelope( )
  {
    return getBoundedBy();
  }

  /**
   * Recalculates the bounding box of this feature.<br/>
   * By default the bounding box is calculated by merging all bounding boxes of all contained geometries within this
   * feature.<br/>
   * Overwrite the change this behavior.
   */
  protected GM_Envelope calculateEnv( )
  {
    GM_Envelope env = null;

    final GM_Object[] geoms = getGeometryPropertyValues();
    for( final GM_Object geometry : geoms )
    {
      final GM_Envelope geomEnv = geometry.getEnvelope();
      if( env == null )
        env = geomEnv;
      else
        env = env.getMerged( geomEnv );
    }

    return env;
  }

  @Override
  public void setProperty( final IPropertyType pt, final Object value )
  {
    final int pos = m_featureType.getPropertyPosition( pt );
    if( pos == -1 )
    {
      final String message = String.format( "Feature[%s] does not know this property %s", toString(), pt.getQName().toString() );
      throw new RuntimeException( new GMLSchemaException( message ) );
    }

    final IFeaturePropertyHandler fsh = getPropertyHandler();
    final Object newValue = fsh.setValue( this, pt, value );

    /* Make sure, inline features are always unregistered */
    final Object oldValue = m_properties[pos];
    unregisterSubFeature( oldValue );

    m_properties[pos] = newValue;

    if( fsh.invalidateEnvelope( pt ) )
    {
      setEnvelopesUpdated();
    }
  }

  /**
   * @deprecated use getProperty(IPropertyType)
   * @see org.kalypsodeegree.model.feature.Feature#getProperty(java.lang.String)
   */
  @Override
  @Deprecated
  public Object getProperty( final String propNameLocalPart )
  {
    if( propNameLocalPart.indexOf( ':' ) > 0 )
      throw new UnsupportedOperationException( propNameLocalPart + " is not a localPart" );

    final IPropertyType pt = m_featureType.getProperty( propNameLocalPart );
    if( pt == null )
      throw new IllegalArgumentException( "unknown local part: " + propNameLocalPart );

    return getProperty( pt );
  }

  @Override
  public Object getProperty( final QName propQName )
  {
    final IPropertyType pt = m_featureType.getProperty( propQName );
    if( pt == null )
    {
      final String message = String.format( "Unknown property:\n\tfeatureType=%s\n\tprop QName=%s", getFeatureType().getQName(), propQName );
      throw new IllegalArgumentException( message );
    }
    return getProperty( pt );
  }

  @Override
  public GMLWorkspace getWorkspace( )
  {
    if( m_parent instanceof GMLWorkspace )
      return (GMLWorkspace)m_parent;
    if( m_parent instanceof Feature )
      return ((Feature)m_parent).getWorkspace();
    return null;
  }

  @Override
  public IRelationType getParentRelation( )
  {
    return m_parentRelation;
  }

  void setWorkspace( final GMLWorkspace workspace )
  {
    if( m_parent != null && m_parent != workspace )
      throw new UnsupportedOperationException( "is not a root feature" ); //$NON-NLS-1$

    m_parent = workspace;
  }

  @Override
  public String toString( )
  {
    final StringBuffer buffer = new StringBuffer( "Feature " );
    if( m_featureType != null )
    {
      buffer.append( m_featureType.getQName().getLocalPart() );
    }
    if( m_id != null )
    {
      buffer.append( "#" + m_id );
    }
    return buffer.toString();
  }

  @Override
  public void setProperty( final QName propQName, final Object value )
  {
    final IFeatureType featureType = getFeatureType();

    final IPropertyType prop = featureType.getProperty( propQName );
    if( prop == null )
      throw new IllegalArgumentException( "Property not found: " + propQName );

    setProperty( prop, value );
  }

  private IFeaturePropertyHandler getPropertyHandler( )
  {
    return FeaturePropertyHandlerFactory.getInstance().getHandler( getFeatureType() );
  }

  /**
   * REMARK: only for internal use. Is used to determine if a property is a function property. Function properties do
   * not get transformed during load.<br/>
   * This is needed in order to prohibit loading of xlinked-workspaces during gml-loading, in order to avoid dead-locks.
   */
  public boolean isFunctionProperty( final IPropertyType pt )
  {
    final IFeaturePropertyHandler propertyHandler = getPropertyHandler();
    return propertyHandler.isFunctionProperty( pt );
  }

  @Override
  public GM_Envelope getBoundedBy( )
  {
    if( m_envelope == Feature_Impl.INVALID_ENV )
      m_envelope = calculateEnv();

    return m_envelope;
  }

  @Override
  public GM_Object getDefaultGeometryPropertyValue( )
  {
    final IValuePropertyType defaultGeomProp = m_featureType.getDefaultGeometryProperty();
    if( defaultGeomProp == null )
      return null;

    final Object prop = getProperty( defaultGeomProp );
    if( defaultGeomProp.isList() )
    {
      final List< ? > props = (List< ? >)prop;
      return (GM_Object)(props.size() > 0 ? props.get( 0 ) : null);
    }

    if( prop == null || prop instanceof GM_Object )
      return (GM_Object)prop;

    throw new IllegalStateException( "Wrong geometry type: " + prop.getClass().getName() );
  }

  @Override
  public GM_Object[] getGeometryPropertyValues( )
  {
    final List<GM_Object> result = new ArrayList<>();
    final IPropertyType[] ftp = m_featureType.getProperties();
    for( final IPropertyType element : ftp )
    {
      if( element instanceof IValuePropertyType && ((IValuePropertyType)element).isGeometry() )
      {
        final Object o = getProperty( element );
        if( o == null )
        {
          continue;
        }

        if( element.isList() )
        {
          result.addAll( (List)o );
        }
        else
        {
          result.add( (GM_Object)o );
        }
      }
    }

    return result.toArray( new GM_Object[result.size()] );
  }

  @Override
  public Feature getOwner( )
  {
    if( m_parent instanceof Feature )
      return (Feature)m_parent;

    return null;
  }

  @Override
  public QName getQualifiedName( )
  {
    return getFeatureType().getQName();
  }

  @Override
  public void setEnvelopesUpdated( )
  {
    m_envelope = INVALID_ENV;

    /* Invalidate geo-index of all feature-list which contains this feature. */
    // TODO: At the moment, only the owning list is invalidated. Lists who link to this feature are invalid but not
    // invalidated.
    // TODO: This code is probably not very performant. How to improve this?
    // Alternative: instead of invalidating: before every query we check if any feature-envelope is invalid
    final Feature parent = getOwner();
    if( parent == null )
      return;

    final IRelationType rt = getParentRelation();
    if( rt != null && rt.isList() )
    {
      // rt relation type and this relation type can differ (different feature workspaces!)
      final IRelationType relation = (IRelationType)parent.getFeatureType().getProperty( rt.getQName() );
      final FeatureList list = (FeatureList)parent.getProperty( relation );
      list.invalidate( this );
    }
  }

  @Override
  public void setFeatureType( final IFeatureType ft )
  {
    throw new UnsupportedOperationException();
  }

  void setId( final String fid )
  {
    m_id = fid;
  }

  @Override
  public String getName( )
  {
    return NamedFeatureHelper.getName( this );
  }

  @Override
  public void setName( final String name )
  {
    NamedFeatureHelper.setName( this, name );
  }

  @Override
  public String getDescription( )
  {
    return NamedFeatureHelper.getDescription( this );
  }

  @Override
  public void setDescription( final String desc )
  {
    NamedFeatureHelper.setDescription( this, desc );
  }

  @Override
  public GM_Object getLocation( )
  {
    final Object property = getProperty( NamedFeatureHelper.GML_LOCATION );
    if( property instanceof GM_Object )
      return (GM_Object)property;

    return null;
  }

  @Override
  public void setLocation( final GM_Object location )
  {
    setProperty( NamedFeatureHelper.GML_LOCATION, location );
  }

  /**
   * feature given the property {@link QName}
   * 
   * @param propertyQName
   *          the {@link QName} of the property to get.
   */
  @SuppressWarnings( "unchecked" )
  protected <T> T getProperty( final QName propertyQName, final Class<T> propClass )
  {
    final Object prop = getProperty( propertyQName );
    try
    {
      if( prop == null )
        return null;

      if( propClass.isAssignableFrom( prop.getClass() ) )
        return (T)prop;

      if( prop instanceof IAdaptable )
        return (T)((IAdaptable)prop).getAdapter( propClass );

      throw new RuntimeException( "Property of type[" + propClass + "] expected " + "\n\tbut found this type :" + prop.getClass() );
    }
    catch( final ClassCastException e )
    {
      throw new RuntimeException( "Property of type[" + propClass + "] expected " + "\n\tbut found this type :" + prop.getClass() );
    }
  }

  protected String getStringProperty( final QName property, final String defaultValue )
  {
    final String value = getProperty( property, String.class );
    if( value == null )
      return defaultValue;

    return value;
  }

  protected boolean getBooleanProperty( final QName property, final boolean defaultValue )
  {
    final Boolean value = getProperty( property, Boolean.class );
    if( value == null )
      return defaultValue;

    return value;
  }

  protected <T> T getProperty( final QName property, final T defaultValue )
  {
    @SuppressWarnings( "unchecked" ) final T value = (T)getProperty( property, defaultValue.getClass() );
    if( value == null )
      return defaultValue;

    return value;
  }

  /**
   * Returns a property that is represented by an enumeration.
   */
  protected <T extends Enum<T>> T getEnumProperty( final QName propertyName, final Class<T> enumType, final T defaultValue )
  {
    final String value = getProperty( propertyName, String.class );
    if( StringUtils.isBlank( value ) )
      return defaultValue;

    try
    {
      return Enum.valueOf( enumType, value );
    }
    catch( final IllegalArgumentException e )
    {
      e.printStackTrace();
      return defaultValue;
    }
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    /*
     * Small performance tweak and also works for new directly instantiated features when not registered with adapter
     * stuff.
     */
    if( adapter.isInstance( this ) )
      return this;

    return super.getAdapter( adapter );
  }

  protected double getDoubleProperty( final QName property, final double defaultValue )
  {
    final Double value = getProperty( property, Double.class );
    if( value == null )
      return defaultValue;

    return value.doubleValue();
  }

  protected int getIntegerProperty( final QName property, final int defaultValue )
  {
    final Integer value = getProperty( property, Integer.class );
    if( value == null )
      return defaultValue;

    return value.intValue();
  }

  @Override
  public Feature getMember( final QName relationName )
  {
    final IRelationType relation = ensureRelation( relationName );
    return getMember( relation );
  }

  @Override
  public Feature getMember( final IRelationType relation )
  {
    final Object linkValue = getProperty( relation );
    if( linkValue == null )
      return null;

    if( linkValue instanceof Feature )
      return (Feature)linkValue;

    final String linkID = (String)linkValue;
    final GMLWorkspace workspace = getWorkspace();
    if( workspace == null )
      return null;

    /* Create temporary xlink from string-link */
    final String href = "#" + linkID; //$NON-NLS-1$
    final Feature linkedFeature = workspace.getFeature( linkID );
    final IFeatureType linkedType = linkedFeature == null ? relation.getTargetFeatureType() : linkedFeature.getFeatureType();

    return new XLinkedFeature_Impl( this, relation, linkedType, href );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final Feature target )
  {
    final IRelationType relation = ensureRelation( relationName );
    final GMLWorkspace sourceWorkspace = m_parent instanceof Feature ? ((Feature)m_parent).getWorkspace() : (GMLWorkspace)m_parent;
    final String href = FeatureLinkUtils.findLinkPath( target, sourceWorkspace );
    return setLink( relation, href );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final String href )
  {
    final IRelationType relation = ensureRelation( relationName );
    return setLink( relation, href );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href )
  {
    final IFeatureType targetFeatureType = relation.getTargetFeatureType();
    return setLink( relation, href, targetFeatureType );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final String href, final QName featureTypeName )
  {
    final IRelationType relation = ensureRelation( relationName );
    return setLink( relation, href, featureTypeName );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final String href, final IFeatureType featureType )
  {
    final IRelationType relation = ensureRelation( relationName );
    return setLink( relation, href, featureType );
  }

  private IRelationType ensureRelation( final QName relationName )
  {
    final IPropertyType relation = getFeatureType().getProperty( relationName );

    if( relation == null )
    {
      final String message = String.format( "Unknown property: %s", relationName ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    if( relation instanceof IRelationType )
      return (IRelationType)relation;

    final String message = String.format( "Property is not a relation: '%s'", relationName ); //$NON-NLS-1$
    throw new IllegalArgumentException( message );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final QName featureTypeName )
  {
    final IFeatureType featureType = GMLSchemaUtilities.getFeatureTypeQuiet( featureTypeName );
    if( featureType == null )
    {
      final String message = String.format( "Unknown feature type: %s", featureTypeName ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    return setLink( relation, href, featureType );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final IFeatureType featureType )
  {
    Assert.isNotNull( relation );

    // TODO: check, old code often created xlinks with null-feature type.
    // Probably we should use the target feature type in this case
    // Assert.isNotNull( featureType );

    /* Check if the target relation is a property of this feature */
    if( relation != getFeatureType().getProperty( relation.getQName() ) )
    {
      final String message = String.format( "Relation '%s' is not a property of this feature", relation.getQName() ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    /* Are links allowed? */
    if( !relation.isLinkAble() )
    {
      final String message = String.format( "Relation '%s' does not support linked features", relation.getQName() ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    if( relation.isList() )
    {
      final String message = String.format( "Relation '%s' is a list, single links cannot be created.", relation.getQName() ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    /* Check if the target type is a substitute of the target type of the relation */
    final IFeatureType targetType = relation.getTargetFeatureType();
    if( featureType != null && !GMLSchemaUtilities.substitutes( targetType, targetType.getQName() ) )
    {
      final String message = String.format( "featureType '%s' does not substitute the allowed target type '%s' of the relation '%s'", featureType.getQName(), targetType.getQName(), relation.getQName() );
      throw new IllegalArgumentException( message );
    }

    if( StringUtils.isBlank( href ) )
    {
      setProperty( relation, null );
      return null;
    }

    /* Create link and set to myself as property */
    final IXLinkedFeature link = new XLinkedFeature_Impl( this, relation, featureType, href );

    // REMARK: backwards compatibility; insert local href as string instead of xlink
    // else, old client code that not correctly resolves the links will break
    final Object linkOrString;
    if( link.getUri() == null )
    {
      if( href.startsWith( "#" ) ) //$NON-NLS-1$
        linkOrString = href.substring( 1 );
      else
        linkOrString = href;
    }
    else
      linkOrString = link;

    setProperty( relation, linkOrString );
    return link;
  }

  @Override
  public Feature createSubFeature( final QName relationName )
  {
    final IRelationType relation = ensureRelation( relationName );
    return createSubFeature( relation );
  }

  @Override
  public Feature createSubFeature( final QName relationName, final QName featureTypeName )
  {
    final IRelationType relation = ensureRelation( relationName );
    return createSubFeature( relation, featureTypeName );
  }

  @Override
  public Feature createSubFeature( final IRelationType relation )
  {
    final IFeatureType targetFeatureType = relation.getTargetFeatureType();
    return createSubFeature( relation, targetFeatureType );
  }

  @Override
  public Feature createSubFeature( final IRelationType relation, final QName featureTypeName )
  {
    final IFeatureType featureType = getWorkspace().getGMLSchema().getFeatureType( featureTypeName );

    if( featureType == null )
    {
      final String message = String.format( "Unknown feature type: %s", featureTypeName );
      throw new IllegalArgumentException( message );
    }

    return createSubFeature( relation, featureType );
  }

  private Feature createSubFeature( final IRelationType relation, final IFeatureType featureType )
  {
    if( featureType.isAbstract() )
    {
      final String message = String.format( "Cannot create feature for type '%s': type is abstract", featureType.getQName() ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    if( relation.isList() )
    {
      final String message = String.format( "Cannot create feature for list property: '%s'", relation.getQName() ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    if( !relation.isInlineAble() )
    {
      final String message = String.format( "Inline feature not supported for property '%s': ", relation.getQName() ); //$NON-NLS-1$
      throw new IllegalArgumentException( message );
    }

    /* Remove and unregister old feature */
    final Object oldFeature = getProperty( relation );
    setProperty( relation, null );
    unregisterSubFeature( oldFeature );

    /* Create and set new feature */
    final GMLWorkspace_Impl workspace = (GMLWorkspace_Impl)getWorkspace();
    final String id = workspace.createFeatureId( featureType );

    final Feature newFeature = FeatureFactory.createFeature( this, relation, id, featureType, true, -1 );
    setProperty( relation, newFeature );

    /* Register new feature into workspace */
    workspace.registerFeature( newFeature );

    return newFeature;
  }

  private void unregisterSubFeature( final Object oldValue )
  {
    if( oldValue == null )
      return;

    /* Nothing to do for linked featrues */
    if( oldValue instanceof String || oldValue instanceof IXLinkedFeature )
      return;

    if( !(oldValue instanceof Feature) )
      return;

    final Feature oldFeature = (Feature)oldValue;

    final GMLWorkspace_Impl workspace = (GMLWorkspace_Impl)getWorkspace();

    /* Unregister everything below the old feature that is no linked */
    workspace.unregisterFeature( oldFeature );
  }

  @Override
  public Feature resolveMember( final QName relationName )
  {
    final IRelationType relation = ensureRelation( relationName );
    return resolveMember( relation );
  }

  @Override
  public Feature resolveMember( final IRelationType relation )
  {
    final Object linkValue = getProperty( relation );
    if( linkValue == null )
      return null;

    if( linkValue instanceof IXLinkedFeature )
      return ((IXLinkedFeature)linkValue).getFeature();

    if( linkValue instanceof Feature )
      return (Feature)linkValue;

    final String linkID = (String)linkValue;
    final GMLWorkspace workspace = getWorkspace();
    if( workspace == null )
      return null;

    return workspace.getFeature( linkID );
  }

  @Override
  public Feature[] resolveMembers( final QName relationName )
  {
    final IRelationType relation = ensureRelation( relationName );
    return resolveMembers( relation );
  }

  @Override
  public Feature[] resolveMembers( final IRelationType relation )
  {
    if( relation.getMaxOccurs() > 1 )
    {
      final IFeatureBindingCollection<Feature> memberList = getMemberList( relation );
      final Feature[] resolvedMembers = new Feature[memberList.size()];

      for( int i = 0; i < memberList.size(); i++ )
      {
        Feature feature = memberList.get( i );

        // TODO: this should not be necessary, but it is still not really clear what happens in the
        // IFeatureBindingCollection; so just in case...
        if( feature instanceof IXLinkedFeature )
          feature = ((IXLinkedFeature)feature).getFeature();

        resolvedMembers[i] = feature;
      }

      return resolvedMembers;
    }
    else
    {
      final Feature resolvedMember = resolveMember( relation );
      if( resolvedMember == null )
        return new Feature[] {};

      return new Feature[] { resolvedMember };
    }
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final QName relationName )
  {
    return getMemberList( relationName, Feature.class );
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final QName relationName, final Class<T> type )
  {
    final IRelationType ensureRelation = ensureRelation( relationName );
    return getMemberList( ensureRelation, type );
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final IRelationType relation )
  {
    return getMemberList( relation, Feature.class );
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final IRelationType relation, final Class<T> type )
  {
    Assert.isNotNull( relation );

    if( !relation.isList() )
    {
      final String message = String.format( "Trying to access a property with maxOccurs='1' as list: %s", relation.getQName() );
      throw new IllegalArgumentException( message );
    }

    return new FeatureBindingCollection<>( this, type, relation.getQName() );
  }

  @Override
  public int removeMember( final QName relationName, final Object toRemove )
  {
    final IRelationType relation = ensureRelation( relationName );
    return removeMember( relation, toRemove );
  }

  @Override
  public int removeMember( final IRelationType relation, final Object toRemove )
  {
    if( relation.isList() )
      return removeListMember( relation, toRemove );
    else
      return removeNonListMember( relation, toRemove );
  }

  private int removeListMember( final IRelationType relation, final Object toRemove )
  {
    final FeatureList members = (FeatureList)getProperty( relation );

    final int posToRemove = findMemberToRemove( members, toRemove );
    if( posToRemove == -1 )
      return -1;

    final Object removedElement = members.remove( posToRemove );

    unregisterSubFeature( removedElement );

    return posToRemove;
  }

  private int findMemberToRemove( final FeatureList members, final Object toRemove )
  {
    for( int i = 0; i < members.size(); i++ )
    {
      final Object element = members.get( i );

      /* In other cases, check for identity */
      if( element == toRemove )
        return i;
    }

    /* nothing found */
    return -1;
  }

  private int removeNonListMember( final IRelationType relation, final Object toRemove )
  {
    final Object property = getProperty( relation );
    if( property == toRemove )
    {
      setProperty( relation, null );

      /* Unregister inline feature */
      unregisterSubFeature( property );

      return 0;
    }

    return -1;
  }

  /**
   * Implemented to make final: reimplementation of equals is forbidden for {@link Feature}s.
   */
  @Override
  public final boolean equals( final Object obj )
  {
    return super.equals( obj );
  }

  /**
   * Implemented to make final: reimplementation of hashCode is forbidden for {@link Feature}s.
   */
  @Override
  public final int hashCode( )
  {
    return super.hashCode();
  }
}