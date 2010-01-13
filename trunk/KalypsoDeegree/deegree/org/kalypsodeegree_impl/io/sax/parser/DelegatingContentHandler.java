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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A {@link ContentHandler} implementation used to delegate calls to a content handler to a child content handler that parses a sub-element of the
 * current scope. <br>
 *   
 * Subclasses should implement the blank methods to application-especific behavior. 
 * 
 * @author Gernot Belger
 * @author Felipe Maximino 
 * 
 */
public class DelegatingContentHandler implements ContentHandler
{
  protected ContentHandler m_delegate;
   
  protected final XMLReader m_xmlReader;
  
  protected Locator m_locator;
  
  protected final ContentHandler m_parentContentHandler;
  
  public DelegatingContentHandler ( final XMLReader xmlReader, final ContentHandler parentContentHandler )
  { 
    m_xmlReader = xmlReader;
    m_parentContentHandler = parentContentHandler;        
  }
  
  protected void setDelegate( final ContentHandler delegate )
  {
    m_delegate = delegate;

    if( m_delegate != null )
    {
      m_delegate.setDocumentLocator( m_locator );
    }
  }
  
  protected void delegate( )
  {
    if( m_delegate != null )
    {
      m_xmlReader.setContentHandler( m_delegate );
    }
  }
  
  protected void delegate( final ContentHandler delegate  )
  {
    setDelegate( delegate );
    delegate();
  }
  
  
  public void endDelegation()
  {
    if ( m_parentContentHandler != null )
    {
      setDelegate( null );
      m_xmlReader.setContentHandler( m_parentContentHandler );
    }
  }  
  
  public ContentHandler getDelegate( )
  {
    return m_delegate;
  }

  public Locator getLocator( )
  {
    return m_locator;
  }
  
  /**
   * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
   */
  public void setDocumentLocator( final Locator locator )
  {
    /* We remember the document locator in order to set it to every new delegate. */
    m_locator = locator;

    if( m_delegate != null )
      m_delegate.setDocumentLocator( locator );
  }
  
  public XMLReader getXmlReader( )
  {
    return m_xmlReader;
  }

  public ContentHandler getParentContentHandler( )
  {
    return m_parentContentHandler;
  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  @Override
  public void characters( char[] ch, int start, int length ) throws SAXException
  {
    // no op
  }

  /**
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  @Override
  public void endDocument( ) throws SAXException
  {
    // no op
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement( String uri, String localName, String qName ) throws SAXException
  {
    // endDelegation();    
  }

  /**
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  @Override
  public void endPrefixMapping( String prefix ) throws SAXException
  {
    // no op    
  }

  /**
   * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
   */
  @Override
  public void ignorableWhitespace( char[] ch, int start, int length ) throws SAXException
  {
    // no op    
  }

  /**
   * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
   */
  @Override
  public void processingInstruction( String target, String data ) throws SAXException
  {
    // no op    
  }

  /**
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  @Override
  public void skippedEntity( String name ) throws SAXException
  {
    // no op    
  }

  /**
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  @Override
  public void startDocument( ) throws SAXException
  {
    // no op    
  }

  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement( String uri, String localName, String qName, Attributes atts ) throws SAXException
  {
    // delegate();    
  }

  /**
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
   */
  @Override
  public void startPrefixMapping( String prefix, String uri ) throws SAXException
  {
    // no op    
  }
}
