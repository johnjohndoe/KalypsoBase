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
package org.kalypsodeegree_impl.io.sax;

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:LinearRing element.<br>
 * 
 * @author Gernot Belger
 */
public class TriangleContentHandler extends GMLElementContentHandler implements IRingHandler
{
  public static final String ELEMENT_TRIANGLE = "Triangle";

  private final ITriangleHandler m_triangleHandler;

  private GM_Ring m_ring;

  public TriangleContentHandler( final ITriangleHandler triangleHandler, final String defaultSrs, final XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_TRIANGLE, xmlReader, defaultSrs, triangleHandler  );

    m_triangleHandler = triangleHandler;
  }
  
  @Override
  public void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  {
    setDelegate( new ExteriorContentHandler( this, m_defaultSrs, m_xmlReader ) );
  }

  @Override
  public void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  { 
    if( m_ring == null )
      throw new SAXParseException( "Triangle contains no valid exterior.", m_locator );

    final GM_Position[] ring = m_ring.getPositions();
    
    if( ring.length != 4 )
      throw new SAXParseException( "Triangle must contain exactly 4 coordinates: " + ring.length, m_locator );
    
    final String srs = m_ring.getCoordinateSystem();
    m_ring = null;

    try
    {
      final GM_Triangle gmTriangle = GeometryFactory.createGM_Triangle( ring[0], ring[1], ring[2], srs );
      m_triangleHandler.handleElement( gmTriangle );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();

      throw new SAXParseException( "Failed to create triangle", m_locator, e );
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
}