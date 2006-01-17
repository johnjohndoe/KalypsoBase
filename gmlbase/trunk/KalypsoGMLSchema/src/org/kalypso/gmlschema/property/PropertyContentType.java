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
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.kalypso.gmlschema.GMLSchema;

/**
 * 
 * representation of a property content definition from xml schema
 * 
 * @author doemming
 */
public class PropertyContentType implements IPropertyContentType
{

  private final GMLSchema m_gmlSchema;

  private final SimpleType m_simpleType;

  private QName m_valueQName = null;

  public PropertyContentType( final GMLSchema gmlSchema, final SimpleType simpleType )
  {
    m_gmlSchema = gmlSchema;
    m_simpleType = simpleType;
  }

  public SimpleType getSimpleType()
  {
    return m_simpleType;
  }

  public void init()
  {
    final String name = m_simpleType.getName();
    final String targetNamespace = m_gmlSchema.getTargetNamespace();
    m_valueQName = new QName( targetNamespace, name );
  }

  public GMLSchema getGMLSchema()
  {
    return m_gmlSchema;
  }

  public String toString()
  {
    return m_simpleType.getName();
  }

  /**
   */
  public QName getValueQName()
  {
    return m_valueQName;
  }

  /**
   * @param writer
   * @param indent
   */
  public void toStringTree( StringWriter writer, int indent )
  {
    writer.write( "\n" + StringUtils.repeat( " ", indent ) );
    writer.write( "PropertyContentType " + m_valueQName );
  }
}
