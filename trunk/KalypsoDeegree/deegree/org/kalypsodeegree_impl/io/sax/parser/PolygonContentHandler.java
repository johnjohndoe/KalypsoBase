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
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.PolygonSpecification;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:Polygon element.<br>
 * 
 * @author Gernot Belger
 */
public class PolygonContentHandler extends GMLElementContentHandler implements IRingHandler
{
  private final UnmarshallResultEater m_resultEater;

  private GM_Polygon m_surface = null;

  private String m_activeSrs;

  private GM_Position[] m_exteriorRing = null;

  private final List<GM_Position[]> m_interiorRings = new ArrayList<>();

  private final ISurfaceHandler<GM_PolygonPatch> m_surfaceHandler;

  public PolygonContentHandler( final XMLReader xmlReader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    this( xmlReader, null, resultEater, parentContentHandler, defaultSrs );
  }

  public PolygonContentHandler( final XMLReader xmlReader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler )
  {
    this( xmlReader, null, resultEater, parentContentHandler, null );
  }

  public PolygonContentHandler( final XMLReader xmlReader, final ISurfaceHandler<GM_PolygonPatch> surfaceHandler, final String defaultSrs )
  {
    this( xmlReader, surfaceHandler, null, surfaceHandler, defaultSrs );
  }

  private PolygonContentHandler( final XMLReader xmlReader, final ISurfaceHandler<GM_PolygonPatch> surfaceHandler, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    super( xmlReader, NS.GML3, GM_Polygon.POLYGON_ELEMENT.getLocalPart(), defaultSrs, parentContentHandler );

    m_resultEater = resultEater;
    m_surfaceHandler = surfaceHandler;
  }

  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    m_surface = endSurface();

    if( m_resultEater != null )
      m_resultEater.unmarshallSuccesful( m_surface );

    if( m_surfaceHandler != null )
      m_surfaceHandler.handle( m_surface );
  }

  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // maybe the property was expecting a polygon, but it was empty */
    if( m_surface == null )
    {
      activateParent();
      getParentContentHandler().endElement( uri, localName, name );
    }
    else
      super.handleUnexpectedEndElement( uri, localName, name );
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, getDefaultSrs() );
    /* m_srsDimension = */ContentHandlerUtils.parseSrsDimensionFromAttributes( atts );

    final GMLPropertySequenceContentHandler choiceContentHandler = new GMLPropertySequenceContentHandler( getXMLReader(), this, this, m_activeSrs, new PolygonSpecification() );
    setDelegate( choiceContentHandler );
  }

  private GM_Polygon endSurface( ) throws SAXParseException
  {
    try
    {
      if( m_exteriorRing == null )
        throwSAXParseException( "A gml:Polygon must have an exterior ring!" );

      final GM_Position[][] interiorRings = m_interiorRings.toArray( new GM_Position[m_interiorRings.size()][] );
      return GeometryFactory.createGM_Surface( m_exteriorRing, interiorRings, m_activeSrs );
    }
    catch( final GM_Exception e )
    {
      throwSAXParseException( "It was not possible to create a gml:Polygon!" );
      return null;
    }
  }

  @Override
  public void handle( final GM_Ring ring )
  {
    if( m_exteriorRing == null )
      m_exteriorRing = ring.getPositions();
    else
      m_interiorRings.add( ring.getPositions() );
  }
}
