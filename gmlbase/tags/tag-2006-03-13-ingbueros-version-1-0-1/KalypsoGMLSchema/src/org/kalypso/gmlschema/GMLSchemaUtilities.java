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
package org.kalypso.gmlschema;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexRestrictionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexContentDocument.ComplexContent;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction;
import org.kalypso.gmlschema.types.ITypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.Reference;
import org.kalypso.gmlschema.xml.SimpleTypeReference;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class GMLSchemaUtilities
{
  private static final int CONSTRUCTION_REFERENCED_TYPE = 1;

  private static final int CONSTRUCTION_NAMED_TYPE = 2;

  private static final int CONSTRUCTION_ANONYMOUS_SIMPLE_TYPE = 3;

  private static final int CONSTRUCTION_ANONYMOUS_COMPLEX_TYPE = 4;

  /**
   * @param gmlSchema
   * @param complexType
   */
  public static QName findBaseType( GMLSchema gmlSchema, ComplexType complexType )
  {
    final ComplexContent complexContent = complexType.getComplexContent();
    final ExplicitGroup sequence = complexType.getSequence();
    final String name = complexType.getName();

    // 1. check if it is a named type
    // propably a typehanlder exists for this
    if( name != null )
    {
      final QName typeQName = new QName( gmlSchema.getTargetNamespace(), name );
      if( isKnownType( typeQName ) )
        return getKnownTypeFor( typeQName );
    }

    if( sequence != null )
    {
      return null;
    }
    if( complexContent == null )
      return null;
    // 2. check for base (extension or restriction)
    // propably a typehanlder exists for this
    final ExtensionType extension = complexContent.getExtension();
    final ComplexRestrictionType restriction = complexContent.getRestriction();
    final QName base;
    if( extension != null )
    {
      base = extension.getBase();
    }
    else if( restriction != null )
    {
      base = restriction.getBase();
    }
    else
      throw new UnsupportedOperationException( "unknown base type for " + complexType.toString() );
    if( isXDSAnyType( base ) )
      return null;

    if( isKnownType( base ) )
      return getKnownTypeFor( base );

    // 3. handle a reference
    final Reference reference = gmlSchema.resolveReference( base );
    if( reference instanceof ComplexTypeReference )
      return findBaseType( (ComplexTypeReference) reference );
    else if( reference instanceof SimpleTypeReference )
      return findBaseType( (SimpleTypeReference) reference );
    else
      throw new UnsupportedOperationException();
  }

  private static boolean isXDSAnyType( QName base )
  {
    if( !base.getNamespaceURI().equals( GMLSchemaConstants.NS_XMLSCHEMA ) )
      return false;
    return base.getLocalPart().equals( "anyType" );
  }

  /**
   * @param schema
   * @param simpleType
   */
  public static QName findBaseType( GMLSchema schema, SimpleType simpleType )
  {
    final Restriction restriction = simpleType.getRestriction();

    // 1. check if it is a named type
    // propably a typehanlder exists for this
    final String name = simpleType.getName();
    if( name != null )
    {
      final QName namedBase = new QName( schema.getTargetNamespace(), name );
      if( isKnownType( namedBase ) )
        return getKnownTypeFor( namedBase );
    }

    // 2. check for base
    // propably a typehanlder exists for this
    final QName base = restriction.getBase();
    if( isKnownType( base ) )
      return getKnownTypeFor( base );

    // 3. handle a reference
    final ElementReference reference = (ElementReference) schema.resolveReference( base );
    return findBaseType( reference );
  }

  public static QName findBaseType( final GMLSchema schema, final Element element )
  {
    final QName qName;
    switch( getConstructionType( element ) )
    {
      case CONSTRUCTION_ANONYMOUS_SIMPLE_TYPE:
        final LocalSimpleType simpleType = element.getSimpleType();
        return findBaseType( schema, simpleType );
      case CONSTRUCTION_ANONYMOUS_COMPLEX_TYPE:
        final LocalComplexType complexType = element.getComplexType();
        return findBaseType( schema, complexType );
      case CONSTRUCTION_REFERENCED_TYPE:
        qName = element.getRef();
        break;
      case CONSTRUCTION_NAMED_TYPE:
        qName = element.getType();
        break;
      default:
        throw new UnsupportedOperationException();
    }
    if( isKnownType( qName ) )
      return getKnownTypeFor( qName );
    final Reference reference = schema.resolveReference( qName );
    if( reference instanceof ComplexTypeReference )
      return findBaseType( (ComplexTypeReference) reference );
    else if( reference instanceof SimpleTypeReference )
      return findBaseType( (SimpleTypeReference) reference );
    else if( reference instanceof ElementReference )
      return findBaseType( (ElementReference) reference );
    else
      throw new UnsupportedOperationException( "unknown base type for " + element.toString() );
  }

  /**
   * @param element
   * @return constructiontyped integer
   */
  private static int getConstructionType( Element element )
  {
    final QName ref = element.getRef();
    if( ref != null )
      return CONSTRUCTION_REFERENCED_TYPE;
    final String name = element.getName();
    final QName type = element.getType();
    if( name != null && type != null )
      return CONSTRUCTION_NAMED_TYPE;
    final LocalSimpleType simpleType = element.getSimpleType();
    if( name != null && simpleType != null )
      return CONSTRUCTION_ANONYMOUS_SIMPLE_TYPE;
    final LocalComplexType complexType = element.getComplexType();
    if( name != null && complexType != null )
      return CONSTRUCTION_ANONYMOUS_COMPLEX_TYPE;
    throw new UnsupportedOperationException( "unknown construction type:\n" + element.toString() );
  }

  /**
   * @param reference
   */
  private static QName findBaseType( SimpleTypeReference reference )
  {
    return findBaseType( reference.getGMLSchema(), reference.getSimpleType() );
  }

  /**
   * @param reference
   */
  private static QName findBaseType( ComplexTypeReference reference )
  {
    return findBaseType( reference.getGMLSchema(), reference.getComplexType() );
  }

  /**
   * known types are all types that should be builded to something e.g. featuretype, propertytype or relationtype
   * 
   * @param qName
   * @return true is qName is a known type
   */
  private static boolean isKnownType( final QName qName )
  {
    return getKnownTypeFor( qName ) != null;
  }

  private static QName findBaseType( final ElementReference reference )
  {
    final Element element = reference.getElement();
    final GMLSchema schema = reference.getGMLSchema();
    return findBaseType( schema, element );
  }

  /**
   * @param qName
   */
  private static QName getKnownTypeFor( final QName qName )
  {
    final ITypeRegistry typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final ITypeHandler typeHandler = typeRegistry.getTypeHandlerForTypeName( qName );
    if( typeHandler != null )
      return qName;

    final QName result = qName;
    final String namespaceURI = qName.getNamespaceURI();
    final String localPart = qName.getLocalPart();
    // if( GMLSchemaConstants.NS_XMLSCHEMA.equals( namespaceURI ) )
    // return result;
    if( GMLSchemaConstants.NS_GML2.equals( namespaceURI ) )
    {
      // if( "GeometryAssociationType".equals( localPart ) )
      // return result;
      if( "AbstractFeatureType".equals( localPart ) ) // -> build a FeatureType
        return result;
      else if( "FeatureAssociationType".equals( localPart ) ) // -> build a RelationType
        return result;
      // else if( "AbstractGeometryType".equals( localPart ) )
      // return result;
      // else if( "PointType".equals( localPart ) )
      // return result;
      // else if( "LineStringType".equals( localPart ) )
      // return result;
      // else if( "PolygonType".equals( localPart ) )
      // return result;
      // else if( "MultiPointType".equals( localPart ) )
      // return result;
      // else if( "MultiLineStringType".equals( localPart ) )
      // return result;
      // else if( "MultiPolygonType".equals( localPart ) )
      // return result;
      // else if( "BoundingShapeType".equals( localPart ) )
      // return result;
      // else
      // return null;
    }
    // else if( GMLSchemaConstants.NS_OBSLINK.equals( namespaceURI ) )
    // {
    // if( "TimeseriesLinkType".equals( localPart ) )
    // return result;
    // }
    return null;
  }

  /**
   * @param gmlSchema
   * @param element
   * @return complextype
   */
  public static ComplexTypeReference getComplexTypeReferenceFor( final GMLSchema gmlSchema, final Element element )
  {
    final QName type = element.getType();
    if( type != null )
      return (ComplexTypeReference) gmlSchema.resolveReference( type );
    final ComplexType complexType = element.getComplexType();
    return new ComplexTypeReference( gmlSchema, complexType );
  }
}
