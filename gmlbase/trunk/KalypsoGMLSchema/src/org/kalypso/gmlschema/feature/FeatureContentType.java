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
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.builder.BuildedFromSchema;
import org.kalypso.gmlschema.property.PropertyType;
import org.kalypso.gmlschema.relation.RelationType;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * 
 * representation of a feature content definition from xml schema
 * 
 * @author doemming
 */
public abstract class FeatureContentType implements BuildedFromSchema
{

  private final GMLSchema m_gmlSchema;

  private final ComplexType m_complexType;

  private PropertyType[] m_pt = null;

  private RelationType[] m_rt = null;

  public FeatureContentType( final GMLSchema gmlSchema, ComplexType complexType )
  {
    m_gmlSchema = gmlSchema;
    m_complexType = complexType;
  }

  public GMLSchema getGMLSchema()
  {
    return m_gmlSchema;
  }

  public abstract ExplicitGroup getSequence();

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "FCT: " + m_complexType.getName();
  }

  public void init()
  {
    final ExplicitGroup sequence = getSequence();
    final List propertyTypes = new ArrayList();
    final List relationTypes = new ArrayList();
    if( sequence != null )
    {
      final Element[] elementArray = sequence.getElementArray();
      for( int i = 0; i < elementArray.length; i++ )
      {
        final QName ref = elementArray[i].getRef();
        final Element element;
        final GMLSchema schema;
        if( ref == null )
        {
          element = elementArray[i];
          schema = getGMLSchema();
        }
        else
        {
          final ElementReference reference = (ElementReference)m_gmlSchema.resolveReference( ref );
          schema = reference.getGMLSchema();
          element = reference.getElement();
        }
        final Object buildedObject = schema.getBuildedObjectFor( element );
        if( buildedObject instanceof PropertyType )
          propertyTypes.add( buildedObject );
        if( buildedObject instanceof RelationType )
          relationTypes.add( buildedObject );
      }
    }
    m_pt = (PropertyType[])propertyTypes.toArray( new PropertyType[propertyTypes.size()] );
    m_rt = (RelationType[])relationTypes.toArray( new RelationType[relationTypes.size()] );
  }

  /**
   * 
   * @param writer
   * @param indent
   */
  public void toStringTree( StringWriter writer, int indent )
  {
    writer.write( "\n" + StringUtils.repeat( " ", indent ) );
    writer.write( "FeatureContentType " + m_complexType.getName() );
    //    super.toStringTree( writer, indent );
    indent++;
    final PropertyType[] pts = getPTs();
    for( int i = 0; i < pts.length; i++ )
    {
      pts[i].toStringTree( writer, indent );
    }
    final RelationType[] rts = m_rt;
    for( int i = 0; i < rts.length; i++ )
    {
      rts[i].toStringTree( writer, indent );
    }
  }

  public PropertyType[] getPTs()
  {
    return m_pt;
  }

  public ComplexType getComplexType()
  {
    return m_complexType;
  }
}
