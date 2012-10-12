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
package org.kalypsodeegree_impl.model.feature.visitors;

import java.util.HashMap;
import java.util.Map;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;

/**
 * Indiziert alle besuchten Features anhand einer ihrer Properties. Die Features werden in eine HashMap id -> feature
 * gesteckt.
 * <p>
 * Es kann aber auch nach der Feature-ID gehacht werden.
 * </p>
 *
 * @author bce
 */
public class IndexFeaturesVisitor implements FeatureVisitor
{
  private final String m_indexProperty;

  private final Map<Object, Feature> m_index = new HashMap<>();

  public IndexFeaturesVisitor( final String indexProperty )
  {
    m_indexProperty = indexProperty;
  }

  @Override
  public boolean visit( final Feature f )
  {
    final Object property = getProperty( f );

    if( m_index.containsKey( property ) )
    {
      System.err.println( String.format( "Duplicate feature index: %s", property ) );
    }

    m_index.put( property, f );

    return true;
  }

  private Object getProperty( final Feature feature )
  {
    if( m_indexProperty == null )
      return feature.getId();

    final IFeatureType featureType = feature.getFeatureType();
    final IPropertyType pt = featureType.getProperty( m_indexProperty );
    if( pt == null )
    {
      System.err.println( String.format( "Unknown feature property: ", m_indexProperty ) );
      return null;
    }

    final Object value = feature.getProperty( pt );

    /* Special handling for gml:name */
    if( Feature.QN_NAME.equals( pt.getQName() ) )
      return feature.getName();

    return value;
  }

  public Map<Object, Feature> getIndex( )
  {
    return m_index;
  }
}
