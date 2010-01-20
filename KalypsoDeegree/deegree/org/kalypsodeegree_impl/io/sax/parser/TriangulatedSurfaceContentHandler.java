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
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.GM_TriangulatedSurface_Impl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:TriangulatedSurface element.<br>
 * Parsing must hence starts with the gml:TriangulatedSurface element.<br>
 * 
 * @author Gernot Belger
 */
public class TriangulatedSurfaceContentHandler extends GMLElementContentHandler implements ITriangleHandler
{
  public final static String ELEMENT_TRIANGULATED_SURFACE = "TriangulatedSurface";

  private String m_crs;

  private final UnmarshallResultEater m_resultEater;

  private List<GM_Triangle> m_triangles = new ArrayList<GM_Triangle>();

  private GM_TriangulatedSurface_Impl m_triangulatedSurface;
  
  public TriangulatedSurfaceContentHandler( final UnmarshallResultEater resultEater, XMLReader xmlReader )
  {
    this( resultEater, null, xmlReader );
  }
  
  public TriangulatedSurfaceContentHandler( final UnmarshallResultEater resultEater, final ContentHandler parentContentHandler, XMLReader xmlReader )
  {
    super( NS.GML3, ELEMENT_TRIANGULATED_SURFACE, xmlReader, parentContentHandler );

    m_resultEater = resultEater;
  }

  @Override
  public void doStartElement( final String uri, final String localName, final String name, final Attributes attributes )
  {
    m_crs = ContentHandlerUtils.parseSrsFromAttributes( attributes, null );
    setDelegate( new TrianglePatchesContentHandler( this, m_crs, m_xmlReader ) );
  }

  @Override
  public void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    try
    { 
      m_triangulatedSurface = new GM_TriangulatedSurface_Impl( m_triangles, m_crs );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();

      throw new SAXException( "Unable to create GM_TriangulatedSurface", e );
    }
    finally
    {
      m_triangles = null;
    }

    m_resultEater.unmarshallSuccesful( m_triangulatedSurface );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.ITriangleHandler#handleTriangle(org.kalypsodeegree.model.geometry.GM_Triangle)
   */
  @Override
  public void handle( final GM_Triangle triangle )
  {
    if( m_crs == null )
      m_crs = triangle.getCoordinateSystem();

    if( m_triangles == null )
      m_triangles = new ArrayList<GM_Triangle>();

      m_triangles.add( triangle );
      // TODO: project triangles to my srs?
  }
}
