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
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.AbstractPropertyTypeFromElement;
import org.kalypso.gmlschema.property.CustomPropertyContentType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.ContentRestrictionFactory;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.Occurs;
import org.kalypso.gmlschema.xml.SimpleTypeReference;
import org.kalypso.gmlschema.xml.TypeReference;

/**
 * Representation a property definition from xml schema.
 *
 * @author doemming
 */
public class PropertyType extends AbstractPropertyTypeFromElement implements IValuePropertyType
{
  /**
   * if value is XML-SCHEMA-simpletype it is null
   */
  private IPropertyContentType m_propertyContentType = null;

  private IRestriction[] m_restrictions = new IRestriction[0];

  private final QName m_featureQName;

  public PropertyType( final GMLSchema gmlSchema, final Element element, final Occurs occurs, final IFeatureType featureType )
  {
    super( gmlSchema, featureType, element, occurs, null );

    m_featureQName = featureType == null ? null : featureType.getQName();
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun ) throws GMLSchemaException
  {
    final GMLSchema gmlSchema = getGMLSchema();
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
            m_propertyContentType = new CustomPropertyContentType( typeHandler );
          else
          {
            final TypeReference reference = gmlSchema.resolveTypeReference( type );
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
              m_propertyContentType = (IPropertyContentType) buildedObject;
            }
          }
        }
        else if( simpleType != null )
        {
          final IPropertyContentType buildedObjectFor = (IPropertyContentType) gmlSchema.getBuildedObjectFor( simpleType );
          m_propertyContentType = buildedObjectFor;
        }
        else
        {
          // no type definition -> xmlAnyType
          final ITypeRegistry<IMarshallingTypeHandler> marshallingRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
          final IMarshallingTypeHandler typeHandler = marshallingRegistry.getTypeHandlerForTypeName( new QName( NS.XSD_SCHEMA, "anyType" ) ); //$NON-NLS-1$
          final String localName = getElement().getName();
          final QName qName = new QName( gmlSchema.getTargetNamespace(), localName );
          m_propertyContentType = new CustomPropertyContentType( qName, typeHandler );
        }

        if( m_propertyContentType == null )
        {
          // may happen for virtual properties
          // System.out.println();
        }
        else
        {
          final Object typeObject = m_propertyContentType.getTypeObject();
          if( typeObject instanceof SimpleType && ((SimpleType) typeObject).isSetRestriction() )
          {
            final SimpleType contentSimpleType = (SimpleType) typeObject;

            final GMLSchema restrictionSchema;
            final QName[] qnames;
            final QName simpleQName;
            if( typeObject instanceof TopLevelSimpleType )
            {
              final IGMLSchema contentGmlSchema = m_propertyContentType.getGmlSchema();
              final String namespaceURI = contentGmlSchema == null ? null : contentGmlSchema.getTargetNamespace();
              final String localPart = contentSimpleType.getName();
              simpleQName = new QName( namespaceURI, localPart );

              qnames = new QName[] { simpleQName };
              restrictionSchema = gmlSchema.getGMLSchemaForNamespaceURI( namespaceURI );
            }
            else
            {
              simpleQName = null;
              qnames = new QName[] { m_featureQName, getQName() };
              restrictionSchema = m_featureQName == null ? gmlSchema : gmlSchema.getGMLSchemaForNamespaceURI( m_featureQName.getNamespaceURI() );
            }

            final Restriction restriction = contentSimpleType.getRestriction();
           
            if( restriction != null )
              m_restrictions = ContentRestrictionFactory.createRestrictions(simpleQName,restriction, restrictionSchema, qnames);
          }
        }

        break;
    }
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
    return m_restrictions.length > 0;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return m_restrictions;
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

    if( m_propertyContentType != null )
      return m_propertyContentType.isGeometry();

    // TODO: this happens, which is bad... maybe some geometries are not recognized
    // wile the feature type searches for its default geometry

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

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#cloneForFeatureType(org.kalypso.gmlschema.feature.IFeatureType)
   */
  public IPropertyType cloneForFeatureType( final IFeatureType featureType )
  {
    return new PropertyType( getGMLSchema(), getElement(), getOccurs(), featureType );
  }
}
