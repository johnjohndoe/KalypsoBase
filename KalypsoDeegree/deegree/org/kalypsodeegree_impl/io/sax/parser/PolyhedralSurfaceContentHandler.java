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
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.model.geometry.GM_PolyhedralSurface_Impl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:PolyhedralSurface element.
 * <p>
 * Parsing must hence start with the gml:PolyhedralSurface element.
 * </p>
 * 
 * @author Felipe Maximino
 */
public class PolyhedralSurfaceContentHandler extends GMLElementContentHandler implements IPolygonHandler
{
  private static final String ELEMENT_POLYHEDRAL_SURFACE = "PolyhedralSurface";
  
  private List<GM_Polygon> m_polygons = null;

  private String m_crs;
  
  private GM_PolyhedralSurface_Impl<GM_Polygon> m_polyhedralSurface;
  
  private final UnmarshallResultEater m_resultEater;
    
  
  public PolyhedralSurfaceContentHandler( final UnmarshallResultEater resultEater, XMLReader xmlReader )
  {
    this( resultEater, null, xmlReader );
  }
  
  public PolyhedralSurfaceContentHandler( UnmarshallResultEater resultEater, ContentHandler parentContentHandler, XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_POLYHEDRAL_SURFACE, xmlReader, parentContentHandler );

    m_resultEater = resultEater;
    
    m_polyhedralSurface = null;
  }


  /**
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( String uri, String localName, String name ) throws SAXException
  {
    try
    { 
      m_polyhedralSurface = new GM_PolyhedralSurface_Impl<GM_Polygon>( m_polygons, m_crs );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();

      throw new SAXException( "Unable to create GM_TriangulatedSurface", e );
    }
    finally
    {
      m_polygons = null;
    }
    
    m_resultEater.unmarshallSuccesful( m_polyhedralSurface );    
  }
  
  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( String uri, String localName, String name ) throws SAXException
  {
    // maybe the property was expecting a triangulated surface, but it was empty */
    if( m_polyhedralSurface == null )
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
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( String uri, String localName, String name, Attributes atts )
  {
    m_crs = ContentHandlerUtils.parseSrsFromAttributes( atts, null );
    setDelegate(  new PolygonPatchesContenHandler( this, m_crs, m_xmlReader )  );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.IPolygonHandler#handlePolygon()
   */
  @Override
  public void handle( final GM_Polygon polygon )
  {
    if( m_crs ==  null)
    {
      m_crs = polygon.getCoordinateSystem();
    }
    
    if( m_polygons == null )
    {
      m_polygons = new ArrayList<GM_Polygon>();
    }
    m_polygons.add( polygon );
  }
}