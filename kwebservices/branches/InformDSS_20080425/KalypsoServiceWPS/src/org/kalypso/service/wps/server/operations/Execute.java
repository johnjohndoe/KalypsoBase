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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.ows.ExceptionType;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.StatusType;

import org.apache.commons.vfs.FileObject;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.VFSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;
import org.kalypso.service.wps.utils.simulation.WPSSimulationInfo;
import org.kalypso.service.wps.utils.simulation.WPSSimulationManager;

/**
 * This operation will execute a specific simulation.
 * 
 * @author Holger Albert
 */
public class Execute implements IOperation
{
  /**
   * The execute request. If this variable is set, the execute request came via xml. Otherwise it was only a get.
   */
  private net.opengeospatial.wps.Execute m_executeRequest = null;

  /**
   * The constructor.
   */
  public Execute( )
  {
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( RequestBean request ) throws OWSException
  {
    try
    {
      /* Start the operation. */
      Debug.println( "Operation \"Execute\" started." );

      /* Gets the identifier, but also unmarshalls the request, so it has to be done! */
      getIdentifier( request );

      /* Execute the simulation via a manager, so that more than one simulation can be run at the same time. */
      WPSSimulationManager manager = WPSSimulationManager.getInstance();
      WPSSimulationInfo info = manager.startSimulation( m_executeRequest.getIdentifier().getValue(), m_executeRequest.getIdentifier().getValue(), m_executeRequest );

      /* Prepare the execute response. */
      FileObject resultDir = manager.getResultDir( info.getId() );
      FileObject resultFile = resultDir.resolveFile( "executeResponse.xml" );
      String statusLocation = WPSUtilities.convertInternalToClient( resultFile.getURL().toExternalForm() );
      StatusType status = OGCUtilities.buildStatusType( "Process accepted.", true );
      ExecuteResponseType value = OGCUtilities.buildExecuteResponseType( m_executeRequest.getIdentifier(), status, m_executeRequest.getDataInputs(), m_executeRequest.getOutputDefinitions(), null, statusLocation, OGCUtilities.VERSION );
      JAXBElement<ExecuteResponseType> executeResponse = OGCUtilities.buildExecuteResponse( value );

      /* Marshall it into one XML string. */
      String xml = MarshallUtilities.marshall( executeResponse );

      /* Copy the execute response to this url. */
      VFSUtilities.copyStringToFileObject( xml, resultFile );

      /* Build the response. */
      StringBuffer response = new StringBuffer();
      response.append( xml );

      return response;
    }
    catch( Exception e )
    {
      if( m_executeRequest == null )
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage(), "" );

      List<String> list = new ArrayList<String>();
      list.add( e.getLocalizedMessage() );

      ExceptionType exception = OGCUtilities.buildExceptionType( list, "NO_APPLICABLE_CODE", "" );
      List<ExceptionType> exceptions = new ArrayList<ExceptionType>();
      exceptions.add( exception );

      ExceptionReport exceptionReport = OGCUtilities.buildExceptionReport( exceptions, OGCUtilities.VERSION, null );
      ProcessFailedType processFailed = OGCUtilities.buildProcessFailedType( exceptionReport );

      StatusType status = OGCUtilities.buildStatusType( processFailed, false );

      ExecuteResponseType value = OGCUtilities.buildExecuteResponseType( m_executeRequest.getIdentifier(), status, m_executeRequest.getDataInputs(), m_executeRequest.getOutputDefinitions(), null, null, OGCUtilities.VERSION );
      JAXBElement<ExecuteResponseType> executeResponse = OGCUtilities.buildExecuteResponse( value );

      String xml = "";
      try
      {
        /* Marshall it into one XML string. */
        xml = MarshallUtilities.marshall( executeResponse );
      }
      catch( Exception ex )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage() + " and " + ex.getLocalizedMessage(), "" );
      }

      /* Build the response. */
      StringBuffer response = new StringBuffer();
      response.append( xml );

      return response;
    }
  }

  /**
   * Checks for the parameter or attribute Identifier and returns it. Furthermore it sets the member m_xxxRequest.
   * 
   * @param request
   *          The request.
   * @return The Identifier parameter or null if not present.
   */
  private String getIdentifier( RequestBean request ) throws OWSException
  {
    String simulationType = null;
    if( request.isPost() )
    {
      try
      {
        /* POST with XML. */
        String xml = request.getBody();

        /* Check if ALL parameter are available. */
        m_executeRequest = (net.opengeospatial.wps.Execute) MarshallUtilities.unmarshall( xml );

        /* Need the XML attribute service. */
        CodeType identifier = m_executeRequest.getIdentifier();
        if( identifier != null )
          simulationType = identifier.getValue();
      }
      catch( JAXBException e )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getLocalizedMessage(), "" );
      }
    }
    else
    {
      /* GET or simple POST. */

      /* Search for the parameter Identifier. */
      simulationType = request.getParameterValue( "Identifier" );
    }

    if( simulationType == null || simulationType.length() == 0 )
    {
      Debug.println( "Missing parameter Identifier!" );
      throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'Identifier' is missing ...", "Identifier" );
    }

    return simulationType;
  }
}