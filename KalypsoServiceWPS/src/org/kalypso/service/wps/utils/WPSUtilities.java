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
package org.kalypso.service.wps.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.ows.ExceptionType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.kalypso.commons.net.ProxyUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.utils.ogc.WPS040ObjectFactoryUtilities;
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
  public static final QName QNAME_ANY_URI = new QName( NS.XSD_SCHEMA, "anyURI" );

  /**
   * Service type identifier.
   */
  public static final String SERVICE = "WPS";

  public static enum WPS_VERSION
  {
    V100("1.0.0"),
    V040("0.4.0");

    private final String m_version;

    WPS_VERSION( final String version )
    {
      m_version = version;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString( )
    {
      return m_version;
    }

    public static WPS_VERSION getValue( final String value )
    {
      for( WPS_VERSION version : values() )
      {
        if( version.toString().equals( value ) )
          return version;
      }
      return null;
    }

  }

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
   *          The XML string to be send.
   * @param url
   *          The address of the server.
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
    // TODO: this is maybe a bit heavy, if the request is big (got an OutOfMemory once at marshalling the xml string)

    post.setRequestEntity( new StringRequestEntity( xml, "text/xml", null ) );

    /* Let the method handle the authentication, if any. */
    post.setDoAuthentication( true );

    /* Execute the method. */
    int status = client.executeMethod( post );

    /* Handle the response. */
    Debug.println( "Status code: " + String.valueOf( status ) );

    if( status != 200 )
    {
      // TODO: we should also add the body into a sub-status;
      // so we could show it to the user if he examines it more closely
      // String body = post.getResponseBodyAsString();
      String msg = String.format( "Request failed! Server response code %d.", status );
      throw new CoreException( StatusUtilities.createErrorStatus( msg ) );
    }

    InputStream is = post.getResponseBodyAsStream();
    if( is == null )
      return null;

    return MarshallUtilities.fromInputStream( is );
  }

  public static List<ProcessDescriptionType> callDescribeProcess( String serviceEndpoint, String... processIds ) throws CoreException
  {
    /* Build the describe process request. */
    List<CodeType> identifiers = new LinkedList<CodeType>();
    for( String processId : processIds )
    {
      identifiers.add( WPS040ObjectFactoryUtilities.buildCodeType( "", processId ) );
    }

    DescribeProcess describeProcess = WPS040ObjectFactoryUtilities.buildDescribeProcess( identifiers );

    /* Send the request. */
    Object describeProcessObject = null;
    try
    {
      String describeProcessResponse = WPSUtilities.send( MarshallUtilities.marshall( describeProcess, WPS_VERSION.V040 ), serviceEndpoint );

      /* Try to unmarshall. */
      describeProcessObject = MarshallUtilities.unmarshall( describeProcessResponse, WPS_VERSION.V040 );
    }
    catch( IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    catch( JAXBException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }

    // TODO: error handling: our WPS service throws other types of exceptions
    // (http://www.opengis.net/ows}ExceptionReport) than expected
    // here {http://www.opengeospatial.net/ows}ExceptionReport. We need to fix that... find out what is correct in
    // respect to WPS specification
    if( describeProcessObject instanceof ExceptionReport )
    {
      ExceptionReport report = (ExceptionReport) describeProcessObject;
      throw new CoreException( StatusUtilities.createErrorStatus( createErrorString( report ) ) );
    }

    /* Use the process description for building the DataInputs and the OutputDefinitions. */
    ProcessDescriptions processDescriptions = (ProcessDescriptions) describeProcessObject;

    /* The descriptions of all processes from the process descriptions response. */
    List<ProcessDescriptionType> processDescriptionList = processDescriptions.getProcessDescription();

    /* Check describe process. */
    if( processDescriptionList == null || processDescriptionList.size() == 0 )
      throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, "DescribeProcess returned no process description.", null ) );

    return processDescriptionList;
  }

  @SuppressWarnings("unchecked")
  public static ExecuteResponseType callExecute( String serviceEndpoint, String typeID, DataInputsType dataInputs, OutputDefinitionsType outputDefinitions ) throws CoreException
  {
    try
    {
      /* Build the execute request. */
      Execute execute = WPS040ObjectFactoryUtilities.buildExecute( WPS040ObjectFactoryUtilities.buildCodeType( "", typeID ), dataInputs, outputDefinitions, true, true );
      String executeRequestString = MarshallUtilities.marshall( execute, WPS_VERSION.V040 );
      String executeResponseString = WPSUtilities.send( executeRequestString, serviceEndpoint );

      /* Handle the execute response. */
      Debug.println( "Response:\n" + executeResponseString );

      if( executeResponseString == null || executeResponseString.length() == 0 )
        throw new CoreException( StatusUtilities.createErrorStatus( "Got an empty response ..." ) );

      Object response = MarshallUtilities.unmarshall( executeResponseString, WPS_VERSION.V040 );

      if( response instanceof ExceptionReport )
        throw new CoreException( StatusUtilities.createErrorStatus( WPSUtilities.createErrorString( (ExceptionReport) response ) ) );

      JAXBElement<ExecuteResponseType> elmt = (JAXBElement<ExecuteResponseType>) response;
      ExecuteResponseType executeResponse = elmt.getValue();
      return executeResponse;
    }
    catch( JAXBException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    catch( IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * This function will create an error string, containing all error messages.
   * 
   * @param exceptionReport
   *          The exception report.
   * @return The error messages as one string.
   */
  public static String createErrorString( ExceptionReport exceptionReport )
  {
    List<ExceptionType> exceptions = exceptionReport.getException();
    String messages = "";
    for( ExceptionType exception : exceptions )
      messages = messages + "Code: " + exception.getExceptionCode() + "\nMessage: " + exception.getExceptionText() + "\nLocator: " + exception.getLocator();

    return messages;
  }

  /**
   * Returns the simulation with the given type id.
   * 
   * @param simulationType
   *          The simulations type id.
   * @return The simulation.
   */
  public static ISimulation getSimulation( final String simulationType ) throws CoreException
  {
    /* Get the simulation. */
    Debug.println( "Searching for simulation \"" + simulationType + "\" ..." );
    final ISimulation simulation = KalypsoSimulationCoreExtensions.createSimulation( simulationType );
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
   *          The URL, which the server has used internally to copy the results.<br>
   *          Example:<br>
   *          Server URL: file://var/lib/wwwrun/apache/informdss/htdocs/webdav/results<br>
   *          Replacement URL: http://informdss.bafg.de/webdav/results<br>
   *          ----------------------------------------------------------------------------------<br>
   *          serverUrl param : file://var/lib/wwwrun/apache/informdss/htdocs/webdav/results/xxx<br>
   *          ----------------------------------------------------------------------------------<br>
   *          Result: http://informdss.bafg.de/webdav/results/xxx
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
   *          The URL, which the client has used internally to copy the input data.<br>
   *          Example:<br>
   *          Client URL: webdav://informdss.bafg.de/webdav/input<br>
   *          Replacement URL: http://informdss.bafg.de/webdav/input<br>
   *          ----------------------------------------------------------------------------------<br>
   *          clientUrl param : webdav://informdss.bafg.de/webdav/input/xxx<br>
   *          ----------------------------------------------------------------------------------<br>
   *          Result: http://informdss.bafg.de/webdav/input/xxx
   * @param clientProperty
   *          The client URL.
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