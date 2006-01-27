/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema.visitor;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.feature.FeatureType;

/**
 * 
 * schemavisitor that collects the substitutes of a feature inside a given schema-context
 * 
 * @author doemming
 */
public class FindSubstitutesGMLSchemaVisitor implements IGMLSchemaVisitor
{

  private final FeatureType m_substitutionHeadFT;

  private final boolean m_includeAbstract;

  private final boolean m_includeThis;

  private final List m_result = new ArrayList();

  private final GMLSchema m_contextGMLSchema;

  public FindSubstitutesGMLSchemaVisitor( final GMLSchema contextGMLSchema, final FeatureType substitutionHeadFT,
      boolean includeAbstract, boolean includeThis )
  {
    m_contextGMLSchema = contextGMLSchema;
    m_substitutionHeadFT = substitutionHeadFT;
    m_includeAbstract = includeAbstract;
    m_includeThis = includeThis;
  }

  /**
   * @see org.kalypso.gmlschema.visitor.IGMLSchemaVisitor#visit(org.kalypso.gmlschema.GMLSchema)
   */
  public boolean visit( final GMLSchema gmlSchema )
  {
    final List rHeads = step1( gmlSchema );
    step2( rHeads );
    return true;
  }

  private List step1( GMLSchema gmlSchema )
  {
    // step 1
    final List rheads = new ArrayList();
    final FeatureType[] allFeatureTypes = gmlSchema.getAllFeatureTypes();
    for( int i = 0; i < allFeatureTypes.length; i++ )
    {
      final FeatureType ft = allFeatureTypes[i];
      if( ft.getSubstitutionGroupFT() == m_substitutionHeadFT )
      {
        m_result.add( ft );
        rheads.add( ft );
      }
    }
    return rheads;
  }

  private void step2( final List heads )
  {
    // step 2 check also for sub-substitutionGroups
    final FeatureType[] headFTs = (FeatureType[])heads.toArray( new FeatureType[heads.size()] );
    for( int i = 0; i < headFTs.length; i++ )
    {
      final FeatureType ft = headFTs[i];
      final FindSubstitutesGMLSchemaVisitor visitor = new FindSubstitutesGMLSchemaVisitor( m_contextGMLSchema, ft,
          m_includeAbstract, m_includeThis );
      m_contextGMLSchema.accept( visitor );
      final FeatureType[] substitutes = visitor.getSubstitutes();
      for( int j = 0; j < substitutes.length; j++ )
      {
        final FeatureType ft2 = substitutes[j];
        m_result.add( ft2 );
      }
    }
  }

  /**
   * @return
   *  
   */
  public FeatureType[] getSubstitutes()
  {
    final List result = new ArrayList();
    final FeatureType[] fT = (FeatureType[])m_result.toArray( new FeatureType[m_result.size()] );
    for( int i = 0; i < fT.length; i++ )
    {
      final FeatureType ft = fT[i];
      if( !result.contains( ft ) && isRequested( ft ) )
        result.add( ft );
    }
    return (FeatureType[])result.toArray( new FeatureType[result.size()] );
  }

  /**
   * @param ft
   */
  private boolean isRequested( FeatureType ft )
  {
    // abstract behaviour
    if( !m_includeAbstract && ft.isAbstract() )
      return false;
    // self-include behaviour
    if( m_includeThis && ft == m_substitutionHeadFT )
      return false;
    return true;
  }
}
