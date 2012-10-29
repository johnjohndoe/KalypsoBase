/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.gml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Holger Albert
 */
public class ProfileObjectBinding extends Feature_Impl
{
  public static final QName FEATURE_PROFILE_OBJECT = new QName( IWspmConstants.NS_WSPMPROF, "ProfileObject" ); //$NON-NLS-1$

  public static final QName MEMBER_METADATA = new QName( IWspmConstants.NS_WSPMPROF, "metadataMember" ); //$NON-NLS-1$

  private IFeatureBindingCollection<Metadata> m_metadata = null;

  public ProfileObjectBinding( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public String[] getMetadataKeys( )
  {
    final List<String> metadataKeys = new ArrayList<>();

    final IFeatureBindingCollection<Metadata> metadata = getMetadata();
    for( final Metadata existingData : metadata )
      metadataKeys.add( existingData.getKey() );

    return metadataKeys.toArray( new String[] {} );
  }

  public String getMetadata( final String key )
  {
    final IFeatureBindingCollection<Metadata> metadata = getMetadata();
    for( final Metadata existingData : metadata )
    {
      if( existingData.getKey().equals( key ) )
        return existingData.getValue();
    }

    return null;
  }

  IFeatureBindingCollection<Metadata> getMetadata( )
  {
    if( m_metadata == null )
      m_metadata = new FeatureBindingCollection<>( this, Metadata.class, MEMBER_METADATA, true );

    return m_metadata;
  }

  void setMetadata( final String key, final String value )
  {
    final IFeatureBindingCollection<Metadata> metadata = getMetadata();
    for( final Metadata existingData : metadata )
    {
      if( existingData.getKey().equals( key ) )
      {
        existingData.setValue( value );
        return;
      }
    }

    final Metadata newData = metadata.addNew( Metadata.FEATURE_METADATA );
    newData.setKey( key );
    newData.setValue( value );
  }

  String removeMetadata( final String key )
  {
    final IFeatureBindingCollection<Metadata> metadata = getMetadata();
    for( final Metadata existingData : metadata )
    {
      if( existingData.getKey().equals( key ) )
      {
        metadata.remove( existingData );
        return existingData.getValue();
      }
    }

    return null;
  }
}