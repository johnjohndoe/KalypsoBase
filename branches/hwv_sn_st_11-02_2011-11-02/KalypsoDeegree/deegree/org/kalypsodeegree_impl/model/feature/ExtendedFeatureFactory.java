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
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Extended Feature Factory. Difference to old Feature Factory: looks for IFeatureBinding handlers and returns special
 * feature Implementations
 * 
 * @author Dirk Kuch
 */
public final class ExtendedFeatureFactory
{
  private ExtendedFeatureFactory( )
  {
  }

  public static Feature getFeature( final Feature parent, final IRelationType parentRelation, final IFeatureType featureType, final String id, final Object[] properties )
  {
    return createFeature( parent, parentRelation, featureType, id, properties, featureType );
  }

  private static Feature createFeature( final Feature parent, final IRelationType parentRelation, final IFeatureType featureType, final String id, final Object[] properties, final IFeatureType targetType )
  {
    if( featureType == null )
      throw new IllegalArgumentException( "must provide a featuretype" );

    /* get feature binding class for feature type */
    final QName qName = featureType.getQName();

    if( Feature.QNAME_FEATURE.equals( qName ) )
      return new Feature_Impl( parent, parentRelation, targetType, id, properties );

    return createBindingFeature( parent, parentRelation, featureType, id, properties, targetType, qName );
  }

  protected static Feature createBindingFeature( final Feature parent, final IRelationType parentRelation, final IFeatureType featureType, final String id, final Object[] properties, final IFeatureType targetType, final QName qName )
  {
    final FeatureBindingConstructor constructor = FeatureBindingConstructor.findBindingConstructor( qName );
    if( constructor != null )
    {
      final Feature instance = constructor.createInstance( parent, parentRelation, id, properties, targetType );
      if( instance != null )
        return instance;
      // FALLTHROUGH: in case of exception build a normal feature
    }

    final IFeatureType substitutionFT = featureType.getSubstitutionGroupFT();
    if( substitutionFT == null )
    {
      // Should never happen, all features must substitute gml:Feature
      return new Feature_Impl( parent, parentRelation, targetType, id, properties );
    }

    return createFeature( parent, parentRelation, substitutionFT, id, properties, targetType );
  }
}
