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

import java.net.URL;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.marshaller.MultiCurveMarshaller;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 */
public class MarshallMultiCurveTest
{
  final GM_Position m_position1 = GeometryFactory.createGM_Position( 0.0, 1.0, 0.0 );

  final GM_Position m_position2 = GeometryFactory.createGM_Position( 1.0, 1.0, 1.0 );

  final GM_Position m_position3 = GeometryFactory.createGM_Position( 2.0, 2.0, 1.0 );

  final GM_Position m_position4 = GeometryFactory.createGM_Position( 0.0, 1.0, 1.0 );

  final GM_Position[] m_positions1 = new GM_Position[] { m_position1, m_position2, m_position3 };

  final GM_Position[] m_positions2 = new GM_Position[] { m_position4, m_position2, m_position3 };

  private final GM_Curve LINESTRING_1 = GeometryFactory.createGM_Curve( m_positions1, "EPSG:31467" );

  private final GM_Curve LINESTRING_2 = GeometryFactory.createGM_Curve( m_positions2, "EPSG:31467" );

  public MarshallMultiCurveTest( ) throws GM_Exception
  {
  }

  @Test
  public void testMultiCurve1( ) throws Exception
  {
    final GM_MultiCurve multiCurve = GeometryFactory.createGM_MultiCurve( new GM_Curve[] { LINESTRING_1 }, "EPSG:31467" );

    final String content = marshallMultiCurve( multiCurve );

    final URL url = getClass().getResource( "resources/multiCurve_marshall1.gml" );

    SaxParserTestUtils.assertContentEquals( url, content );
  }

  @Test
  public void testMultiCurve2( ) throws Exception
  {
    final GM_MultiCurve multiLineString = GeometryFactory.createGM_MultiCurve( new GM_Curve[] { LINESTRING_1, LINESTRING_2 }, "EPSG:31467" );

    final String content = marshallMultiCurve( multiLineString );

    final URL url = getClass().getResource( "resources/multiCurve_marshall2.gml" );

    SaxParserTestUtils.assertContentEquals( url, content );
  }

  private String marshallMultiCurve( final GM_MultiCurve multiCurve ) throws Exception
  {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    final XMLReader reader = SaxParserTestUtils.createXMLReader( os );
    final MultiCurveMarshaller marshaller = new MultiCurveMarshaller( reader );
    SaxParserTestUtils.marshallDocument( reader, marshaller, multiCurve );
    os.close();

    return os.toString();
  }
}
