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
import org.kalypso.gmlschema.types.AbstractGmlContentHandler;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A {@link DelegatingContentHandler} implementation witch delegates all calls to a delegate and also some kind of
 * sanity checks.<br>
 * Typically used to delegate calls to a content handler to a child content handler that parses a sub-element of the
 * current scope. A GMLContentHandler has a 'localName' which is the name of the element to be parsed, e.g, "triangle".
 * *
 * 
 * @author Gernot Belger
 * @author Felipe Maximino
 */
public abstract class GMLElementContentHandler extends AbstractGmlContentHandler
{
  protected final String m_uri;

  protected final String m_localName;

  protected String m_defaultSrs;

  public GMLElementContentHandler( final XMLReader reader, final String uri, final String localName )
  {
    this( reader, uri, localName, null, null );
  }

  public GMLElementContentHandler( final XMLReader reader, final String uri, final String localName, final IGmlContentHandler parentContentHandler )
  {
    this( reader, uri, localName, null, parentContentHandler );
  }

  public GMLElementContentHandler( final XMLReader reader, final String uri, final String localName, final String defaultSrs, final IGmlContentHandler parentContentHandler )
  {
    super( reader, parentContentHandler );

    if( defaultSrs != null )
    {
      m_defaultSrs = defaultSrs;
    }

    m_uri = uri;
    m_localName = localName;
  }

  /**
   * By default, this methods compares the incoming end xml tag with the expected tag (localName). If they match, the
   * method {@link doEndElement} performs the specific actions. Otherwise, calls {@link HandleUnexpectedEndElement},
   * also to allow specific actions.
   * 
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement( final String uri, final String localName, final String name ) throws SAXException
  {
    if( m_uri.equals( uri ) && m_localName.equals( localName ) )
    {
      doEndElement( uri, localName, name );
      activateParent();
    }
    else
    {
      handleUnexpectedEndElement( uri, localName, name );
    }
  }

  /**
   * This method must be implemented by the subclasses to perform specific actions when the xml end tag matches this
   * content handler 'localName'.
   * 
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  protected abstract void doEndElement( String uri, String localName, String name ) throws SAXException;

  /**
   * By default, this methods compares the incoming start xml tag with the expected tag (localName). If they match, the
   * method {@link doStartElement} performs the specific actions. Otherwise, calls {@link HandleUnexpectedStartElement},
   * also to allow specific actions.
   * 
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
   *      org.xml.sax.Attributes)
   */
  @Override
  public void startElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException
  {
    if( m_uri.equals( uri ) && m_localName.equals( localName ) )
      doStartElement( uri, localName, name, atts );
    else
      handleUnexpectedStartElement( uri, localName, name, atts );
  }

  public String getLocalName( )
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

  /**
   * This method must be implemented by the subclasses to perform specific actions when the start xml tag matches this
   * content handler 'localName'. Inside this method, a {@link setDelegate} call must be done, otherwise, there will be
   * no delegation.
   * 
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  protected abstract void doStartElement( final String uri, final String localName, final String name, final Attributes atts ) throws SAXException;

  /*
   * This is the default behavior for this method. Subclasses shall override this method to specific behavior.
   */
  public void handleUnexpectedStartElement( final String uri, final String localName, final String name, @SuppressWarnings("unused") final Attributes atts ) throws SAXException
  {
    throwSAXParseException( "Unexpected start element: {%s}%s = %s - should be {%s}%s", uri, localName, name, NS.GML3, m_localName );
  }

  /*
   * This is the default behavior for this method. Subclasses shall override this method to specific behavior.
   */
  public void handleUnexpectedEndElement( final String uri, final String localName, final String name ) throws SAXException
  {
    throwSAXParseException( "Unexpected end element: {%s}%s = %s - should be {%s}%s", uri, localName, name, m_uri, m_localName );
  }
}
