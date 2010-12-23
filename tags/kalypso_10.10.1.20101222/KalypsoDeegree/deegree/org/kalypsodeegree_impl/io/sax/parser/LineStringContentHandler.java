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
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:LineString element.<br>
 * 
 * @author Felipe Maximino
 *
 */
public class LineStringContentHandler extends GMLElementContentHandler implements IPositionHandler, ICoordinatesHandler
{
  public static final String ELEMENT_LINE_STRING = "LineString";

  private final UnmarshallResultEater m_resultEater;

  private GM_Curve m_lineString;

  private final ICurveHandler m_lineStringHandler;

  private final List<GM_Position> m_positions;

  private String m_activeSrs;

  private Integer m_srsDimension;

  public LineStringContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler )
  {
    this( reader, null, resultEater, parentContentHandler, null );
  }

  public LineStringContentHandler( final XMLReader reader, final ICurveHandler lineStringHandler, final String defaultSrs )
  {
    this( reader, lineStringHandler, null, lineStringHandler, defaultSrs );
  }

  private LineStringContentHandler( final XMLReader reader, final ICurveHandler lineStringHandler, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_LINE_STRING, defaultSrs, parentContentHandler );

    m_resultEater = resultEater;    
    m_lineStringHandler = lineStringHandler;    
    m_positions = new ArrayList<GM_Position>();
    m_lineString = null;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    m_lineString = endLineString();

    if( m_resultEater != null )
    {
      m_resultEater.unmarshallSuccesful( m_lineString );
    }    

    if( m_lineStringHandler != null )
    {
      m_lineStringHandler.handle( m_lineString );
    }    
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // maybe the property was expecting a line string, but it was empty */
    if( m_lineString == null )
    {
      activateParent();
      getParentContentHandler().endElement( uri, localName, name );
    }
    else
    {
      super.handleUnexpectedEndElement( uri, localName, name );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXParseException
  {
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, m_defaultSrs );
    m_srsDimension = ContentHandlerUtils.parseSrsDimensionFromAttributes( atts );

    final GMLPropertyChoiceContentHandler choiceContentHandler = new GMLPropertyChoiceContentHandler( getXMLReader(), this, m_activeSrs );
    choiceContentHandler.loadPropertiesFor( GMLConstants.QN_LINE_STRING );
    choiceContentHandler.activate();
  }  

  private GM_Curve endLineString( ) throws SAXParseException
  {
    try
    {
      final int size = m_positions.size();

      if( size < 2 )
        throwSAXParseException( "A gml:LineString must contain at least two positions!" );

      return GeometryFactory.createGM_Curve( m_positions.toArray( new GM_Position[m_positions.size()] ), m_activeSrs );
    }
    catch( final GM_Exception e)
    {
      throwSAXParseException( e, "It was not possible to create a gml:LineString!" );
      return null;
    }        
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.IPositionHandler#handle(org.kalypsodeegree.model.geometry.GM_Position[], java.lang.String)
   */
  @Override
  public void handle( final GM_Position[] positions, final String srs ) throws SAXParseException
  {
    for( final GM_Position position : positions )
    {
      /* check srsDimension */
      if( m_srsDimension != null && position.getCoordinateDimension() != m_srsDimension )
        throwSAXParseException( "The position " + position.toString() + "in this gml:LineString does not have the number of coordinates specified in 'srsDimension': " + m_srsDimension );

      m_positions.add( position );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.ICoordinatesHandler#handle(java.util.List)
   */
  @Override
  public void handle( final List<Double[]> element ) throws SAXParseException
  {
    for( final Double[] tuple : element )
    {
      final int tupleSize = tuple.length;

      /* check srsDimension */
      if( m_srsDimension != null && tupleSize != m_srsDimension )
        throwSAXParseException( "The position " + tuple.toString() + "in this gml:LineString does not have the number of coordinates specified in 'srsDimension': " + m_srsDimension );

      if( tuple.length == 2 )
        m_positions.add( GeometryFactory.createGM_Position( tuple[0], tuple[1] ) );
      else
        m_positions.add( GeometryFactory.createGM_Position( tuple[0], tuple[1], tuple[2] ) );
    }    
  }
}
