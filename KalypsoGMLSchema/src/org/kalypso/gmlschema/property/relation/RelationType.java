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
package org.kalypso.gmlschema.property.relation;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.kalypso.gmlschema.ElementWithOccurs;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.AbstractPropertyTypeFromElement;
import org.kalypso.gmlschema.xml.ComplexTypeReference;

/**
 * @author doemming
 */
public class RelationType extends AbstractPropertyTypeFromElement implements IRelationType
{
  private IRelationContentType m_relationContentType;

  public RelationType( final GMLSchema gmlSchema, final ElementWithOccurs element )
  {
    super( gmlSchema, element );
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun ) throws GMLSchemaException
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:

        final ComplexTypeReference complexTypeReference = GMLSchemaUtilities.getComplexTypeReferenceFor( getGMLSchema(), getElement() );
        final ComplexType complexType = complexTypeReference.getComplexType();
        final GMLSchema schema = complexTypeReference.getGMLSchema();
        m_relationContentType = (RelationContentType) schema.getBuildedObjectFor( complexType );
        if( m_relationContentType == null )
          throw new UnsupportedOperationException();
        break;
    }
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isInlineAble()
   */
  public boolean isInlineAble( )
  {
    return m_relationContentType.isInlineable();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isLinkAble()
   */
  public boolean isLinkAble( )
  {
    return m_relationContentType.isLinkable();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getTargetFeatureTypes(org.kalypso.gmlschema.GMLSchema,
   *      boolean)
   */
  public IFeatureType getTargetFeatureType( )
  {
    return m_relationContentType.getTargetFeatureType();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getDocumentReferences()
   */
  public IDocumentReference[] getDocumentReferences( )
  {
    return m_relationContentType.getDocumentReferences();
  }

}
