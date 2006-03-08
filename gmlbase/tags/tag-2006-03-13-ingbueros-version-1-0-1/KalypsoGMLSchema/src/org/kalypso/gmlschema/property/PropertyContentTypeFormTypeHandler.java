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
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.ITypeHandler;

/**
 * representation of a property content definition from xml schema
 * 
 * @author doemming
 */
public class PropertyContentTypeFormTypeHandler implements IPropertyContentType
{
  private final GMLSchema m_gmlSchema;

  private final Object m_typeObject;

  private QName m_valueQName;

  private final ITypeHandler m_typeHandler;

  private final IRestriction[] m_valueRestriction;

  public PropertyContentTypeFormTypeHandler( final GMLSchema gmlSchema, final Object typeObject, ITypeHandler typeHandler )
  {
    // TODO check for restriction
    m_gmlSchema = gmlSchema;
    m_typeObject = typeObject;
    m_typeHandler = typeHandler;

    if( typeObject instanceof ComplexType )
    {
      m_valueQName = GMLSchemaUtilities.findBaseType( gmlSchema, (ComplexType) typeObject );
      m_valueRestriction = new IRestriction[0];
    }
    else if( typeObject instanceof SimpleType )
    {
      final SimpleType simpleType = (SimpleType) typeObject;
      m_valueQName = GMLSchemaUtilities.findBaseType( gmlSchema, simpleType );
      if( simpleType.isSetRestriction() )
        m_valueRestriction = ContentRestrictionFactory.createRestrictions( simpleType.getRestriction() );
      else
        m_valueRestriction = new IRestriction[0];
    }
    else
      throw new UnsupportedOperationException();

  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:

        // final String name = m_simpleType.getName();
        // final String targetNamespace = m_gmlSchema.getTargetNamespace();
        // m_valueQName = new QName( targetNamespace, name );
        break;
    }
  }

  public GMLSchema getGMLSchema( )
  {
    return m_gmlSchema;
  }

  /**
   */
  public QName getValueQName( )
  {
    return m_valueQName;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    return m_valueRestriction.length > 0;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return m_valueRestriction;
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
  public Class getValueClass( )
  {
    return m_typeHandler.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyContentType#getTypeHandler()
   */
  public ITypeHandler getTypeHandler( )
  {
    return m_typeHandler;
  }
}
