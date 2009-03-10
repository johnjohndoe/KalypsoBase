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
package org.kalypso.service.wps.client;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.Activator;
import org.kalypso.service.wps.client.exceptions.WPSException;
import org.kalypso.service.wps.server.operations.DescribeProcess;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;
import org.kalypso.service.wps.utils.simulation.WPSSimulationInfo;
import org.kalypso.service.wps.utils.simulation.WPSSimulationManager;
import org.kalypso.simulation.core.SimulationException;

/**
 * This class manages the connect between the client and the server.<br>
 * It polls regularly and checks the status of the calculation, that can be retrieved from it then.<br>
 * Furthermore it has the ability to be canceled.<br>
 * 
 * @author Holger Albert
 */
public class NonBlockingWPSRequest
{
  /**
   * The identifier of the service to be called.
   */
  private final String m_identifier;

  /**
   * The address of the service.
   */
  private final String m_serviceEndpoint;

  private DataInputsType m_dataInputs;

  private OutputDefinitionsType m_outputDefinitions;

  private String m_statusLocation;

  private ProcessDescriptionType m_processDescription;

  /**
   * The constructor.
   * 
   * @param identifier
   *          The identifier of the service to be called.
   * @param serviceEndpoint
   *          The address of the service.
   * @param timeout
   *          After this period of time, the job gives up, waiting for a result of the service.
   */
  public NonBlockingWPSRequest( final String identifier, final String serviceEndpoint )
  {
    /* Initializing of the given variables. */
    m_identifier = identifier;
    m_serviceEndpoint = serviceEndpoint;
  }

  /**
   * Initializes the WPS by getting the process description
   */
  public ProcessDescriptionType getProcessDescription( final IProgressMonitor monitor ) throws CoreException
  {
    if( m_processDescription == null )
    {
      initProcessDescription( monitor );
    }

    return m_processDescription;
  }

  /**
   * Use {@link #getProcessDescription(IProgressMonitor)} instead.
   */
  @Deprecated
  public ProcessDescriptionType getProcessDescription( ) throws CoreException
  {
    if( m_processDescription == null )
    {
      initProcessDescription( null );
    }

    return m_processDescription;
  }

  /**
   * This function initializes the request.<br>
   * TODO: let it throw an CoreException instead of returning the status; makes error handling much easier.
   * 
   * @param monitor
   *          A progress monitor.
   * @return A status, indicating the success of the function.
   */
  public IStatus init( final Map<String, Object> inputs, final List<String> outputs, final IProgressMonitor monitor )
  {
    try
    {
      /* Get the process description. */
      final ProcessDescriptionType processDescription = getProcessDescription( monitor );

      /* Get the input data. */
      m_dataInputs = createDataInputs( processDescription, inputs );

      /* Get the output data. */
      m_outputDefinitions = createOutputDefinitions( processDescription, outputs );

      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      Activator.getDefault().getLog().log( status );
      return status;
    }
  }

