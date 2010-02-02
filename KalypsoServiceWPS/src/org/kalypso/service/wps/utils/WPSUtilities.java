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
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.kalypso.commons.io.VFSUtilities;
import org.kalypso.commons.net.ProxyUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.util.Arrays;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;
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
  public static final QName QNAME_ANY_URI = new QName( NS.XSD_SCHEMA, "anyURI" ); //$NON-NLS-1$

  /**
   * Service type identifier.
   */
  public static final String SERVICE = "WPS"; //$NON-NLS-1$

  public static enum WPS_VERSION
  {
    V100("1.0.0"), //$NON-NLS-1$
    V040("0.4.0"); //$NON-NLS-1$

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
      for( final WPS_VERSION version : values() )
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
  public static String send( final String xml, final String url ) throws CoreException, HttpException, IOException
  {
    /* Send the request. */
    KalypsoServiceWPSDebug.DEBUG.printf( "Calling " + url + " ...\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    /* Create the client. */
    final HttpClient client = ProxyUtilities.getConfiguredHttpClient( 25000, new URL( url ), 0 );

    /* Build the method. */
    final PostMethod post = new PostMethod( url );

    // TODO: this is maybe a bit heavy, if the request is big (got an OutOfMemory once at marshalling the xml string)
    // Instead we should provide an overloaded method that accepts the stringRequestEntity from outside (allowing for
    // stream or file-based entities)
    post.setRequestEntity( new StringRequestEntity( xml, "text/xml", null ) ); //$NON-NLS-1$

    /* Let the method handle the authentication, if any. */
    post.setDoAuthentication( true );

    /* Execute the method. */
    final int status = client.executeMethod( post );

    /* Handle the response. */
    KalypsoServiceWPSDebug.DEBUG.printf( "Status code: " + String.valueOf( status ) + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    if( status != 200 )
    {
      // TODO: we should also add the body into a sub-status;
      // so we could show it to the user if he examines it more closely
      // String body = post.getResponseBodyAsString();
      // TODO2: also dump post-xml and url!
      final String msg = Messages.getString( "org.kalypso.service.wps.utils.WPSUtilities.0", status ); //$NON-NLS-1$
      throw new CoreException( StatusUtilities.createErrorStatus( msg ) );
    }

    final InputStream is = post.getResponseBodyAsStream();
    if( is == null )
      return null;

    try
    {
      return IOUtils.toString( is );
    }
    finally
    {
      is.close();
    }
  }

  public static List<ProcessDescriptionType> callDescribeProcess( final String serviceEndpoint, final String... processIds ) throws CoreException
  {
    /* Build the describe process request. */
    final List<CodeType> identifiers = new LinkedList<CodeType>();
    for( final String processId : processIds )
    {
      identifiers.add( WPS040ObjectFactoryUtilities.buildCodeType( "", processId ) ); //$NON-NLS-1$
    }

    final DescribeProcess describeProcess = WPS040ObjectFactoryUtilities.buildDescribeProcess( identifiers );

    /* Send the request. */
    Object describeProcessObject = null;
    try
    {
      final String describeProcessMsg = MarshallUtilities.marshall( describeProcess, WPS_VERSION.V040 );
      final String describeProcessResponse = WPSUtilities.send( describeProcessMsg, serviceEndpoint );

      /* Try to unmarshall. */
      describeProcessObject = MarshallUtilities.unmarshall( describeProcessResponse, WPS_VERSION.V040 );
    }
    catch( final IOException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    catch( final JAXBException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }

    // TODO: error handling: our WPS service throws other types of exceptions
    // (http://www.opengis.net/ows}ExceptionReport) than expected
    // here {http://www.opengeospatial.net/ows}ExceptionReport. We need to fix that... find out what is correct in
    // respect to WPS specification
    if( describeProcessObject instanceof ExceptionReport )
    {
      final ExceptionReport report = (ExceptionReport) describeProcessObject;
      throw new CoreException( StatusUtilities.createErrorStatus( createErrorString( report ) ) );
    }

    /* Use the process description for building the DataInputs and the OutputDefinitions. */
    final ProcessDescriptions processDescriptions = (ProcessDescriptions) describeProcessObject;

    /* The descriptions of all processes from the process descriptions response. */
    final List<ProcessDescriptionType> processDescriptionList = processDescriptions.getProcessDescription();

    /* Check describe process. */
    if( processDescriptionList == null || processDescriptionList.size() == 0 )
      throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.service.wps.utils.WPSUtilities.1" ), null ) ); //$NON-NLS-1$

    return processDescriptionList;
  }

  @SuppressWarnings("unchecked")
  public static ExecuteResponseType callExecute( final String serviceEndpoint, final String typeID, final DataInputsType dataInputs, final OutputDefinitionsType outputDefinitions ) throws CoreException
  {
    try
    {
      /* Build the execute request. */
      final Execute execute = WPS040ObjectFactoryUtilities.buildExecute( WPS040ObjectFactoryUtilities.buildCodeType( "", typeID ), dataInputs, outputDefinitions, true, true ); //$NON-NLS-1$
      final String executeRequestString = MarshallUtilities.marshall( execute, WPS_VERSION.V040 );
      final String executeResponseString = WPSUtilities.send( executeRequestString, serviceEndpoint );

      /* Handle the execute response. */
      KalypsoServiceWPSDebug.DEBUG.printf( "Response:\n" + executeResponseString + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

      if( executeResponseString == null || executeResponseString.length() == 0 )
        throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.service.wps.utils.WPSUtilities.2" ) ) ); //$NON-NLS-1$

      final Object response = MarshallUtilities.unmarshall( executeResponseString, WPS_VERSION.V040 );

      if( response instanceof ExceptionReport )
        throw new CoreException( StatusUtilities.createErrorStatus( WPSUtilities.createErrorString( (ExceptionReport) response ) ) );

      final JAXBElement<ExecuteResponseType> elmt = (JAXBElement<ExecuteResponseType>) response;
      final ExecuteResponseType executeResponse = elmt.getValue();
      return executeResponse;
    }
    catch( final JAXBException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    catch( final IOException e )
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
  public static String createErrorString( final ExceptionReport exceptionReport )
  {
    final List<ExceptionType> exceptions = exceptionReport.getException();
    String messages = ""; //$NON-NLS-1$
    for( final ExceptionType exception : exceptions )
    {
      final List<String> exceptionList = exception.getExceptionText();
      final String exceptionText = Arrays.toString( exceptionList.toArray( new String[exceptionList.size()] ), "\n" ); //$NON-NLS-1$
      messages = messages + "Code: " + exception.getExceptionCode() + "\nMessage: " + exceptionText + "\nLocator: " + exception.getLocator(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

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
    KalypsoServiceWPSDebug.DEBUG.printf( "Searching for simulation \"" + simulationType + "\" ...\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    final ISimulation simulation = KalypsoSimulationCoreExtensions.createSimulation( simulationType );
    if( simulation == null )
      throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.service.wps.utils.WPSUtilities.3", simulationType ) ) ); //$NON-NLS-1$
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
    catch( final CoreException e )
    {
      KalypsoServiceWPSDebug.DEBUG.printf( "Error retrieving the simulations!\n" ); //$NON-NLS-1$
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" ); //$NON-NLS-1$
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
  public static String convertInternalToClient( final String serverUrl )
  {
    /* If no property for the replacement is set, use the server URL and provide it to the client. */
    final String clientProperty = FrameworkProperties.getProperty( "org.kalypso.service.wps.client.replacement" ); //$NON-NLS-1$
    if( clientProperty == null )
      return serverUrl;

    final String serverProperty = FrameworkProperties.getProperty( "org.kalypso.service.wps.results" ); //$NON-NLS-1$
    final String clientUrl = serverUrl.replace( serverProperty, clientProperty );
    KalypsoServiceWPSDebug.DEBUG.printf( "Converting " + serverUrl + " to " + clientUrl + " ...\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
  public static String convertInternalToServer( final String clientUrl, final String clientProperty )
  {
    /* If no property for the replacement is set, use the client URL and provide it to the server. */
    final String serverProperty = FrameworkProperties.getProperty( "org.kalypso.service.wps.server.replacement" ); //$NON-NLS-1$
    if( serverProperty == null )
      return clientUrl;

    final String serverUrl = clientUrl.replace( clientProperty, serverProperty );
    KalypsoServiceWPSDebug.DEBUG.printf( "Converting " + clientUrl + " to " + serverUrl + " ...\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    return serverUrl;
  }

  /**
   * This function creates the data outputs the clients expects for the WPS.<br>
   * Only the excepted outputs are created, other output definitions provided by this process are omitted (filtered
   * out).
   * 
   * @param processDescription
   *          The description of the process.
   * @param expectedOutputs
   *          expected outputs
   * @return The output of the model spec in wps format.
   */
  public static OutputDefinitionsType createOutputDefinitions( final ProcessDescriptionType processDescription, final List<String> expectedOutputs )
  {
    /* The storage for the output values. */
    final List<OutputDefinitionType> outputValues = new LinkedList<OutputDefinitionType>();

    /* Get the output list. */
    final ProcessOutputs processOutputs = processDescription.getProcessOutputs();
    final List<OutputDescriptionType> outputDescriptions = processOutputs.getOutput();

    /* Iterate over all outputs and build the data inputs for the execute request. */
    for( final OutputDescriptionType outputDescription : outputDescriptions )
    {
      final CodeType identifier = outputDescription.getIdentifier();

      /* Check if the output is in our model data, too. */
      if( !expectedOutputs.contains( identifier.getValue() ) )
      {
        /* Ooops, it is missing in our model data. */
        // throw new CoreException( StatusUtilities.createErrorStatus( "The data output " + identifier.getValue() +
        // " is missing. Check your model data." ) );
        continue;
      }

      // TODO: maybe only ask for outputs that are in the list m_outputs?

      final CodeType code = WPS040ObjectFactoryUtilities.buildCodeType( null, identifier.getValue() );
      final OutputDefinitionType outputDefinition = WPS040ObjectFactoryUtilities.buildOutputDefinitionType( code, outputDescription.getTitle(), outputDescription.getAbstract(), null, null, null, null );

      /* Add the output. */
      outputValues.add( outputDefinition );
    }

    return WPS040ObjectFactoryUtilities.buildOutputDefinitionsType( outputValues );
  }

  public static ExecuteResponseType readExecutionResponse( final FileSystemManager manager, final String statusLocation ) throws CoreException
  {
    try
    {
      final FileObject statusFile = VFSUtilities.checkProxyFor( statusLocation, manager );
      if( !statusFile.exists() )
        return null;

      /* Try to read the status at least 3 times, before exiting. */
      Exception lastError = new Exception();
      for( int i = 0; i < 3; i++ )
      {
        InputStream inputStream = null;
        try
        {
          final FileContent content = statusFile.getContent();
          inputStream = content.getInputStream();
          final String xml = MarshallUtilities.fromInputStream( inputStream );
          if( xml == null || "".equals( xml ) ) //$NON-NLS-1$
            throw new IOException( Messages.getString( "org.kalypso.service.wps.utils.WPSUtilities.4" ) + statusFile.toString() ); //$NON-NLS-1$

          final Object object = MarshallUtilities.unmarshall( xml );
          final JAXBElement< ? > executeState = (JAXBElement< ? >) object;
          return (ExecuteResponseType) executeState.getValue();
        }
        catch( final Exception e )
        {
          lastError = e;

          KalypsoServiceWPSDebug.DEBUG.printf( "An error has occured with the message: " + e.getLocalizedMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
          KalypsoServiceWPSDebug.DEBUG.printf( "Retry: " + String.valueOf( i ) + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

          Thread.sleep( 1000 );
        }
        finally
        {
          IOUtils.closeQuietly( inputStream );
          statusFile.close();
        }
      }

      KalypsoServiceWPSDebug.DEBUG.printf( "The second retry has failed, rethrowing the error ..." ); //$NON-NLS-1$ //$NON-NLS-2$
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.service.wps.utils.WPSUtilities.5" ) + lastError.getLocalizedMessage(), lastError ); //$NON-NLS-1$
      throw new CoreException( status );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.service.wps.utils.WPSUtilities.6" ) + e.getLocalizedMessage(), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }
}