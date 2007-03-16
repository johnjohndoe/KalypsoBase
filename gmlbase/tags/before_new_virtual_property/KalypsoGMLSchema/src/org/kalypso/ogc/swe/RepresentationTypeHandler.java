/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.swe;

import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.SimpleDOMTypeHandler;
import org.kalypso.gmlschema.types.TypeRegistryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Marc Schlienger
 */
public class RepresentationTypeHandler extends SimpleDOMTypeHandler
{
  public RepresentationTypeHandler( )
  {
    super( "representation", new QName( NS.SWE, "RepresentationType" ), false );
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalUnmarshall(org.w3c.dom.Node, java.lang.Object,
   *      org.kalypso.contribs.java.net.IUrlResolver)
   */
  @Override
  protected Object internalUnmarshall( final Node node ) throws TypeRegistryException
  {
    if( node != null && node.hasChildNodes() )
    {
      final Element typeElt = (Element) node.getChildNodes().item( 0 );

      final String localName = typeElt.getLocalName();
      final String base;
      if( "SimpleType".equals( localName ) )
      {
        // read base-attribute of restriction
        final NodeList elementsByTagName = typeElt.getElementsByTagNameNS( NS.XST, "restriction" );
        if( elementsByTagName.getLength() == 1 )
        {
          final Element restrictionElt = (Element) elementsByTagName.item( 0 );
          base = restrictionElt.getAttribute( "base" );

          // TODO: recurse into simple-type and collect all restrictions
        }
        else
          // error handling?
          throw new TypeRegistryException( "SimpleType without st:restriction definition." );
      }
      else if( "Number".equals( localName ) )
        base = "double";
      else if( "Word".equals( localName ) )
        base = "string";
      else if( "Boolean".equals( localName ) )
        base = "boolean";
      else
        base = "string";

      // read base-attribute of restriction
//      final NodeList elementsByTagName = typeElt.getElementsByTagNameNS( NS.SWE, "restriction" );
//      Element restrictionElt = null;
//      if( elementsByTagName.getLength() == 1 )
//        restrictionElt = (Element) elementsByTagName.item( 0 );
      
      // TODO: read IRestriction - Elements from restrictionElt and append them to the RepresentationType
      
      // parse unitOfMeasure
      final NodeList nlUom = typeElt.getElementsByTagNameNS( NS.GML3, "unitOfMeasure" );
      Element uomElt = null;
      if( nlUom.getLength() == 1 )
        uomElt = (Element) nlUom.item( 0 );

      String unit = "";
      if( uomElt != null )
        unit = uomElt.getAttribute( "uom" );

      // parse frame (i.e. timezone information for dates)
      final NodeList nlFrame = typeElt.getElementsByTagNameNS( NS.GML3, "frame" );
      Element frameElt = null;
      if( nlFrame.getLength() == 1 )
        frameElt = (Element) nlFrame.item( 0 );

      String frame = "";
      if( frameElt != null )
        frame = frameElt.getAttributeNS( NS.XLINK, "href" );

      // TODO: parse classification
      String classification = "";

      // TODO: parse restrictions

      // from qname we can get the type handler via the registry
      return new RepresentationType( RepresentationType.KIND.valueOf( localName ), new QName( NS.XSD_SCHEMA, base ), unit, frame, classification );
    }
    else
      throw new TypeRegistryException( "Empty or non-existent representation: cannot create RepresentationType instance" );
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalMarshall(java.lang.Object, org.w3c.dom.Element,
   *      java.net.URL)
   */
  @Override
  protected void internalMarshall( Object value, Element element, URL context )
  {
    final RepresentationType type = (RepresentationType) value;
    final Document doc = element.getOwnerDocument();

    switch( type.getKind() )
    {
      case Boolean:
      {
        final Element eltBoolean = doc.createElementNS( NS.SWE, "Boolean" );

        element.appendChild( eltBoolean );
        break;
      }

      case Word:
      {
        final Element eltWord = doc.createElementNS( NS.SWE, "Word" );

        if( type.getClassification().length() > 0 )
        {
          final Element eltClassification = doc.createElementNS( NS.SWE, "classification" );
          eltWord.appendChild( eltClassification );
        }

        element.appendChild( eltWord );
        break;
      }

      case Number:
      {
        final Element eltNumber = doc.createElementNS( NS.SWE, "Number" );

        handleUnit( type, doc, eltNumber );
        handleFrame( type, doc, eltNumber );

        element.appendChild( eltNumber );
        break;
      }

      case SimpleType:
      {
        final Element eltSimpleType = doc.createElementNS( NS.SWE, "SimpleType" );

        final Element eltRestriction = doc.createElementNS( NS.XST, "restriction" );
        eltRestriction.setAttributeNS( NS.XST, "base", type.getValueTypeName().getLocalPart() );
        eltSimpleType.appendChild( eltRestriction );

        handleUnit( type, doc, eltSimpleType );
        handleFrame( type, doc, eltSimpleType );

        element.appendChild( eltSimpleType );
        break;
      }
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#getValueClass()
   */
  @Override
  public Class getValueClass( )
  {
    return RepresentationType.class;
  }

  private static void handleUnit( final RepresentationType type, final Document doc, final Element elt )
  {
    if( type.getUnit().length() > 0 )
    {
      final Element eltUom = doc.createElementNS( NS.GML3, "unitOfMeasure" );
      eltUom.setAttributeNS( NS.GML3, "uom", type.getUnit() );

      elt.appendChild( eltUom );
    }
  }

  private static void handleFrame( final RepresentationType type, final Document doc, final Element elt )
  {
    final String frame = type.getFrame();
    if( frame != null && frame.length() > 0 )
    {
      final Element eltFrame = doc.createElementNS( NS.SWE, "frame" );
      eltFrame.setAttributeNS( NS.SWE, "frame", frame );

      elt.appendChild( eltFrame );
    }
  }
}
