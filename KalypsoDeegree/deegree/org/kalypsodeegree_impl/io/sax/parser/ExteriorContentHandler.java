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

import org.kalypso.commons.xml.NS;
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
public class ExteriorContentHandler extends GMLElementContentHandler implements IRingHandler
{
  public static final String ELEMENT_EXTERIOR = "exterior";
  
  /*
   * xsd attribute 'minOccurs'. If it is 0, we should not always throw an exception
   * if this element doesn't appear.
   * Default value it's 1.
   */
  public int m_elementMinOccurs;

  private final IRingHandler m_exteriorHandler;
  
  private GM_Ring m_ring;
  
  public ExteriorContentHandler( final IRingHandler exteriorHandler, final String defaultSrs, final XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_EXTERIOR, xmlReader, defaultSrs, exteriorHandler);

    m_exteriorHandler = exteriorHandler;
    m_elementMinOccurs = 1;
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  { 
    setDelegate( new LinearRingContentHandler( this, m_defaultSrs, m_xmlReader ) );
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void doEndElement( final String uri, final String localName, final String name )
  { 
    m_exteriorHandler.handleElement( m_ring );
    m_ring = null;
  }
  
  /**
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( String uri, String localName, String name ) throws SAXException
  {
    if( m_elementMinOccurs == 0 && localName.equals( ( ( GMLElementContentHandler ) m_parentContentHandler ).getLocalName() ) )
    {
      ( ( GMLElementContentHandler ) m_parentContentHandler ).endElement( uri, localName, name );
    }
    else
    {
      super.handleUnexpectedEndElement( uri, localName, name );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.IRingHandler#handleRing(org.kalypsodeegree.model.geometry.GM_Ring)
   */
  @Override
  public void handleElement( final GM_Ring ring )
  {
    m_ring = ring;    
  }
  
  public void setElementMinOccurs( int elementMinOccurs )
  {
    m_elementMinOccurs = elementMinOccurs;
  }
}