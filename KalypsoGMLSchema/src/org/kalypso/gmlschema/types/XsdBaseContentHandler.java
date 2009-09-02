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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The content handler for the {@link org.kalypsodeegree.model.XsdBaseTypeHandler}.
 * <p>
 * It combines the before used classes {@link org.kalypso.gmlschema.types.SimpleTypeUnmarshalingContentHandler},
 * {@link org.kalypso.gml.ToStringContentHandler}, internally used
 * {@link org.kalypso.gmlschema.types.UnMarshallResultEater} and internally used
 * {@link org.kalypso.contribs.java.util.logging.ILogger}.
 * </p>
 * <p>
 * The classes where combined for performance reasons and to avoid the creation of a content handler for each call to
 * {@link org.kalypsodeegree.model.XsdBaseTypeHandler#unmarshal(XMLReader, URL, UnMarshallResultEater, String)}.
 * </p>
 * <p>
 * See also remark in
 * {@link org.kalypsodeegree.model.XsdBaseTypeHandler#unmarshal(XMLReader, URL, UnMarshallResultEater, String)}.
 * </p>
 * 
 * @author Gernot Belger
 */
public class XsdBaseContentHandler implements ContentHandler
{
  private final StringBuffer m_buffer = new StringBuffer();

  private UnmarshallResultEater m_marshalResultEater;

  private final IMarshallingTypeHandler m_typeHandler;

  /**
   * @param marshalResultEater
   *            will be feeded with the result of unmarshalling process
   * @param unmarshallResultProvider
   *            should provide the parsed values
   * @param unmarshallerHandler
   *            contenthandler that will be wrapped for unmarshalling
   */
  public XsdBaseContentHandler( final IMarshallingTypeHandler typeHandler, final UnmarshallResultEater marshalResultEater )
  {
    m_typeHandler = typeHandler;
    m_marshalResultEater = marshalResultEater;
  }

  /**
   * @param resetBuffer
   *            Clears the intenal string buffer
   */
  public void setMarshalResultEater( final UnmarshallResultEater marshalResultEater, final boolean resetBuffer )
  {
    m_marshalResultEater = marshalResultEater;
    if( resetBuffer == true )
      m_buffer.delete( 0, m_buffer.length() );
  }

  public void startElement( final String uri, final String local, final String qname, final Attributes atts ) throws SAXException
  {
    end();
  }

  public void endElement( final String uri, final String local, final String qname ) throws SAXException
  {
    end();
  }

  private void end( ) throws SAXParseException
  {
    Object value = null;
    try
    {
      final String stringResult = m_buffer.toString();
      value = m_typeHandler.parseType( stringResult );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      // TODO: why no rethrow exception here?
    }
    finally
    {
      if( m_marshalResultEater != null )
        m_marshalResultEater.unmarshallSuccesful( value );
    }
  }

  public void characters( final char[] ch, final int start, final int length )
  {
    m_buffer.append( ch, start, length );
  }

  public void ignorableWhitespace( final char[] ch, final int start, final int len )
  {
    m_buffer.append( ch, start, len );
  }

  public void processingInstruction( final String target, final String data )
  {
    m_buffer.append( "processingInstruction: " + target + " / " + data + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public void startDocument( )
  {
  }

  public void endDocument( )
  {
  }

  public void startPrefixMapping( final String prefix, final String uri )
  {
  }

  public void endPrefixMapping( final String prefix )
  {
  }

  public void setDocumentLocator( final Locator locator )
  {
  }

  public void skippedEntity( final String name )
  {
    m_buffer.append( "skippedEntity: " + name + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
