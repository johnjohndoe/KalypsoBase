/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.contribs.org.xml.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * The {@link DelegateContentHandler} disguised as {@link XMLReader}. Mainly the setContentHandler method sets the
 * delegate.<br>
 * Not all methods of {@link XMLReader}, notably {@link #parse(InputSource)} are implemented.
 * 
 * @author Gernot Belger
 */
public class DelegateXmlReader extends DelegateContentHandler implements XMLReader
{
  private ErrorHandler m_errorHandler;

  private EntityResolver m_resolver;

  private DTDHandler m_dtdHandler;

  @Override
  public void setContentHandler( final ContentHandler handler )
  {
    setDelegate( handler );
  }

  /**
   * @see org.kalypso.contribs.javax.xml.sax.IXmlReaderFragment#getContentHandler()
   */
  @Override
  public ContentHandler getContentHandler( )
  {
    return getDelegate();
  }

  @Override
  public void setErrorHandler( final ErrorHandler errorHandler )
  {
    m_errorHandler = errorHandler;
  }

  /**
   * @see org.kalypso.contribs.javax.xml.sax.IXmlReaderFragment#getErrorHandler()
   */
  @Override
  public ErrorHandler getErrorHandler( )
  {
    return m_errorHandler;
  }

  /**
   * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
   */
  @Override
  public boolean getFeature( final String name ) throws SAXNotSupportedException
  {
    throw new SAXNotSupportedException();
  }

  /**
   * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
   */
  @Override
  public void setFeature( final String name, final boolean value ) throws SAXNotSupportedException
  {
    throw new SAXNotSupportedException();
  }

  /**
   * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
   */
  @Override
  public Object getProperty( final String name ) throws SAXNotSupportedException
  {
    throw new SAXNotSupportedException();
  }

  /**
   * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
   */
  @Override
  public void setProperty( final String name, final Object value ) throws SAXNotSupportedException
  {
    throw new SAXNotSupportedException();
  }

  /**
   * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
   */
  @Override
  public void setEntityResolver( final EntityResolver resolver )
  {
    m_resolver = resolver;
  }

  /**
   * @see org.xml.sax.XMLReader#getEntityResolver()
   */
  @Override
  public EntityResolver getEntityResolver( )
  {
    return m_resolver;
  }

  /**
   * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
   */
  @Override
  public void setDTDHandler( final DTDHandler handler )
  {
    m_dtdHandler = handler;
  }

  /**
   * @see org.xml.sax.XMLReader#getDTDHandler()
   */
  @Override
  public DTDHandler getDTDHandler( )
  {
    return m_dtdHandler;
  }

  /**
   * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
   */
  @Override
  public void parse( final InputSource input ) throws SAXException
  {
    throw new SAXNotSupportedException();
  }

  /**
   * @see org.xml.sax.XMLReader#parse(java.lang.String)
   */
  @Override
  public void parse( final String systemId ) throws SAXException
  {
    throw new SAXNotSupportedException();
  }
}
