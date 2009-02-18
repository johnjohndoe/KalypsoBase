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
package org.kalypso.gmlschema.adv;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.commons.xml.NS;
import org.kalypso.commons.xml.NSPrefixProvider;
import org.kalypso.commons.xml.NSUtilities;

/**
 * utilities to handle adv related stuff
 * 
 * @author doemming
 */
public class ADVUtilities
{
  final static String advPrefix;

  final static String xsPrefix;
  static
  {
    final NSPrefixProvider provider = NSUtilities.getNSProvider();
    advPrefix = provider.getPreferredPrefix( NS.ADV_NAS, "adv" );
    xsPrefix = provider.getPreferredPrefix( NS.XSD_SCHEMA, "xsd" );
  }

  private static final String ADV_XPATH_NS = "declare namespace " + xsPrefix + "='" + NS.XSD_SCHEMA + "' " + "declare namespace " + advPrefix + "='" + NS.ADV_NAS + "' ";

  private static final String ADV_XPATH = ADV_XPATH_NS + xsPrefix + ":annotation/" + xsPrefix + ":appinfo/" + "adv" + ":referenziertesElement";

  /**
   * @param element
   *          example: <code>
   * <xs:annotation>
   *   <xs:appinfo>
   *     <adv:referenziertesElement>xplan:BPlanObjekt</adv:referenziertesElement>
   *   </xs:appinfo>
   * </xs:annotation>
   * </code>
   */
  public static QName getReferenziertesElement( final Element element )
  {
    return selectXPathAsQName( element, ADV_XPATH );
  }

  /**
   * @param element
   * @param xpath
   *          xpath mus return single result
   * @return result as QName or <code>null</code> for unknown element
   */
  private static QName selectXPathAsQName( final Element element, final String xpath )
  {
    final XmlObject[] xmlObjects = selectXPath( element, xpath );

    switch( xmlObjects.length )
    {
      case 0:
        return null;
      case 1:
        final XmlAnyTypeImpl qName = (XmlAnyTypeImpl) xmlObjects[0];
        final String name = qName.stringValue();
        final String[] names = name.split( ":" );
        final String namespace = qName.get_store().getNamespaceForPrefix( names[0] );
        return new QName( namespace, names[1] );
      default:
        throw new UnsupportedOperationException( "can not handle multi 'adv:referenziertesElement' fragments in schema: \n" + element.toString() );
    }
  }

  /**
   * @param element
   * @param xpath
   * @return result of query
   */
  private static XmlObject[] selectXPath( final Element element, final String xpath )
  {
    final String fullXpath = xpath;
    return element.selectPath( fullXpath );
  }

}
