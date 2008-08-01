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
package org.kalypso.gmlschema.types;

import org.kalypso.gmlschema.GMLSchemaException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ContentHandler that wraps another contenthandler. When the simpletype element has been parsed complete <br>
 * the UnMarshallResultEater will be feeded with the value from the UnmarshalResultProvider
 * 
 * @author doemming
 */

public class SimpleTypeUnmarshalingContentHandler implements ContentHandler
{
  private final ContentHandler m_unmarshallerHandler;

  private final UnmarshallResultEater m_marshalResultEater;

  private final UnmarshalResultProvider m_result;

  /**
   * @param marshalResultEater
   *            will be feeded with the result of unmarshalling process
   * @param unmarshallResultProvider
   *            should provide the parsed values
   * @param unmarshallerHandler
   *            contenthandler that will be wrapped for unmarshalling
   */
  public SimpleTypeUnmarshalingContentHandler( final ContentHandler unmarshallerHandler, final UnmarshalResultProvider unmarshallResultProvider, final UnmarshallResultEater marshalResultEater )
  {
    m_unmarshallerHandler = unmarshallerHandler;
    m_result = unmarshallResultProvider;
    m_marshalResultEater = marshalResultEater;
  }

  public void startElement( final String uri, final String local, final String qname, final Attributes atts ) throws SAXException
  {
    try
    {
      end();
    }
    catch( final GMLSchemaException e )
    {
      throw new SAXException( e );
    }
  }

  public void endElement( final String uri, final String local, final String qname ) throws SAXException
  {
    try
    {
      end();
    }
    catch( final GMLSchemaException e )
    {
      throw new SAXException( e );
    }
  }

  private void end( ) throws SAXParseException, GMLSchemaException
  {
    Object value = null;
    try
    {
      value = m_result.getResult();
    }
    finally
    {
      m_marshalResultEater.unmarshallSuccesful( value );
    }
  }

  public void characters( final char[] ch, final int start, final int length ) throws SAXException
  {
    m_unmarshallerHandler.characters( ch, start, length );
  }

  public void ignorableWhitespace( final char[] ch, final int start, final int len ) throws SAXException
  {
    m_unmarshallerHandler.ignorableWhitespace( ch, start, len );
  }

  public void processingInstruction( final String target, final String data ) throws SAXException
  {
    m_unmarshallerHandler.processingInstruction( target, data );
  }

  public void startDocument( ) throws SAXException
  {
    m_unmarshallerHandler.startDocument();
  }

  public void endDocument( ) throws SAXException
  {
    m_unmarshallerHandler.endDocument();
  }

  public void startPrefixMapping( final String prefix, final String uri ) throws SAXException
  {
    m_unmarshallerHandler.startPrefixMapping( prefix, uri );
  }

  public void endPrefixMapping( final String prefix ) throws SAXException
  {
    m_unmarshallerHandler.endPrefixMapping( prefix );
  }

  public void setDocumentLocator( final Locator locator )
  {
    m_unmarshallerHandler.setDocumentLocator( locator );
  }

  public void skippedEntity( final String name ) throws SAXException
  {
    m_unmarshallerHandler.skippedEntity( name );
  }
}
