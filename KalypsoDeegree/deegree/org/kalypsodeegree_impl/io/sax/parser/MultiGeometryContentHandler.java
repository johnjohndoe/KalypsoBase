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
import org.kalypso.gmlschema.types.IGMLElementHandler;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_MultiGeometry;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * A content handler for gml:MultiGeometry
 *
 * @author Felipe Maximino
 */
// FIXME: almost the same code as the other Multi...ContentHandler's;
public class MultiGeometryContentHandler extends GMLElementContentHandler implements IGMLElementHandler<GM_Object>
{
  private final List<GM_Object> m_geometries = new ArrayList<>();

  private final UnmarshallResultEater m_resultEater;

  private GM_MultiGeometry m_multiGeometry;

  private String m_activeSrs;

  public MultiGeometryContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContenthandler, final String defaultSrs )
  {
    this( reader, resultEater, parentContenthandler, defaultSrs, GM_MultiGeometry.MULTI_GEOMETRY_ELEMENT.getLocalPart() );
  }

  protected MultiGeometryContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContenthandler, final String defaultSrs, final String elementName )
  {
    super( reader, NS.GML3, elementName, defaultSrs, parentContenthandler );

    m_resultEater = resultEater;
  }

  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final GM_MultiGeometry curve = endMultiGeometry();

    if( m_resultEater != null )
    {
      m_resultEater.unmarshallSuccesful( curve );
    }
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, getDefaultSrs() );

    final IGMLElementHandler<GM_Object> memberHandler = createMemberHandler( m_activeSrs );
    memberHandler.activate();
  }

  protected IGMLElementHandler<GM_Object> createMemberHandler( final String activeSrs )
  {
    return new GeometryMemberContentHandler( getXMLReader(), this, activeSrs );
  }

  @Override
  public void handleUnexpectedStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    if( localName.equals( memberName() ) )
    {
      final IGmlContentHandler delegateHandler = createMemberHandler( m_activeSrs );
      delegateHandler.activate();
      delegateHandler.startElement( uri, localName, name, atts );
    }
    else
    {
      super.handleUnexpectedStartElement( uri, localName, name, atts );
    }
  }

  protected String memberName( )
  {
    return GM_MultiGeometry.MEMBER_GEOMETRY.getLocalPart();
  }

  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // the property above was expecting a multiLineString, but it is actually empty
    if( m_multiGeometry == null )
    {
      activateParent();
      getParentContentHandler().endElement( uri, localName, name );
    }
    else
    {
      super.handleUnexpectedEndElement( uri, localName, name );
    }
  }

  @Override
  public void handle( final GM_Object element )
  {
    m_geometries.add( element );
  }

  private GM_MultiGeometry endMultiGeometry( )
  {
    final GM_Object[] elements = m_geometries.toArray( new GM_Object[m_geometries.size()] );
    m_multiGeometry = GeometryFactory.createGM_MultiGeometry( elements, m_activeSrs );
    return m_multiGeometry;
  }
}