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

import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaConstants;
import org.kalypso.gmlschema.builder.BuildedFromSchema;
import org.kalypso.gmlschema.xml.SimpleTypeReference;

/**
 * 
 * representation a the property definition from xml schema
 * 
 * @author doemming
 */
public class PropertyType implements BuildedFromSchema
{

  private final GMLSchema m_gmlSchema;

  private final Element m_element;

  private PropertyContentType m_propertyContentType = null;

  private final QName m_qName;

  // null if not a XML-Schematype // TODO
  private QName m_valueQName = null;

  public PropertyType( GMLSchema gmlSchema, Element element )
  {
    m_gmlSchema = gmlSchema;
    m_element = element;
    final String targetNamespace = gmlSchema.getTargetNamespace();
    final String name = m_element.getName();
    m_qName = new QName( targetNamespace, name );
  }

  public Element getElement()
  {
    return m_element;
  }

  public void init()
  {
    final QName type = m_element.getType();
    if( type != null )
    {
      if( type.getNamespaceURI().equals( GMLSchemaConstants.NS_XMLSCHEMA ) )
        m_valueQName = type;
      else
      {
        final SimpleTypeReference reference = (SimpleTypeReference)m_gmlSchema.resolveReference( type );
        final GMLSchema schema = reference.getGMLSchema();
        final SimpleType simpleType = reference.getSimpleType();
        m_propertyContentType = (PropertyContentType)schema.getBuildedObjectFor( simpleType );
      }
    }
    else
    {
      final SimpleType simpleType = m_element.getSimpleType();
      m_propertyContentType = (PropertyContentType)m_gmlSchema.getBuildedObjectFor( simpleType );
    }
  }

  public PropertyContentType getPropertyContentType()
  {
    return m_propertyContentType;
  }

  /**
   */
  public QName getQName()
  {
    return m_qName;
  }

  /**
   */
  public QName getValueQName()
  {
    if( m_valueQName != null )
      return m_valueQName;
    return m_propertyContentType.getValueQName();
  }

  /**
   * @param writer
   * @param indent
   */
  public void toStringTree( StringWriter writer, int indent )
  {
    writer.write( "\n" + StringUtils.repeat( " ", indent ) );
    writer.write( "PropertyType " + m_qName );
    if( m_propertyContentType != null )
    {
      indent++;
      m_propertyContentType.toStringTree( writer, indent );
    }
    else
    {
      writer.write( "\n" + StringUtils.repeat( " ", indent ) );
      writer.write( "  valueType: " + m_valueQName );
    }
  }
}
