/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 * 
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 * 
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.io.sax.parser;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:exterior element.<br>
 * 
 * @author Gernot Belger
 * @author Felipe Maximino
 */
public class AbstractRingPropertyContentHandler extends GMLElementContentHandler implements IRingHandler
{
  /*
   * xsd attribute 'minOccurs'. If it is 0, we should not always throw an exception if this element doesn't appear.
   */
  private int m_elementMinOccurs;

  private final IRingHandler m_ringHandler;

  private GM_Ring m_ring;

  public AbstractRingPropertyContentHandler( final XMLReader reader, final QName elementName, final IGmlContentHandler parent, final IRingHandler ringHandler, final String defaultSrs, final int minOccurs )
  {
    super( reader, elementName.getNamespaceURI(), elementName.getLocalPart(), defaultSrs, parent );

    m_ringHandler = ringHandler;
    m_elementMinOccurs = minOccurs;
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  {
    new LinearRingContentHandler( getXMLReader(), this, m_defaultSrs ).activate();
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    try
    {
      m_ringHandler.handle( m_ring );
    }
    finally
    {
      m_ring = null;
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final GMLElementContentHandler parentContentHandler = (GMLElementContentHandler) getParentContentHandler();
    if( m_elementMinOccurs == 0 && localName.equals( parentContentHandler.getLocalName() ) )
    {
      activateParent();
      parentContentHandler.endElement( uri, localName, name );
    }
    else
      super.handleUnexpectedEndElement( uri, localName, name );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.IRingHandler#handleRing(org.kalypsodeegree.model.geometry.GM_Ring)
   */
  @Override
  public void handle( final GM_Ring ring )
  {
    m_ring = ring;
  }

  public void setElementMinOccurs( final int elementMinOccurs )
  {
    m_elementMinOccurs = elementMinOccurs;
  }
}