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
package org.kalypso.ogc.gml.dict;

import javax.xml.namespace.QName;

import org.deegree.model.spatialschema.GeometryException;
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
 * Another feature implementation used by the dictionary catalog.
 * <p>
 * There are two reasons for a new implementation:
 * </p>
 * <p>
 * 1) This distinguishes between separate calls to {@link org.kalypso.ogc.gml.dict.DictionaryCatalog#getEntry(String)}, so releasing works fine.
 * </p>
 * <p>
 * 2) This feature does not support change of any values, which is prohibited for dictionary entries.
 * </p>
 * 
 * @author Gernot Belger
 */
public class DictionaryFeature implements Feature
{
  private static final String DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED = "Dictionary entries may not be changed."; //$NON-NLS-1$

  private final Feature m_feature;

  public DictionaryFeature( final Feature feature )
  {
    m_feature = feature;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    return m_feature.getAdapter( adapter );
  }

  @Override
  public GM_Envelope getEnvelope( )
  {
    return m_feature.getEnvelope();
  }

  @Override
  public IFeatureType getFeatureType( )
  {
    return m_feature.getFeatureType();
  }

  @Override
  public String getId( )
  {
    return m_feature.getId();
  }

  @Override
  public IRelationType getParentRelation( )
  {
    return m_feature.getParentRelation();
  }

  @Override
  public Object[] getProperties( )
  {
    return m_feature.getProperties();
  }

  @Override
  public Object getProperty( final IPropertyType propertyType )
  {
    return m_feature.getProperty( propertyType );
  }

  @Override
  public Object getProperty( final QName propQName )
  {
    return m_feature.getProperty( propQName );
  }

  @Override
  public Object getProperty( final String propLocalName )
  {
    return m_feature.getProperty( propLocalName );
  }

  @Override
  public GMLWorkspace getWorkspace( )
  {
    return m_feature.getWorkspace();
  }

  @Override
  public void setProperty( final IPropertyType propertyType, final Object value )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public void setProperty( final QName propQName, final Object value )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public GM_Envelope getBoundedBy( ) throws GeometryException
  {
    return m_feature.getBoundedBy();
  }

  @Override
  public GM_Object getDefaultGeometryPropertyValue( )
  {
    return m_feature.getDefaultGeometryPropertyValue();
  }

  @Override
  public GM_Object[] getGeometryPropertyValues( )
  {
    return m_feature.getGeometryPropertyValues();
  }

  @Override
  public Feature getOwner( )
  {
    return m_feature.getOwner();
  }

  @Override
  public QName getQualifiedName( )
  {
    return m_feature.getQualifiedName();
  }

  @Override
  public void setEnvelopesUpdated( )
  {
    m_feature.setEnvelopesUpdated();
  }

  @Override
  public void setFeatureType( final IFeatureType ft )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public String getName( )
  {
    return NamedFeatureHelper.getName( m_feature );
  }

  @Override
  public void setName( final String name )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public String getDescription( )
  {
    return NamedFeatureHelper.getDescription( m_feature );
  }

  @Override
  public void setDescription( final String desc )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public GM_Object getLocation( )
  {
    final Object property = m_feature.getProperty( NamedFeatureHelper.GML_LOCATION );
    if( property instanceof GM_Object )
      return (GM_Object)property;

    return null;
  }

  @Override
  public void setLocation( final GM_Object location )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature getMember( final QName relation )
  {
    return m_feature.getMember( relation );
  }

  @Override
  public Feature getMember( final IRelationType relation )
  {
    return m_feature.getMember( relation );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final Feature href )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final String href )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final QName featureType )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final IFeatureType featureType )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IXLinkedFeature setLink( final QName relation, final String href, final QName featureType )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IXLinkedFeature setLink( final QName relation, final String href, final IFeatureType featureType )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature createSubFeature( final IRelationType relation, final QName featureTypeName )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature createSubFeature( final IRelationType relation )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature createSubFeature( final QName relationName, final QName featureTypeName )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature createSubFeature( final QName relationName )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final QName relationName )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final QName relationName, final Class<T> type )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final IRelationType relation )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final IRelationType relation, final Class<T> type )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature resolveMember( final IRelationType relation )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature resolveMember( final QName relation )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature[] resolveMembers( final IRelationType relation )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public Feature[] resolveMembers( final QName relation )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public int removeMember( final IRelationType relation, final Object toRemove )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }

  @Override
  public int removeMember( final QName relationName, final Object toRemove )
  {
    throw new UnsupportedOperationException( DICTIONARY_ENTRIES_MAY_NOT_BE_CHANGED );
  }
}