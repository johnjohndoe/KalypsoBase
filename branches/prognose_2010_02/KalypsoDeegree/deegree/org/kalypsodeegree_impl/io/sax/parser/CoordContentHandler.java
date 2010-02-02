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
package org.kalypsodeegree_impl.io.sax.parser;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.commons.xml.NS;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:coord element.<br>
 * 
 * @author Felipe Maximino
 */
public class CoordContentHandler extends GMLElementContentHandler
{
  public static final String ELEMENT_COORD = "coord";  
  
  public static final String ELEMENT_X = "X";
  public static final String ELEMENT_Y = "Y";
  public static final String ELEMENT_Z = "Z";  
  
  private final ICoordinatesHandler m_coordinatesHandler;
  
  private int m_xyz;
  
  private List<Double> m_coords;

  private StringBuffer m_coordBuffer;

  private String m_currentName;
  
  public CoordContentHandler( ContentHandler parentContentHandler, ICoordinatesHandler coordinatesHandler, String defaultSrs, XMLReader xmlReader )
  { 
    super( NS.GML3, ELEMENT_COORD, xmlReader, defaultSrs, parentContentHandler );
    
    m_coordinatesHandler = coordinatesHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( String uri, String localName, String name ) throws SAXException
  {    
    List<Double[]> coords = endCoords();
    m_coordinatesHandler.handle( coords );      
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( String uri, String localName, String name, Attributes atts )
  {
    m_xyz = 0;
    m_coords = new ArrayList<Double>();
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( String uri, String localName, String name ) throws SAXException
  {
    
    if( m_uri.equals( uri ) && m_currentName.equals( localName ) ) 
    {
      endOneCoordinate();
    }
    else
    {
      throw new SAXParseException( String.format( "Unexpected end element: {%s}%s = %s ", uri, localName, name ), m_locator );
    }
  }

  private void endOneCoordinate( )
  {
    String element = m_coordBuffer.toString();
    m_coords.add( Double.valueOf( element ) );
    m_coordBuffer.delete( 0, m_coordBuffer.length() );
  }

  private List<Double[]> endCoords( )
  { 
    List<Double[]> coords = new ArrayList<Double[]>(1);    
    coords.add( m_coords.toArray( new Double[m_coords.size()] ) );
    
    return coords;
  }  
  
  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void handleUnexpectedStartElement( String uri, String localName, String name, Attributes atts ) throws SAXException
  {
    m_currentName = localName;
    if( m_xyz == 0 && m_uri.equals( uri ) && ELEMENT_X.equals( localName ) ) // first element must be a gml:X
    {
      m_xyz++;
    }
    else if( m_xyz == 1 && m_uri.equals( uri ) && ELEMENT_Y.equals( localName ) ) // second element must be a gml:Y
    {
      m_xyz++;
    }
    else if( m_xyz == 2 && m_uri.equals( uri ) && ELEMENT_Z.equals( localName ) ) // when present, third element must be a gml:Z
    {
      m_xyz++;
    }
    else
    {
      throw new SAXParseException( String.format( "Unexpected start element: {%s}%s = %s ", uri, localName, name ), m_locator );
    }
  }
  
  @Override
  public void characters( final char[] ch, final int start, final int length )
  {
    if( m_coordBuffer == null )
      m_coordBuffer = new StringBuffer();

    m_coordBuffer.append( ch, start, length );
  }    
}
