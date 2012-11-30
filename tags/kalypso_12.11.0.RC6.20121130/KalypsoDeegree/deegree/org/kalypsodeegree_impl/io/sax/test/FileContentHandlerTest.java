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
package org.kalypsodeegree_impl.io.sax.test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree_impl.io.sax.parser.RangeSetFileContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class FileContentHandlerTest extends AssertGeometry
{
  @Test
  public void testFile( ) throws Exception
  {
    final RangeSetFile actual = parseFile( "/etc/test/resources/file_marshall.gml" );

    /* Test data */
    final RangeSetFile expected = new RangeSetFile( "testname.tif" ); //$NON-NLS-1$
    expected.setMimeType( "image/tif" ); //$NON-NLS-1$

    assertFile( expected, actual );
  }

  private void assertFile( final RangeSetFile expected, final RangeSetFile actual )
  {
    assertEquals( expected.getFileName(), actual.getFileName() );
    assertEquals( expected.getFileStructure(), actual.getFileStructure() );
    assertEquals( expected.getMimeType(), actual.getMimeType() );
    assertEquals( expected.getCompression(), actual.getCompression() );
  }

  private RangeSetFile parseFile( final String name ) throws Exception
  {
    final InputSource is = new InputSource( getClass().getResourceAsStream( name ) );

    final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware( true );

    final SAXParser parser = saxFactory.newSAXParser();
    final XMLReader reader = parser.getXMLReader();

    final RangeSetFile[] result = new RangeSetFile[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof RangeSetFile );
        result[0] = (RangeSetFile) value;
      }
    };

    final ContentHandler contentHandler = new RangeSetFileContentHandler( reader, resultEater, null );
    reader.setContentHandler( contentHandler );
    reader.parse( is );

    return result[0];
  }
}
