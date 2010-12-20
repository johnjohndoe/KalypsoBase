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
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.io.sax.marshaller.MultiPointMarshaller;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Felipe Maximino
 */
public class MarshallGMMultiPointTest extends TestCase
{
  @Test
  public void testeMultiPoint( ) throws IOException, SAXException
  {
    final File temp = File.createTempFile( "multiPoint", "gml" );
    temp.deleteOnExit();

    final GM_MultiPoint multiPoint = createMultiPoint();

    final FileOutputStream os = new FileOutputStream( temp );
    final XMLReader reader = SaxParserTestUtils.createXMLReader( os );
    final MultiPointMarshaller marshaller = new MultiPointMarshaller( reader, multiPoint );
    SaxParserTestUtils.marshallDocument( reader, marshaller );
    os.close();

    final URL url = getClass().getResource( "resources/multiPointa.gml" );

    SaxParserTestUtils.assertContentEquals( temp, url );
  }

  private GM_MultiPoint createMultiPoint( )
  {
    final GM_Point[] multiPoint = new GM_Point[3];
    multiPoint[0] = GeometryFactory.createGM_Point( 0.0, 0.0, "EPSG:4267" );
    multiPoint[1] = GeometryFactory.createGM_Point( 0.0, 1.0, "EPSG:4267" );
    multiPoint[2] = GeometryFactory.createGM_Point( 1.0, 0.0, "EPSG:4267" );

    return GeometryFactory.createGM_MultiPoint( multiPoint, "EPSG:4267" );
  }
}