  private void initProcessDescription( IProgressMonitor monitor ) throws CoreException
  {
    Debug.println( "Initializing ..." );

    /* Monitor. */
    monitor = SubMonitor.convert( monitor, "Frage nach der Prozess-Beschreibung ...", 300 );
    Debug.println( "Asking for a process description ..." );

    try
    {
      // decide between local and remote invocation
      if( WPSRequest.SERVICE_LOCAL.equals( m_serviceEndpoint ) )
      {
        m_processDescription = DescribeProcess.buildProcessDescriptionType( m_identifier );
      }
      else
      {
        final List<ProcessDescriptionType> processDescriptionList = WPSUtilities.callDescribeProcess( m_serviceEndpoint, m_identifier );
        if( processDescriptionList.size() != 1 )
        {
          throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, "DescribeProcess returned more than one process description.", null ) );
        }

        /* Monitor. */
        monitor.worked( 300 );

        /* We will always take the first one. */
        m_processDescription = processDescriptionList.get( 0 );
      }
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final Exception e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * Starts the simulation.
   * 
   * @param monitor
   *          The progress monitor.
   */
  public IStatus run( IProgressMonitor monitor )
  {
    // TODO: clear old results

    /* Monitor. */
    monitor = SubMonitor.convert( monitor, "Berechnung vorbereiten ...", 200 );
    Debug.println( "Checking for service URL ..." );

    /* Check, if we have a service endpoint. */
    if( m_serviceEndpoint == null )
    {
      Debug.println( "No URL to the service is given." );
      return StatusUtilities.statusFromThrowable( new WPSException( "No URL to the the service is given." ) );
    }

    /* Send the request. */
    monitor.setTaskName( "Starte die Simulation ..." );
    Debug.println( "Start the simulation ..." );

    ExecuteResponseType executeResponse;
    final CodeType simulationIdentifier = OGCUtilities.buildCodeType( "", m_identifier );

    try
    {
      // decide between local and remote invocation
      if( WPSRequest.SERVICE_LOCAL.equals( m_serviceEndpoint ) )
      {
	    FileObject resultFile = null;
        try
        {
          /* Execute the simulation via a manager, so that more than one simulation can be run at the same time. */
          final Execute execute = OGCUtilities.buildExecute( simulationIdentifier, m_dataInputs, m_outputDefinitions, true, true );
          final WPSSimulationManager manager = WPSSimulationManager.getInstance();
          final WPSSimulationInfo info = manager.startSimulation( m_identifier, m_identifier, execute );

          /* Prepare the execute response. */
          final FileObject resultDir = manager.getResultDir( info.getId() );
		  resultFile = resultDir.resolveFile( "executeResponse.xml" );
          final String statusLocation = WPSUtilities.convertInternalToClient( resultFile.getURL().toExternalForm() );
          final StatusType status = OGCUtilities.buildStatusType( "Process accepted.", true );
          executeResponse = OGCUtilities.buildExecuteResponseType( simulationIdentifier, status, m_dataInputs, m_outputDefinitions, null, statusLocation, OGCUtilities.VERSION );
        }
        catch( final IOException e )
        {
          throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
        }
        catch( final SimulationException e )
        {
          throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
        }
        catch( final OWSException e )
        {
          throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
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
      }
      else
      {
        executeResponse = WPSUtilities.callExecute( m_serviceEndpoint, m_identifier, m_dataInputs, m_outputDefinitions );
      }
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }

    final StatusType status = executeResponse.getStatus();
    final ProcessFailedType processFailed = status.getProcessFailed();
    if( processFailed != null )
    {
      final String errorString = WPSUtilities.createErrorString( processFailed.getExceptionReport() );
      return StatusUtilities.createErrorStatus( errorString );
    }

    /* If the user aborted the job. */
    monitor.worked( 100 );
    if( monitor.isCanceled() )
    {
      return Status.CANCEL_STATUS;
    }

    /* Retrieve the path to the status file of the process. */
    m_statusLocation = executeResponse.getStatusLocation();

    /* Finish. */
    monitor.worked( 100 );

    return Status.OK_STATUS;
  }

  private DataInputsType createDataInputs( final ProcessDescriptionType description, final Map<String, Object> inputs ) throws WPSException
  {
    /* The storage for the input values. */
    final List<IOValueType> inputValues = new LinkedList<IOValueType>();

    /* Get the input list. */
    final DataInputs dataInputs = description.getDataInputs();
    final List<InputDescriptionType> inputDescriptions = dataInputs.getInput();

    /* Iterate over all inputs and build the data inputs for the execute request. */
    for( final InputDescriptionType inputDescription : inputDescriptions )
    {
      final CodeType identifier = inputDescription.getIdentifier();

      /* Check if the input is in our model data, too. */
      final String inputId = identifier.getValue();
      if( inputs.containsKey( inputId ) )
      {
        /* Input is here. */
        final Object inputValue = inputs.get( inputId );
        inputValues.add( createDataInput( inputDescription, inputValue ) );
        continue;
      }

      /* Check, if it is an optional one. */
      if( inputDescription.getMinimumOccurs().intValue() == 1 )
      {
        /* Ooops, it is a mandatory one, but it is missing in our model data. */
        throw new WPSException( "The data input " + inputId + " is mandatory. Check your input data." );
      }
    }

    return OGCUtilities.buildDataInputsType( inputValues );
  }

  private IOValueType createDataInput( final InputDescriptionType inputDescription, final Object inputValue )
  {
    final CodeType identifier = inputDescription.getIdentifier();
    final String inputId = identifier.getValue();

    /* Supported complex data type. */
    final SupportedComplexDataType complexData = inputDescription.getComplexData();
    if( complexData != null )
    {
      if( inputValue instanceof URI )
      {
        /* Build the complex value reference. */
        final URI uri = (URI) inputValue;
        final CodeType code = OGCUtilities.buildCodeType( null, inputId );
        final ComplexValueReference valueReference = OGCUtilities.buildComplexValueReference( uri.toASCIIString(), null, null, null );
        final IOValueType ioValue = OGCUtilities.buildIOValueType( code, inputDescription.getTitle(), inputDescription.getAbstract(), valueReference );

        /* Add the input. */
        return ioValue;
      }

      // REMARK: hack/convention: the input must now be the raw input for the anyType element
      final List<Object> value = new ArrayList<Object>( 1 );
      value.add( inputValue );

      /* Build the complex value. */
      final CodeType code = OGCUtilities.buildCodeType( null, inputId );
      final ComplexValueType valueType = OGCUtilities.buildComplexValueType( null, null, null, value );
      final IOValueType ioValue = OGCUtilities.buildIOValueType( code, inputDescription.getTitle(), inputDescription.getAbstract(), valueType );

      /* Add the input. */
      return ioValue;
    }

    /* Literal input type */
    final LiteralInputType literalInput = inputDescription.getLiteralData();
    if( literalInput != null )
    {
      /* Build the literal value type. */
      final CodeType code = OGCUtilities.buildCodeType( null, inputId );

      final String inputType = literalInput.getDataType().getValue();
      final String value = marshalLiteral( inputValue, inputType );

      final LiteralValueType literalValue = OGCUtilities.buildLiteralValueType( value, inputType, null );
      final IOValueType ioValue = OGCUtilities.buildIOValueType( code, inputDescription.getTitle(), inputDescription.getAbstract(), literalValue );

      /* Add the input. */
      return ioValue;
    }

    /* Supported CRSs type. */
    // TODO: support SupportedCRSsType
    throw new UnsupportedOperationException();
  }

  private String marshalLiteral( final Object literalValue, @SuppressWarnings("unused") final String inputType )
  {
    // REMARK: (crude) Hack: if it is already a String, we always return this value.
    if( literalValue instanceof String )
    {
      return (String) literalValue;
    }

    throw new UnsupportedOperationException();
  }

  /**
   * This function creates the data outputs the clients expects for the wps.
   * 
   * @param description
   *          The description of the process.
   * @param data
   *          The modeldata.
   * @return The output of the model spec in wps format.
   */
  private OutputDefinitionsType createOutputDefinitions( final ProcessDescriptionType description, final List<String> outputs )
  {
    /* The storage for the output values. */
    final List<OutputDefinitionType> outputValues = new LinkedList<OutputDefinitionType>();

    /* Get the output list. */
    final ProcessOutputs processOutputs = description.getProcessOutputs();
    final List<OutputDescriptionType> outputDescriptions = processOutputs.getOutput();

    /* Iterate over all outputs and build the data inputs for the execute request. */
    for( final OutputDescriptionType outputDescription : outputDescriptions )
    {
      final CodeType identifier = outputDescription.getIdentifier();

      /* Check if the output is in our model data, too. */
      if( !outputs.contains( identifier.getValue() ) )
      {
        /* Ooops, it is missing in our model data. */
        // throw new CoreException( StatusUtilities.createErrorStatus( "The data output " + identifier.getValue() + " is missing. Check your model data." ) );
        continue;
      }

      // TODO: maybe only ask for outputs that are in the list m_outputs?

      final CodeType code = OGCUtilities.buildCodeType( null, identifier.getValue() );
      final OutputDefinitionType outputDefinition = OGCUtilities.buildOutputDefinitionType( code, outputDescription.getTitle(), outputDescription.getAbstract(), null, null, null, null );

      /* Add the output. */
      outputValues.add( outputDefinition );
    }

    return OGCUtilities.buildOutputDefinitionsType( outputValues );
  }

  public String getStatusLocation( )
  {
    return m_statusLocation;
  }

}