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
import org.kalypso.gmlschema.types.IGMLElementHandler;
import org.kalypsodeegree.model.geometry.GM_MultiGeometry;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler for gml:geometryMember property.
 *
 * @author Gernot Belger
 */
public class GeometryMemberContentHandler extends GMLElementContentHandler implements IGMLElementHandler<GM_Object>
{
  private final IGMLElementHandler<GM_Object> m_geometryHandler;

  public GeometryMemberContentHandler( final XMLReader reader, final IGMLElementHandler<GM_Object> geometryHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, GM_MultiGeometry.MEMBER_GEOMETRY.getLocalPart(), defaultSrs, geometryHandler );

    m_geometryHandler = geometryHandler;
  }

  @Override
  protected void doEndElement( final String uri, final String localName, final String name )
  {
    // nothing to do
  }

  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final GMLElementContentHandler parentContentHandler = (GMLElementContentHandler) getParentContentHandler();

    // this property may have 0 occurences
    if( localName.equals( parentContentHandler.getLocalName() ) )
    {
      activateParent();
      parentContentHandler.endElement( uri, localName, name );
    }
    else
      super.handleUnexpectedEndElement( uri, localName, name );
  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    new GeometryContentHandler( getXMLReader(), this, getDefaultSrs() ).activate();
  }

  @Override
  public void handle( final GM_Object element ) throws SAXException
  {
    m_geometryHandler.handle( element );
  }
}