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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.io.sax.parser.PolygonContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class PolygonContentHandlerTest extends Assert
{
  private final SAXParserFactory m_saxFactory = SAXParserFactory.newInstance();

  private GM_Surface<GM_Polygon> m_surfaceOneHole;

  private GM_Surface<GM_Polygon> m_surfaceTwoHoles;

  private GM_Surface<GM_Polygon> m_surfaceNoHole;

  @Before
  public void setUp( ) throws Exception
  {
    m_saxFactory.setNamespaceAware( true );

    final GM_Position pos0 = GeometryFactory.createGM_Position( 0.0, 0.0, 0.2 );

    final GM_Position pos1 = GeometryFactory.createGM_Position( 0.0, 1.0, 0.2 );

    final GM_Position pos2 = GeometryFactory.createGM_Position( 1.0, 1.0, 1.2 );

    final GM_Position pos3 = GeometryFactory.createGM_Position( 1.0, 0.0, 2.2 );

    final GM_Position pos4 = GeometryFactory.createGM_Position( 0.3, 0.6, 4.0 );

    final GM_Position pos5 = GeometryFactory.createGM_Position( 0.6, 0.3, 5.0 );

    final GM_Position pos6 = GeometryFactory.createGM_Position( 0.6, 0.7, 6.0 );

    final GM_Position pos7 = GeometryFactory.createGM_Position( 0.7, 0.6, 7.0 );

    final GM_Position[] exteriorRing = new GM_Position[] { pos0, pos1, pos2, pos3, pos0 };
    final GM_Position[] interiorRing1 = new GM_Position[] { pos0, pos4, pos5, pos0 };
    final GM_Position[] interiorRing2 = new GM_Position[] { pos2, pos7, pos6, pos2 };

    m_surfaceNoHole = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] {}, "EPSG:31467" );
    m_surfaceOneHole = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] { interiorRing1 }, "EPSG:31467" );
    m_surfaceTwoHoles = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] { interiorRing1, interiorRing2 }, "EPSG:31467" );
  }

  @Test
  public void testPolygonNoHole( ) throws Exception
  {
    final GM_Surface<GM_Polygon> polygon = parsePolygon( "resources/polygonNoHole.gml" );
    assertPolygon( m_surfaceNoHole, polygon );
  }

  @Test
  public void testPolygonOneHole( ) throws Exception
  {
    final GM_Surface<GM_Polygon> polygon = parsePolygon( "resources/polygonOneHole.gml" );
    assertPolygon( m_surfaceOneHole, polygon );
  }

  @Test
  public void testPolygonTwoHoles( ) throws Exception
  {
    final GM_Surface<GM_Polygon> polygon = parsePolygon( "resources/polygonTwoHoles.gml" );
    assertPolygon( m_surfaceTwoHoles, polygon );
  }

  // Sax: 5 sec
  @Test
  @Ignore(value = "Too slow for normal testing")
  public void testReadOften( ) throws Exception
  {
    final URL resource = getClass().getResource( "resources/polygonBig.gml" );
    final String content = UrlUtilities.toString( resource, SaxParserTestUtils.ENCODING );

    for( int i = 0; i < 10000; i++ )
      parsePolygonFromContent( content );

  }

  // Sax: 55 sec
  @Test
  @Ignore(value = "Too slow for normal testing")
  public void testReadOften2( ) throws Exception
  {
    final URL resource = getClass().getResource( "resources/polygonBig2.gml" );
    final String content = UrlUtilities.toString( resource, SaxParserTestUtils.ENCODING );

    for( int i = 0; i < 10000; i++ )
      parsePolygonFromContent( content );
  }

  public static void assertPolygon( final GM_Surface<GM_Polygon> expected, final GM_Surface<GM_Polygon> polygon )
  {
    assertEquals( expected.getCoordinateSystem(), polygon.getCoordinateSystem() );

    assertEquals( expected.size(), polygon.size() );

    assertPatch( expected.get( 0 ), polygon.get( 0 ) );
  }

  public static void assertPatch( final GM_SurfacePatch expectedPatch, final GM_SurfacePatch patch )
  {
    assertRing( expectedPatch.getExteriorRing(), patch.getExteriorRing() );

    final GM_Position[][] expectedInteriorRings = expectedPatch.getInteriorRings();
    final GM_Position[][] interiorRings = patch.getInteriorRings();

    if( expectedInteriorRings == null )
      assertNull( interiorRings );
    else
    {
      assertEquals( expectedInteriorRings.length, interiorRings.length );

      for( int i = 0; i < interiorRings.length; i++ )
        assertRing( expectedInteriorRings[i], interiorRings[i] );
    }
  }

  public static void assertRing( final GM_Position[] expectedRing, final GM_Position[] ring )
  {
    assertEquals( expectedRing.length, ring.length );

    for( int i = 0; i < ring.length; i++ )
      assertEquals( expectedRing[i], ring[i] );
  }

  private GM_Surface<GM_Polygon> parsePolygonFromContent( final String content ) throws Exception
  {
    final InputStream inputStream = IOUtils.toInputStream( content );

    return parsePolygon( inputStream );
  }

  private GM_Surface<GM_Polygon> parsePolygon( final String resourceLocation ) throws Exception
  {
    final URL resource = getClass().getResource( resourceLocation );
    final String content = UrlUtilities.toString( resource, SaxParserTestUtils.ENCODING );
    final InputStream inputStream = IOUtils.toInputStream( content );

    return parsePolygon( inputStream );
  }

  @SuppressWarnings("unchecked")
  private GM_Surface<GM_Polygon> parsePolygon( final InputStream inputStream ) throws ParserConfigurationException, SAXException, IOException
  {
    final InputSource is = new InputSource( inputStream );

    final SAXParser saxParser = m_saxFactory.newSAXParser();
    final XMLReader reader = saxParser.getXMLReader();

    final GM_Surface< ? >[] result = new GM_Surface< ? >[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof GM_Surface );
        result[0] = (GM_Surface< ? >) value;
      }
    };

    final PolygonContentHandler contentHandler = new PolygonContentHandler( reader, resultEater, null );
    reader.setContentHandler( contentHandler );
    reader.parse( is );

    return (GM_Surface<GM_Polygon>) result[0];
  }

  // REMARK: exchange method in order to use binding-stuff instead
  // In order to make this work; change the line 'if( m_level < 0 )' to <= in BindingUnmarshalingContentHandler
