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
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.MultiPointSpecification;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:MultiPoint element.<br>
 * 
 * @author Felipe Maximino
 *
 */
public class MultiPointContentHandler extends GMLElementContentHandler implements IPointHandler
{
  public static final String ELEMENT_MULTI_POINT = "MultiPoint";

  private final UnmarshallResultEater m_resultEater;

  private GM_MultiPoint m_multiPoint;

  private final IMultiPointHandler m_multiPointHandler;

  private final List<GM_Point> m_points;

  private Integer m_srsDimension;

  private String m_activeSrs;

  public MultiPointContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContenthandler )
  {
    this( reader, null, resultEater, parentContenthandler, null );
  }

  public MultiPointContentHandler( final XMLReader reader, final IMultiPointHandler multiPointHandler, final String defaultSrs )
  {
    this( reader, multiPointHandler, null, multiPointHandler, defaultSrs );
  }

  private MultiPointContentHandler( final XMLReader reader, final IMultiPointHandler multiPointHandler, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_MULTI_POINT, defaultSrs, parentContentHandler );

    m_resultEater = resultEater;
    m_multiPointHandler = multiPointHandler;    

    m_points = new ArrayList<GM_Point>();
    m_multiPoint = null;
  }  

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    m_multiPoint = endMultiPoint();

    if( m_resultEater != null )
    {
      m_resultEater.unmarshallSuccesful( m_multiPoint );
    }

    if( m_multiPointHandler != null )
    {
      m_multiPointHandler.handle( m_multiPoint );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // maybe the property was expecting a triangulated surface, but it was empty */
    if( m_multiPoint == null )
    {
      activateParent();
      getParentContentHandler().endElement( uri, localName, name );
    }
    else
    {
      super.handleUnexpectedEndElement( uri, localName, name );
    }
  }

  private GM_MultiPoint endMultiPoint( ) throws SAXParseException
  {
    final int nPoints = m_points.size();

    /* A MultiPoint is defined by one or more Points */
    if( !( nPoints >= 1 ) )
      throwSAXParseException( "A gml:MultiPoint must contain one or more points!" );

    final GM_Point[] pointsArr = m_points.toArray( new GM_Point[nPoints] );
    m_multiPoint = GeometryFactory.createGM_MultiPoint( pointsArr, m_activeSrs );

    return m_multiPoint;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  { 
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, m_defaultSrs );
    m_srsDimension = ContentHandlerUtils.parseSrsDimensionFromAttributes( atts );

    final GMLPropertyChoiceContentHandler choiceContentHandler = new GMLPropertyChoiceContentHandler( getXMLReader(), this, this, m_activeSrs, new MultiPointSpecification() );
    setDelegate( choiceContentHandler );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.IGMLElementHandler#handle(java.lang.Object)
   */
  @Override
  public void handle( final GM_Point point ) throws SAXParseException
  {
    if( m_srsDimension != null && point.getCoordinateDimension() != m_srsDimension )
      throwSAXParseException( "The point " + point.toString() + "in this gml:MultiPoint does not have the number of coordinates specified in 'srsDimension': " + m_srsDimension );

    m_points.add( point );        
  }
}
