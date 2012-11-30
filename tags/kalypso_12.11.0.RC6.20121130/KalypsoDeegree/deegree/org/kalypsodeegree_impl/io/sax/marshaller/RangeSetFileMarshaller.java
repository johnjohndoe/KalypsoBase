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
package org.kalypsodeegree_impl.io.sax.marshaller;

import javax.xml.namespace.QName;

import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author "Gernot Belger"
 */
public class RangeSetFileMarshaller extends AbstractMarshaller<RangeSetFile>
{
  public RangeSetFileMarshaller( final XMLReader reader )
  {
    super( reader, RangeSetFile.FILE_ELEMENT.getLocalPart() );
  }

  @Override
  protected void doMarshallContent( final RangeSetFile marshalledObject ) throws SAXException
  {
    final ContentHandler contentHandler = getXMLReader().getContentHandler();

    /* <element ref="gml:rangeParameters"/> */
    // skipped for the moment

    /* <element name="fileName" type="anyURI"/> */
    final String fileName = marshalledObject.getFileName();
    startElement( contentHandler, RangeSetFile.PROPERTY_FILENAME, EMPTY_ATTRIBUTES );
    contentHandler.characters( fileName.toCharArray(), 0, fileName.length() );
    endElement( contentHandler, RangeSetFile.PROPERTY_FILENAME );

    /* <element name="fileStructure" type="gml:FileValueModelType"/> */
    final String fileStructure = marshalledObject.getFileStructure();
    startElement( contentHandler, RangeSetFile.PROPERTY_FILESTRUCTURE, EMPTY_ATTRIBUTES );
    contentHandler.characters( fileStructure.toCharArray(), 0, fileStructure.length() );
    endElement( contentHandler, RangeSetFile.PROPERTY_FILESTRUCTURE );

    /* <element name="mimeType" type="anyURI" minOccurs="0"/> */
    final String mimeType = marshalledObject.getMimeType();
    if( mimeType != null )
    {
      startElement( contentHandler, RangeSetFile.PROPERTY_MIMETYPE, EMPTY_ATTRIBUTES );
      contentHandler.characters( mimeType.toCharArray(), 0, mimeType.length() );
      endElement( contentHandler, RangeSetFile.PROPERTY_MIMETYPE );
    }

    /* <element name="compression" type="anyURI" minOccurs="0"/> */
    final String compression = marshalledObject.getCompression();
    if( compression != null )
    {
      startElement( contentHandler, RangeSetFile.PROPERTY_COMPRESSION, EMPTY_ATTRIBUTES );
      contentHandler.characters( compression.toCharArray(), 0, compression.length() );
      endElement( contentHandler, RangeSetFile.PROPERTY_COMPRESSION );
    }
  }

  // TODO: move to helper
  private static void startElement( final ContentHandler contentHandler, final QName tagName, final Attributes attributes ) throws SAXException
  {
    // TODO: not nice... we should use a prefix depending on the namespace
    final String prefixedQName = QNAME_GML + tagName.getLocalPart();

    contentHandler.startElement( tagName.getNamespaceURI(), tagName.getLocalPart(), prefixedQName, attributes );
  }

  private static void endElement( final ContentHandler contentHandler, final QName tagName ) throws SAXException
  {
    // TODO: not nice... we should use a prefix depending on the namespace
    final String prefixedQName = QNAME_GML + tagName.getLocalPart();

    contentHandler.endElement( tagName.getNamespaceURI(), tagName.getLocalPart(), prefixedQName );
  }
}