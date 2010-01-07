/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.io.sax.TriangulatedSurfaceContentHandler;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class TriangulatedSurfaceContentHandlerTest extends Assert
{
  private static final double DELTA = 0.00001;

  private static final GM_Position CHECK_POS_1 = GeometryFactory.createGM_Position( 3929114.75, 773783.6875, 363.08599853515625 );

  private static final GM_Position CHECK_POS_2 = GeometryFactory.createGM_Position( 3929026.5, 773786.0, 363.08599853515625 );

  private static final GM_Position CHECK_POS_3 = GeometryFactory.createGM_Position( 3929025.0, 773778.625, 362.7200012207031 );

  @Test
  public void loadSurface( ) throws IOException, ParserConfigurationException, SAXException
  {
    // load a surface from a file

    final URL tinLocation = getClass().getResource( "triangulatedSurface.xml.gz" );
    assertNotNull( tinLocation );

    final InputStream is = new GZIPInputStream( tinLocation.openStream() );
    final byte[] buf = IOUtils.toByteArray( is );
    is.close();

    final TimeLogger allLogger = new TimeLogger();

    for( int i = 0; i < 100; i++ )
    {
      final TimeLogger oneSurfaceLogger = new TimeLogger();

      final ByteArrayInputStream bais = new ByteArrayInputStream( buf );
      final GM_TriangulatedSurface surface = readTriangles( new InputSource( bais ) );
      final String msg = String.format( "Read %d triangles in ", surface.size() );

      assertSurface( surface );

      oneSurfaceLogger.takeInterimTime();
      oneSurfaceLogger.printCurrentTotal( msg );
    }

    allLogger.takeInterimTime();
    allLogger.printCurrentTotal( "All read in " );
  }

  private void assertSurface( final GM_TriangulatedSurface surface )
  {
    assertEquals( 244, surface.size() );

    final GM_Triangle triangle = surface.get( 0 );
    final String srs = triangle.getCoordinateSystem();

    assertEquals( "EPSG:31467", srs );

    final GM_Position[] exteriorRing = triangle.getExteriorRing();
    assertTrue( exteriorRing[0].getDistance( CHECK_POS_1 ) < DELTA );
    assertEquals( CHECK_POS_1.getZ(), exteriorRing[0].getZ(), DELTA );

    assertTrue( exteriorRing[1].getDistance( CHECK_POS_2 ) < DELTA );
    assertEquals( CHECK_POS_2.getZ(), exteriorRing[1].getZ(), DELTA );

    assertTrue( exteriorRing[2].getDistance( CHECK_POS_3 ) < DELTA );
    assertEquals( CHECK_POS_3.getZ(), exteriorRing[2].getZ(), DELTA );

    assertTrue( exteriorRing[3].getDistance( CHECK_POS_1 ) < DELTA );
    assertEquals( CHECK_POS_1.getZ(), exteriorRing[3].getZ(), DELTA );
  }

  private GM_TriangulatedSurface readTriangles( final InputSource is ) throws IOException, ParserConfigurationException, SAXException
  {
    final SAXParserFactory saxFac = SAXParserFactory.newInstance();
    saxFac.setNamespaceAware( true );

    final SAXParser saxParser = saxFac.newSAXParser();
    // make namespace-prefixes visible to content handler
    // used to allow necessary schemas from gml document
    final XMLReader xmlReader = saxParser.getXMLReader();
    xmlReader.setFeature( "http://xml.org/sax/features/namespace-prefixes", Boolean.TRUE ); //$NON-NLS-1$

    final GM_TriangulatedSurface[] result = new GM_TriangulatedSurface[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        assertTrue( value instanceof GM_TriangulatedSurface );
        result[0] = (GM_TriangulatedSurface) value;
      }
    };
    final TriangulatedSurfaceContentHandler contentHandler = new TriangulatedSurfaceContentHandler( resultEater, xmlReader );

    xmlReader.setContentHandler( contentHandler );
    xmlReader.parse( is );

    return result[0];
  }

}
