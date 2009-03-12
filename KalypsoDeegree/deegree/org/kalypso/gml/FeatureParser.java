/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gml;

import java.util.Stack;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.xml.sax.Attributes;

/**
 * @author doemming
 */
public class FeatureParser
{
  private final FeatureTypeProvider m_provider;

  final Stack<Feature> m_stackFE = new Stack<Feature>();

  public FeatureParser( final FeatureTypeProvider provider )
  {
    m_provider = provider;
  }

  /**
   * @param parent
   *          feature or workspace
   */
  public void createFeature( final Object parent, final IRelationType parentRelation, final String uri, final String localName, final Attributes atts ) throws GMLException
  {
    final QName qNameFT = new QName( uri, localName );
    final IFeatureType featureType = m_provider.getFeatureType( qNameFT );

    if( featureType == null )
      throw new GMLException( "No feature type found for: " + qNameFT );

    final String fid;
    // GMLContentHandler.print( atts );
    // TODO check for alternatives xml:id gml:fid
    final int id1 = atts.getIndex( NS.GML2, "fid" );
    final int id2 = atts.getIndex( "fid" );
    final int id3 = atts.getIndex( NS.GML2, "id" );
    final int id4 = atts.getIndex( "id" );
    if( id1 >= 0 )
      fid = atts.getValue( id1 );
    else if( id2 >= 0 )
      fid = atts.getValue( id2 );
    else if( id3 >= 0 )
      fid = atts.getValue( id3 );
    else if( id4 >= 0 )
      fid = atts.getValue( id4 );
    else
      fid = null; // TODO the ID must be generated AFTER the other elements have been generated, so that it does not
    // conflict with other ids
    final Feature feature = FeatureFactory.createFeature( (Feature) parent, parentRelation, fid, featureType, false );
    // System.out.println( " | created Feature " + fid + " " + featureType.getQName() );
    m_stackFE.push( feature );
  }

  public Feature getCurrentFeature( )
  {
    if( m_stackFE.empty() )
      return null;
    return m_stackFE.peek();
  }

  public void popFeature( )
  {
    m_stackFE.pop();
  }
}
