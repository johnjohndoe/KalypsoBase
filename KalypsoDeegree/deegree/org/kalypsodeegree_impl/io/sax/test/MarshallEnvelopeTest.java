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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.io.sax.marshaller.EnvelopeMarshaller;
import org.kalypsodeegree_impl.model.geometry.GM_Envelope_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * @author Gernot Belger
 */
public class MarshallEnvelopeTest
{
  @Test
  public void writeEnvelope( ) throws IOException, SAXException
  {
    /* Output: to stream */
    final OutputStream os = new ByteArrayOutputStream();

    final XMLReader reader = SaxParserTestUtils.createXMLReader( os );

    /* Test data */
    final GM_Position lower = GeometryFactory.createGM_Position( 1.0, 2.0 );
    final GM_Position upper = GeometryFactory.createGM_Position( 3.0, 4.0 );
    final GM_Envelope expected = new GM_Envelope_Impl( lower, upper, "EPSG:31467" );

    final EnvelopeMarshaller marshaller = new EnvelopeMarshaller( reader );
    SaxParserTestUtils.marshallDocument( reader, marshaller, expected );
    os.close();

    final URL url = getClass().getResource( "resources/envelope_marshall.gml" );
    final String actualContent = os.toString();

    SaxParserTestUtils.assertContentEquals( url, actualContent );
  }
}