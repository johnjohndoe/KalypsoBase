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

import org.kalypso.contribs.javax.xml.namespace.ListQName;
import org.kalypso.contribs.javax.xml.namespace.MixedQName;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.ListSimpleTypeHandler;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;

/**
 * representation of a property content definition from xml schema
 *
 * @author doemming
 */
public class PropertyContentTypeFromTypeHandler implements IPropertyContentType
{
  private final IGMLSchema m_gmlSchema;

  private final QName m_valueQName;

  private final IMarshallingTypeHandler m_typeHandler;

  private Object m_typeObject;

  public PropertyContentTypeFromTypeHandler( final GMLSchema gmlSchema, final Object typeObject, final QName valueQName )
  {
    m_gmlSchema = gmlSchema;
    m_valueQName = valueQName;

    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    m_typeHandler = typeHandlerForQName( registry, m_valueQName );
    m_typeObject = typeObject;
  }

  private static IMarshallingTypeHandler typeHandlerForQName( final ITypeRegistry<IMarshallingTypeHandler> registry, final QName qname )
  {
    final IMarshallingTypeHandler baseTypeHandler = registry.getTypeHandlerForTypeName( qname );

    if( qname instanceof ListQName )
      return new ListSimpleTypeHandler( baseTypeHandler );

    if( qname instanceof MixedQName )
    {
      // TODO: use MixedSimpleTypeHandler for that
      return baseTypeHandler;
    }

    return registry.getTypeHandlerForTypeName( qname );
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:
        break;
    }
  }

  public IGMLSchema getGMLSchema( )
  {
    return m_gmlSchema;
  }

  public QName getValueQName( )
  {
    return m_valueQName;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getTypeObject()
   */
  public Object getTypeObject( )
  {
    return m_typeObject;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#isGeometry()
   */
  public boolean isGeometry( )
  {
    return m_typeHandler.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getValueClass()
   */
  public Class< ? > getValueClass( )
  {
    return m_typeHandler.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getTypeHandler()
   */
  public IMarshallingTypeHandler getTypeHandler( )
  {
    return m_typeHandler;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getGmlSchema()
   */
  public IGMLSchema getGmlSchema( )
  {
    return m_gmlSchema;
  }
}
