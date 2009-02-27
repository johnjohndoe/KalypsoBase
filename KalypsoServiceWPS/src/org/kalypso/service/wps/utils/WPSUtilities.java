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
package org.kalypso.service.wps.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.kalypso.commons.net.ProxyUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreExtensions;

/**
 * This class contains functions for handling requests and responses via XML and other functions.
 * 
 * @author Holger Albert
 */
@SuppressWarnings("restriction")
public class WPSUtilities
{
  /**
   * AnyURI QName.
   */
  public static QName QNAME_ANY_URI = new QName( NS.XSD_SCHEMA, "anyURI" );

  /**
   * The constructor.
   */
  private WPSUtilities( )
  {
  }

  /**
   * This function is responsible for sending a request to a server.
   * 
   * @param body
   *            The XML string to be send.
   * @param url
   *            The address of the server.
   * @return The response as String.
   */
  public static String send( String xml, String url ) throws CoreException, HttpException, IOException
  {
    /* Send the request. */
    Debug.println( "Calling " + url + " ..." );

    /* Create the client. */
    HttpClient client = ProxyUtilities.getConfiguredHttpClient( 10000, new URL( url ), 0 );

    /* Build the method. */
    PostMethod post = new PostMethod( url );
    post.setRequestEntity( new StringRequestEntity( xml, "text/xml", null ) );

    /* Let the method handle the authentication, if any. */
    post.setDoAuthentication( true );

    /* Execute the method. */
    int status = client.executeMethod( post );

    /* Handle the response. */
    Debug.println( "Status code: " + String.valueOf( status ) );

    if( status != 200 )
      throw new CoreException( StatusUtilities.createErrorStatus( "Request failed! The server responded :" + String.valueOf( status ) ) );

    InputStream is = post.getResponseBodyAsStream();
    if( is == null )
      return null;

    return MarshallUtilities.fromInputStream( is );
  }

  /**
   * Returns the simulation with the given type id.
   * 
   * @param simulationType
   *            The simulations type id.
   * @return The simulation.
   */
  public static ISimulation getSimulation( String simulationType ) throws OWSException
  {
    ISimulation simulation = null;
    try
    {
      /* Get the simulation. */
      Debug.println( "Searching for simulation \"" + simulationType + "\" ..." );
      simulation = KalypsoSimulationCoreExtensions.createSimulation( simulationType );

      if( simulation == null )
      {
        Debug.println( "Unsupported simulation \"" + simulationType + "\"!" );
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, "The simulation \"" + simulationType + "\" is not available.", "" );
      }
    }
    catch( CoreException e )
    {
      Debug.println( "Unsupported simulation \"" + simulationType + "\"!" );
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" );
    }

    Debug.println( "Found simulation \"" + simulationType + "\"." );
    return simulation;
  }

  /**
   * This function returns all simulations.
   * 
   * @return All simulations.
   */
  public static List<ISimulation> getSimulations( ) throws OWSException
  {
    try
    {
      return KalypsoSimulationCoreExtensions.createSimulations();
    }
    catch( CoreException e )
    {
      Debug.println( "Error retrieving the simulations!" );
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" );
    }
  }

  /**
   * This function converts the URL that, the server has used and converts it to an URL, which the client can use.
   * 
   * @param serverUrl
   *            The URL, which the server has used internally to copy the results.<br>
   *            Example:<br>
   *            Server URL: file://var/lib/wwwrun/apache/informdss/htdocs/webdav/results<br>
   *            Replacement URL: http://informdss.bafg.de/webdav/results<br>
   *            ----------------------------------------------------------------------------------<br>
   *            serverUrl param : file://var/lib/wwwrun/apache/informdss/htdocs/webdav/results/xxx<br>
   *            ----------------------------------------------------------------------------------<br>
   *            Result: http://informdss.bafg.de/webdav/results/xxx
   */
  public static String convertInternalToClient( String serverUrl )
  {
    /* If no property for the replacement is set, use the server URL and provide it to the client. */
    String clientProperty = FrameworkProperties.getProperty( "org.kalypso.service.wps.client.replacement" );
    if( clientProperty == null )
      return serverUrl;

    String serverProperty = FrameworkProperties.getProperty( "org.kalypso.service.wps.results" );
    String clientUrl = serverUrl.replace( serverProperty, clientProperty );
    Debug.println( "Converting " + serverUrl + " to " + clientUrl + " ..." );

    return clientUrl;
  }

  /**
   * This function converts the URL, that the client has used to copy the input data and converts it to an URL, which
   * the server can use to retrieve them.
   * 
   * @param clientUrl
   *            The URL, which the client has used internally to copy the input data.<br>
   *            Example:<br>
   *            Client URL: webdav://informdss.bafg.de/webdav/input<br>
   *            Replacement URL: http://informdss.bafg.de/webdav/input<br>
   *            ----------------------------------------------------------------------------------<br>
   *            clientUrl param : webdav://informdss.bafg.de/webdav/input/xxx<br>
   *            ----------------------------------------------------------------------------------<br>
   *            Result: http://informdss.bafg.de/webdav/input/xxx
   * @param clientProperty
   *            The client URL.
   */
  public static String convertInternalToServer( String clientUrl, String clientProperty )
  {
    /* If no property for the replacement is set, use the client URL and provide it to the server. */
    String serverProperty = FrameworkProperties.getProperty( "org.kalypso.service.wps.server.replacement" );
    if( serverProperty == null )
      return clientUrl;

    String serverUrl = clientUrl.replace( clientProperty, serverProperty );
    Debug.println( "Converting " + clientUrl + " to " + serverUrl + " ..." );

    return serverUrl;
  }
}