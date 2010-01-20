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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A content handler that parses control points. Actually, it delegates the parsing to a child content handler that
 * parses an especific sub-element. The control points to which this content handler can delegate are provided in a Map.   
 * 
 * @author Felipe Maximino
 */
public class GMLControlPointsContentHandler extends DelegatingContentHandler
{
  private Map<QName, ContentHandler> m_registeredCtrlPoints;
  
  public GMLControlPointsContentHandler( IControlPointHandler ctrlPointHandler, final XMLReader xmlReader )
  {
    super( xmlReader, ctrlPointHandler );
    
    m_registeredCtrlPoints = new HashMap<QName, ContentHandler>();
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
    ContentHandler delegate = findDelegate( new QName( uri, localName ) );

    if( delegate != null )
    {
      delegate( delegate );
      delegate.startElement( uri, localName, name, atts );      
    }
    else
    {
      throw new SAXParseException( String.format( "Unexpected start element: %s - %s -  %s", uri, localName, name ), getLocator() );
    }
  }

  private ContentHandler findDelegate( final QName qName )
  {
    return m_registeredCtrlPoints.get( qName );
  }
  
  public void registerControlPoint( final QName qName, final ContentHandler contentHandler )
  {
    m_registeredCtrlPoints.put( qName, contentHandler );
  }
}
