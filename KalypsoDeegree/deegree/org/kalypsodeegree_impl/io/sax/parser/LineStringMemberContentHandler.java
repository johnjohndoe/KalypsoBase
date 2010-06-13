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
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler for gml:lineStringMember property. NOTE: Deprecated with GML 3.0 and included only for backwards
 * compatibility with GML 2.0. Use "curveMember" instead.
 * 
 * @author Felipe Maximino
 */
public class LineStringMemberContentHandler extends GMLElementContentHandler implements ICurveHandler
{
  public static final String ELEMENT_LINE_STRING_MEMBER = "lineStringMember";

  private final ICurveHandler m_curveHandler;

  public LineStringMemberContentHandler( final XMLReader xmlReader, final ICurveHandler curveHandler, final String defaultSrs )
  {
    super( NS.GML3, ELEMENT_LINE_STRING_MEMBER, xmlReader, defaultSrs, curveHandler );

    m_curveHandler = curveHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name )
  {
    // nothing to do
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final GMLElementContentHandler parentContentHandler = (GMLElementContentHandler) m_parentContentHandler;

    // this property may have 0 occurences
    if( localName.equals( parentContentHandler.m_localName ) )
    {
      endDelegation();
      parentContentHandler.endElement( uri, localName, name );
    }
    else
    {
      super.handleUnexpectedEndElement( uri, localName, name );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    setDelegate( new LineStringContentHandler( this, m_defaultSrs, m_xmlReader ) );
  }

  /**
   * @see org.kalypso.gmlschema.types.IGMLElementHandler#handle(java.lang.Object)
   */
  @Override
  public void handle( final GM_Curve element ) throws SAXException
  {
    m_curveHandler.handle( element );
  }
}
