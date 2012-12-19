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
package org.kalypsodeegree_impl.gml.binding.shape;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.shape.ShapeType;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * Binding for the feature type we create when loading a shape into a gml workspace.<br/>
 * Binding for {org.kalypso.shape}ShapeCollection
 * 
 * @author Gernot Belger
 */
public class ShapeCollection extends Feature_Impl
{
  public static final String SHP_NAMESPACE_URI = "org.kalypso.shape"; //$NON-NLS-1$

  public static final QName FEATURE_SHAPE_COLLECTION = new QName( SHP_NAMESPACE_URI, "ShapeCollection" ); //$NON-NLS-1$

  public static final String MEMBER_FEATURE_LOCAL = "featureMember"; //$NON-NLS-1$

  private static final QName PROPERTY_TYPE = new QName( SHP_NAMESPACE_URI, "type" ); //$NON-NLS-1$

  private IFeatureBindingCollection<AbstractShape> m_shapes;

  public ShapeCollection( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public ShapeType getShapeType( )
  {
    final Integer actualTypeNumber = getProperty( PROPERTY_TYPE, Integer.class );
    return ShapeType.valueOf( actualTypeNumber );
  }

  public void setShapeType( final ShapeType type )
  {
    setProperty( PROPERTY_TYPE, type.getType() );
  }

  public synchronized IFeatureBindingCollection<AbstractShape> getShapes( )
  {
    if( m_shapes == null )
    {
      final QName memberProperty = findMemberProperty();
      m_shapes = new FeatureBindingCollection<>( this, AbstractShape.class, memberProperty );
    }

    return m_shapes;
  }

  @SuppressWarnings( "deprecation" )
  private QName findMemberProperty( )
  {
    final IFeatureType featureType = getFeatureType();
    // REMARK: the collection schema varies with the custom type, so the namespace of the member is not fix
    return featureType.getProperty( MEMBER_FEATURE_LOCAL ).getQName();
  }
}