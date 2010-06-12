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
package org.kalypso.gmlschema.builder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexContentDocument.ComplexContent;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexRestrictionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.property.value.GeometryPropertyType;
import org.kalypso.gmlschema.property.value.ReferencedPropertyType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;
import org.kalypso.gmlschema.xml.Occurs;
import org.kalypso.gmlschema.xml.TypeReference;

/**
 * Handles geometry properties: recognises properties of the following pattern: <br>
 * <code>
 *  <complexType name="PointPropertyType">
 *      <sequence minOccurs="0">
 *          <element ref="gml:Point"/>
 *      </sequence>
 *      <attributeGroup ref="gml:AssociationAttributeGroup"/>
 *  </complexType>
 * <code>
 *
 * @author Gernot Belger
 */
public class GeometryPropertyBuilder extends AbstractBuilder
{
  private final String m_version;

  public GeometryPropertyBuilder( final String version )
  {
    m_version = version;
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#build(org.kalypso.gmlschema.GMLSchema, java.lang.Object)
   */
  @Override
  public Object[] build( final GMLSchema gmlSchema, final Object elementObject ) throws GMLSchemaException
  {
    final ElementWithOccurs element = (ElementWithOccurs) elementObject;

    final Object result = innerBuild( gmlSchema, element );

    gmlSchema.register( element.getElement(), result );

    return new Object[] { result };
  }

  private Object innerBuild( final GMLSchema gmlSchema, final ElementWithOccurs elementWithOccurs ) throws GMLSchemaException
  {
    final Element element = elementWithOccurs.getElement();
    final Occurs occurs = elementWithOccurs.getOccurs();
    final QName ref = element.getRef();
    if( ref != null )
    {
      final ElementReference reference = gmlSchema.resolveElementReference( ref );
      return new ReferencedPropertyType( gmlSchema, element, occurs, reference, null );
    }

    final QName[] referencedGeometries = referencedQNames( gmlSchema, elementWithOccurs );
    return new GeometryPropertyType( gmlSchema, element, referencedGeometries, occurs, null );
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  @Override
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass ) throws GMLSchemaException
  {
    final QName[] referencedElements = referencedQNames( gmlSchema, object );
    return referencedElements != null && referencedElements.length > 0;
  }

  private QName[] referencedQNames( final GMLSchema gmlSchema, final Object object ) throws GMLSchemaException
  {
    if( !(object instanceof ElementWithOccurs) )
      return null;

    final Element element = ((ElementWithOccurs) object).getElement();
    final Element relationElement = findReference( gmlSchema, element );
    if( relationElement == null )
      return null;

    final ComplexType complexType = findComplexType( gmlSchema, relationElement );
    if( complexType == null )
      return null;

    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();

    // Special case for gml2.0: follow restrictions, if type is restriction
    // of gml:GeometryAssociationType
    boolean followExtensions = false;
    final ComplexContent complexContent = complexType.getComplexContent();
    if( complexContent != null )
    {
      final ComplexRestrictionType restriction = complexContent.getRestriction();
      if( restriction != null && GMLSchemaUtilities.GML2_GeometryAssociationType.equals( restriction.getBase() ) )
        followExtensions = true;
    }

    // find referenced element (target)
    final List<ElementWithOccurs> collector = GMLSchemaUtilities.collectElements( gmlSchema, complexType, null, null, followExtensions );
    final List<QName> results = new ArrayList<QName>( collector.size() );
    for( final ElementWithOccurs elementWithOccurs : collector )
    {
      final Element targetElement = elementWithOccurs.getElement();

      final QName qname = findReferenceQName( gmlSchema, targetElement );
      // TODO: we should identify geometries by the fact that they substitute 'gml:_Geometry' instead; in that
      // case all geometries would be recognised automatically.
      // TODO: how to recognise GML2 geometries?
      // GMLSchemaUtilities.substitutes( referencedElement, new QName(NS.GML3, "_Geometry") );
      final IMarshallingTypeHandler handler = typeRegistry.getTypeHandlerForTypeName( qname );
      if( handler != null && handler.isGeometry() )
        results.add( qname );
    }

    return results.toArray( new QName[results.size()] );
  }


  /**
   * Finds the complex definition of the given element, if any.
   */
  private ComplexType findComplexType( final GMLSchema gmlSchema, final Element element ) throws GMLSchemaException
  {
    if( element.isSetComplexType() )
      return element.getComplexType();

    if( !element.isSetType() )
      return null;

    final QName typeReference = element.getType();
    // Needed here?
    if( GMLSchemaUtilities.isKnownType( typeReference, m_version ) )
      return null;

    final TypeReference reference = gmlSchema.resolveTypeReference( typeReference );
    if( reference == null )
      return null;

    if( reference instanceof ComplexTypeReference )
      return ((ComplexTypeReference) reference).getComplexType();

    return null;
  }

  /**
   * Return the referenced element, if the given element is a reference; else return the given element.
   */
  private Element findReference( final GMLSchema gmlSchema, final Element element ) throws GMLSchemaException
  {
    // if element is a reference, follow it
    final QName elementQNameRef = element.getRef();
    if( elementQNameRef == null )
      return element;

    final ElementReference elementReference = gmlSchema.resolveElementReference( elementQNameRef );
    if( elementReference == null )
      return null;

    return elementReference.getElement();
  }

  private QName findReferenceQName( final GMLSchema gmlSchema, final Element element )
  {
    // if element is a reference, follow it
    final QName elementQNameRef = element.getRef();
    if( elementQNameRef != null )
      return elementQNameRef;

    return new QName( gmlSchema.getTargetNamespace(), element.getName() );
  }


  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#replaces(org.kalypso.gmlschema.builder.IBuilder)
   */
  @Override
  public boolean replaces( final IBuilder other )
  {
    return true;
  }

}