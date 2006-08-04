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

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.feature.IFeatureType;

/**
 * schemavisitor that collects the substitutes of a feature inside a given schema-context
 * 
 * @author doemming
 */
public class FindSubstitutesGMLSchemaVisitor implements IGMLSchemaVisitor
{
  private final boolean m_includeAbstract;

  private final boolean m_includeThis;

  private final List<IFeatureType> m_result = new ArrayList<IFeatureType>();

  private final QName m_subsHeadQName;

  private final IFeatureType m_substitutionHeadFT;

  public FindSubstitutesGMLSchemaVisitor( final IFeatureType substitutionHeadFT, boolean includeAbstract, boolean includeThis )
  {
    m_substitutionHeadFT = substitutionHeadFT;
    m_includeAbstract = includeAbstract;
    m_includeThis = includeThis;
    m_subsHeadQName = substitutionHeadFT.getQName();
  }

  /**
   * @see org.kalypso.gmlschema.visitor.IGMLSchemaVisitor#visit(org.kalypso.gmlschema.GMLSchema)
   */
  public boolean visit( final IGMLSchema gmlSchema )
  {
    final IFeatureType[] allFeatureTypes = gmlSchema.getAllFeatureTypes();
    for( final IFeatureType ft : allFeatureTypes )
    {
      if( GMLSchemaUtilities.substitutes( ft, m_subsHeadQName ) )
      {
        m_result.add( ft );
      }
    }
    return true;
  }

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
    return result.toArray( new IFeatureType[result.size()] );
  }

  private boolean isRequested( final IFeatureType ft )
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
