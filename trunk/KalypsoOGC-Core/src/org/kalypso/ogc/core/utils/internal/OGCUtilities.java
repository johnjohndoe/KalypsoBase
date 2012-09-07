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
package org.kalypso.ogc.core.utils.internal;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.opengis.wps._1_0.DescribeProcess;
import net.opengis.wps._1_0.Execute;
import net.opengis.wps._1_0.GetCapabilities;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.service.IOGCService;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.utils.ExtensionUtilities;
import org.kalypso.ogc.core.utils.OWSUtilities;
import org.kalypso.ogc.core.utils.WPSUtilities;
import org.kalypso.ogc.core.utils.internal.comparator.OGCServiceComparator;
import org.kalypso.ogc.core.utils.internal.parameter.OGCParameter;

/**
 * This utilities class provides functions for OGC tasks.
 *
 * @author Toni DiNardo
 */
public class OGCUtilities
{
  /**
   * The constructor.
   */
  private OGCUtilities( )
  {
  }

  /**
   * This function returns the OGC parameter.
   *
   * @param ogcRequest
   *          The OGC request.
   * @return The OGC parameter.
   */
  public static OGCParameter getParameter( final OGCRequest ogcRequest ) throws OWSException, JAXBException
  {
    /* Check for GET. */
    final OGCParameter getParameter = getGetParameter( ogcRequest );
    if( getParameter != null )
      return getParameter;

    /* Check for POST. */
    final OGCParameter postParameter = getPostParameter( ogcRequest );
    if( postParameter != null )
      return postParameter;

    throw new OWSException( "No service found, which can handle the request...", OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );
  }

  /**
   * This function returns the OGC parameter, if the request is a GET request. It should work for all kind of services.
   *
   * @param ogcRequest
   *          The OGC request.
   * @return The OGC parameter or null.
   */
  private static OGCParameter getGetParameter( final OGCRequest ogcRequest )
  {
    if( ogcRequest.isPost() )
      return null;

    /* GET request. */
    final String service = ogcRequest.getParameterValue( IOGCService.PARAMETER_SERVICE );
    final String request = ogcRequest.getParameterValue( IOGCService.PARAMETER_REQUEST );

    String version = null;
    if( request != null && request.equals( IOGCService.OPERATION_GET_CAPABILITIES ) )
      version = WPSUtilities.WPS_VERSION;
    else
      version = ogcRequest.getParameterValue( IOGCService.PARAMETER_VERSION );

    String[] acceptVersions = null;
    final String acceptVersionsString = ogcRequest.getParameterValue( IOGCService.PARAMETER_ACCEPT_VERSIONS );
    if( acceptVersionsString != null && acceptVersionsString.length() > 0 )
      acceptVersions = StringUtils.split( acceptVersionsString, "," );

    final String language = ogcRequest.getParameterValue( IOGCService.PARAMETER_LANGUAGE );

    return new OGCParameter( service, request, version, acceptVersions, language );
  }

  /**
   * This function returns the OGC parameter, if the request is a POST request. It should work for all kind of services.
   *
   * @param ogcRequest
   *          The OGC request.
   * @return The OGC parameter or null.
   */
  private static OGCParameter getPostParameter( final OGCRequest ogcRequest ) throws OWSException, JAXBException
  {
    if( !ogcRequest.isPost() )
      return null;

    /* POST request. */
    final String body = ogcRequest.getBody();
    if( body == null || body.length() == 0 )
      throw new OWSException( "The POST request was sent without content.", OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );

    /* Is it a request for the version 1.0.0 of the WPS? */
    final OGCParameter wpsParameter = getWPSVersion100( body );
    if( wpsParameter != null )
      return wpsParameter;

    // TODO Here other services can be checked.

    return null;
  }

