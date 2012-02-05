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
package org.kalypso.gmlschema.types;

import java.util.Enumeration;
import java.util.Properties;

import javax.xml.namespace.NamespaceContext;

import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.javax.xml.namespace.NamespaceContextImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A {@link ContentHandler} implementation used to delegate calls to a content handler to a child content handler that
 * parses a sub-element of the current scope. <br>
 * Subclasses should implement the blank methods to application-specific behavior.
 *
 * @author Gernot Belger
 * @author Felipe Maximino
 */
public abstract class AbstractGmlContentHandler implements IGmlContentHandler
{
  /* This receiver always holds the currently active content handler */
  // FIXME: replace with more general interface
  private final XMLReader m_reader;

  private Locator m_locator;

  private final IGmlContentHandler m_parentContentHandler;

  private Properties m_prefixMapping;

  public AbstractGmlContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler )
  {
    m_reader = reader;
    m_parentContentHandler = parentContentHandler;
    if( parentContentHandler instanceof AbstractGmlContentHandler )
      m_prefixMapping = new Properties( ((AbstractGmlContentHandler) parentContentHandler).getPrefixMapping() );
    else
      m_prefixMapping = new Properties();
  }

  protected Properties getPrefixMapping( )
  {
    return m_prefixMapping;
  }

  @Override
  public NamespaceContext getNamespaceContext( )
  {
    final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

    final Enumeration< ? > prefixes = m_prefixMapping.propertyNames();
    while( prefixes.hasMoreElements() )
    {
      final String prefix = (String) prefixes.nextElement();
      final String namespace = m_prefixMapping.getProperty( prefix );
      namespaceContext.put( prefix, namespace );
    }

    return namespaceContext;
  }

  @Override
  public Locator getDocumentLocator( )
  {
    return m_locator;
  }

  protected void throwSAXParseException( final String msg, final Object... formatArguments ) throws SAXParseException
  {
    throw createSAXParseException( null, msg, formatArguments );
  }

  protected void throwSAXParseException( final Exception cause, final String msg, final Object... formatArguments ) throws SAXParseException
  {
    throw createSAXParseException( cause, msg, formatArguments );
  }

  protected void warnSAXParseException( final Exception cause, final String format, final Object... formatArguments ) throws SAXException
  {
    final SAXParseException exception = createSAXParseException( cause, format, formatArguments );
    final ErrorHandler errorHandler = m_reader.getErrorHandler();
    if( errorHandler != null )
      errorHandler.warning( exception );
  }

  private SAXParseException createSAXParseException( final Exception cause, final String format, final Object... formatArguments )
  {
    final String messgae = String.format( format, formatArguments );
    return new SAXParseException( messgae, m_locator, cause );
  }

  protected ContentHandler getTopLevel( )
  {
    return m_reader.getContentHandler();
  }

  @Override
  public void activate( )
  {
    setDelegate( this );
  }

  @Override
  public void activateParent( )
  {
    if( m_parentContentHandler != null )
      setDelegate( m_parentContentHandler );
  }

  protected void setDelegate( final ContentHandler contentHandler )
  {
    Assert.isNotNull( contentHandler );

    m_reader.setContentHandler( contentHandler );
  }

  @Override
  public void setDocumentLocator( final Locator locator )
  {
    m_locator = locator;
  }

  protected XMLReader getXMLReader( )
  {
    return m_reader;
  }

  protected ContentHandler getParentContentHandler( )
  {
    return m_parentContentHandler;
  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  @Override
  @SuppressWarnings("unused")
  public void characters( final char[] ch, final int start, final int length ) throws SAXException
  {
    // no op
  }

  /**
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  @Override
  @SuppressWarnings("unused")
  public void endDocument( ) throws SAXException
  {
    // no op
  }

  @Override
  @SuppressWarnings("unused")
  public void endElement( final String uri, final String localName, final String qName ) throws SAXException
  {
    // endDelegation();
  }

  @SuppressWarnings("unused")
  @Override
  public void endPrefixMapping( final String prefix ) throws SAXException
  {
    m_prefixMapping.remove( prefix );
  }

  @Override
  @SuppressWarnings("unused")
  public void ignorableWhitespace( final char[] ch, final int start, final int length ) throws SAXException
  {
    // no op
  }

  @Override
  @SuppressWarnings("unused")
  public void processingInstruction( final String target, final String data ) throws SAXException
  {
    // no op
  }

  /**
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  @Override
  @SuppressWarnings("unused")
  public void skippedEntity( final String name ) throws SAXException
  {
    // no op
  }

  /**
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  @Override
  @SuppressWarnings("unused")
  public void startDocument( ) throws SAXException
  {
    // no op
  }

  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
   *      org.xml.sax.Attributes)
   */
  @Override
  @SuppressWarnings("unused")
  public void startElement( final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  {
    // no op
  }

  /**
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unused")
  @Override
  public void startPrefixMapping( final String prefix, final String uri ) throws SAXException
  {
    m_prefixMapping.setProperty( prefix, uri );
  }
}
