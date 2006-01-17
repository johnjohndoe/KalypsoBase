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
import org.kalypso.gmlschema.basics.QualifiedElement;
import org.kalypso.gmlschema.visitor.FindSubstitutesGMLSchemaVisitor;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * 
 * representation of a feature definition from xml schema
 * 
 * @author doemming
 */
public class FeatureType extends QualifiedElement 
{

  // will initilized in init()
  private FeatureContentType m_featureContentType = null;

  // will initilized in init()
  private FeatureType m_substituteFT = null;

  public FeatureType( final GMLSchema gmlSchema, final Element element )
  {
    super(gmlSchema, element);
  }

  public void init()
  {
    final ComplexTypeReference complexTypeReference = GMLSchemaUtilities.getComplexTypeReferenceFor( m_gmlSchema,
        m_element );
    final ComplexType complexType = complexTypeReference.getComplexType();
    final GMLSchema schema = complexTypeReference.getGMLSchema();
    m_featureContentType = (FeatureContentType)schema.getBuildedObjectFor( complexType );

    // initialize substitution group
    final QName substitutionGroup = m_element.getSubstitutionGroup();
    if( substitutionGroup != null )
    {
      final ElementReference substitutesReference = (ElementReference)m_gmlSchema.resolveReference( substitutionGroup );
      final GMLSchema substiututesGMLSchema = substitutesReference.getGMLSchema();
      final Element substitutesElement = substitutesReference.getElement();
      final FeatureType ft = (FeatureType)substiututesGMLSchema.getBuildedObjectFor( substitutesElement );
      m_substituteFT = ft;
    }
    else
      m_substituteFT = null;
  }

  public FeatureContentType getFeatureContentType()
  {
    return m_featureContentType;
  }

  public FeatureType[] getSubstituts( final GMLSchema contextSchema, final boolean includeAbstract,
      final boolean inclusiveThis )
  {
    if( m_substituteFT == null ) // no substitutiongroup
    {
      if( inclusiveThis ) // return this ?
      {
        if( !includeAbstract && isAbstract() ) // abstract-behaviour ?
          return new FeatureType[0];
        return new FeatureType[]
        { this };
      }
      return new FeatureType[0];
    }
    // query for it
    final FindSubstitutesGMLSchemaVisitor visitor = new FindSubstitutesGMLSchemaVisitor( contextSchema, this,
        includeAbstract, inclusiveThis );
    contextSchema.accept( visitor );
    return visitor.getSubstitutes();
  }

  /**
   * @return
   */
  public boolean isAbstract()
  {
    return m_element.isSetAbstract();
  }

  /**
   * @param writer
   * @param indent
   */
  public void toStringTree( StringWriter writer, int indent )
  {
    writer.write( "\n\n" + StringUtils.repeat( " ", indent ) );
    writer.write( "FeatureType : " + getQName() );
    if( m_substituteFT != null )
    {
      writer.write( "\n" + StringUtils.repeat( " ", indent ) );
      writer.write( " substitutes: " + m_substituteFT.getQName() );
    }
    indent++;
    m_featureContentType.toStringTree( writer, indent );
  }

  /**
   * @return
   */
  public FeatureType getSubstitutionGroupFT()
  {
    return m_substituteFT;
  }
}
