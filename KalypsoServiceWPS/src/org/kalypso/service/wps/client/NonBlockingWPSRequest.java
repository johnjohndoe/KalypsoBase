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
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import net.opengeospatial.ows.CodeType;
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
import net.opengeospatial.wps.SupportedCRSsType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.commons.io.VFSUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.client.exceptions.WPSException;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities.WPS_VERSION;
import org.kalypso.service.wps.utils.ogc.ExecuteMediator;
import org.kalypso.service.wps.utils.ogc.ProcessDescriptionMediator;
import org.kalypso.service.wps.utils.ogc.WPS040ObjectFactoryUtilities;
import org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider;
import org.kalypso.service.wps.utils.simulation.WPSSimulationInfo;
import org.kalypso.service.wps.utils.simulation.WPSSimulationManager;
import org.kalypso.simulation.core.SimulationException;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * This class manages the connect between the client and the server.<br>
 * It polls regularly and checks the status of the calculation, that can be retrieved from it then.<br>
 * Furthermore it has the ability to be canceled.<br>
 * 
 * @author Holger Albert
 * @deprecated currently working on a refactoring of the wps service see
 *             {@link org.kalypso.service.wps.refactoring.IWPSProcess}
 */
@Deprecated
public class NonBlockingWPSRequest
{
  /**
   * The identifier of the service to be called.
   */
  private final String m_identifier;

