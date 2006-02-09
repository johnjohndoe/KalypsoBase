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
import org.kalypso.gmlschema.feature.IFeatureType;

/**
 * schemavisitor that collects the substitutes of a feature inside a given schema-context
 * 
 * @author doemming
 */
public class FindSubstitutesGMLSchemaVisitor implements IGMLSchemaVisitor
{

  private final IFeatureType m_substitutionHeadFT;

  private final boolean m_includeAbstract;

  private final boolean m_includeThis;

  private final List<IFeatureType> m_result = new ArrayList<IFeatureType>();

  private final GMLSchema m_contextGMLSchema;

  public FindSubstitutesGMLSchemaVisitor( final GMLSchema contextGMLSchema, final IFeatureType substitutionHeadFT, boolean includeAbstract, boolean includeThis )
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
    final List<IFeatureType> rHeads = step1( gmlSchema );
    step2( rHeads );
    return true;
  }

  private List<IFeatureType> step1( GMLSchema gmlSchema )
  {
    // step 1
    final List<IFeatureType> rheads = new ArrayList<IFeatureType>();
    final IFeatureType[] allFeatureTypes = gmlSchema.getAllFeatureTypes();
    for( int i = 0; i < allFeatureTypes.length; i++ )
    {
      final IFeatureType ft = allFeatureTypes[i];
      if( ft.getSubstitutionGroupFT() == m_substitutionHeadFT )
      {
        m_result.add( ft );
        rheads.add( ft );
      }
    }
    return rheads;
  }

  private void step2( final List<IFeatureType> heads )
  {
    // step 2 check also for sub-substitutionGroups
    final IFeatureType[] headFTs = heads.toArray( new IFeatureType[heads.size()] );
    for( int i = 0; i < headFTs.length; i++ )
    {
      final IFeatureType ft = headFTs[i];
      final FindSubstitutesGMLSchemaVisitor visitor = new FindSubstitutesGMLSchemaVisitor( m_contextGMLSchema, ft, m_includeAbstract, m_includeThis );
      m_contextGMLSchema.accept( visitor );
      final IFeatureType[] substitutes = visitor.getSubstitutes();
      for( int j = 0; j < substitutes.length; j++ )
      {
        final IFeatureType ft2 = substitutes[j];
        m_result.add( ft2 );
      }
    }
  }

  /**
   * @return
   */
  public IFeatureType[] getSubstitutes( )
  {
    final List<IFeatureType> result = new ArrayList<IFeatureType>();
    if( !result.contains( m_substitutionHeadFT ) && isRequested( m_substitutionHeadFT ) )
      result.add( m_substitutionHeadFT );
    final IFeatureType[] fT = m_result.toArray( new IFeatureType[m_result.size()] );
    for( int i = 0; i < fT.length; i++ )
    {
      final IFeatureType ft = fT[i];
      if( !result.contains( ft ) && isRequested( ft ) )
        result.add( ft );
    }
    return result.toArray( new FeatureType[result.size()] );
  }

  /**
   * @param ft
   */
  private boolean isRequested( IFeatureType ft )
  {
    // abstract behaviour
    if( !m_includeAbstract && ft.isAbstract() )
      return false;
    // self-include behaviour
    if( !m_includeThis && ft == m_substitutionHeadFT )
      return false;
    return true;
  }
}
