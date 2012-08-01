/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.deegree;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.zml.obslink.ObjectFactory;
import org.kalypso.zml.obslink.TimeseriesLinkType;

/**
 * @author belger
 */
public class ObservationLinkHandlerTest extends TestCase
{
  public void testMarshal( ) throws JAXBException
  {
    final ObjectFactory factory = new ObjectFactory();
    final JAXBContext jc = JaxbUtilities.createQuiet( ObjectFactory.class );

    final Marshaller marshaller = JaxbUtilities.createMarshaller( jc );
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

    final TimeseriesLinkType link = factory.createTimeseriesLinkType();

    link.setHref( "path=blubb" );
    link.setType( "simple" );

    final JAXBElement<TimeseriesLinkType> element = factory.createTimeseriesLink( link );
    // FIXME: compare with some result
    marshaller.marshal( element, new NullOutputStream() );
  }
}