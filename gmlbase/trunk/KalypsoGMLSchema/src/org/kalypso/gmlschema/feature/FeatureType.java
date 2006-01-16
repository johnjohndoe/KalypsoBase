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
package org.kalypso.gmlschema.feature;

import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.builder.BuildedFromSchema;
import org.kalypso.gmlschema.xml.ComplexTypeReference;

/**
 * 
 * representation of a feature definition from xml schema
 * 
 * @author doemming
 */
public class FeatureType implements BuildedFromSchema
{

  private final GMLSchema m_gmlSchema;

  private final Element m_element;

  private FeatureContentType m_featureContentType = null;

  private final QName m_qName;

  public FeatureType( final GMLSchema gmlSchema, final Element element )
  {
    m_gmlSchema = gmlSchema;
    m_element = element;
    final String targetNamespace = gmlSchema.getTargetNamespace();
    final String name = m_element.getName();
    m_qName = new QName( targetNamespace, name );
  }

  public void init()
  {
    final ComplexTypeReference complexTypeReference = GMLSchemaUtilities.getComplexTypeReferenceFor(m_gmlSchema,m_element);
    final ComplexType complexType = complexTypeReference.getComplexType();
    final GMLSchema schema = complexTypeReference.getGMLSchema();
    m_featureContentType = (FeatureContentType)schema.getBuildedObjectFor( complexType );
  }

  public FeatureContentType getFeatureContentType()
  {
    return m_featureContentType;
  }

  /**
   */
  public QName getQName()
  {
    return m_qName;
  }

  /**
   * @param writer
   * @param indent
   */
  public void toStringTree( StringWriter writer, int indent )
  {
    writer.write( "\n" + StringUtils.repeat( " ", indent ) );
    writer.write( "FeatureType : " + getQName() );
    indent++;
    m_featureContentType.toStringTree( writer, indent );
  }

  /**
   * 
   */
  public Element getElement()
  {
    return m_element;
  }
}