  private String m_jobId = ""; //$NON-NLS-1$

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
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }
    return Status.OK_STATUS;
  }

  private void initProcessDescription( IProgressMonitor monitor ) throws CoreException
  {
    Debug.println( "Initializing ..." ); //$NON-NLS-1$

    /* Monitor. */
    monitor = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.service.wps.client.NonBlockingWPSRequest.0" ), 300 ); //$NON-NLS-1$
    Debug.println( "Asking for a process description ..." ); //$NON-NLS-1$

    // decide between local and remote invocation
    if( WPSRequest.SERVICE_LOCAL.equals( m_serviceEndpoint ) )
    {
      final ProcessDescriptionMediator processDescriptionMediator = new ProcessDescriptionMediator( WPS_VERSION.V040 );
      m_processDescription = (ProcessDescriptionType) processDescriptionMediator.getProcessDescription( m_identifier );
    }
    else
    {
      final List<ProcessDescriptionType> processDescriptionList = WPSUtilities.callDescribeProcess( m_serviceEndpoint, m_identifier );
      if( processDescriptionList.size() != 1 )
      {
        throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, Messages.getString( "org.kalypso.service.wps.client.NonBlockingWPSRequest.1" ), null ) ); //$NON-NLS-1$
      }

      /* Monitor. */
      monitor.worked( 300 );

      /* We will always take the first one. */
      m_processDescription = processDescriptionList.get( 0 );
    }
  }

  /**
   * this function forwards the functionality of cancel of active job from the member wpsRequest fixes the bug #242, in
   * actual situation works only with local jobs and was tested only on windows machine. this class is already signed as
   * deprecated, so complete functionality test will not be done
   */
  public IStatus cancelJob( )
  {
    if( WPSRequest.SERVICE_LOCAL.equals( m_serviceEndpoint ) )
    {
      final WPSSimulationManager instance = WPSSimulationManager.getInstance();
      try
      {
        final WPSSimulationInfo job = instance.getJob( m_jobId );
        job.cancel();
        return Status.CANCEL_STATUS;
      }
      catch( final SimulationException e )
      {
        return StatusUtilities.statusFromThrowable( e, "Simulation could not be cancelled." );
      }
    }
    else
    {
      return StatusUtilities.createErrorStatus( "Canceling only possible for local simulations." );
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
    monitor = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.service.wps.client.NonBlockingWPSRequest.2" ), 200 ); //$NON-NLS-1$
    Debug.println( "Checking for service URL ..." ); //$NON-NLS-1$

    /* Check, if we have a service endpoint. */
    if( m_serviceEndpoint == null )
    {
      Debug.println( "No URL to the service is given." ); //$NON-NLS-1$
      return StatusUtilities.statusFromThrowable( new WPSException( Messages.getString( "org.kalypso.service.wps.client.NonBlockingWPSRequest.3" ) ) ); //$NON-NLS-1$
    }

    /* Send the request. */
    monitor.setTaskName( Messages.getString( "org.kalypso.service.wps.client.NonBlockingWPSRequest.4" ) ); //$NON-NLS-1$
    Debug.println( "Start the simulation ..." ); //$NON-NLS-1$

    ExecuteResponseType executeResponse;
    final CodeType simulationIdentifier = WPS040ObjectFactoryUtilities.buildCodeType( "", m_identifier ); //$NON-NLS-1$

    try
    {
      // decide between local and remote invocation
      if( WPSRequest.SERVICE_LOCAL.equals( m_serviceEndpoint ) )
      {
        FileObject resultFile = null;
        try
        {
          /* Execute the simulation via a manager, so that more than one simulation can be run at the same time. */
          final Execute execute = WPS040ObjectFactoryUtilities.buildExecute( simulationIdentifier, m_dataInputs, m_outputDefinitions, true, true );
          final WPSSimulationManager manager = WPSSimulationManager.getInstance();

          final ExecuteMediator executeMediator = new ExecuteMediator( execute );
          final WPSSimulationInfo info = manager.startSimulation( executeMediator );

          m_jobId = info.getId();
          /* Prepare the execute response. */
          final FileObject resultDir = manager.getResultDir( info.getId() );
          resultFile = resultDir.resolveFile( "executeResponse.xml" ); //$NON-NLS-1$
          final String statusLocation = WPSUtilities.convertInternalToClient( resultFile.getURL().toExternalForm() );
          final StatusType status = WPS040ObjectFactoryUtilities.buildStatusType( "Process accepted.", true ); //$NON-NLS-1$
          executeResponse = WPS040ObjectFactoryUtilities.buildExecuteResponseType( simulationIdentifier, status, m_dataInputs, m_outputDefinitions, null, statusLocation, WPSUtilities.WPS_VERSION.V040.toString() );
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

  private DataInputsType createDataInputs( final ProcessDescriptionType description, final Map<String, Object> inputs ) throws CoreException
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
        throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.service.wps.client.NonBlockingWPSRequest.5", inputId ) ) ); //$NON-NLS-1$
      }
    }

    return WPS040ObjectFactoryUtilities.buildDataInputsType( inputValues );
  }

  private IOValueType createDataInput( final InputDescriptionType inputDescription, final Object inputValue ) throws CoreException
  {
    final CodeType identifier = inputDescription.getIdentifier();
    final String inputId = identifier.getValue();
    final String title = inputDescription.getTitle();
    final String abstrakt = inputDescription.getAbstract();

    /* Supported complex data type. */
    final SupportedComplexDataType complexData = inputDescription.getComplexData();
    final LiteralInputType literalInput = inputDescription.getLiteralData();
    final SupportedCRSsType boundingBoxInput = inputDescription.getBoundingBoxData();

    if( complexData != null )
    {
      // TODO: we ignore this information at the time, but it should be checked if we can actually send this kind of
      // data
      // final String defaultEncoding = complexData.getDefaultEncoding();
      // final String defaultFormat = complexData.getDefaultFormat();
      // final String defaultSchema = complexData.getDefaultSchema();
      // final List<ComplexDataType> supportedComplexData = complexData.getSupportedComplexData();
      // for( ComplexDataType complexDataType : supportedComplexData )
      // {
      // final String encoding = complexDataType.getEncoding();
      // final String format = complexDataType.getFormat();
      // final String schema = complexDataType.getSchema();
      // }
      final IOValueType ioValue = getInputValue( inputValue, inputId, title, abstrakt );

      /* Add the input. */
      return ioValue;
    }

    if( literalInput != null )
    {
      /* Build the literal value type. */
      final CodeType code = WPS040ObjectFactoryUtilities.buildCodeType( null, inputId );

      final String inputType = literalInput.getDataType().getValue();
      final String value = marshalLiteral( inputValue, inputType );

      final LiteralValueType literalValue = WPS040ObjectFactoryUtilities.buildLiteralValueType( value, inputType, null );
      final IOValueType ioValue = WPS040ObjectFactoryUtilities.buildIOValueType( code, title, abstrakt, literalValue );

      /* Add the input. */
      return ioValue;
    }

    /* Supported CRSs type. */
    // TODO: support SupportedCRSsType
    throw new UnsupportedOperationException();
  }

  private IOValueType getInputValue( final Object inputValue, final String inputId, final String title, final String abstrakt ) throws CoreException
  {
    final Object valueType;
    final String format;
    final String encoding;
    final String schema;
    final Object valueString;
    /* Build the complex value reference. */
    if( inputValue instanceof URI )
    {
      // this way we can probably not check the format, encoding and schema of the input
      final URI uri = (URI) inputValue;
      format = null;
      encoding = null;
      schema = null;
      valueType = WPS040ObjectFactoryUtilities.buildComplexValueReference( uri.toASCIIString(), format, encoding, schema );
    }
    /* Build the complex value. */
    else
    {
      // TODO: support other complex types, e.g. regular XML, geometries, ...
      if( inputValue instanceof GMLWorkspace )
      {
        final GMLWorkspace gmlWorkspace = (GMLWorkspace) inputValue;
        final IGMLSchema gmlSchema = gmlWorkspace.getGMLSchema();
        final String schemaLocationString = gmlSchema.getContext().toString();

        format = WPSSimulationDataProvider.TYPE_GML;
        encoding = "UTF-8"; //$NON-NLS-1$

        // TODO: copy the schema to a place where the server can find it
        // REMARK: makes no sense to give platform: or bundleresource: urls; they do not exist on theother side
        if( schemaLocationString != null && !schemaLocationString.startsWith( "bundleresource:" ) && !schemaLocationString.startsWith( "platform:" ) ) //$NON-NLS-1$ //$NON-NLS-2$
          schema = schemaLocationString;
        else
          schema = null;

        // enforce the schemaLocation
        // TODO: copy the schema to a place where the server can find it
        gmlWorkspace.setSchemaLocation( schemaLocationString );
        valueString = getGML( gmlWorkspace );
      }
      else
      {
        // do not know, maybe it can be marshalled by the binding framework?
        // TODO: this will probably throw an exception later, maybe throw it now?
        format = null;
        encoding = null;
        schema = null;
        valueString = inputValue;
      }

      // REMARK: hack/convention: the input must now be the raw input for the anyType element
      final List<Object> value = new ArrayList<Object>( 1 );
      value.add( valueString );
      valueType = WPS040ObjectFactoryUtilities.buildComplexValueType( format, encoding, schema, value );
    }

    final CodeType code = WPS040ObjectFactoryUtilities.buildCodeType( null, inputId );
    final IOValueType ioValue = WPS040ObjectFactoryUtilities.buildIOValueType( code, title, abstrakt, valueType );
    return ioValue;
  }

  private Object getGML( final GMLWorkspace gmlWorkspace ) throws CoreException
  {
    final Object valueString;
    // 0.5 MB text file default buffer
    final StringWriter stringWriter = new StringWriter( 512 * 1024 );
    try
    {
      GmlSerializer.serializeWorkspace( stringWriter, gmlWorkspace, "UTF-8", true ); //$NON-NLS-1$
    }
    catch( final GmlSerializeException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    valueString = stringWriter.toString();
    return valueString;
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

  public String getStatusLocation( )
  {
    return m_statusLocation;
  }

  @SuppressWarnings("unchecked")
  public ExecuteResponseType getExecuteResponse( final FileSystemManager manager ) throws Exception, InterruptedException
  {
    final FileObject statusFile = VFSUtilities.checkProxyFor( m_statusLocation, manager );
    if( statusFile.exists() )
    {
      /* Some variables for handling the errors. */
      boolean success = false;
      int cnt = 0;

      /* Try to read the status at least 3 times, before exiting. */
      JAXBElement<ExecuteResponseType> executeState = null;
      while( success == false )
      {
        final FileContent content = statusFile.getContent();
        InputStream inputStream = null;
        try
        {
          inputStream = content.getInputStream();
          final String xml = MarshallUtilities.fromInputStream( inputStream );
          if( xml != null && !"".equals( xml ) ) //$NON-NLS-1$
          {
            final Object object = MarshallUtilities.unmarshall( xml );
            executeState = (JAXBElement<ExecuteResponseType>) object;
            success = true;
          }
        }
        catch( final Exception e )
        {
          /* An error has occured while copying the file. */
          Debug.println( "An error has occured with the message: " + e.getLocalizedMessage() ); //$NON-NLS-1$

          /* If a certain amount (here 2) of retries was reached before, rethrow the error. */
          if( cnt >= 2 )
          {
            Debug.println( "The second retry has failed, rethrowing the error ..." ); //$NON-NLS-1$
            throw e;
          }

          /* Retry the copying of the file. */
          cnt++;
          Debug.println( "Retry: " + String.valueOf( cnt ) ); //$NON-NLS-1$
          success = false;

          /* Wait for some milliseconds. */
          Thread.sleep( 1000 );
        }
        finally
        {
          IOUtils.closeQuietly( inputStream );
          statusFile.close();
        }
      }
      return executeState.getValue();
    }
    else
      return null;
  }

}