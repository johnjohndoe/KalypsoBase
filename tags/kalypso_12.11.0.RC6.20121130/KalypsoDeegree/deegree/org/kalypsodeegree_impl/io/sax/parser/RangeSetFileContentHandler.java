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
package org.kalypsodeegree_impl.io.sax.parser;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.types.AbstractGmlContentHandler;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class RangeSetFileContentHandler extends AbstractGmlContentHandler
{
  private final UnmarshallResultEater m_resultEater;

  private final StringBuilder m_buffer = new StringBuilder();

  private RangeSetFile m_file;

  public RangeSetFileContentHandler( final XMLReader reader )
  {
    this( reader, null, null );
  }

  public RangeSetFileContentHandler( final XMLReader reader, final UnmarshallResultEater resultEater, final IGmlContentHandler parentHandler )
  {
    super( reader, parentHandler );

    m_resultEater = resultEater;
  }

  @Override
  public void startElement( final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  {
    // NS.GML3, File.FILE_ELEMENT.getLocalPart(), parentHandler
    final QName elementName = new QName( uri, localName );
    if( RangeSetFile.FILE_ELEMENT.equals( elementName ) )
    {
      m_file = new RangeSetFile( null );
      return;
    }

    flushBuffer();

    if( RangeSetFile.PROPERTY_FILENAME.equals( elementName ) )
    {
    }
    else if( RangeSetFile.PROPERTY_FILESTRUCTURE.equals( elementName ) )
    {
    }
    else if( RangeSetFile.PROPERTY_MIMETYPE.equals( elementName ) )
    {
    }
    else if( RangeSetFile.PROPERTY_COMPRESSION.equals( elementName ) )
    {
    }
    else
      throwSAXParseException( "Unknown element: %s", elementName );
  }

  @Override
  public void characters( final char[] ch, final int start, final int length )
  {
    m_buffer.append( ch, start, length );
  }

  @Override
  public void endElement( final String uri, final String localName, final String qName ) throws SAXException
  {
    final QName elementName = new QName( uri, localName );
    if( RangeSetFile.FILE_ELEMENT.equals( elementName ) )
    {
      endFile();
      return;
    }

    if( RangeSetFile.PROPERTY_FILENAME.equals( elementName ) )
      m_file.setFileName( flushBuffer() );
    else if( RangeSetFile.PROPERTY_FILESTRUCTURE.equals( elementName ) )
    {
      final String structure = flushBuffer();
      if( !m_file.getFileStructure().equals( structure ) )
        throwSAXParseException( "fileStructure must have value '%s'", m_file.getFileStructure() );
    }
    else if( RangeSetFile.PROPERTY_MIMETYPE.equals( elementName ) )
      m_file.setMimeType( flushBuffer() );
    else if( RangeSetFile.PROPERTY_COMPRESSION.equals( elementName ) )
      m_file.setCompression( flushBuffer() );
    else
    {
      endFile();
      return;

      // FIXME: happens for empty rangeSet's
// throwSAXParseException( "Unknown element: %s", elementName );
    }
  }

  private void endFile( ) throws SAXParseException
  {
    super.activateParent();

    if( m_resultEater != null )
      m_resultEater.unmarshallSuccesful( m_file );

    m_file = null;
  }

  private String flushBuffer( )
  {
    final String string = m_buffer.toString();
    m_buffer.delete( 0, m_buffer.length() );
    return string;
  }
}