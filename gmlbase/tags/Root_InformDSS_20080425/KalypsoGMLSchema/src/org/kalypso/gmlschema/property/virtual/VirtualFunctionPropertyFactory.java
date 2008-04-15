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
package org.kalypso.gmlschema.property.virtual;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IDetailedFeatureType;
import org.kalypso.gmlschema.feature.IFeatureContentType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * Provides mechanism to create virtual function properties for a details feature type
 * 
 * @author Patrice Congo
 */
public class VirtualFunctionPropertyFactory
{
  public static final QName QNAME_FUNCTION = new QName( NS.KALYPSO_APPINFO, "functionId" );

  public static final QName QNAME_PROPERTY = new QName( NS.KALYPSO_APPINFO, "property" );

  public static final QName QNAME_NAME = new QName( NS.KALYPSO_APPINFO, "name" );

  public static final QName QNAME_VALUE = new QName( NS.KALYPSO_APPINFO, "value" );

  public static final QName QNAME_IS_VIRTUAL = new QName( NS.KALYPSO_APPINFO, "isVirtual" );

  public static final QName QNAME_IS_GEOMETRY = new QName( NS.KALYPSO_APPINFO, "isGeometry" );

  // AbstractGeometryType
  public static final QName QNAME_GML_GEOMETRY = new QName( NS.GML3, "_Geometry" );

  // kapp:valueType
  public static final QName QNAME_VALUE_TYPE = new QName( NS.KALYPSO_APPINFO, "valueType" );

  private VirtualFunctionPropertyFactory( )
  {

  }

  /**
   * Create the virtual function propety types for the given feature type. {@link IFeatureType#getProperty(QName)} is
   * used to look up regular feature type regular properties. Therefor the feature type must be in a state after the
   * collection of those regular properties
   * 
   * @param featureType
   *            the feature properties for which virtual properties are to be collected
   * @return a collection of virtual properties for the given feature
   */
  public static final Collection<IVirtualFunctionPropertyType> createVirtualFunctionPropertyTypes( final IDetailedFeatureType featureType ) throws IllegalArgumentException, GMLSchemaException
  {
    if( featureType == null )
    {
      throw new IllegalArgumentException( "Parameter featureType must not be null" );
    }
    final Map<QName, IVirtualFunctionPropertyType> vftpMap = new HashMap<QName, IVirtualFunctionPropertyType>();

// List<IVirtualFunctionPropertyType> vfptList =
// new ArrayList<IVirtualFunctionPropertyType>();
    final IFeatureContentType featureContentType = featureType.getFeatureContentType();
    // return local before base function properties
    final XmlObject[] funcProps = featureContentType.collectFunctionProperties();

    try
    {
      for( final XmlObject funcProp : funcProps )
      {
        final XmlCursor funcCursor = funcProp.newCursor();
        final IVirtualFunctionPropertyType pt = createFromCursor( featureType, funcCursor );
        if( pt != null )
        {
          final QName name = pt.getQName();
          if( !vftpMap.containsKey( name ) )
          {
            vftpMap.put( name, pt );
          }
// vfptList.add( pt);
        }
      }
    }
    catch( final GMLSchemaException e )
    {
      e.printStackTrace();
      throw e;
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
      final String message = String.format( "Exception while computig the virtual properties of %s ", getPrefixedName( featureType.getQName() ) );
      throw new GMLSchemaException( message, th );
    }

    return vftpMap.values();
  }

  /**
   * Creates a virtual function property for the specified featureType given a cursor describing a function property.
   * This method relies on {@link IFeatureType#getProperty(QName)} to check for existing regular property with the same
   * q-name as the function property. Therefore the feature type instance must be in a state where it regular property
   * are already collected.
   * 
   * @param featureType
   *            the feature type to find the function property for
   * @param funcCursor
   *            the cursor containing the function property description
   * @return
   */
  static public final IVirtualFunctionPropertyType createFromCursor( final IFeatureType featureType, final XmlCursor funcCursor ) throws GMLSchemaException
  {
// final String property = funcCursor.getAttributeText( QNAME_PROPERTY );
    final boolean isVirtual = getBooleanKAppProperty( QNAME_IS_VIRTUAL, funcCursor );
    if( !isVirtual )
    {
      return null;
    }

    final QName propertyQName = getPropertyAsQName( funcCursor, QNAME_PROPERTY );

    // this supposed that getProperty only returns non virtual property
    if( featureType.getProperty( propertyQName ) != null )
    {
      final String message = String.format( "Feature must not have a regular and a virtual property with the same qname:%s", getPrefixedName( propertyQName ) );
      throw new GMLSchemaException( message );
    }

    // check value type
    try
    {
      final QName valueTypeQName = getPropertyAsQName( funcCursor, QNAME_VALUE_TYPE );
      if( valueTypeQName != null )
      {
        final IMarshallingTypeHandler typeHandler = getTypeHandler( valueTypeQName );
        if( typeHandler != null )
        {
// System.out.println("isGeometry="+isGeometry( featureType, valueTypeQName ));
// System.out.println("FEATURETYPE="+featureType.getQName());
// System.out.println("VALUE_CLASS"+typeHandler.getValueClass());
// System.out.println("VALUE_QANME="+typeHandler.getTypeName());
// System.out.println("VALUE_IS_GEO="+typeHandler.isGeometry());
// System.out.println();
          final IVirtualFunctionValuePropertyType valProp = new VirtualFunctionValuePropertyType( typeHandler, propertyQName );
          return valProp;
        }
        else
        {
          throw new GMLSchemaException( "Value-Type not supported:" + valueTypeQName );
        }

      }
      else
      {
// System.out.println("Type handler is null");
        final IVirtualFunctionPropertyType vfpt = new VirtualFunctionPropertyType( propertyQName );
        return vfpt;
      }
    }
    catch( final GMLSchemaException e )
    {
      throw e;
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
      throw new GMLSchemaException( "Exception while creating virtual property type for " + featureType.getQName(), th );
    }

  }

