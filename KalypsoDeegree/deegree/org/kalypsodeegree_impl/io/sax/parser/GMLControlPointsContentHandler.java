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

import org.kalypsodeegree.model.geometry.GM_Position;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler that parses control points. Actually, it delegates the parsing to a child content handler that
 * parses an especific sub-element. Control Points must be gml:pos, gml:posList or gml:pointProperty elements
 * 
 * @author Felipe Maximino
 */
public class GMLControlPointsContentHandler extends DelegatingContentHandler implements IPositionHandler
{
  private final IPositionHandler m_positionHandler;

  private final String m_defaultSrs;
  
  public GMLControlPointsContentHandler( IPositionHandler positionHandler, final String defaultSrs, final XMLReader xmlReader )
  {
    super( xmlReader, positionHandler );

    m_positionHandler = positionHandler;
    m_defaultSrs = defaultSrs;
  }

  @Override
  public void endElement( final String uri, final String localName, final String name ) throws SAXException
  { 
    GMLElementContentHandler parentContentHandler = (GMLElementContentHandler) getParentContentHandler();
    if(parentContentHandler.getLocalName().equals( localName ))
    {
      endDelegation();
      parentContentHandler.endElement( uri, localName, name );      
    }
  }

  @Override
  public void startElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    ContentHandler delegate = findDelegate( uri, localName, name );

    delegate.startElement( uri, localName, name, atts );
    delegate( delegate );
  }

  private ContentHandler findDelegate( final String uri, final String localName, final String name ) throws SAXParseException
  {
    if( localName.equals( PosContentHandler.ELEMENT_POS ) )
    {
      return new PosContentHandler( this, m_defaultSrs, m_xmlReader );
    }

    if( localName.equals( PosListContentHandler.ELEMENT_POSLIST ) )
    {
      return new PosListContentHandler( this,  m_defaultSrs, m_xmlReader );
    }

    throw new SAXParseException( String.format( "Unexpected start element: %s - %s -  %s", uri, localName, name ), getLocator() );
  }
  
  @Override
  public void handleElement(GM_Position[] pos )
  { 
    m_positionHandler.handleElement( pos );
  }  

  /**
   * @see org.kalypsodeegree_impl.io.sax.IPositionHandler#handleElement(org.kalypsodeegree.model.geometry.GM_Position[], java.lang.String)
   */
  @Override
  public void handleElement( GM_Position[] pos, String text )
  {
    m_positionHandler.handleElement( pos, text );    
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.IPositionHandler#parseType(java.lang.String)
   */
  @Override
  public Object parseType( String text )
  {
    return m_positionHandler.parseType( text );
  }

}
