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
package org.kalypso.ogc.core.service;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.operations.IOGCOperation;
import org.kalypso.ogc.core.utils.ExtensionUtilities;
import org.kalypso.ogc.core.utils.OWSUtilities;
import org.kalypso.ogc.core.utils.internal.OGCUtilities;
import org.kalypso.ogc.core.utils.internal.parameter.OGCParameter;

/**
 * This servlet handles the incoming requests and delegates it to the responsible service.
 * 
 * @author Toni DiNardo
 */
public class OGCServlet extends HttpServlet
{
  /**
   * The constructor.
   */
  public OGCServlet( )
  {
  }

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
  {
    doRequest( false, request, response );
  }

  /**
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
  {
    doRequest( true, request, response );
  }

  /**
   * This function handles the request.
   * 
   * @param post
   *          The type of the servlet request. True for a POST request. False for a GET request.
   * @param request
   *          The servlet request.
   * @param response
   *          The servlet response.
   */
  private void doRequest( final boolean post, final HttpServletRequest request, final HttpServletResponse response )
  {
    /* Create the OGC request and the OGC response. */
    final OGCRequest ogcRequest = new OGCRequest( post, request );
    final OGCResponse ogcResponse = new OGCResponse( response );

    try
    {
      /* Handle the request. */
      doRequest( ogcRequest, ogcResponse );
    }
    catch( final OWSException ex )
    {
      /* Send a error response to the client. */
      sendErrorResponse( ogcResponse, ex );
    }
  }

  /**
   * This function handles the request.
   * 
   * @param ogcRequest
   *          The OGC request.
   * @param ogcResponse
   *          The OGC response.
   */
  private void doRequest( final OGCRequest ogcRequest, final OGCResponse ogcResponse ) throws OWSException
  {
    try
    {
      /* Get the OGC parameter. */
      final OGCParameter parameter = OGCUtilities.getParameter( ogcRequest );

      final String parameterService = parameter.getService();
      if( parameterService == null || parameterService.length() == 0 )
        throw new OWSException( "The SERVICE parameter is mandantory.", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

      final String parameterRequest = parameter.getRequest();
      if( parameterRequest == null || parameterRequest.length() == 0 )
        throw new OWSException( "The REQUEST parameter is mandantory.", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

      /* On a GetCapablities request, this parameter will never be null. */
      final String parameterVersion = parameter.getVersion();
      if( parameterVersion == null || parameterVersion.length() == 0 )
        throw new OWSException( "The VERSION parameter is mandantory.", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

      /* Negotiate the version. */
      final String negotiatedVersion = OGCUtilities.negotiateVersion( parameter );

      /* Get the service. */
      final IOGCService service = ExtensionUtilities.getService( parameterService, negotiatedVersion );

      /* Get the operation. */
      final IOGCOperation operation = ExtensionUtilities.getOperation( parameterService, negotiatedVersion, parameterRequest );

      /* Execute the operation. */
      service.execute( ogcRequest, ogcResponse, operation );

      /* Dispose the service. */
      service.dispose();
    }
    catch( final OWSException ex )
    {
      throw ex;
    }
    catch( final Exception ex )
    {
      throw new OWSException( String.format( "Encountered an error while preparing the execution of the request. Cause: %s", ex.getMessage() ), ex, OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );
    }
  }

  /**
   * This function sends a error response to the client.
   * 
   * @param ogcResponse
   *          The OGC response.
   * @param owsException
   *          The OWS exception.
   */
  private void sendErrorResponse( final OGCResponse ogcResponse, final OWSException owsException )
  {
    /* The output stream writer. */
    OutputStreamWriter writer = null;

    try
    {
      /* Create the output stream writer. */
      writer = new OutputStreamWriter( ogcResponse.getOutputStream() );

      /* Send the response. */
      writer.write( owsException.toXML() );
    }
    catch( final IOException ex )
    {
      /* Ignore this exception. */
      ex.printStackTrace();
    }
    finally
    {
      /* Close the output stream writer. */
      IOUtils.closeQuietly( writer );
    }
  }
}