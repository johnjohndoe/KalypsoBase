/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.service.wps.unittests;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.JUnit4TestAdapter;
import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.DescribeProcess;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;

/**
 * This unittest should test a http connection, accepting the certificate.
 * 
 * @author Holger Albert
 */
public class HttpsAcceptCertificate
{
  /**
   * This function will only send a connection to a https server and tries to accept the given untrusted certificate.
   */
  @Test
  public void testHttpsAcceptCertificate( ) throws HttpException, CoreException, IOException, JAXBException
  {
    /* Build the describe process request. */
    List<CodeType> identifier = new LinkedList<CodeType>();
    identifier.add( OGCUtilities.buildCodeType( "", "InformDSSHydraulicV1.0" ) );
    DescribeProcess descripeProcess = OGCUtilities.buildDescribeProcess( identifier );

    /* Send the request. */
    Debug.println( "Asking for a process description ..." );
    String describeProcessResponse = WPSUtilities.send( MarshallUtilities.marshall( descripeProcess ), "https://informdss.bafg.de/bridge/ogc" );

    /* Handle the response. */
    Debug.println( "Response:\n" + describeProcessResponse );
  }

  /**
   * This function creates the JUnit4TestAdapter.
   * 
   * @return JUnit4TestAdapter
   */
  public static junit.framework.Test suite( )
  {
    return new JUnit4TestAdapter( HttpsAcceptCertificate.class );
  }
}
