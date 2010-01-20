/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A {@link DelegatingContentHandler} implementation witch delegates all calls to a delegate and also some kind of sanity checks.<br>
 * Typically used to delegate calls to a content handler to a child content handler that parses a sub-element of the
 * current scope.
 * 
 * @author Gernot Belger
 * @author Felipe Maximino
 */
public abstract class GMLElementContentHandler extends DelegatingContentHandler
{
  protected final String m_uri;

  protected final String m_localName;  
  
  protected String m_defaultSrs;
  
  public GMLElementContentHandler( final String uri, final String localName, final XMLReader xmlReader )
  {
    this(uri, localName, xmlReader, null, null);
  }
  
  public GMLElementContentHandler( final String uri, final String localName, final XMLReader xmlReader, final ContentHandler parentContentHandler )
  {
    this(uri, localName, xmlReader, null, parentContentHandler);
  }
  
  public GMLElementContentHandler( final String uri, final String localName, final XMLReader xmlReader, final String defaultSrs, final ContentHandler parentContentHandler )
  {
    super(xmlReader, parentContentHandler); 
    
    if(defaultSrs != null)
    {
      m_defaultSrs = defaultSrs;
    }    
    
    m_uri = uri;
    m_localName = localName;
  }   
    
  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement( final String uri, final String localName, final String name ) throws SAXException
  {
    if( m_uri.equals( uri ) && m_localName.equals( localName ) )
    { 
      doEndElement( uri, localName, name );
      endDelegation();
    }
    else
      handleUnexpectedEndElement( uri, localName, name);
  }
  
  protected abstract void doEndElement( String uri, String localName, String name ) throws SAXException;
  
  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
   *      org.xml.sax.Attributes)
   */
  @Override
  public void startElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    if( m_uri.equals( uri ) && m_localName.equals( localName ) )
    {
      doStartElement( uri, localName, name, atts );
      delegate();
    }
    else
      handleUnexpectedStartElement( uri, localName, name, atts );      
  }
  
  public String getLocalName()
  {
    return m_localName;
  }
  
  public String getUri( )
  {
    return m_uri;
  }

  public String getDefaultSrs( )
  {
    return m_defaultSrs;
  }  
  
  protected abstract void doStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException;
  
  /*
   * This is the default behavior for this method. Subclasses shall override this method to especific behavior.
   */  
  public void handleUnexpectedStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    throw new SAXParseException( String.format( "Unexpected start element: {%s}%s = %s - should be {%s}%s", uri, localName, name, NS.GML3, m_localName ), m_locator );
  }
  
  /*
   * This is the default behavior for this method. Subclasses shall override this method to especific behavior.
   */ 
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    throw new SAXParseException( String.format( "Unexpected end element: {%s}%s = %s - should be {%s}%s", uri, localName, name, m_uri, m_localName ), m_locator );  
  }
}
