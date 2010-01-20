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

import java.util.List;

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler that parses a gml:pos element.<br>
 * 
 * @author Gernot Belger
 * @author Felipe Maximino
 */
public class PosContentHandler extends GMLElementContentHandler
{
  public static final String ELEMENT_POS = "pos";

  private StringBuffer m_coordBuffer = new StringBuffer();

  private String m_srs;
  
  private IPositionHandler m_positionHandler;
  
  public PosContentHandler( final ContentHandler parentContentHandler, final IPositionHandler positionHandler, final String defaultSrs, final XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_POS, xmlReader, defaultSrs, parentContentHandler );
    m_positionHandler =  positionHandler;    
  }

  @Override
  public void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  {
    m_srs = ContentHandlerUtils.parseSrsFromAttributes( attributes, m_defaultSrs );      
  }

  @Override
  public void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  { 
    final GM_Position pos = endPos();
    m_positionHandler.handle( new GM_Position[]{pos}, m_srs );
  }

  private GM_Position endPos() throws SAXParseException
  {
    final String coordsString = m_coordBuffer == null ? "" : m_coordBuffer.toString().trim();
    m_coordBuffer = null;
    final List<Double> doubles = (List<Double>) m_positionHandler.parseType( coordsString );

// final int dimension = m_currentCrs.getDimension();
// TODO: check against crs
    final int coordCount = doubles.size();
    
    // HACK: as long as we have no variable sized coordinates, we have only the choice between dimension 2 or 3.
    if( coordCount >= 3 )
    {  
      return GeometryFactory.createGM_Position( doubles.get( 0 ), doubles.get( 1 ), doubles.get( 2 ) );
    }
    if( coordCount == 2 )
      return GeometryFactory.createGM_Position( doubles.get( 0 ), doubles.get( 1 ) );

    throw new SAXParseException( "Not enough coords in pos element: " + coordsString, m_locator );
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  @Override
  public void characters( final char[] ch, final int start, final int length )
  {
    if( m_coordBuffer == null )
      m_coordBuffer = new StringBuffer();

    m_coordBuffer.append( ch, start, length );
  }
}