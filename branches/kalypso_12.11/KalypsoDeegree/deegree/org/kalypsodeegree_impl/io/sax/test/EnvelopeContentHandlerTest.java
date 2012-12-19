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
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.EnvelopeContentHandler;
import org.kalypsodeegree_impl.io.sax.parser.IEnvelopeHandler;
import org.kalypsodeegree_impl.io.sax.parser.NullContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class EnvelopeContentHandlerTest extends AssertGeometry
{
  @Test
  public void testNull( ) throws Exception
  {
    final GM_Envelope actual = parseNull( "/etc/test/resources/null.gml" );

    /* Test data */
    final GM_Envelope expected = null;

    assertEnvelope( expected, actual );
  }

  private void testEnvelope( final String testResource ) throws Exception
  {
    final GM_Envelope actual = parseEnvelope( "/etc/test/resources/" + testResource );

    /* Test data */
    final GM_Position lower = GeometryFactory.createGM_Position( 1.0, 2.0 );
    final GM_Position upper = GeometryFactory.createGM_Position( 3.0, 4.0 );
    final GM_Envelope expected = GeometryFactory.createGM_Envelope( lower, upper, "EPSG:31467" );

    assertEnvelope( expected, actual );
  }

  @Test
  public void testEnvelope1( ) throws Exception
  {
    testEnvelope( "envelope1.gml" );
  }

  @Test
  public void testEnvelope2( ) throws Exception
  {
    testEnvelope( "envelope2.gml" );
  }

  @Test
  public void testEnvelope3( ) throws Exception
  {
    testEnvelope( "envelope3.gml" );
  }

  @Test
  public void testEnvelope4( ) throws Exception
  {
    testEnvelope( "envelope4.gml" );
  }

  private void assertEnvelope( final GM_Envelope expected, final GM_Envelope actual )
  {
    if( expected == null && actual == null )
      return;

    assertEquals( expected.getCoordinateSystem(), actual.getCoordinateSystem() );

    final double tolerance = 0.000001;

    assertEquals( expected.getMinX(), actual.getMinX(), tolerance );
    assertEquals( expected.getMinY(), actual.getMinY(), tolerance );
    assertEquals( expected.getMaxX(), actual.getMaxX(), tolerance );
    assertEquals( expected.getMaxY(), actual.getMaxY(), tolerance );
  }

  private GM_Envelope parseNull( final String name ) throws Exception
  {
    final GM_Envelope[] result = new GM_Envelope[1];
    final IEnvelopeHandler handler = new IEnvelopeHandler()
    {
      @Override
      public void handle( final GM_Envelope element )
      {
        result[0] = element;
      }
    };

    /* Create parser */
    final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware( true );

    final SAXParser parser = saxFactory.newSAXParser();
    final XMLReader reader = parser.getXMLReader();

    /* create handler */
    final ContentHandler contentHandler = new NullContentHandler( reader, null, handler );
    reader.setContentHandler( contentHandler );

    /* Read the file */
    final InputSource is = new InputSource( getClass().getResourceAsStream( name ) );
    reader.parse( is );

    return result[0];
  }

  private GM_Envelope parseEnvelope( final String name ) throws Exception
  {
    final GM_Envelope[] result = new GM_Envelope[1];
    final IEnvelopeHandler handler = new IEnvelopeHandler()
    {
      @Override
      public void handle( final GM_Envelope element )
      {
        result[0] = element;
      }
    };

    /* Create parser */
    final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware( true );

    final SAXParser parser = saxFactory.newSAXParser();
    final XMLReader reader = parser.getXMLReader();

    /* create handler */
    final ContentHandler contentHandler = new EnvelopeContentHandler( reader, null, handler );
    reader.setContentHandler( contentHandler );

    /* Read the file */
    final InputSource is = new InputSource( getClass().getResourceAsStream( name ) );
    reader.parse( is );

    return result[0];
  }
}