  /**
   * To get the marshalling type handler for the given QName
   * 
   * @param valueQName
   *            the q-name to find the marshalling type for
   * @return the mashalling handler for the provided q-name
   */
  public static final IMarshallingTypeHandler getTypeHandler( final QName valueQName )
  {
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final IMarshallingTypeHandler typeHandlerForTypeName = typeRegistry.getTypeHandlerForTypeName( valueQName );
    return typeHandlerForTypeName;

  }

  /**
   * To get the valueClass associated with the given q-name. This method uses a the associated marshaling handler to
   * find the value class
   * 
   * @return the class of object representing the element of q-name
   */
  public static final Class< ? > getValueClass( final QName valueQName )
  {
    Class< ? > cls = null;
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final IMarshallingTypeHandler typeHandlerForTypeName = typeRegistry.getTypeHandlerForTypeName( valueQName );
    if( typeHandlerForTypeName != null )
    {
      cls = typeHandlerForTypeName.getValueClass();
    }
    return cls;
  }

  /**
   * Answer whether a given q-name is for a geometry element. A element is a geometry if its in the substitution group
   * of gml:_Geometry
   * 
   * @param valueTypeQName
   *            the q-name to test
   * @param schema
   *            the gml containing the q-name
   * @return true if the element of given q-name represents a geometry
   */
  public static final boolean isGeometry( final QName valueTypeQName, final GMLSchema schema )
  {
    try
    {
      QName curQName = valueTypeQName;
      ElementReference elementReference;
      for( elementReference = schema.resolveElementReference( curQName ); elementReference != null; elementReference = schema.resolveElementReference( curQName ) )
      {

        if( elementReference != null )
        {

          final Element element = elementReference.getElement();
          if( element != null )
          {
            final QName substitutionGroup = element.getSubstitutionGroup();
            System.out.println( "elementReference=" + element.getName() + "\n\tSUBG=" + substitutionGroup );
            if( substitutionGroup == null )
            {
              return false;
            }
            else if( QNAME_GML_GEOMETRY.equals( substitutionGroup ) )
            {
              return true;
            }
            else
            {
              // continue looking
              curQName = substitutionGroup;
            }
          }
        }
      }
      return false;
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
      return false;
    }
  }

  public static final String getPrefixedName( final QName qname ) throws IllegalArgumentException
  {
    if( qname == null )
    {
      throw new IllegalArgumentException( "Argument qname must not be null:" + qname );
    }
    return qname.getPrefix() + ":" + qname.getLocalPart();
  }

  /**
   * Get the property, the attribute of an element represented by the given cursor as QName
   * 
   * @param cursor
   *            the cursor representing the element to get the xml element from
   * @param propertyQName
   *            the q-name to get the property, attribute qname to get as q-name
   * @return the value of the attribute as q-name
   * @throws GMLSchemaException
   *             if the property does not have the form &lt;ns&gt;:&lt;localName&gt;
   */
  public static final QName getPropertyAsQName( final XmlCursor cursor, final QName propertyQName ) throws GMLSchemaException
  {
    if( cursor == null || propertyQName == null )
    {
      final String message = String.format( "Parameter cursor[%s] and name[%s] must not be null", cursor == null ? "null" : cursor.toString(), propertyQName == null ? "null"
          : propertyQName.toString() );
      throw new IllegalArgumentException( message );
    }
    final String strQName = cursor.getAttributeText( propertyQName );
    if( strQName == null )
    {
      return null;
    }

    final String[] qnameParts = strQName.split( ":" );
    final String propertyNamespace;
    final String propertyLocalPart;
    if( qnameParts.length == 2 )
    {
      propertyNamespace = cursor.namespaceForPrefix( qnameParts[0] );
      propertyLocalPart = qnameParts[1];
    }
    else if( qnameParts.length == 1 )
    {
      propertyNamespace = cursor.namespaceForPrefix( "" );
      propertyLocalPart = qnameParts[0];
    }
    else
    {
      final String message = String.format( "Property[%s] must have the form \"[<prefix>:]<local name>\" but is \"%s\"", getPrefixedName( propertyQName ), qnameParts );
      throw new GMLSchemaException( message );
    }
    
    final QName propertyValueQName = new QName( propertyNamespace, propertyLocalPart );

    return propertyValueQName;
  }

// public static final QName makeQName(
// String strQName,
// XmlCursor cursor)
// {
// String[] qnameParts = strQName.split( ":" );
// String propertyNamespace = cursor.namespaceForPrefix( qnameParts[0] );
// String propertyLocalPart = qnameParts[1];
//
// QName propertyQName = new QName( propertyNamespace, propertyLocalPart );
// return propertyQName;
// }

// private static final IVirtualFunctionPropertyType getPropertyForVirtualFunc( final QName propertyQName, boolean
// isGeometry )
// {
// return new VirtualFunctionPropertyType(propertyQName,isGeometry);
// }

  /**
   * @param propQName
   *            name of the property to get
   * @param cursor
   *            the xml cursor that provide acces to the element to get the property from
   */
  private static final boolean getBooleanKAppProperty( final QName propQName, final XmlCursor cursor )
  {
    final String boolText = cursor.getAttributeText( propQName );
    if( boolText == null )
    {
      return false;
    }

    return Boolean.parseBoolean( boolText );
  }
}
