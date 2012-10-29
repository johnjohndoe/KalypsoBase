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
package org.kalypso.ogc.sensor.deegree;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree_impl.gml.schema.schemata.DeegreeUrlCatalog;
import org.kalypsodeegree_impl.io.sax.marshaller.IGmlMarshaller;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Gernot Belger
 */
public class ObservationLinkMarshaller implements IGmlMarshaller<TimeseriesLinkType>
{
  private static final String PREFIX_XLINK = "xlink"; //$NON-NLS-1$

  private static final String QNAME_XLINK_PREFIX = PREFIX_XLINK + ":"; //$NON-NLS-1$ //$NON-NLS-1$

  String QNAME_OBSLINK_PREFIX = DeegreeUrlCatalog.PREFIX_OBSLINK + ":"; //$NON-NLS-1$

  private final XMLReader m_reader;

  public ObservationLinkMarshaller( final XMLReader reader )
  {
    m_reader = reader;
  }

  @Override
  public void marshall( final TimeseriesLinkType element ) throws SAXException
  {
    /* Basic names */
    final QName elementName = ObservationLinkHandler.TYPE_NAME;
    final String namespaceURI = elementName.getNamespaceURI();
    final String qname = QNAME_OBSLINK_PREFIX + elementName.getLocalPart();

    /* Build attributes */
    final AttributesImpl attributes = new AttributesImpl();
    addAttribute( attributes, "href", element.getHref() ); //$NON-NLS-1$
    addAttribute( attributes, "actuate", element.getActuate() ); //$NON-NLS-1$
    addAttribute( attributes, "arcrole", element.getArcrole() ); //$NON-NLS-1$
//    addAttribute( attributes, "linktype", element.getLinktype() ); //$NON-NLS-1$
    addAttribute( attributes, "role", element.getRole() ); //$NON-NLS-1$
    addAttribute( attributes, "show", element.getShow() ); //$NON-NLS-1$
//    addAttribute( attributes, "timeaxis", element.getTimeaxis() ); //$NON-NLS-1$
    addAttribute( attributes, "title", element.getTitle() ); //$NON-NLS-1$
    addAttribute( attributes, "type", element.getType() ); //$NON-NLS-1$
//    addAttribute( attributes, "valueaxis", element.getValueaxis() ); //$NON-NLS-1$

    /* write it */
    final ContentHandler contentHandler = m_reader.getContentHandler();
    contentHandler.startPrefixMapping( DeegreeUrlCatalog.PREFIX_OBSLINK, namespaceURI ); //$NON-NLS-1$
    contentHandler.startPrefixMapping( PREFIX_XLINK, NS.XLINK ); //$NON-NLS-1$
    contentHandler.startElement( namespaceURI, elementName.getLocalPart(), qname, attributes );
    contentHandler.endElement( namespaceURI, elementName.getLocalPart(), qname );
  }

  private void addAttribute( final AttributesImpl attributes, final String name, final String value )
  {
    if( StringUtils.isEmpty( value ) )
      return;

    final String qname = QNAME_XLINK_PREFIX + name; //$NON-NLS-1$

    attributes.addAttribute( NS.XLINK, name, qname, "string", value ); //$NON-NLS-1$
  }
}
