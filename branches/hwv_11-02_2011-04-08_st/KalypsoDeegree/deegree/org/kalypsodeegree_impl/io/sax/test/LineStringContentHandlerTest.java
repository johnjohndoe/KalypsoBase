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
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.LineStringContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 *
 */
public class LineStringContentHandlerTest extends TestCase
{
  private final SAXParserFactory m_saxFactory = SAXParserFactory.newInstance();

  private static final GM_Position POSITION1 = GeometryFactory.createGM_Position( 0.0, 0.1, 0.2);
  private static final GM_Position POSITION2 = GeometryFactory.createGM_Position( 1.0, 1.1, 1.2);
  private static final GM_Position POSITION3 = GeometryFactory.createGM_Position( 2.0, 2.1, 2.2);
  private static final GM_Position POSITION4 = GeometryFactory.createGM_Position( 3.0, 3.1, 3.2);

  private static final String FEW_COORDINATES_MSG = "A gml:LineString must contain at least two positions!";

  private static GM_Curve LINE_STRING;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp( ) throws Exception
  {
    super.setUp();
    m_saxFactory.setNamespaceAware( true );

    final GM_Position[] positions = new GM_Position[]{POSITION1, POSITION2, POSITION3, POSITION4 };
    LINE_STRING = GeometryFactory.createGM_Curve( positions, "EPSG:31467");
  }

  /**
   * tests gml:LineString specified with gml:coordinates
   */
  @Test
  public void testLineString1() throws Exception
  {
    final GM_Curve lineString = parseLineString( "resources/lineString1.gml" );
    assertLineString( lineString );
  }

  /**
   * tests gml:LineString specified with gml:coord elements
   */
  @Test
  public void testLineString2() throws Exception
  {
    final GM_Curve lineString = parseLineString( "resources/lineString2.gml" );
    assertLineString( lineString );
  }

  /**
   * tests gml:LineString specified with gml:pos elements
   */
  @Test
  public void testLineString3() throws Exception
  {
    final GM_Curve lineString = parseLineString( "resources/lineString3.gml" );
    assertLineString( lineString );
  }

  /**
   * tests gml:LineString specified with gml:posList
   */
  @Test
  public void testLineString4() throws Exception
  {
    final GM_Curve lineString = parseLineString( "resources/lineString4.gml" );
    assertLineString( lineString );
  }

  /**
   * tests gml:LineString specified with too few coordinates.
   * should throw an excpetion
   */
  @Test
  public void testLineString5() throws Exception
  {
    try
    {
      parseLineString( "resources/lineString5.gml" );
    }
    catch( final SAXParseException e )
    {
      assertEquals( FEW_COORDINATES_MSG, e.getMessage() );
    }
  }

  private void assertLineString( final GM_Curve lineString )
  {
    assertEquals( LINE_STRING.getCoordinateSystem(), lineString.getCoordinateSystem() );
    assertEquals( LINE_STRING.getNumberOfCurveSegments(), lineString.getNumberOfCurveSegments() );
    assertEquals( LINE_STRING.getStartPoint(), lineString.getStartPoint() );
    assertEquals( LINE_STRING.getEndPoint(), lineString.getEndPoint() );    
  }

  private GM_Curve parseLineString( final String source ) throws Exception
  {
    final InputSource is = new InputSource( getClass().getResourceAsStream( source ) );

    final SAXParser saxParser = m_saxFactory.newSAXParser();
    final XMLReader reader = saxParser.getXMLReader();

    final GM_Curve[] result = new GM_Curve[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {      
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof GM_Curve );
        result[0] = (GM_Curve) value;
      }
    };

    final LineStringContentHandler contentHandler = new LineStringContentHandler( reader, resultEater, null );
    reader.setContentHandler( contentHandler );
    reader.parse( is );

    return result[0];
  }
}
