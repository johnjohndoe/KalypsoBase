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
package org.kalypso.gmlschema.property.value;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.property.AbstractPropertyTypeFromElement;
import org.kalypso.gmlschema.property.CustomPropertyContentType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;
import org.kalypso.gmlschema.xml.SimpleTypeReference;
import org.kalypso.gmlschema.xml.TypeReference;

/**
 * Representation a the property definition from xml schema.
 * 
 * @author doemming
 */
public class PropertyType extends AbstractPropertyTypeFromElement implements IValuePropertyType
{
  /**
   * if value is XML-SCHEMA-simpletype it is null
   */
  private IPropertyContentType m_propertyContentType = null;

  public PropertyType( final GMLSchema gmlSchema, final ElementWithOccurs element )
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

        final QName type = getElement().getType();
        final SimpleType simpleType = getElement().getSimpleType();
        if( type != null )
        {
          final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
          final IMarshallingTypeHandler typeHandler = typeRegistry.getTypeHandlerForTypeName( type );
          if( typeHandler != null )
            m_propertyContentType = new CustomPropertyContentType( type, typeHandler );
          else
          {
            final TypeReference reference = getGMLSchema().resolveTypeReference( type );
            final GMLSchema schema = reference.getGMLSchema();
            if( reference instanceof SimpleTypeReference )
            {
              final SimpleType referencedSimpleType = ((SimpleTypeReference) reference).getSimpleType();
              m_propertyContentType = (IPropertyContentType) schema.getBuildedObjectFor( referencedSimpleType );
            }
            else if( reference instanceof ComplexTypeReference )
            {
              final ComplexType complexType = ((ComplexTypeReference) reference).getComplexType();
              final Object buildedObject = schema.getBuildedObjectFor( complexType );
              // TODO:
// if( buildedObject == null )
// System.out.println( "Could not build PopertyContentType for: " + simpleType );
              m_propertyContentType = (IPropertyContentType) buildedObject;
            }
          }
        }
        else if( simpleType != null )
        {
          final IPropertyContentType buildedObjectFor = (IPropertyContentType) getGMLSchema().getBuildedObjectFor( simpleType );
          // TODO
// if( buildedObjectFor == null )
// System.out.println( "Could not build PopertyContentType for: " + simpleType );
          m_propertyContentType = buildedObjectFor;
        }
        else
        {
          // no type definition -> xmlAnyType
          final ITypeRegistry<IMarshallingTypeHandler> marshallingRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
          final IMarshallingTypeHandler typeHandler = marshallingRegistry.getTypeHandlerForTypeName( new QName( NS.XSD_SCHEMA, "anyType" ) );
          final String localName = getElement().getName();
          final QName qName = new QName( getGMLSchema().getTargetNamespace(), localName );
          m_propertyContentType = new CustomPropertyContentType( qName, typeHandler );
        }
        break;
    }
  }

  public IPropertyContentType getPropertyContentType( )
  {
    return m_propertyContentType;
  }

  public QName getValueQName( )
  {
    return m_propertyContentType.getValueQName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    if( m_propertyContentType == null )
      return false;
    return m_propertyContentType.hasRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return m_propertyContentType.getRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isFixed()
   */
  public boolean isFixed( )
  {
    return getElement().isSetFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getFixed()
   */
  public String getFixed( )
  {
    return getElement().getFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasDefault()
   */
  public boolean hasDefault( )
  {
    return getElement().isSetDefault();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getDefault()
   */
  public String getDefault( )
  {
    return getElement().getDefault();
  }

  public boolean isNullable( )
  {
    return getElement().isSetNillable();
  }

  /**
   * @deprecated
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public String getName( )
  {
    return getQName().getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    // HACK: this happens for not initialized property types....
    // 

    if( m_propertyContentType != null )
      return m_propertyContentType.isGeometry();

    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class< ? > getValueClass( )
  {
    return m_propertyContentType.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public IMarshallingTypeHandler getTypeHandler( )
  {
    return m_propertyContentType.getTypeHandler();
  }

}
