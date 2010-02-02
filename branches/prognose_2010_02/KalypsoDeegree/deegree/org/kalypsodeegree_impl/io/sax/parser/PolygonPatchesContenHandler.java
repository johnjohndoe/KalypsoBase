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
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:polygonPatches element.<br>
 * 
 * @author Felipe Maximino 
 * 
 */
public class PolygonPatchesContenHandler extends GMLElementContentHandler implements IPolygonHandler
{
  public static final String ELEMENT_POLYGON_PATCHES = "polygonPatches";

  private final IPolygonHandler m_polygonHandler;  

  public PolygonPatchesContenHandler( final IPolygonHandler polygonHandler, final String defaultSrs, final XMLReader xmlReader )
  { 
    super( NS.GML3, ELEMENT_POLYGON_PATCHES, xmlReader, defaultSrs, polygonHandler );    
    m_polygonHandler = polygonHandler;    
  }
  
  /**
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( String uri, String localName, String name )
  { 
    
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( String uri, String localName, String name, Attributes atts )
  { 
    setDelegate( new PolygonPatchContenHandler( this, m_defaultSrs, m_xmlReader ) );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.IPolygonHandler#handlePolygon(org.kalypsodeegree.model.geometry.GM_Polygon)
   */
  @Override
  public void handle( GM_Polygon polygon ) throws SAXException
  {
    m_polygonHandler.handle( polygon );
  }
  
  @Override
  public void handleUnexpectedStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    if( localName.equals( PolygonPatchContenHandler.ELEMENT_POLYGON_PATCH ) )
    {
      ContentHandler polygonPatchContentHandler = new PolygonPatchContenHandler( this, m_defaultSrs, m_xmlReader );
      delegate( polygonPatchContentHandler );    
      polygonPatchContentHandler.startElement( uri, localName, name, atts );        
    }
    else
      throw new SAXParseException( String.format( "Unexpected start element: {%s}%s = %s - should be {%s}%s", uri, localName, name, NS.GML3, m_localName ), m_locator );
  }  
}
