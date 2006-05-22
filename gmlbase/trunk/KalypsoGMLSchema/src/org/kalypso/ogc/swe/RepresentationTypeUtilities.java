/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchemaException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author schlienger
 */
public class RepresentationTypeUtilities
{
  public static RepresentationType unmarshalRepresentationType( final Node repNode ) throws GMLSchemaException
  {
    if( repNode != null && repNode.hasChildNodes() )
    {
      final Element typeElt = (Element) repNode.getChildNodes().item( 0 );

      // read base-attribute of restriction
      final NodeList elementsByTagName = typeElt.getElementsByTagNameNS( NS.XST, "restriction" );
      final Element restrictionElt;
      if( elementsByTagName.getLength() != 1 )
        restrictionElt = null;
      else
        restrictionElt = (Element) elementsByTagName.item( 0 );

      final String localName = typeElt.getLocalName();
      final String base;
      if( "SimpleType".equals( localName ) )
      {
        if( restrictionElt != null )
          base = restrictionElt.getAttribute( "base" );
        else
          // error handling?
          base = "string";
      }
      else if( "Number".equals( localName ) )
        base = "double";
      else if( "Word".equals( localName ) )
        base = "string";
      else if( "Boolean".equals( localName ) )
        base = "boolean";
      else
        base = "string";

      // TODO: parse unitOfMeasure

      // TODO: parse frame (i.e. timezone information for dates)

      // TODO: parse restrictions

      // from qname we can get the type handler via the registry
      return new RepresentationType( RepresentationType.KIND.valueOf( localName ), new QName( NS.XSD_SCHEMA, base ) );
    }

    throw new GMLSchemaException( "Empty or non-existent representation: cannot create RepresentationType instance" );
  }

  public static Node marshalRepresentationType( final RepresentationType type, final Element element )
  {
    // TODO [marc]
    return null;
  }
}
