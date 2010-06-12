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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexContentDocument.ComplexContent;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexRestrictionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.property.relation.RelationContentType;
import org.kalypso.gmlschema.property.relation.RelationContentTypeFromExtension;
import org.kalypso.gmlschema.property.relation.RelationContentTypeFromRestriction;
import org.kalypso.gmlschema.property.relation.RelationContentTypeFromSequence;
import org.kalypso.gmlschema.xml.ElementWithOccurs;

/**
 * another builder
 * 
 * @author doemming
 */
public class ComplexTypeDirectReference2RelationContentTypeBuilder extends AbstractBuilder
{
  private final String m_version;

  public ComplexTypeDirectReference2RelationContentTypeBuilder( final String version )
  {
    m_version = version;
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#build(org.kalypso.gmlschema.GMLSchema, java.lang.Object)
   */
  @Override
  public Object[] build( final GMLSchema gmlSchema, final Object complexTypeObject ) throws GMLSchemaException
  {
    RelationContentType result = null;
    final ComplexType complexType = (ComplexType) complexTypeObject;
    final ComplexContent complexContent = complexType.getComplexContent();
    if( complexContent != null )
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
    else
    {
      final List<ElementWithOccurs> elements = GMLSchemaUtilities.collectElements( gmlSchema, complexType, null, null );
      result = new RelationContentTypeFromSequence( gmlSchema, complexType, elements );
    }
    gmlSchema.register( complexTypeObject, result );
    return new Object[] { result };
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  @Override
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass ) throws GMLSchemaException
  {
    if( !(object instanceof ComplexType) )
      return false;
    final ComplexType complexType = (ComplexType) object;
    // check if base is a RelationType
    final QName baseType = GMLSchemaUtilities.findBaseType( gmlSchema, (ComplexType) object, m_version );
    if( baseType != null && GMLSchemaUtilities.isRelationType( m_version, baseType ) )
      return true;

    // find referenced element (target)
    final List<ElementWithOccurs> elements = GMLSchemaUtilities.collectElements( gmlSchema, complexType, null, null );
    // Release this to allow array property types?
    if( elements.size() != 1 )
      return false;
    final ElementWithOccurs targetElement = elements.get( 0 );
    // check if target is a feature
    final QName baseQName = GMLSchemaUtilities.findBaseType( gmlSchema, targetElement.getElement(), m_version );
    if( baseQName == null )
      return false;

    if( isFeature( baseQName ) )
      return true;

    return false;
  }

  private boolean isFeature( final QName baseQName )
  {
    if( !NS.GML2.equals( baseQName.getNamespaceURI() ) )
      return false;
    return GMLSchemaUtilities.getBaseOfFeatureType( m_version ).equals( baseQName.getLocalPart() );
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#replaces(org.kalypso.gmlschema.builder.IBuilder)
   */
  @Override
  public boolean replaces( final IBuilder other )
  {
    if( other instanceof ComplexType2RelationContentTypeBuilder )
      return true;
    return false;
  }
}
