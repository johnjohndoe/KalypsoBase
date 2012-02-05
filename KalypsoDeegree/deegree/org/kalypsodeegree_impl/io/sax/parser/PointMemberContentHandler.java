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
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler that parses a gml:pointMember property.<br>
 *
 * @author Felipe Maximino
 */
public class PointMemberContentHandler extends GMLElementContentHandler implements IPointHandler
{
  public static final String ELEMENT_POINT_MEMBER = "pointMember";

  private final IPointHandler m_pointHandler;

  public PointMemberContentHandler( final XMLReader reader, final IGmlContentHandler parent, final IPointHandler pointHandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_POINT_MEMBER, defaultSrs, parent );

    m_pointHandler = pointHandler;
  }

  @Override
  protected void doEndElement( final String uri, final String localName, final String name )
  {

  }

  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    // TODO: verify if this property has an xlink
    setDelegate( new PointContentHandler( getXMLReader(), this, getDefaultSrs() ) );
  }

  @Override
  public void handle( final GM_Point element ) throws SAXException
  {
    m_pointHandler.handle( element );
  }
}
