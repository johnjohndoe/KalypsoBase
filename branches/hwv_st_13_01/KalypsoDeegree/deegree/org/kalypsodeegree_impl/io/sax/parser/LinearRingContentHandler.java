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

import java.util.ArrayList;
import java.util.List;

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.LinearRingSpecification;
import org.kalypsodeegree_impl.model.geometry.GM_Ring_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:LinearRing element.<br>
 * 
 * @author Gernot Belger
 */
public class LinearRingContentHandler extends GMLElementContentHandler implements IPositionHandler, ICoordinatesHandler
{
  public static final String ELEMENT_LINEAR_RING = "LinearRing";

  private final List<GM_Position> m_poses;

  private final IRingHandler m_lineaRingHandler;

  private String m_srs;

  public LinearRingContentHandler( final XMLReader reader, final IRingHandler lineaRingHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_LINEAR_RING, defaultSrs, lineaRingHandler );

    m_lineaRingHandler = lineaRingHandler;
    m_poses = new ArrayList<GM_Position>();
  }

  @Override
  public void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  {
    m_srs = ContentHandlerUtils.parseSrsFromAttributes( attributes, m_defaultSrs );

    final GMLPropertyChoiceContentHandler choiceContentHandler = new GMLPropertyChoiceContentHandler( getXMLReader(), this, this, m_defaultSrs, new LinearRingSpecification() );
    choiceContentHandler.activate();
  }

  @Override
  public void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // a LinearRing is defined by four or more coordinate tuples
    if( m_poses.size() < 4 )
      throwSAXParseException( "A gml:LinearRing must contain at least 4 coordinates: %s", +m_poses.size() );

    // the first and last coordinates must be coincident
    if( !m_poses.get( 0 ).equals( m_poses.get( m_poses.size() - 1 ) ) )
      throwSAXParseException( "The first and last coordinates of this gml:LinearRing must be coincident: %s/%s", m_poses.get( 0 ), m_poses.get( m_poses.size() - 1 ) );

    final GM_Position[] poses = new GM_Position[m_poses.size()];

    int cnt = 0;
    for( final GM_Position pos : m_poses )
    {
      poses[cnt++] = pos;
    }
    m_poses.clear();

    try
    {
      final GM_Ring_Impl ring = GeometryFactory.createGM_Ring( poses, m_srs );
      m_lineaRingHandler.handle( ring );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();

      throwSAXParseException( e, "Failed to create ring" );
    }
  }

  @Override
  public void handle( final PositionsWithSrs pws )
  {
    // TODO: should transform the pos if it is not in the same crs as myself
    if( m_srs == null )
      m_srs = pws.getSrs();

    for( final GM_Position pos : pws.getPositions() )
      m_poses.add( pos );
  }

  @Override
  public void handle( final List<Double[]> element ) throws SAXParseException
  {
    for( final Double[] tuple : element )
    {
      if( tuple.length < 2 )
        throwSAXParseException( "A position must have at least 2 coordinates." );

      GM_Position position;
      if( tuple.length == 2 )
      {
        position = GeometryFactory.createGM_Position( tuple[0], tuple[1] );
      }
      else
        // >2
      {
        position = GeometryFactory.createGM_Position( tuple[0], tuple[1], tuple[2] );
      }
      m_poses.add( position );
    }
  }
}