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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;

/**
 * Provides mechanism to create virtual function properties for a details feature type
 * 
 * @author Patrice Congo
 * @author Gernot Belger
 */
public class VirtualFunctionPropertyFactory
{
  public static final QName QNAME_FUNCTION = new QName( NS.KALYPSO_APPINFO, "functionId" ); //$NON-NLS-1$

  public static final QName QNAME_MINOCCURS = new QName( NS.KALYPSO_APPINFO, "minOccurs" ); //$NON-NLS-1$

  public static final QName QNAME_MAXOCCURS = new QName( NS.KALYPSO_APPINFO, "maxOccurs" ); //$NON-NLS-1$

  public static final QName QNAME_PROPERTY = new QName( NS.KALYPSO_APPINFO, "property" ); //$NON-NLS-1$

  public static final QName QNAME_NAME = new QName( NS.KALYPSO_APPINFO, "name" ); //$NON-NLS-1$

  public static final QName QNAME_VALUE = new QName( NS.KALYPSO_APPINFO, "value" ); //$NON-NLS-1$

  private static final QName QNAME_VALUE_TYPE = new QName( NS.KALYPSO_APPINFO, "valueType" ); //$NON-NLS-1$

  private VirtualFunctionPropertyFactory( )
  {

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
  public static final IFunctionPropertyType createFromCursor( final IFeatureType featureType, final XmlObject funcProp ) throws GMLSchemaException
  {
    try
    {
      final XmlCursor funcCursor = funcProp.newCursor();

      final QName propertyQName = getPropertyAsQName( funcCursor, QNAME_PROPERTY );
      final QName valueTypeQName = getPropertyAsQName( funcCursor, QNAME_VALUE_TYPE );

      // We simply ignore the case where the valueQName is not defined but a real property is defined in the schema.
      // In that case, the virtual property will later be replaced by a wrapper to the real one, we do not need the
      // valueQName any more.

      final String functionId = funcCursor.getAttributeText( VirtualFunctionPropertyFactory.QNAME_FUNCTION );
      final int minOccurs = parseInt( funcCursor.getAttributeText( VirtualFunctionPropertyFactory.QNAME_MINOCCURS ), 0 );
      final int maxOccurs = parseInt( funcCursor.getAttributeText( VirtualFunctionPropertyFactory.QNAME_MAXOCCURS ), 1 );
      final Map<String, String> properties = parseParameters( funcProp );

      final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
      final IMarshallingTypeHandler typeHandler = valueTypeQName == null ? null : typeRegistry.getTypeHandlerForTypeName( valueTypeQName );

      return new VirtualFunctionValuePropertyType( featureType, typeHandler, propertyQName, functionId, minOccurs, maxOccurs, properties );
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
      throw new GMLSchemaException( "Exception while creating virtual property type for " + featureType.getQName(), th ); //$NON-NLS-1$
    }
  }

  public static int parseInt( String attributeText, int defaultValue )
  {
    if( attributeText == null )
      return defaultValue;
    else if( attributeText.trim().isEmpty() )
      return defaultValue;
    else if( "unbounded".equals( attributeText ) )
      return Integer.MAX_VALUE;

    Integer parseQuietInteger = NumberUtils.parseQuietInteger( attributeText );
    if( parseQuietInteger == null )
      return defaultValue;

    return parseQuietInteger;
  }

  private static final String getPrefixedName( final QName qname ) throws IllegalArgumentException
  {
    if( qname == null )
      throw new IllegalArgumentException( "Argument qname must not be null:" + qname ); //$NON-NLS-1$

    return qname.getPrefix() + ":" + qname.getLocalPart(); //$NON-NLS-1$
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
      final String message = String.format( "Parameter cursor[%s] and name[%s] must not be null", cursor == null ? "null" : cursor.toString(), propertyQName == null ? "null" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          : propertyQName.toString() );
      throw new IllegalArgumentException( message );
    }

    final String strQName = cursor.getAttributeText( propertyQName );
    if( strQName == null )
      return null;

    final String[] qnameParts = strQName.split( ":" ); //$NON-NLS-1$
    if( qnameParts.length == 2 )
    {
      final String namespace = cursor.namespaceForPrefix( qnameParts[0] );
      return new QName( namespace, qnameParts[1] );
    }

    if( qnameParts.length == 1 )
    {
      final String namespace = cursor.namespaceForPrefix( "" ); //$NON-NLS-1$
      return new QName( namespace, qnameParts[0] );
    }

    final String message = String.format( "Property[%s] must have the form \"[<prefix>:]<local name>\" but is \"%s\"", getPrefixedName( propertyQName ), qnameParts ); //$NON-NLS-1$
    throw new GMLSchemaException( message );
  }

  private static Map<String, String> parseParameters( final XmlObject funcProp )
  {
    final XmlObject[] parameters = funcProp.selectPath( "declare namespace xs='" + NS.XSD_SCHEMA + "' " + "declare namespace kapp" + "='" + NS.KALYPSO_APPINFO + "' ./kapp:parameter" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    /* IMPORTENT: Use linked hash map in order to preserve parameter order. */
    final Map<String, String> properties = new LinkedHashMap<String, String>();
    for( final XmlObject parameter : parameters )
    {
      final XmlObject[] names = parameter.selectChildren( VirtualFunctionPropertyFactory.QNAME_NAME );
      final XmlObject[] values = parameter.selectChildren( VirtualFunctionPropertyFactory.QNAME_VALUE );

      final String name = names.length == 0 ? null : names[0].newCursor().getTextValue();
      final String value = values.length == 0 ? null : values[0].newCursor().getTextValue();

      if( name != null && value != null )
        properties.put( name, value );
    }

    return properties;
  }

}
