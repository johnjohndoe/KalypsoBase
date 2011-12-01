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
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:TriangulatedSurface element.<br>
 * Parsing must hence starts with the gml:TriangulatedSurface element.<br>
 * 
 * @author Gernot Belger
 */
public class TrianglePatchesContentHandler extends GMLElementContentHandler implements ITriangleHandler
{
  public static final String ELEMENT_TRIANGLE_PATCH = "trianglePatches";

  private final ITriangleHandler m_triangleHandler;  

  public TrianglePatchesContentHandler( final XMLReader reader, final ITriangleHandler triangleHandler, final String defaultSrs )
  { 
    super( reader, NS.GML3, ELEMENT_TRIANGLE_PATCH, defaultSrs, triangleHandler );
    m_triangleHandler = triangleHandler;    
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  {
    new TriangleContentHandler( getXMLReader(), this, m_defaultSrs ).activate();
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void doEndElement( final String uri, final String localName, final String name )
  {

  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.ITriangleHandler#handleTriangle(org.kalypsodeegree.model.geometry.GM_Triangle)
   */
  @Override
  public void handle( final GM_Triangle triangle ) throws SAXException
  {
    m_triangleHandler.handle( triangle );
  }

  @Override
  public void handleUnexpectedStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    if( localName.equals( TriangleContentHandler.ELEMENT_TRIANGLE ) )
    {
      final IGmlContentHandler triangleContentHandler = new TriangleContentHandler( getXMLReader(), this, m_defaultSrs );
      triangleContentHandler.activate();
      triangleContentHandler.startElement( uri, localName, name, atts );        
    }
    else
      throwSAXParseException( "Unexpected start element: {%s}%s = %s - should be {%s}%s", uri, localName, name, NS.GML3, m_localName );
  }
}