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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.contribs.java.xml.XMLHelper;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * abstract typehandler to support old deprecated kalypso marshalling methodes, let them extend from this
 * 
 * @author doemming
 */
public abstract class AbstractOldFormatMarshallingTypeHandlerAdapter implements IMarshallingTypeHandler
{
  private static final DocumentBuilderFactory DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();

  private static final SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();

  private DocumentBuilder m_builder;

  public AbstractOldFormatMarshallingTypeHandlerAdapter( )
  {
    try
    {
      m_builder = DOCUMENT_FACTORY.newDocumentBuilder();
      DOCUMENT_FACTORY.setNamespaceAware( true );
    }
    catch( final ParserConfigurationException e )
    {
      e.printStackTrace();
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.IMarshallingTypeHandler#marshal(java.lang.Object, org.xml.sax.ContentHandler,
   *      org.xml.sax.ext.LexicalHandler, java.net.URL)
   */
  @Override
  public void marshal( final Object value, final XMLReader reader, final URL context, final String gmlVersion ) throws SAXException
  {
    try
    {
      final Document document = m_builder.newDocument();
      final Node node = marshall( value, document, context );
      // value is encoded in xml in document object
      final StringWriter writer = new StringWriter();
      // REMARK: we do not write the dom formatted here (argument false), because later the
      // content handler will probably do the formatting (IndentContentHandler). If we format here, we will get empty
      // lines later.
      XMLHelper.writeDOM( node, "UTF-8", writer, false ); //$NON-NLS-1$
      IOUtils.closeQuietly( writer );
      // value is encoded in string
      final String xmlString = writer.toString();
      final String xmlStringNoHeader = XMLUtilities.removeXMLHeader( xmlString );
      final InputSource input = new InputSource( new StringReader( xmlStringNoHeader ) );

      SAX_FACTORY.setNamespaceAware( true );
      final SAXParser saxParser = SAX_FACTORY.newSAXParser();
      final XMLReader xmlReader = saxParser.getXMLReader();
      xmlReader.setContentHandler( reader.getContentHandler() );
      xmlReader.parse( input );
    }
    catch( final SAXException e )
    {
      throw e;
    }
    catch( final Exception e )
    {
      throw new SAXException( e );
    }
  }

  @Override
  public void unmarshal( final XMLReader reader, final URL context, final UnmarshallResultEater marshalResultEater, final String gmlVersion ) throws TypeRegistryException
  {
    // xml to memory
    try
    {
      final UnmarshallResultEater eater = new UnmarshallResultEater()
      {
        @Override
        public void unmarshallSuccesful( final Object value ) throws SAXParseException
        {
          final Node node = (Node) value;
          if( node == null )
            marshalResultEater.unmarshallSuccesful( null );
          else
          {
            Object object = null;
            try
            {
              object = unmarshall( node, context, UrlResolverSingleton.getDefault() );
            }
            catch( final TypeRegistryException e )
            {
              e.printStackTrace();
            }
            marshalResultEater.unmarshallSuccesful( object );
          }
        }
      };

      final Document document = m_builder.newDocument();
      final DOMConstructor domBuilderContentHandler = new DOMConstructor( document, eater );
      // simulate property-tag for dombuilder
      reader.setContentHandler( domBuilderContentHandler );
    }
    catch( final Exception e )
    {
      throw new TypeRegistryException( e );
    }
  }

  /**
   * * old kalypso unmarshall method
   * 
   * @deprecated
   * @see #unmarshal(XMLReader, IUrlResolver, UnMarshallResultEater)
   */
  @Deprecated
  public abstract Object unmarshall( Node node, URL context, IUrlResolver urlResolver ) throws TypeRegistryException;

  /**
   * old kalypso marshall method
   * 
   * @deprecated
   * @see #marshal(QName, Object, ContentHandler, LexicalHandler, URL)
   */
  @Deprecated
  public abstract Node marshall( Object object, Document document, URL context ) throws TypeRegistryException;
}
