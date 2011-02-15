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

import java.util.List;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.PointSpecification;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:Point element.<br>
 * 
 * @author Felipe Maximino
 */
public class PointContentHandler extends GMLElementContentHandler implements ICoordinatesHandler, IPositionHandler
{
  public static final String ELEMENT_POINT = "Point";

  private final UnmarshallResultEater m_resultEater;

  private final IPointHandler m_pointHandler;

  private String m_activeSrs;

  private GM_Point m_point;

  public PointContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    this( reader, null, resultEater, parentContentHandler, defaultSrs );
  }

  public PointContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler )
  {
    this( reader, null, resultEater, parentContentHandler, null );
  }

  public PointContentHandler( final XMLReader reader, final IPointHandler pointHandler, final String defaultSrs )
  {
    this( reader, pointHandler, null, pointHandler, defaultSrs );
  }

  private PointContentHandler( final XMLReader reader, final IPointHandler pointHandler, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_POINT, defaultSrs, parentContentHandler );

    m_resultEater = resultEater;
    m_pointHandler = pointHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    if( m_resultEater != null )
    {
      m_resultEater.unmarshallSuccesful( m_point );
    }

    if( m_pointHandler != null )
    {
      m_pointHandler.handle( m_point );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // maybe the property was expecting a triangulated surface, but it was empty */
    if( m_point == null )
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
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, m_defaultSrs );

    /* creates the controlPointsContentHandler allowing it to parse either gml:coordinates or gml:coord or gml:pos */
    final GMLPropertyChoiceContentHandler ctrlPointsContentHandler = new GMLPropertyChoiceContentHandler( getXMLReader(), this, this, m_activeSrs, new PointSpecification() );
    setDelegate( ctrlPointsContentHandler );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.IGMLElementHandler#handle(java.lang.Object)
   */
  @Override
  public void handle( final List<Double[]> element ) throws SAXParseException
  {
    /* a point must have at least one tuple */
    if( element.size() != 1 )
      throwSAXParseException( "One point must have exactly one tuple of coordinates." );

    /* the point is the first tuple. */
    final Double[] tuple = element.get( 0 );

    if( tuple.length < 2 || tuple.length > 3 )
      throwSAXParseException( "One point must have at least 2 coordinates and at most 3 coordinates!" );

    if( tuple.length == 2 )
    {
      m_point = GeometryFactory.createGM_Point( tuple[0], tuple[1], m_activeSrs );
    }
    else
    {
      m_point = GeometryFactory.createGM_Point( tuple[0], tuple[1], tuple[2], m_activeSrs );
    }
  }

  @Override
  public void handle( final PositionsWithSrs pws )
  {
    final GM_Position[] positions = pws.getPositions();
    final String srs = pws.getSrs();
    m_point = GeometryFactory.createGM_Point( positions[0], srs );
  }
}
