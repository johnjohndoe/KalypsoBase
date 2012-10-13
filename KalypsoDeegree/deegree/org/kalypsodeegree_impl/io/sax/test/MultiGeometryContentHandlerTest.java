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
import org.kalypsodeegree.model.geometry.GM_Aggregate;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiGeometry;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.parser.MultiGeometryContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 */
public class MultiGeometryContentHandlerTest extends AssertGeometry
{
  final GM_Position m_position1 = GeometryFactory.createGM_Position( 0.0, 1.0, 0.0 );

  final GM_Position m_position2 = GeometryFactory.createGM_Position( 1.0, 1.0, 1.0 );

  final GM_Position m_position3 = GeometryFactory.createGM_Position( 2.0, 2.0, 1.0 );

  final GM_Position m_position4 = GeometryFactory.createGM_Position( 0.0, 1.0, 1.0 );


  /**
   * tests empty gml:MultiGeometry with two members
   */
  @Test
  public void testEmptyMultiGeometry( ) throws Exception
  {
    final GM_Aggregate expectedGeometry = GeometryFactory.createGM_MultiGeometry( "EPSG:31467" ); //$NON-NLS-1$

    final GM_MultiGeometry actualGeometry = parseMultiGeometry( "/etc/test/resources/multiGeometry_empty.gml" );
    assertMultiGeometry( expectedGeometry, actualGeometry );
  }

  /**
   * tests gml:MultiGeometry with mixed members
   */
  @Test
  public void testMultiGeometry( ) throws Exception
  {
    final GM_Point point1 = GeometryFactory.createGM_Point( 0.0, 1.0, 2.0, "EPSG:31468" ); //$NON-NLS-1$
    final GM_Curve curve1 = GeometryFactory.createGM_Curve( new GM_Position[] { m_position1, m_position2, m_position3, m_position4 }, "EPSG:31467" ); //$NON-NLS-1$

    final GM_MultiGeometry expected = GeometryFactory.createGM_MultiGeometry( new GM_Object[] { point1, curve1 }, "EPSG:31467" ); //$NON-NLS-1$

    final GM_MultiGeometry actual = parseMultiGeometry( "/etc/test/resources/multiGeometry.gml" );

    assertMultiGeometry( expected, actual );
  }

  private GM_MultiGeometry parseMultiGeometry( final String source ) throws Exception
  {
    final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware( true );

    final InputSource is = new InputSource( getClass().getResourceAsStream( source ) );

    final SAXParser saxParser = saxFactory.newSAXParser();
    final XMLReader reader = saxParser.getXMLReader();

    final GM_MultiGeometry[] result = new GM_MultiGeometry[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof GM_MultiGeometry );
        result[0] = (GM_MultiGeometry) value;
      }
    };

    final MultiGeometryContentHandler contentHandler = new MultiGeometryContentHandler( reader, resultEater, null, null );
    reader.setContentHandler( contentHandler );
    reader.parse( is );

    return result[0];
  }
}
