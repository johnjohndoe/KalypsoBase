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

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.PolygonSpecification;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:PolygonPatch element.
 * <p>
 * Parsing must hence start with the gml:PolygonPatch element.
 * </p>
 * 
 * @author Felipe Maximino
 */
public class PolygonPatchContentHandler extends GMLElementContentHandler implements IRingHandler
{
  public static final String ELEMENT_POLYGON_PATCH = "PolygonPatch";

  private final IPolygonHandler m_polygonHandler;

  private GM_Ring m_ring;

  public PolygonPatchContentHandler( final XMLReader reader, final IPolygonHandler polygonHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_POLYGON_PATCH, defaultSrs, polygonHandler );

    m_polygonHandler = polygonHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    if( m_ring == null )
      throwSAXParseException( "Polygon contains no valid exterior." );

    if( m_ring.getPositions().length < 4 )
      throwSAXParseException( "Polygon contains no enough coordinates." );

    final String crs = m_ring.getCoordinateSystem();

    try
    {
      final GM_Polygon polygon = (GM_Polygon) GeometryFactory.createGM_SurfacePatch( m_ring, null, crs );
      m_polygonHandler.handle( polygon );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();

      throwSAXParseException( e, "Failed to create polygon" );
    }
    finally
    {
      m_ring = null;
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String,
   *      java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    final String activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, m_defaultSrs );
// m_srsDimension = ContentHandlerUtils.parseSrsDimensionFromAttributes( atts );

    final GMLPropertySequenceContentHandler choiceContentHandler = new GMLPropertySequenceContentHandler( getXMLReader(), this, this, activeSrs, new PolygonSpecification() );
    setDelegate( choiceContentHandler );

// // FIXME: Eeek! We need to support interior rings as well!
// final ExteriorContentHandler exteriorContentHandler = new ExteriorContentHandler( getXMLReader(), this, this,
// m_defaultSrs );
// exteriorContentHandler.setElementMinOccurs( 0 );
// exteriorContentHandler.activate();
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.IRingHandler#handleRing(org.kalypsodeegree.model.geometry.GM_Ring)
   */
  @Override
  public void handle( final GM_Ring ring )
  {
    m_ring = ring;
  }
}
