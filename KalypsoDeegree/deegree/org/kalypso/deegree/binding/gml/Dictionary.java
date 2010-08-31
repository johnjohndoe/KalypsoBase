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
package org.kalypso.deegree.binding.gml;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Gernot Belger
 */
public class Dictionary extends Feature_Impl
{
  public static final QName QNAME_Dictionary = new QName( NS.GML3, "Dictionary" );

  private static final QName QNAME_PROPERTY_dictionaryEntry = new QName( NS.GML3, "dictionaryEntry" );

  private static final QName QNAME_PROPERTY_indirectEntry = new QName( NS.GML3, "indirectEntry" );

  public Dictionary( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public Definition[] getAllDefinitions( )
  {
    final FeatureList directEntries = getProperty( QNAME_PROPERTY_dictionaryEntry, FeatureList.class );
    // TODO: also handle indirect entries
    /* final FeatureList indirectEntries = */getProperty( QNAME_PROPERTY_indirectEntry, FeatureList.class );

    final Collection<Definition> definitions = new ArrayList<Definition>( directEntries.size() );
    for( final Object object : directEntries )
    {
      // TODO: probably a more sophisticated check/conversion is needed
      if( object instanceof Definition )
        definitions.add( (Definition) object );
    }

    return definitions.toArray( new Definition[definitions.size()] );
  }

  public Definition getDefinition( final String itemId )
  {
    return (Definition) getWorkspace().getFeature( itemId );
  }
}
