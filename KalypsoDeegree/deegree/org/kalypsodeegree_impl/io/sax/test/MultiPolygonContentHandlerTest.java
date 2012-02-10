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
import org.junit.Test;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.io.sax.parser.MultiPolygonContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class MultiPolygonContentHandlerTest extends Assert
{
  private final SAXParserFactory m_saxFactory = SAXParserFactory.newInstance();

  private GM_MultiSurface m_multiPolygon;

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

    final String crs = "EPSG:31467";
    final GM_Surface<GM_Polygon> surfaceNoHole = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] {}, crs );
    final GM_Surface<GM_Polygon> surfaceOneHole = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] { interiorRing1 }, crs );
    final GM_Surface<GM_Polygon> surfaceTwoHoles = GeometryFactory.createGM_Surface( exteriorRing, new GM_Position[][] { interiorRing1, interiorRing2 }, crs );

    m_multiPolygon = GeometryFactory.createGM_MultiSurface( new GM_Surface[] { surfaceNoHole, surfaceOneHole, surfaceTwoHoles }, crs );
  }

  @Test
  public void testMultiSurface( ) throws Exception
  {
    final GM_MultiSurface polygon = parseMultiPolygon( "resources/multiPolygon_marshall.gml" );
    assertMultiPolygon( m_multiPolygon, polygon );
  }

  @Test
  public void testMultiSurfaceOuterBoundary( ) throws Exception
  {
    final GM_MultiSurface multi = parseMultiPolygon( "resources/multiPolygon_outerBoundary.gml" );
    assertNotNull( multi );
    assertEquals( 1, multi.getSize() );

    final GM_Surface<GM_Polygon> surface = (GM_Surface<GM_Polygon>) multi.getObjectAt( 0 );
    final GM_Polygon polygon = surface.get( 0 );
    final GM_Position[] exteriorRing = polygon.getExteriorRing();

    assertEquals( 165, exteriorRing.length );
  }

  public static void assertMultiPolygon( final GM_MultiSurface expected, final GM_MultiSurface actual )
  {
    assertEquals( expected.getCoordinateSystem(), actual.getCoordinateSystem() );

    final int expectedSize = expected.getSize();
    assertEquals( expectedSize, actual.getSize() );

    for( int i = 0; i < expectedSize; i++ )
    {
      final GM_Object expectedPolygon = expected.getObjectAt( i );
      final GM_Object actualPolygon = actual.getObjectAt( i );

      assertTrue( expectedPolygon instanceof GM_Surface );
      assertTrue( actualPolygon instanceof GM_Surface );

    }
  }

  private GM_MultiSurface parseMultiPolygon( final String resourceLocation ) throws Exception
  {
    final URL resource = getClass().getResource( resourceLocation );
    final String content = UrlUtilities.toString( resource, SaxParserTestUtils.ENCODING );
    final InputStream inputStream = IOUtils.toInputStream( content );

    return parseMultiPolygon( inputStream );
  }

  private GM_MultiSurface parseMultiPolygon( final InputStream inputStream ) throws ParserConfigurationException, SAXException, IOException
  {
    final InputSource is = new InputSource( inputStream );

    final SAXParser saxParser = m_saxFactory.newSAXParser();
    final XMLReader reader = saxParser.getXMLReader();

    final GM_MultiSurface[] result = new GM_MultiSurface[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof GM_MultiSurface );
        result[0] = (GM_MultiSurface) value;
      }
    };

    final MultiPolygonContentHandler contentHandler = new MultiPolygonContentHandler( reader, resultEater, null );
    reader.setContentHandler( contentHandler );
    reader.parse( is );

    return result[0];
  }
}
