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
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
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
  
  public PointContentHandler( UnmarshallResultEater resultEater, ContentHandler parentContentHandler, XMLReader xmlReader )
  {
    this( null, resultEater, parentContentHandler, null, xmlReader );
  }
  
  public PointContentHandler( final IPointHandler pointHandler, final String defaultSrs, final XMLReader xmlReader )
  {
    this( pointHandler, null, pointHandler, defaultSrs, xmlReader );
  }
  
  private PointContentHandler( final IPointHandler pointHandler, final UnmarshallResultEater resultEater, final ContentHandler parentContentHandler, final String defaultSrs, final XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_POINT, xmlReader, defaultSrs, parentContentHandler );

    m_resultEater = resultEater;
    m_pointHandler = pointHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( String uri, String localName, String name ) throws SAXException
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
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( String uri, String localName, String name ) throws SAXException
  {
    // maybe the property was expecting a triangulated surface, but it was empty */
    if( m_point == null )
    {
      endDelegation();
      m_parentContentHandler.endElement( uri, localName, name );
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
  protected void doStartElement( String uri, String localName, String name, Attributes atts ) throws SAXParseException
  {
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, m_defaultSrs );
     
    /* creates the controlPointsContentHandler allowing it to parse either gml:coordinates or gml:coord or gml:pos*/
    GMLPropertyChoiceContentHandler ctrlPointsContentHandler = new GMLPropertyChoiceContentHandler( this, m_xmlReader, m_activeSrs );    
    ctrlPointsContentHandler.loadPropertiesFor( GMLConstants.QN_POINT );
    setDelegate( ctrlPointsContentHandler ); 
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.IGMLElementHandler#handle(java.lang.Object)
   */
  @Override
  public void handle( List<Double[]> element ) throws SAXParseException
  { 
    /* a point must have at least one tuple */
    if( element.size() != 1 )
    {
      throw new SAXParseException( "One point must have exactly one tuple of coordinates.", m_locator );  
    }      

    /* the point is the first tuple.  */
    Double[] tuple = element.get( 0 );

    if( tuple.length < 2 || tuple.length > 3)
    {
      throw new SAXParseException( "One point must have at least 2 coordinates and at most 3 coordinates!", m_locator );  
    }

    if( tuple.length == 2 )
    {
      m_point = GeometryFactory.createGM_Point( tuple[0], tuple[1], m_activeSrs );
    }
    else
    {
      m_point = GeometryFactory.createGM_Point( tuple[0], tuple[1], tuple[2], m_activeSrs );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.IPositionHandler#handle(org.kalypsodeegree.model.geometry.GM_Position[], java.lang.String)
   */
  @Override
  public void handle( GM_Position[] element, String srs )
  {
    m_point = GeometryFactory.createGM_Point( element[0], srs );    
  }
}
