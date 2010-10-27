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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.PropertyContentTypeFromTypeHandler;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.ElementWithOccurs;

/**
 * build all featuretypeproperty for simple XML-Schema types
 * 
 * @author doemming
 */
public class ComplexType2PropertyContentFromTypeHandlerTypeBuilder extends AbstractBuilder
{
  private final String m_version;

  public ComplexType2PropertyContentFromTypeHandlerTypeBuilder( final String version )
  {
    m_version = version;
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#build(org.kalypso.gmlschema.GMLSchema, java.lang.Object)
   */
  @Override
  public Object[] build( final GMLSchema gmlSchema, final Object typeObject ) throws GMLSchemaException
  {
    final QName valueQName;
    if( typeObject instanceof ComplexType )
      valueQName = findBaseType( gmlSchema, (ComplexType) typeObject );
    else if( typeObject instanceof SimpleType )
      valueQName = GMLSchemaUtilities.findBaseType( gmlSchema, (SimpleType) typeObject, m_version );
    else
      throw new GMLSchemaException( "Could not find valueQName for: " + typeObject ); //$NON-NLS-1$

    final IPropertyContentType pct = new PropertyContentTypeFromTypeHandler( gmlSchema, typeObject, valueQName );
    gmlSchema.register( typeObject, pct );
    return new Object[] { pct };
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  @Override
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass ) throws GMLSchemaException
  {
    return getTypeHandler( gmlSchema, object ) != null;
  }

  private ITypeHandler getTypeHandler( final GMLSchema gmlSchema, final Object typeObject ) throws GMLSchemaException
  {
    final QName baseType;
    if( typeObject instanceof ComplexType )
      baseType = findBaseType( gmlSchema, (ComplexType) typeObject );
    else if( typeObject instanceof SimpleType )
      baseType = GMLSchemaUtilities.findBaseType( gmlSchema, (SimpleType) typeObject, m_version );
    else
      return null;

    if( baseType == null )
      return null;

    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    return registry.getTypeHandlerForTypeName( baseType );
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#replaces(org.kalypso.gmlschema.builder.IBuilder)
   */
  @Override
  public boolean replaces( final IBuilder other )
  {
    // TODO: HACK: this overrides the generic geometry type parsing, if a type handler is registered for a specific
    // geometry property type.
    if( other instanceof ComplexTypeDirectReference2RelationContentTypeBuilder )
      return true;

    return false;
  }

  private static QName findBaseType( final GMLSchema gmlSchema, final ComplexType typeObject ) throws GMLSchemaException
  {
    final ComplexType complexType = typeObject;
    final QName baseType = GMLSchemaUtilities.findBaseType( gmlSchema, complexType, gmlSchema.getGMLVersion() );
    if( baseType != null )
      return baseType;

    if( baseType == null )
    {
      final List<ElementWithOccurs> collector = new ArrayList<ElementWithOccurs>();
      GMLSchemaUtilities.collectElements( gmlSchema, complexType, collector, null );
      if( collector.size() == 1 )
      {
        final ElementWithOccurs elementWithOccurs = collector.get( 0 );
        final Element element = elementWithOccurs.getElement();
        return element.getRef();
      }
    }

    return null;
  }
}