// private GM_Surface<GM_Polygon> parsePolygon( final InputStream inputStream ) throws ParserConfigurationException,
// SAXException, IOException, TypeRegistryException
// {
// final InputSource is = new InputSource( inputStream );
//
// final SAXParser saxParser = m_saxFactory.newSAXParser();
// final XMLReader reader = saxParser.getXMLReader();
//
// final GM_Surface< ? >[] result = new GM_Surface< ? >[1];
// final UnmarshallResultEater resultEater = new UnmarshallResultEater()
// {
// @Override
// public void unmarshallSuccesful( final Object value )
// {
// assertTrue( value instanceof GM_Surface );
// result[0] = (GM_Surface< ? >) value;
// }
// };
//
// final JAXBContextProvider jaxbContextProvider = new JAXBContextProvider()
// {
// @Override
// public JAXBContext getJaxBContextForGMLVersion( final String gmlVersion )
// {
// if( (gmlVersion == null) || gmlVersion.startsWith( "2" ) )
// {
// return KalypsoOGC2xJAXBcontext.getContext();
// }
// else if( gmlVersion.startsWith( "3" ) )
// {
// return KalypsoOGC31JAXBcontext.getContext();
// }
// throw new UnsupportedOperationException( "GMLVersion " + gmlVersion + " is not supported" );
// }
// };
//
// final GenericGM_ObjectBindingTypeHandler handler = new GenericGM_ObjectBindingTypeHandler( jaxbContextProvider,
// GMLConstants.QN_POLYGON, GMLConstants.QN_SURFACE, GM_Surface.class, true );
//
// handler.unmarshal( reader, null, resultEater, "3" );
//
// reader.parse( is );
//
// return (GM_Surface<GM_Polygon>) result[0];
// }
}
