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
import java.util.StringTokenizer;

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:coordinates element.
 * <p>
 * The GML default values for tuple and coordinate separators and decimal indicator are:
 * <ul>
 *  <li>tuple separator: " " </li>
 *  <li>coordinate separator: "," </li>
 *  <li>decimal indicator: "." </li>
 * </ul>
 * 
 * @author Felipe Maximino
 */
public class CoordinatesContentHandler extends GMLElementContentHandler
{
  public static final String ELEMENT_COORDINATES = "coordinates";
  
  private StringBuffer m_coordBuffer = new StringBuffer();
  
  private ICoordinatesHandler m_coordinatesHandler;
  
  /* separator for coordinate values */ 
  private String m_cs;
  
  /* tuples separator */
  private String m_ts;
  
  /* decimal indicator */
  private String m_decimal;
  
  public CoordinatesContentHandler( ContentHandler parentContentHandler, ICoordinatesHandler coordinatesHandler, String defaultSrs, XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_COORDINATES, xmlReader, defaultSrs, parentContentHandler );
    
    m_coordinatesHandler = coordinatesHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( String uri, String localName, String name ) throws SAXException
  {
    List<Double[]> coordinates = endCoordinates();
    m_coordinatesHandler.handle( coordinates );    
  }
  
  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( String uri, String localName, String name, Attributes atts )
  {
    m_cs = ContentHandlerUtils.parseCsFromAttributes( atts, GMLConstants.DEFAULT_CS );
    m_ts = ContentHandlerUtils.parseTsFromAttributes( atts, GMLConstants.DEFAULT_TS );
    m_decimal = ContentHandlerUtils.parseDecimalFromAttributes( atts, GMLConstants.DEFAULT_DECIMAL );    
  }

  private List<Double[]> endCoordinates( )
  {
    String coordinatesString = m_coordBuffer == null ? "" : m_coordBuffer.toString(); 
      
    /* if necessary, replace the decimal indicator to '.' to get a java value */
    if( !m_decimal.equals( "." ) ) 
    {
      coordinatesString = coordinatesString.replace( m_decimal, "." );
    }
    
    String[] tuples = separateTuples( coordinatesString );
    
    /* List of tuples. each double is a Double array */
    List<Double[]> coordinates = separateCoordinates( tuples );
    
    return coordinates;
  }

  private List<Double[]> separateCoordinates( String[] tuples )
  {
    List<Double[]> coordinates = new ArrayList<Double[]>();
    for( String tuple : tuples )
    { 
      final StringTokenizer st = new StringTokenizer( tuple, m_cs );
      Double[] tupleValue = new Double[st.countTokens()];
      int i = 0;
      while( st.hasMoreTokens() )
      { 
        tupleValue[i++] = Double.valueOf( st.nextToken() );
      }
      coordinates.add( tupleValue );
    } 
    
    return coordinates;
  }

  private String[] separateTuples( final String coordinatesString )
  { 
    final StringTokenizer st = new StringTokenizer( coordinatesString, m_ts );

    String[] tuples = new String[st.countTokens()];
    int i = 0;
    while ( st.hasMoreTokens() ) 
    {
      tuples[i++] = st.nextToken();
    }

    return tuples;
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
