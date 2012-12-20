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
package org.kalypsodeegree_impl.model.feature;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * A {@link Feature} implementation that does absolute nothing. Useful for classes that needs to implement the {@link Feature} interface but do not need to inherit from {@link Feature_Impl}.
 * 
 * @author Dirk Kuch
 */
public abstract class AbstractEmptyFeature implements Feature
{
  @Override
  public String getDescription( )
  {
    return null;
  }

  @Override
  public GM_Object getLocation( )
  {
    return null;
  }

  @Override
  public String getName( )
  {
    return null;
  }

  @Override
  public void setDescription( final String desc )
  {
    // nothing to do
  }

  @Override
  public void setLocation( final GM_Object location )
  {
    // nothing to do
  }

  @Override
  public void setName( final String name )
  {
    // nothing to do
  }

  @Deprecated
  @Override
  public GM_Envelope getEnvelope( )
  {
    return null;
  }

  @Override
  public IRelationType getParentRelation( )
  {
    return null;
  }

  @Deprecated
  @Override
  public Object getProperty( final String propLocalName )
  {
    return null;
  }

  @Override
  public Object getProperty( final QName propQName )
  {
    return null;
  }

  @Override
  public GMLWorkspace getWorkspace( )
  {
    return null;
  }

  @Override
  public void setProperty( final IPropertyType propertyType, final Object value )
  {
    // nothing to do
  }

  @Override
  public void setProperty( final QName propQName, final Object value )
  {
    // nothing to do
  }

  @Override
  public IFeatureType getFeatureType( )
  {
    return null;
  }

  @Override
  public String getId( )
  {
    return null;
  }

  @Deprecated
  @Override
  public Object[] getProperties( )
  {
    return new Object[] {};
  }

  @Override
  public Object getProperty( final IPropertyType propertyType )
  {
    return null;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    return null;
  }

  @Override
  public GM_Envelope getBoundedBy( )
  {
    return null;
  }

  @Override
  public GM_Object getDefaultGeometryPropertyValue( )
  {
    return null;
  }

  @Override
  public GM_Object[] getGeometryPropertyValues( )
  {
    return new GM_Object[] {};
  }

  @Override
  public Feature getOwner( )
  {
    return null;
  }

  @Override
  public QName getQualifiedName( )
  {
    return null;
  }

  @Override
  public void setEnvelopesUpdated( )
  {
    // nothing to do
  }

  @Override
  public void setFeatureType( final IFeatureType ft )
  {
    // nothing to do
  }

  @Override
  public Feature getMember( final IRelationType relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature getMember( final QName relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final Feature href )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IXLinkedFeature setLink( final QName relationName, final String href )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final IFeatureType featureType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IXLinkedFeature setLink( final IRelationType relation, final String href, final QName featureType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IXLinkedFeature setLink( final QName relation, final String href, final IFeatureType featureType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IXLinkedFeature setLink( final QName relation, final String href, final QName featureType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature createSubFeature( final IRelationType relation, final QName featureTypeName )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature createSubFeature( final IRelationType relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature createSubFeature( final QName relationName, final QName featureTypeName )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature createSubFeature( final QName relationName )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final IRelationType relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final IRelationType relation, final Class<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFeatureBindingCollection<Feature> getMemberList( final QName relationName )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Feature> IFeatureBindingCollection<T> getMemberList( final QName relationName, final Class<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature resolveMember( final IRelationType relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature resolveMember( final QName relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature[] resolveMembers( final IRelationType relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature[] resolveMembers( final QName relation )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int removeMember( final QName relationName, final Object toRemove )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int removeMember( final IRelationType relation, final Object toRemove )
  {
    throw new UnsupportedOperationException();
  }
}