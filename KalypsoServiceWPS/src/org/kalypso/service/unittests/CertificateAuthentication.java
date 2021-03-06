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
package org.kalypso.service.unittests;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.kalypso.commons.net.ProxyUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;

/**
 * This unit test should help verifying, if the client can send a certificate to a server, which requests client-side
 * certification.
 * 
 * @author Holger Albert
 */
public class CertificateAuthentication
{
// /**
// * To get the process description for the send mail simulation.
// */
// private static final String IDENTIFIER = "SendMailV1.0";

  /**
   * The service URL.
   */
  private static final String SERVICE_URL = "https://informdss.bafg.de/bridge/ogc?";

  /**
   * This function sends a request to a server and authenticates itself with a client-certificate.
   */
  @Test
  public void testCertificateAuthentication( ) throws Exception
  {
    /* Configure proxy for testing. */
    System.setProperty( "http.proxySet", "true" );
    System.setProperty( "http.proxyHost", "proxy.bce01.de" );
    System.setProperty( "http.proxyPort", "8080" );

    /* Create the client. */
    final HttpClient client = ProxyUtilities.getConfiguredHttpClient( 10000, new URL( SERVICE_URL ), 0 );

    /* Either like this ... */
    // File clientCert = new File( "C:/Albert/Temp/Projekte/InformDSS/Zertifikate/Client/keystore.jks" );
    // File serverCert = new File( "C:/Albert/Temp/Projekte/InformDSS/Zertifikate/Server/truststore.jks" );
    // SSLUtilities.configureWhole( clientCert.toURL(), "key4ssl", serverCert.toURL(), "key4ssl" );

    /* ... or like this. */
    System.setProperty( "javax.net.ssl.keyStoreType", "JKS" );
    System.setProperty( "javax.net.ssl.keyStore", "C:/Albert/Temp/Projekte/InformDSS/Zertifikate/Client/keystore.jks" );
    System.setProperty( "javax.net.ssl.keyStorePassword", "key4ssl" );
    System.setProperty( "javax.net.ssl.trustStoreType", "JKS" );
    System.setProperty( "javax.net.ssl.trustStore", "C:/Albert/Temp/Projekte/InformDSS/Zertifikate/Server/truststore.jks" );
    System.setProperty( "javax.net.ssl.trustStorePassword", "key4ssl" );

    /* Build the method. */
    final GetMethod get = new GetMethod( "https://informdss.bafg.de/svn/repos/projects/Tutorial/.settings/org.eclipse.core.resources.prefs" );

    /* Let the method handle the authentication, if any. */
    get.setDoAuthentication( true );

    /* Execute the method. */
    KalypsoServiceWPSDebug.DEBUG.printf( "Asking for a directory listing ...\n" );

    final int status = client.executeMethod( get );

    /* Handle the response. */
    KalypsoServiceWPSDebug.DEBUG.printf( "Status code: " + String.valueOf( status ) + "\n" );

    if( status != 200 )
      throw new CoreException( StatusUtilities.createErrorStatus( "Request failed! The server responded :" + String.valueOf( status ) ) );

    final InputStream is = get.getResponseBodyAsStream();
    if( is != null )
    {
      /* Print the response. */
      KalypsoServiceWPSDebug.DEBUG.printf( IOUtils.toString( is ) + "\n" );
    }

// if( true )
// return;
//
// /* Build the describe process request. */
// List<CodeType> identifier = new LinkedList<CodeType>();
// identifier.add( WPS040ObjectFactoryUtilities.buildCodeType( "", IDENTIFIER ) );
// DescribeProcess describeProcess = WPS040ObjectFactoryUtilities.buildDescribeProcess( identifier );
//
// /* Send the request. */
// KalypsoServiceWPSDebug.DEBUG.printf( "Asking for a process describtion ...\n" );
// String describeProcessResponse = WPSUtilities.send( MarshallUtilities.marshall( describeProcess, WPS_VERSION.V040 ),
// SERVICE_URL );
//
// /* Handle the response. */
// KalypsoServiceWPSDebug.DEBUG.printf( "Response:\n" + describeProcessResponse + "\n" );
  }
}