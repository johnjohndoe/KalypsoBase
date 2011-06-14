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
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler for gml:MultiLineString NOTE: Deprecated with GML 3.0 and included for backwards compatibility with
 * GML 2. Use the "MultiCurve" element instead
 * 
 * @author Felipe Maximino
 */
public class MultiLineStringContentHandler extends GMLElementContentHandler implements ICurveHandler
{
  private static final String ELEMENT_MULTI_LINE_STRING = "MultiLineString";

  private final UnmarshallResultEater m_resultEater;

  private String m_activeSrs;

  private final List<GM_Curve> m_lineStrings;

  private GM_MultiCurve m_multiLineString;

  public MultiLineStringContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContenthandler, final String defaultSrs )
  {
    super( reader, NS.GML3, ELEMENT_MULTI_LINE_STRING, defaultSrs, parentContenthandler );

    m_resultEater = resultEater;

    m_lineStrings = new ArrayList<GM_Curve>();
    m_multiLineString = null;
  }

  public MultiLineStringContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentContenthandler )
  {
    this( reader, resultEater, parentContenthandler, null );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  protected void doEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    final GM_MultiCurve lineString = endMultiLineString();

    if( m_resultEater != null )
    {
      m_resultEater.unmarshallSuccesful( lineString );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#doStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  protected void doStartElement( final String uri, final String localName, final String name, final Attributes atts )
  {
    m_activeSrs = ContentHandlerUtils.parseSrsFromAttributes( atts, m_defaultSrs );

    new LineStringMemberContentHandler( getXMLReader(), this, m_activeSrs ).activate();
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedStartElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void handleUnexpectedStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    // if it is a new gml:lineStringMember, delegate
    if( localName.equals( LineStringMemberContentHandler.ELEMENT_LINE_STRING_MEMBER ) )
    {
      final IGmlContentHandler delegateHandler = new LineStringMemberContentHandler( getXMLReader(), this, m_activeSrs );
      delegateHandler.activate();
      delegateHandler.startElement( uri, localName, name, atts );
    }
    else
    {
      super.handleUnexpectedStartElement( uri, localName, name, atts );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.GMLElementContentHandler#handleUnexpectedEndElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    // the property above was expecting a multiLineString, but it is actually empty
    if( m_multiLineString == null )
    {
      activateParent();
      getParentContentHandler().endElement( uri, localName, name );
    }
    else
    {
      super.handleUnexpectedEndElement( uri, localName, name );
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.IGMLElementHandler#handle(java.lang.Object)
   */
  @Override
  public void handle( final GM_Curve element )
  {
    m_lineStrings.add( element );
  }

  private GM_MultiCurve endMultiLineString( )
  {
    final GM_Curve[] lineStringsArr = m_lineStrings.toArray( new GM_Curve[m_lineStrings.size()] );
    m_multiLineString = GeometryFactory.createGM_MultiCurve( lineStringsArr, m_activeSrs );

    return m_multiLineString;
  }
}
