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
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.MultiCurveContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 */
public class MultiCurveContentHandlerTest extends AssertGeometry
{
  private static final GM_Position POSITION1_1 = GeometryFactory.createGM_Position( 0.0, 0.1, 0.2 );

  private static final GM_Position POSITION2_1 = GeometryFactory.createGM_Position( 1.0, 1.1, 1.2 );

  private static final GM_Position POSITION3_1 = GeometryFactory.createGM_Position( 2.0, 2.1, 2.2 );

  private static final GM_Position POSITION4_1 = GeometryFactory.createGM_Position( 3.0, 3.1, 3.2 );

  private static final GM_Position POSITION1_2 = GeometryFactory.createGM_Position( 1.0, 0.1, 0.2 );

  private static final GM_Position POSITION2_2 = GeometryFactory.createGM_Position( 1.1, 1.2, 1.2 );

  private static final GM_Position POSITION3_2 = GeometryFactory.createGM_Position( 2.0, 2.1, 2.2 );

  private static final GM_Position POSITION4_2 = GeometryFactory.createGM_Position( 3.0, 3.1, 3.3 );

  /**
   * tests gml:MultiCurve with two members
   */
  @Test
  public void testMultiCurve( ) throws Exception
  {
    final GM_Curve curve1 = GeometryFactory.createGM_Curve( new GM_Position[] { POSITION1_1, POSITION2_1, POSITION3_1, POSITION4_1 }, "EPSG:31467" ); //$NON-NLS-1$
    final GM_Curve curve2 = GeometryFactory.createGM_Curve( new GM_Position[] { POSITION1_2, POSITION2_2, POSITION3_2, POSITION4_2 }, "EPSG:31467" ); //$NON-NLS-1$
    final GM_MultiCurve expectedCurves = GeometryFactory.createGM_MultiCurve( new GM_Curve[] { curve1, curve2 } );

    final GM_MultiCurve multiLineString = parseMultiLineString( "/etc/test/resources/multiCurve.gml" );
    assertMultiCurve( expectedCurves, multiLineString );
  }

  private GM_MultiCurve parseMultiLineString( final String source ) throws Exception
  {
    final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware( true );

    final InputSource is = new InputSource( getClass().getResourceAsStream( source ) );

    final SAXParser saxParser = saxFactory.newSAXParser();
    final XMLReader reader = saxParser.getXMLReader();

    final GM_MultiCurve[] result = new GM_MultiCurve[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof GM_MultiCurve );
        result[0] = (GM_MultiCurve) value;
      }
    };

    final MultiCurveContentHandler contentHandler = new MultiCurveContentHandler( reader, resultEater, null, null );
    reader.setContentHandler( contentHandler );
    reader.parse( is );

    return result[0];
  }
}
