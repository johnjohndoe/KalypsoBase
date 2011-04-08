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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.Attributes;
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

  private final ICoordinatesHandler m_coordinatesHandler;

  /* separator for coordinate values */ 
  private String m_cs;

  /* tuples separator */
  private String m_ts;

  /* decimal indicator */
  private String m_decimal;

  public CoordinatesContentHandler( final XMLReader reader, final IGmlContentHandler parent, final ICoordinatesHandler coordinatesHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_COORDINATES, defaultSrs, parent );

    m_coordinatesHandler = coordinatesHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final List<Double[]> coordinates = endCoordinates();
    m_coordinatesHandler.handle( coordinates );    
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    m_cs = ContentHandlerUtils.parseCsFromAttributes( atts, GMLConstants.DEFAULT_CS );
    m_ts = ContentHandlerUtils.parseTsFromAttributes( atts, GMLConstants.DEFAULT_TS );
    m_decimal = ContentHandlerUtils.parseDecimalFromAttributes( atts, GMLConstants.DEFAULT_DECIMAL );    
  }

  private List<Double[]> endCoordinates( )
  {
    final String coordinatesString = m_coordBuffer == null ? "" : m_coordBuffer.toString(); 
    final String[] tuples = StringUtils.split( coordinatesString, m_ts );
    return parseCoordinates( tuples );
  }

  private List<Double[]> parseCoordinates( final String[] tuples )
  {
    final List<Double[]> coordinates = new ArrayList<Double[]>();
    for( final String tuple : tuples )
    {
      final String[] cs = StringUtils.split( tuple, m_cs );
      final Double[] values = new Double[cs.length];
      for( int i = 0; i < values.length; i++ )
      {
        final String token = cs[i];
        values[i] = parseCoordinate( token );
      }

      coordinates.add( values );
    } 

    return Collections.unmodifiableList( coordinates );
  }

  private Double parseCoordinate( final String token )
  {
    /* if necessary, replace the decimal indicator to '.' to get a java value */
    String tokenDecimal;
    if( GMLConstants.DEFAULT_DECIMAL.equals( m_decimal ) )
      tokenDecimal = token;
    else
      tokenDecimal = token.replace( m_decimal, GMLConstants.DEFAULT_DECIMAL );

    return Double.valueOf( tokenDecimal );
  }

  @Override
  public void characters( final char[] ch, final int start, final int length )
  {
    if( m_coordBuffer == null )
      m_coordBuffer = new StringBuffer();

    m_coordBuffer.append( ch, start, length );
  }
}
