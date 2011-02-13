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

import junit.framework.TestCase;

import org.junit.Test;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.io.sax.parser.PolygonContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class PolygonContentHandlerTest extends TestCase
{
  private final SAXParserFactory m_saxFactory = SAXParserFactory.newInstance();

  private static final GM_Position POSITION0 = GeometryFactory.createGM_Position( 0.0, 0.0, 0.2 );

  private static final GM_Position POSITION1 = GeometryFactory.createGM_Position( 0.0, 1.0, 0.2 );

  private static final GM_Position POSITION2 = GeometryFactory.createGM_Position( 1.0, 1.0, 1.2 );

  private static final GM_Position POSITION3 = GeometryFactory.createGM_Position( 1.0, 0.0, 2.2 );

  private static final GM_Position POSITION4 = GeometryFactory.createGM_Position( 0.3, 0.6, 4.0 );

  private static final GM_Position POSITION5 = GeometryFactory.createGM_Position( 0.6, 0.3, 5.0 );

  private static final GM_Position POSITION6 = GeometryFactory.createGM_Position( 0.6, 0.7, 6.0 );

  private static final GM_Position POSITION7 = GeometryFactory.createGM_Position( 0.7, 0.6, 7.0 );

  private static GM_Surface<GM_Polygon> SURFACE_ONE_HOLE;

  private static GM_Surface<GM_Polygon> SURFACE_TWO_HOLES;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp( ) throws Exception
  {
    super.setUp();

    m_saxFactory.setNamespaceAware( true );

    final GM_Position[] exteriorRing = new GM_Position[] { POSITION0, POSITION1, POSITION2, POSITION3, POSITION0 };
    final GM_Position[] interiorRing1 = new GM_Position[] { POSITION0, POSITION4, POSITION5, POSITION0 };
    final GM_Position[] interiorRing2 = new GM_Position[] { POSITION2, POSITION7, POSITION6, POSITION2 };

    SURFACE_ONE_HOLE = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] { interiorRing1 }, "EPSG:31467" );
    SURFACE_TWO_HOLES = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] { interiorRing1, interiorRing2 }, "EPSG:31467" );
  }

  @Test
  public void testPolygonOneHole( ) throws Exception
  {
    final GM_Surface<GM_Polygon> polygon = parsePolygon( "resources/polygon1.gml" );
    assertPolygon( SURFACE_ONE_HOLE, polygon );
  }

  @Test
  public void testPolygonTwoHoles( ) throws Exception
  {
    final GM_Surface<GM_Polygon> polygon = parsePolygon( "resources/polygonTwoHoles.gml" );
    assertPolygon( SURFACE_TWO_HOLES, polygon );
  }

  private void assertPolygon( final GM_Surface<GM_Polygon> expected, final GM_Surface<GM_Polygon> polygon )
  {
    assertEquals( expected.getCoordinateSystem(), polygon.getCoordinateSystem() );

    assertEquals( expected.size(), polygon.size() );

    assertPatch( expected.get( 0 ), polygon.get( 0 ) );
  }

  private void assertPatch( final GM_SurfacePatch expectedPatch, final GM_SurfacePatch patch )
  {
    assertRing( expectedPatch.getExteriorRing(), patch.getExteriorRing() );

    final GM_Position[][] expectedInteriorRings = expectedPatch.getInteriorRings();
    final GM_Position[][] interiorRings = patch.getInteriorRings();
    assertEquals( expectedInteriorRings.length, interiorRings.length );

    for( int i = 0; i < interiorRings.length; i++ )
      assertRing( expectedInteriorRings[i], interiorRings[i] );
  }

  private void assertRing( final GM_Position[] expectedRing, final GM_Position[] ring )
  {
    assertEquals( expectedRing.length, ring.length );

    for( int i = 0; i < ring.length; i++ )
      assertEquals( expectedRing[i], ring[i] );
  }

  @SuppressWarnings("unchecked")
  private GM_Surface<GM_Polygon> parsePolygon( final String source ) throws Exception
  {
    final InputSource is = new InputSource( getClass().getResourceAsStream( source ) );

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
}
