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
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.io.sax.parser.geometrySpec.MultiSurfaceSpecification;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler which parses a gml:Polygon element.<br>
 * 
 * @author Gernot Belger
 */
public class MultiSurfaceContentHandler extends GMLElementContentHandler implements ISurfaceHandler<GM_Polygon>
{
  public static final String ELEMENT_MULTI_SURFACE = GMLConstants.QN_MULTI_SURFACE.getLocalPart();

  private final List<GM_Surface< ? extends GM_SurfacePatch>> m_surfaces = new ArrayList<GM_Surface< ? extends GM_SurfacePatch>>();

  private final UnmarshallResultEater m_resultEater;

  private GM_MultiSurface m_multiSurface = null;

  private String m_activeSrs;

  private final IMultiSurfaceHandler m_surfaceHandler;

  public MultiSurfaceContentHandler( final XMLReader xmlReader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    this( xmlReader, null, resultEater, parentContentHandler, defaultSrs );
  }

  private MultiSurfaceContentHandler( final XMLReader xmlReader, final IMultiSurfaceHandler surfaceHandler, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContentHandler, final String defaultSrs )
  {
    super( xmlReader, NS.GML3, ELEMENT_MULTI_SURFACE, defaultSrs, parentContentHandler );

    m_resultEater = resultEater;    
    m_surfaceHandler = surfaceHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    m_multiSurface = endMultiSurface();

    if( m_resultEater != null )
      m_resultEater.unmarshallSuccesful( m_multiSurface );

    if( m_surfaceHandler != null )
      m_surfaceHandler.handle( m_multiSurface );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // maybe the property was expecting an element, but it was empty */
    if( m_multiSurface == null )
    {
      activateParent();
      getParentContentHandler().endElement( uri, localName, name );
    }
    else
      super.handleUnexpectedEndElement( uri, localName, name );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, m_defaultSrs );
    /* Integer srsDimension = */ContentHandlerUtils.parseSrsDimensionFromAttributes( atts );

    final ContentHandler sequenceContentHandler = new GMLPropertySequenceContentHandler( getXMLReader(), this, this, m_activeSrs, new MultiSurfaceSpecification() );
    setDelegate( sequenceContentHandler );
  }

  private GM_MultiSurface endMultiSurface( )
  {
    @SuppressWarnings("unchecked")
    final GM_Surface< ? extends GM_SurfacePatch>[] patches = m_surfaces.toArray( new GM_Surface[m_surfaces.size()] );

    return GeometryFactory.createGM_MultiSurface( patches, m_activeSrs );
  }

  /**
   * @see org.kalypso.gmlschema.types.IGMLElementHandler#handle(java.lang.Object)
   */
  @Override
  public void handle( final GM_Surface<GM_Polygon> element )
  {
    m_surfaces.add( element );
  }
}
