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
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;

/**
 * another builder
 * 
 * @author doemming
 */
public class Element2FeatureTypeBuilder extends AbstractBuilder
{
  private final String m_version;

  public Element2FeatureTypeBuilder( final String version )
  {
    m_version = version;
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#build(org.kalypso.gmlschema.GMLSchema, java.lang.Object)
   */
  public Object[] build( final GMLSchema gmlSchema, final Object elementObject ) throws GMLSchemaException
  {
    final ElementWithOccurs element = (ElementWithOccurs) elementObject;
    final Element theElement = element.getElement();
    final QName ref = theElement.getRef();

    final Element ftElement;
    if( ref == null )
      ftElement = theElement;
    else
    {
      // We should normally never come to this point, because now we have a property whose type is
      // a feature, that is forbidden
      // TODO: maybe this should be handled earlier
      System.out.println( "Property of type feature (probably you must insert a feature-property): " + theElement );

      final ElementReference reference = gmlSchema.resolveElementReference( ref );
      ftElement = reference.getElement();
    }

    final FeatureType result = new FeatureType( gmlSchema, ftElement );
    gmlSchema.register( ftElement, result );
    return new Object[] { result };
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass ) throws GMLSchemaException
  {
    if( !(object instanceof ElementWithOccurs) )
      return false;
    final Element element = ((ElementWithOccurs) object).getElement();
    final QName baseType = GMLSchemaUtilities.findBaseType( gmlSchema, element, m_version );
    if( baseType == null )
      return false;
    final String namespaceURI = baseType.getNamespaceURI();
    final String localPart = baseType.getLocalPart();
    // GML
    if( !NS.GML2.equals( namespaceURI ) )
      return false;
    return GMLSchemaUtilities.getBaseOfFeatureType( m_version ).equals( localPart );
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#replaces(org.kalypso.gmlschema.builder.IBuilder)
   */
  @Override
  public boolean replaces( final IBuilder other )
  {
    return false;
  }

}
