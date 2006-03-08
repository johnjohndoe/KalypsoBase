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
package org.kalypso.gmlschema.property;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaConstants;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.ITypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.Reference;
import org.kalypso.gmlschema.xml.SimpleTypeReference;

/**
 * representation a the property definition from xml schema
 * 
 * @author doemming
 */
public class CopyOfPropertyType extends AbstractPropertyTypeFromElement implements IValuePropertyType
{

  /**
   * if value is XML-SCHEMA-simpletype it is null
   */
  private IPropertyContentType m_propertyContentType = null;

  private QName m_valueQName = null;

  private Class m_valueClass = null;

  public CopyOfPropertyType( GMLSchema gmlSchema, Element element )
  {
    super( gmlSchema, element );

    // final QName baseType = GMLSchemaUtilities.findBaseType( gmlSchema, element );
    // final ITypeRegistry typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    // m_typeHandler = typeRegistry.getTypeHandlerForTypeName( baseType );
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:

        final QName type = m_element.getType();
        if( type != null )
        {
          if( type.getNamespaceURI().equals( GMLSchemaConstants.NS_XMLSCHEMA ) )
          {
            m_valueQName = type;
            final ITypeRegistry typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
            final ITypeHandler typeHandlerForTypeName = typeRegistry.getTypeHandlerForTypeName( m_valueQName );
            if( typeHandlerForTypeName != null )
              m_valueClass = typeHandlerForTypeName.getValueClass();
            else
              System.out.println( "no xtypehandler for " + m_valueQName );
          }
          else
          {
            final Reference reference = m_gmlSchema.resolveReference( type );
            final GMLSchema schema = reference.getGMLSchema();
            if( reference instanceof SimpleTypeReference )
            {
              final SimpleType simpleType = ((SimpleTypeReference) reference).getSimpleType();
              m_propertyContentType = (IPropertyContentType) schema.getBuildedObjectFor( simpleType );
            }
            else if( reference instanceof ComplexTypeReference )
            {
              final ComplexType complexType = ((ComplexTypeReference) reference).getComplexType();
              final Object buildedObject = schema.getBuildedObjectFor( complexType );
              m_propertyContentType = (IPropertyContentType) buildedObject;
            }
          }
        }
        else
        {
          final SimpleType simpleType = m_element.getSimpleType();
          m_propertyContentType = (IPropertyContentType) m_gmlSchema.getBuildedObjectFor( simpleType );
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
    if( m_propertyContentType == null )
      return m_valueQName;
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
    return m_element.isSetFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getFixed()
   */
  public String getFixed( )
  {
    return m_element.getFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasDefault()
   */
  public boolean hasDefault( )
  {
    return m_element.isSetDefault();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getDefault()
   */
  public String getDefault( )
  {
    return m_element.getDefault();
  }

  public boolean isNullable( )
  {
    return m_element.isSetNillable();
  }

  /**
   * @deprecated
   */
  @Deprecated
  @Override
  public String getName( )
  {
    return m_qName.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    if( m_propertyContentType == null )
      return false;
    return m_propertyContentType.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class getValueClass( )
  {
    if( m_propertyContentType == null )
      return m_valueClass;
    return m_propertyContentType.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public ITypeHandler getTypeHandler( )
  {
    return m_propertyContentType.getTypeHandler();
  }

}
