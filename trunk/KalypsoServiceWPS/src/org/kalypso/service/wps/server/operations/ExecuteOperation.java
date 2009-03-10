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
package org.kalypso.service.wps.server.operations;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.ogc.ExecuteMediator;
import org.kalypso.service.wps.utils.simulation.WPSSimulationInfo;
import org.kalypso.service.wps.utils.simulation.WPSSimulationManager;

/**
 * This operation will execute a specific simulation.
 * 
 * @author Holger Albert
 */
public class ExecuteOperation implements IOperation
{
  /**
   * The constructor.
   */
  public ExecuteOperation( )
  {
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( final RequestBean request ) throws OWSException
  {
    final StringBuffer response = new StringBuffer();
// try
// {
    /* Start the operation. */
    Debug.println( "Operation \"Execute\" started." );

    /* Gets the identifier, but also unmarshalls the request, so it has to be done! */
    final String requestXml = request.getBody();
    Object executeRequest = null;
    try
    {
      executeRequest = MarshallUtilities.unmarshall( requestXml );
    }
    catch( final JAXBException e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "Could not parse Execute request." );
    }

    /* Execute the simulation via a manager, so that more than one simulation can be run at the same time. */
    final WPSSimulationManager manager = WPSSimulationManager.getInstance();

    // TODO version 1.0
    final WPSSimulationInfo info = manager.startSimulation( new ExecuteMediator( executeRequest ) );

    /* Prepare the execute response. */
    FileObject resultFile = null;
    try
    {
      final FileObject resultDir = manager.getResultDir( info.getId() );
      resultFile = resultDir.resolveFile( "executeResponse.xml" );
      int time = 0;
      int timeout = 10000;
      final int delay = 500;
      while( !resultFile.exists() && time < timeout )
      {
        Thread.sleep( delay );
        time += delay;
      }
      final FileContent content = resultFile.getContent();
      final InputStream inputStream = content.getInputStream();
      final String responseXml = MarshallUtilities.fromInputStream( inputStream );
      response.append( responseXml );
    }
    catch( final Exception e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" );
    }
    finally
    {
      if( resultFile != null )
        try
        {
          resultFile.close();
        }
        catch( final FileSystemException e )
        {
          // gobble
        }
    }

    return response;
// }
// catch( final Exception e )
// {
// if( m_executeRequest == null )
// throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "");
//
// final List<String> list = new ArrayList<String>();
// list.add( e.getLocalizedMessage() );
//
// try
// {
// final Object executeResponse = buildExceptionResponse( list );
// /* Marshall it into one XML string. */
// final String xml = MarshallUtilities.marshall040( executeResponse );
// response.append( xml );
// }
// catch( final Exception ex )
// {
// throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage() + " and " +
    // ex.getLocalizedMessage(), "" );
// }
// }
    // return response;
  }

// private Object buildExceptionResponse( final List<String> list )
// {
// final Object executeResponse;
// if( m_executeRequest instanceof Execute )
// {
// final Execute executeRequest = (Execute) m_executeRequest;
// final ExceptionType exception = WPS040ObjectFactoryUtilities.buildExceptionType( list, "NO_APPLICABLE_CODE", "" );
// final List<ExceptionType> exceptions = new ArrayList<ExceptionType>();
// exceptions.add( exception );
//
// final ExceptionReport exceptionReport = WPS040ObjectFactoryUtilities.buildExceptionReport( exceptions,
  // WPSUtilities.WPS_VERSION_0_4_0, null );
// final ProcessFailedType processFailed = WPS040ObjectFactoryUtilities.buildProcessFailedType( exceptionReport );
//
// final StatusType status = WPS040ObjectFactoryUtilities.buildStatusType( processFailed, false );
//
// final ExecuteResponseType value = WPS040ObjectFactoryUtilities.buildExecuteResponseType(
  // executeRequest.getIdentifier(), status, executeRequest.getDataInputs(), executeRequest.getOutputDefinitions(),
  // null, null, WPSUtilities.WPS_VERSION_0_4_0 );
// final JAXBElement<ExecuteResponseType> executeResponse040 = WPS040ObjectFactoryUtilities.buildExecuteResponse( value
  // );
// executeResponse = executeResponse040;
// }
// else
// {
// executeResponse = null;
// }
// return executeResponse;
// }

// private Object buildExecuteResponse( final String statusLocation )
// {
// final Object executeResponse;
// if( m_executeRequest instanceof Execute )
// {
// final Execute executeRequest = (Execute) m_executeRequest;
// final CodeType identifier = executeRequest.getIdentifier();
// final StatusType status = WPS040ObjectFactoryUtilities.buildStatusType( "Process accepted.", true );
// final ExecuteResponseType value = WPS040ObjectFactoryUtilities.buildExecuteResponseType( identifier, status,
  // executeRequest.getDataInputs(), executeRequest.getOutputDefinitions(), null, statusLocation,
  // WPSUtilities.WPS_VERSION_0_4_0 );
// final JAXBElement<ExecuteResponseType> executeResponse040 = WPS040ObjectFactoryUtilities.buildExecuteResponse( value
  // );
//
// executeResponse = executeResponse040;
// }
// else
// {
// executeResponse = null;
// }
// return executeResponse;
// }

  /**
   * Checks for the parameter or attribute Identifier and returns it. Furthermore it sets the member m_xxxRequest.
   * 
   * @param request
   *          The request.
   * @return The Identifier parameter or null if not present.
   */
// private String getIdentifier( final RequestBean request ) throws OWSException
// {
// String simulationType = null;
// if( request.isPost() )
// {
// try
// {
// /* Need the XML attribute service. */
// final CodeType identifier = m_executeRequest.getIdentifier();
// if( identifier != null )
// {
// simulationType = identifier.getValue();
// }
// }
// catch( final JAXBException e )
// {
// throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage(), "" );
// }
// }
// else
// {
// /* GET or simple POST. */
//
// /* Search for the parameter Identifier. */
// simulationType = request.getParameterValue( "Identifier" );
// }
//
// if( simulationType == null || simulationType.length() == 0 )
// {
// Debug.println( "Missing parameter Identifier!" );
// throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'Identifier' is missing ...",
  // "Identifier" );
// }
//
// return simulationType;
// }
}