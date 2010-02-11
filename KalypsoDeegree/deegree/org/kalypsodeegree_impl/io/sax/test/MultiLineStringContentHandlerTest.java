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
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.MultiLineStringContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 *
 */
public class MultiLineStringContentHandlerTest extends TestCase
{

  private final SAXParserFactory m_saxFactory = SAXParserFactory.newInstance();
  
  private static final GM_Position POSITION1 = GeometryFactory.createGM_Position( 0.0, 0.1, 0.2);
  private static final GM_Position POSITION2 = GeometryFactory.createGM_Position( 1.0, 1.1, 1.2);
  private static final GM_Position POSITION3 = GeometryFactory.createGM_Position( 2.0, 2.1, 2.2);
  private static final GM_Position POSITION4 = GeometryFactory.createGM_Position( 3.0, 3.1, 3.2);
  
  private static GM_Curve LINE_STRING;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp( ) throws Exception
  {
    super.setUp();
    m_saxFactory.setNamespaceAware( true );
    
    GM_Position[] positions = new GM_Position[]{POSITION1, POSITION2, POSITION3, POSITION4 };
    LINE_STRING = GeometryFactory.createGM_Curve( positions, "EPSG:31467");
  }
  
  /**
   * tests gml:MultiLineString with no members
   */
  @Test
  public void testMultiLineString1() throws Exception
  {
    final GM_MultiCurve multiLineString = parseMultiLineString( "resources/multiLineString1.gml" );
    assertMultiLineString( multiLineString, 0);
  }
  
  /**
   * tests gml:MultiLineString with one member
   */
  @Test
  public void testMultiLineString2() throws Exception
  {
    final GM_MultiCurve multiLineString = parseMultiLineString( "resources/multiLineString2.gml" );
    assertMultiLineString( multiLineString, 1);
  }
  
  /**
   * tests gml:MultiLineString with two members
   */
  @Test
  public void testMultiLineString3() throws Exception
  {
    final GM_MultiCurve multiLineString = parseMultiLineString( "resources/multiLineString3.gml" );
    assertMultiLineString( multiLineString, 2 );
  }
  
  private void assertMultiLineString( GM_MultiCurve multiLineString, int expectedSize )
  {
    if( expectedSize > 0 )
    {
      GM_Curve lineStringGot = multiLineString.getAllCurves()[0];
      
      assertEquals( expectedSize, multiLineString.getSize() );
      
      assertEquals( LINE_STRING.getCoordinateSystem(), lineStringGot.getCoordinateSystem() );
      assertEquals( LINE_STRING.getNumberOfCurveSegments(), lineStringGot.getNumberOfCurveSegments() );
      assertEquals( LINE_STRING.getStartPoint(), lineStringGot.getStartPoint() );
      assertEquals( LINE_STRING.getEndPoint(), lineStringGot.getEndPoint() ); 
    }
  }

  private GM_MultiCurve parseMultiLineString( final String source ) throws Exception
  {
    final InputSource is = new InputSource( getClass().getResourceAsStream( source ) );
    
    final SAXParser saxParser = m_saxFactory.newSAXParser();
    final XMLReader xmlReader = saxParser.getXMLReader();
    
    final GM_MultiCurve[] result = new GM_MultiCurve[1];
    UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {      
      @Override
      public void unmarshallSuccesful( Object value )
      {
        assertTrue( value instanceof GM_MultiCurve );
        result[0] = (GM_MultiCurve) value;
      }
    };
    
    MultiLineStringContentHandler contentHandler = new MultiLineStringContentHandler( resultEater, null, xmlReader );
    xmlReader.setContentHandler( contentHandler );
    xmlReader.parse( is );
    
    return result[0];
  }
}
