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

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.ElementWithOccurs;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.relation.RelationType;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.TypeReference;

/**
 * a builder for adv references <br>
 * <em>
 *  <complexType name="PhenomenonPropertyType">
 *      <sequence minOccurs="0">
 *          <element ref=" swe:Phenomenon "/>
 *      </sequence>
 *      <attributeGroup ref=" gml:AssociationAttributeGroup "/>
 *  </complexType>
 * <em>
 * @author doemming
 */
public class ElementDirektReference2RelationTypeBuilder implements IBuilder
{
  private final String m_version;

  public ElementDirektReference2RelationTypeBuilder( String version )
  {
    m_version = version;
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#build(org.kalypso.gmlschema.GMLSchema, java.lang.Object)
   */
  public Object[] build( GMLSchema gmlSchema, Object elementObject ) throws GMLSchemaException
  {
    final ElementWithOccurs element = (ElementWithOccurs) elementObject;
    return innerBuild( gmlSchema, element );
  }

  public Object[] innerBuild( final GMLSchema gmlSchema, final ElementWithOccurs element ) throws GMLSchemaException
  {
    final QName ref = element.getElement().getRef();

    if( ref != null )
    {
      final ElementReference reference = gmlSchema.resolveElementReference( ref );
      final ReferencedRelationType result = new ReferencedRelationType( gmlSchema, element, reference );
      gmlSchema.register( element.getElement(), result );
      return new Object[] { result };
    }

    final IRelationType result = new RelationType( gmlSchema, element );
    gmlSchema.register( element.getElement(), result );
    return new Object[] { result };
  }

  /**
   * a builder for adv references <br>
   * <em>
   *  <complexType name="PhenomenonPropertyType">
   *      <sequence minOccurs="0">
   *          <element ref=" swe:Phenomenon "/>
   *      </sequence>
   *      <attributeGroup ref=" gml:AssociationAttributeGroup "/>
   *  </complexType>
   * <em>
   * @author doemming
   */
  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass ) throws GMLSchemaException
  {
    if( !(object instanceof ElementWithOccurs) )
      return false;
    final Element element1 = ((ElementWithOccurs) object).getElement();
    final Element relationElement;

    // if element is a reference, follow it
    final QName elementQNameRef = element1.getRef();
    if( elementQNameRef != null )
    {
      final ElementReference elementReference = gmlSchema.resolveElementReference( elementQNameRef );
      if( elementReference == null )
        return false;
      relationElement = elementReference.getElement();
    }
    else
      relationElement = element1;

    final QName typeReference = relationElement.getType();
    // find complexType
    final ComplexType complexType;
    if( typeReference != null )
    {
      if( GMLSchemaUtilities.isKnownType( typeReference, m_version ) )
        return false;
      final TypeReference reference = gmlSchema.resolveTypeReference( typeReference );
      if( reference != null && reference instanceof ComplexTypeReference )
        complexType = ((ComplexTypeReference) reference).getComplexType();
      else
        return false;
    }
    else
    {
      complexType = relationElement.getComplexType();
    }
    if( complexType == null )
      return false;
    // find referenced element (target)
    final List<ElementWithOccurs> collector = GMLSchemaUtilities.collectElements( gmlSchema, complexType, null, null );

    if( collector.size() != 1 )
      return false;
    final ElementWithOccurs targetElement = collector.get( 0 );

    // check if target is a feature
    final QName baseQName = GMLSchemaUtilities.findBaseType( gmlSchema, targetElement.getElement(), m_version );
    if( baseQName == null )
      return false;
    if( !NS.GML2.equals( baseQName.getNamespaceURI() ) )
      return false;
    return GMLSchemaUtilities.getBaseOfFeatureType( m_version ).equals( baseQName.getLocalPart() );
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#replaces(org.kalypso.gmlschema.builder.IBuilder)
   */
  public boolean replaces( IBuilder other )
  {
    if( other instanceof Element2PropertyTypeBuilder // 
        || other instanceof FeaturePropertyType2RelationTypeBuilder )
      return true;
    return false;
  }

}
