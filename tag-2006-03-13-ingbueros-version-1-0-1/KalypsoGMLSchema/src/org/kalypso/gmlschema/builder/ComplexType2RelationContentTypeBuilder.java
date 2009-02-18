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
package org.kalypso.gmlschema.builder;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexRestrictionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexContentDocument.ComplexContent;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaConstants;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.property.relation.RelationContentType;
import org.kalypso.gmlschema.property.relation.RelationContentTypeFromExtension;
import org.kalypso.gmlschema.property.relation.RelationContentTypeFromRestriction;
import org.kalypso.gmlschema.property.relation.RelationContentTypeFromSequence;

/**
 * 
 * another builder
 * 
 * @author doemming
 */
public class ComplexType2RelationContentTypeBuilder implements IBuilder
{

  /**
   * 
   * @param gmlSchema
   * @param complexTypeObject
   */
  public Object[] build( GMLSchema gmlSchema, Object complexTypeObject )
  {
    RelationContentType result = null;
    final ComplexType complexType = (ComplexType)complexTypeObject;
    final ComplexContent complexContent = complexType.getComplexContent();
    final ExplicitGroup sequence = complexType.getSequence();

    if( sequence != null )
      result = new RelationContentTypeFromSequence( gmlSchema, complexType, sequence );
    else
    {
      final ExtensionType extension = complexContent.getExtension();
      final ComplexRestrictionType restriction = complexContent.getRestriction();
      if( extension != null )
        result = new RelationContentTypeFromExtension( gmlSchema, complexType, extension );
      else if( restriction != null )
        result = new RelationContentTypeFromRestriction( gmlSchema, complexType, restriction );
      else
        throw new UnsupportedOperationException();
    }
    if( result != null )
    {
      gmlSchema.register( complexTypeObject, result );
      return new Object[]
      { result };
    }
    return new Object[0];

  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  public boolean isBuilderFor( GMLSchema gmlSchema, Object object, String namedPass )
  {
    if( !( object instanceof ComplexType ) )
      return false;
    QName baseType = GMLSchemaUtilities.findBaseType( gmlSchema, (ComplexType)object );
    if( baseType == null )
      return false;
    final String namespaceURI = baseType.getNamespaceURI();
    final String localPart = baseType.getLocalPart();

    // GML
    if( !GMLSchemaConstants.NS_GML2.equals( namespaceURI ) )
      return false;
    return "FeatureAssociationType".equals( localPart );
  }
}
