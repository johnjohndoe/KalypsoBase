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

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaConstants;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * 
 * another builder
 * 
 * @author doemming
 */
public class Element2FeatureTypeBuilder implements IBuilder
{

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#build(org.kalypso.gmlschema.GMLSchema, java.lang.Object)
   */
  public Object[] build( GMLSchema gmlSchema, Object elementObject )
  {
    final Element element = (Element)elementObject;
    final QName ref = element.getRef();
    if( ref != null )
    {
      final ElementReference reference = (ElementReference)gmlSchema.resolveReference( ref );
      return build( reference );
    }
    final FeatureType result = new FeatureType( gmlSchema, element );
    gmlSchema.register( element, result );
    return new Object[]
    { result };
  }

  /**
   * @param reference
   */
  private Object[] build( ElementReference reference )
  {
    final GMLSchema schema = reference.getGMLSchema();
    final Element element = reference.getElement();
    return build( schema, element );
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  public boolean isBuilderFor( GMLSchema gmlSchema, Object object, String namedPass )
  {
    if( !( object instanceof Element ) )
      return false;
    final QName baseType = GMLSchemaUtilities.findBaseType( gmlSchema, (Element)object );
    if( baseType == null )
      return false;
    final String namespaceURI = baseType.getNamespaceURI();
    final String localPart = baseType.getLocalPart();
    // GML
    if( !GMLSchemaConstants.NS_GML2.equals( namespaceURI ) )
      return false;
    if( !"AbstractFeatureType".equals( localPart ) )
      return false;
    return true;
  }

}
