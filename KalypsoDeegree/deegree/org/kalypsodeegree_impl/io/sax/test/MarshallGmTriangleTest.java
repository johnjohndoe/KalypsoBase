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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.io.sax.marshaller.TriangulatedSurfaceMarshaller;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Gernot Belger
 */
public class MarshallGmTriangleTest
{
  @Test
  public void writeOneTriangleTransform( ) throws IOException, SAXException, GM_Exception
  {
    final File tinFile = File.createTempFile( "tinTest", ".gml" );
    tinFile.deleteOnExit();

    OutputStream os = null;
    try
    {
      /* Output: to stream */
      os = new BufferedOutputStream( new FileOutputStream( tinFile ) );

      final XMLReader reader = SaxParserTestUtils.createXMLReader( os );

      // write one triangle
      final GM_Position pos1 = GeometryFactory.createGM_Position( 0.0, 0.0, 1.0 );
      final GM_Position pos2 = GeometryFactory.createGM_Position( 0.0, 1.0, 2.0 );
      final GM_Position pos3 = GeometryFactory.createGM_Position( 1.0, 0.0, 3.0 );
      final GM_Triangle triangle = GeometryFactory.createGM_Triangle( pos1, pos2, pos3, null );
      final GM_TriangulatedSurface surface = GeometryFactory.createGM_TriangulatedSurface( new GM_Triangle[] { triangle }, null );

      final TriangulatedSurfaceMarshaller marshaller = new TriangulatedSurfaceMarshaller( reader, surface );
      SaxParserTestUtils.marshallDocument( reader, marshaller );
      os.close();

      final URL url = getClass().getResource( "resources/triangulatedSurface_marshall.gml" );
      SaxParserTestUtils.assertContentEquals( url, tinFile );
    }
    finally
    {
      tinFile.delete();

      IOUtils.closeQuietly( os );
    }

  }

}