  /**
   * This function returns the OGC parameter, if the request is a POST request. It should work for the WPS service with
   * the version 1.0.0.
   *
   * @param ogcRequest
   *          The OGC request.
   * @return The OGC parameter or null.
   */
  private static OGCParameter getWPSVersion100( final String body ) throws JAXBException
  {
    final Object unmarshall = WPSUtilities.unmarshall( new StringReader( body ) );
    if( unmarshall instanceof GetCapabilities )
    {
      final GetCapabilities getCapabilities = (GetCapabilities) unmarshall;

      final String service = getCapabilities.getService();
      if( !service.equals( WPSUtilities.WPS_SERVICE ) )
        return null;

      final String request = IOGCService.OPERATION_GET_CAPABILITIES;
      final String version = WPSUtilities.WPS_VERSION;
      final List<String> acceptVersions = getCapabilities.getAcceptVersions().getVersion();
      final String language = getCapabilities.getLanguage();

      return new OGCParameter( service, request, version, acceptVersions.toArray( new String[] {} ), language );
    }

    if( unmarshall instanceof DescribeProcess )
    {
      final DescribeProcess describeProcess = (DescribeProcess) unmarshall;

      final String service = describeProcess.getService();
      if( !service.equals( WPSUtilities.WPS_SERVICE ) )
        return null;

      final String request = IOGCService.OPERATION_DESCRIBE_PROCESS;
      final String version = describeProcess.getVersion();
      final String[] acceptVersions = null;
      final String language = describeProcess.getLanguage();

      return new OGCParameter( service, request, version, acceptVersions, language );
    }

    if( unmarshall instanceof Execute )
    {
      final Execute execute = (Execute) unmarshall;

      final String service = execute.getService();
      if( !service.equals( WPSUtilities.WPS_SERVICE ) )
        return null;

      final String request = IOGCService.OPERATION_EXECUTE;
      final String version = execute.getVersion();
      final String[] acceptVersions = null;
      final String language = execute.getLanguage();

      return new OGCParameter( service, request, version, acceptVersions, language );
    }

    return null;
  }

  /**
   * This function negotiates the version. On a request other than GetCapabilities, the client should have negotiated a
   * version already. In this case the function will return this one. If the version is not supported (which may happen,
   * if a client assumes a version) an exception will be thrown later.
   *
   * @param ogcParameter
   *          The OGC parameter.
   * @return The negotiated version.
   */
  public static String negotiateVersion( final OGCParameter ogcParameter ) throws OWSException
  {
    /* On a request other than GetCapabilities, the client should have send a version parameter he wants to use. */
    final String request = ogcParameter.getRequest();
    if( request != null && !request.equals( IOGCService.OPERATION_GET_CAPABILITIES ) )
      return ogcParameter.getVersion();

    /* Get the all services, matching the service parameter. */
    final IOGCService[] ogcServices = getOGCServices( ogcParameter.getService() );

    /* Sort the services. */
    /* The one with the highest version number should be first. */
    Arrays.sort( ogcServices, new OGCServiceComparator() );

    /* Version negotiation is performed using the optional AcceptVersions parameter */
    /* in the GetCapabilities operation request. */
    /* Although optional, client software should always include this parameter, to simplify version negotiation. */
    /* The value of this parameter is a sequence of protocol version numbers that the client supports, */
    /* in order of client preference. */
    final String[] acceptVersions = ogcParameter.getAcceptVersions();

    /* If a server receives a GetCapabilities request without the AcceptVersions parameter, */
    /* it shall return a service metadata document that is compliant to the highest protocol version */
    /* that the server supports. This makes it convenient for humans to make requests manually, and allows */
    /* for forward compatibility with possible future incarnations of version negotiation. */
    if( acceptVersions == null || acceptVersions.length == 0 )
      return ogcServices[0].getVersion();

    /* The server, upon receiving a GetCapabilities request, shall scan through this list */
    /* and find the first version number that it supports. */
    /* It shall then return a service metadata document conforming to that version of the specification, */
    /* and containing that value of the “version” parameter. */
    /* If the list does not contain any version numbers that the server supports, */
    /* the server shall return an Exception with exceptionCode="VersionNegotiationFailed". */
    for( final String acceptVersion : acceptVersions )
    {
      for( final IOGCService ogcService : ogcServices )
      {
        if( acceptVersion.equals( ogcService.getVersion() ) )
          return acceptVersion;
      }
    }

    throw new OWSException( "The version negotiation has failed...", OWSUtilities.OWS_VERSION, "en", ExceptionCode.VERSION_NEGOTIATON_FAILED, null );
  }

  private static IOGCService[] getOGCServices( final String parameterService ) throws OWSException
  {
    /* Memory for the OGC services. */
    final List<IOGCService> results = new ArrayList<>();

    /* Get all services. */
    final IOGCService[] services = ExtensionUtilities.getServices();
    for( final IOGCService service : services )
    {
      if( parameterService.equals( service.getName() ) )
        results.add( service );
    }

    return results.toArray( new IOGCService[] {} );
  }
}