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
import org.kalypso.gml.test.GmlParsingTester;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.io.sax.parser.MultiPointContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 *
 */
public class MultiPointContentHandlerTest extends GmlParsingTester
{
  private static double DELTA = 0.01;
  
  private static final GM_Point POINT1 = GeometryFactory.createGM_Point( 0.0, 0.0, "EPSG:4267" );
  
  private static final GM_Point POINT2 = GeometryFactory.createGM_Point( 1.0, 0.0, "EPSG:4267" );
  
  private final SAXParserFactory m_saxFactory = SAXParserFactory.newInstance();
  
  /**
   * tests a gml:MultiPoint specified with gml:pointMember properties
   */ 
  @Test
  public void testMultiPoint1() throws Exception
  {
    final GM_MultiPoint multiPoint = parseMultiPoint( "resources/multiPoint1.gml" );
    assertMultiPoint( multiPoint );
  }
  
  /**
   * tests a gml:MultiPoint defined with both gml:pointMember and gml:pointMembers properties
   */
  @Test
  public void testMultiPoint2() throws Exception
  {
    final GM_MultiPoint multiPoint = parseMultiPoint( "resources/multiPoint2.gml" );
    assertMultiPoint( multiPoint );
  }
 
  /**
   * tests a gml:MultiPoint defined with gml:pointMembers properties, but one of the points
   * has one coordinate more.
   * should throw an exception.
   */
  @Test
  public void testMultiPoint3() throws Exception
  {
    try
    {
      parseMultiPoint( "resources/multiPoint2.gml" );
    }
    catch( SAXParseException e )
    {
      assertTrue( e.getMessage().contains( "in this gml:MultiPoint does not have the number of coordinates specified in" ) );
    }    
  }
  
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    m_saxFactory.setNamespaceAware( true );    
  }
  
  private void assertMultiPoint( GM_MultiPoint multiPoint )
  {
    assertNotNull( multiPoint );
    assertEquals( multiPoint.getCoordinateSystem(), POINT1.getCoordinateSystem() );
    
    assertEquals( 3, multiPoint.getAllPoints().length );
    
    GM_Point firstPoint = multiPoint.getPointAt( 0 );    
    assertTrue( POINT1.distance( firstPoint ) < DELTA );
    
    GM_Point lastPoint = multiPoint.getPointAt( 2 );    
    assertTrue( POINT2.distance( lastPoint ) < DELTA );   
  }

  private GM_MultiPoint parseMultiPoint( String name ) throws Exception
  {
    final InputSource is = new InputSource( getClass().getResourceAsStream( name ) );

    final SAXParser parser = m_saxFactory.newSAXParser();
    final XMLReader reader = parser.getXMLReader();

    final GM_MultiPoint[] result = new GM_MultiPoint[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof GM_MultiPoint );
        result[0] = (GM_MultiPoint) value;
      }
    };

    final ContentHandler contentHandler = new MultiPointContentHandler( resultEater, null, reader );
    reader.setContentHandler( contentHandler );
    reader.parse( is );

    return result[0];
  }
}
