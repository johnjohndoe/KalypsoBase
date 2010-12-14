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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.marshaller.LineStringMarshaller;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 */
public class MarshallLineStringTest extends TestCase
{
  @Test
  public void testLineString( ) throws IOException, SAXException, GM_Exception
  {
    final File temp = File.createTempFile( "lineString", "gml" );
    temp.deleteOnExit();

    final GM_Curve lineString = createLineString();

    final FileOutputStream os = new FileOutputStream( temp );
    final XMLReader reader = SaxParserTestUtils.createXMLReader( os );
    final LineStringMarshaller marshaller = new LineStringMarshaller( reader, lineString );
    SaxParserTestUtils.marshallDocument( reader, marshaller );
    os.close();

    final URL url = getClass().getResource( "resources/lineString_marshall.gml" );

    SaxParserTestUtils.assertContentEquals( temp, url );
  }

  private GM_Curve createLineString( ) throws GM_Exception
  {
    final GM_Position[] positions = new GM_Position[5];
    positions[0] = GeometryFactory.createGM_Position( 0.0, 0.0, 0.0 );
    positions[1] = GeometryFactory.createGM_Position( 0.0, 1.0, 2.0 );
    positions[2] = GeometryFactory.createGM_Position( 1.0, 2.0, 2.0 );
    positions[3] = GeometryFactory.createGM_Position( 2.0, 2.0, 2.0 );
    positions[4] = GeometryFactory.createGM_Position( 2.5, 2.0, 1.0 );

    return GeometryFactory.createGM_Curve( positions, "EPSG:31467" );
  }
}
