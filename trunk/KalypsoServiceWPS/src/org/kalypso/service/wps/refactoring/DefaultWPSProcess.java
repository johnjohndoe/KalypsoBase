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
package org.kalypso.service.wps.refactoring;

import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs.FileSystemManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.WPS040ObjectFactoryUtilities;
import org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * This class manages the connect between the client and the server.<br>
 * It polls regularly and checks the status of the calculation, that can be retrieved from it then.<br>
 * Furthermore it has the ability to be canceled.<br>
 * 
 * @author Holger Albert
 */
public class DefaultWPSProcess implements IWPSProcess
{
  /**
   * The identifier of the service to be called.
   */
  private final String m_identifier;

  /**
   * The address of the service.
   */
  private final String m_serviceEndpoint;

  private final FileSystemManager m_manager;

  private OutputDefinitionsType m_outputDefinitions;

  private String m_statusLocation;

  private ProcessDescriptionType m_processDescription;

  private ExecuteResponseType m_executionResponse;

  private Map<String, Object[]> m_output = null;

  /**
   * The constructor.
   * 
   * @param identifier
   *          The identifier of the service to be called.
   * @param serviceEndpoint
   *          The address of the service.
   */
  public DefaultWPSProcess( final String identifier, final String serviceEndpoint, final FileSystemManager manager )
  {
    /* Initializing of the given variables. */
    m_identifier = identifier;
    m_serviceEndpoint = serviceEndpoint;
    m_manager = manager;

    Assert.isNotNull( m_serviceEndpoint, "No URL to the the service is given." );
  }

  /**
   * Initializes the WPS by getting the process description
   */
  public synchronized ProcessDescriptionType getProcessDescription( final IProgressMonitor monitor ) throws CoreException
  {
    if( m_processDescription == null )
    {
      monitor.beginTask( "Frage nach der Prozess-Beschreibung ...", 300 );
      Debug.println( "Asking for a process description ..." );

// // decide between local and remote invocation
// if( WPSRequest.SERVICE_LOCAL.equals( m_serviceEndpoint ) )
// {
// final ProcessDescriptionMediator processDescriptionMediator = new ProcessDescriptionMediator( WPS_VERSION.V040 );
// m_processDescription = (ProcessDescriptionType) processDescriptionMediator.getProcessDescription( m_identifier );
// }
// else
// {
      final List<ProcessDescriptionType> processDescriptionList = WPSUtilities.callDescribeProcess( m_serviceEndpoint, m_identifier );
      if( processDescriptionList.size() != 1 )
      {
        throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, "DescribeProcess returned more than one process description.", null ) );
      }

      ProgressUtilities.worked( monitor, 300 );

      /* We will always take the first one. */
      m_processDescription = processDescriptionList.get( 0 );
// }
    }

    return m_processDescription;
  }

  public synchronized ProcessStatus getProcessStatus( )
  {
    if( m_executionResponse == null )
      return ProcessStatus.NONE;

    final StatusType status = m_executionResponse.getStatus();

    if( status.getProcessAccepted() != null )
      return ProcessStatus.ACCEPED;

    if( status.getProcessStarted() != null )
      return ProcessStatus.STARTED;

    if( status.getProcessSucceeded() != null )
      return ProcessStatus.SUCEEDED;

    if( status.getProcessFailed() != null )
      return ProcessStatus.FAILED;

    throw new IllegalStateException( "Unknwon status: " + status.toString() );
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSProcess#getTitle()
   */
  @Override
  public synchronized String getTitle( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSProcess#getPercentCompleted()
   */
  @Override
  public synchronized Integer getPercentCompleted( )
  {
    if( m_executionResponse == null )
      return null;

    final StatusType status = m_executionResponse.getStatus();
    final String processAccepted = status.getProcessAccepted();
    final ProcessStartedType processStarted = status.getProcessStarted();
    final ProcessFailedType processFailed = status.getProcessFailed();
    final String processSucceeded = status.getProcessSucceeded();
    if( processAccepted != null )
      return 0;
    if( processStarted != null )
      return processStarted.getPercentCompleted();
    if( processFailed != null )
      return 100;
    if( processSucceeded != null )
      return 100;

    return 0;
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSProcess#getStatusDescription()
   */
  @Override
  public synchronized String getStatusDescription( )
  {
    if( m_executionResponse == null )
      return null;

    final StatusType status = m_executionResponse.getStatus();

    final String processAccepted = status.getProcessAccepted();
    final ProcessStartedType processStarted = status.getProcessStarted();
    final ProcessFailedType processFailed = status.getProcessFailed();
    final String processSucceeded = status.getProcessSucceeded();
    if( processAccepted != null )
      return processAccepted;
    if( processStarted != null )
      return processStarted.getValue();
    if( processFailed != null )
      // TODO: can be done better...
      return processFailed.getExceptionReport().toString();
    if( processSucceeded != null )
      return processSucceeded;

    return "Unknown state";
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSProcess#startProcess(java.util.Map, java.util.List,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public synchronized void startProcess( final Map<String, Object> inputs, final List<String> outputs, IProgressMonitor monitor ) throws CoreException
  {
    Assert.isTrue( m_executionResponse == null );

    /* Monitor. */
    monitor = SubMonitor.convert( monitor, "Berechnung vorbereiten ...", 200 );
    Debug.println( "Checking for service URL ..." );

    /* Get the process description. */
    final ProcessDescriptionType processDescription = getProcessDescription( monitor );
    /* Get the input data. */
    final DataInputsType dataInputs = createDataInputs( processDescription, inputs );
/* Get the output data. */
    m_outputDefinitions = WPSUtilities.createOutputDefinitions( processDescription, outputs );

    /* Send the request. */
    monitor.setTaskName( "Starte die Simulation ..." );
    Debug.println( "Start the simulation ..." );

// final CodeType simulationIdentifier = WPS040ObjectFactoryUtilities.buildCodeType( "", m_identifier );

// try
// {
    // decide between local and remote invocation
// if( WPSRequest.SERVICE_LOCAL.equals( m_serviceEndpoint ) )
// {
// FileObject resultFile = null;
// try
// {
// /* Execute the simulation via a manager, so that more than one simulation can be run at the same time. */
// final Execute execute = WPS040ObjectFactoryUtilities.buildExecute( simulationIdentifier, m_dataInputs,
    // m_outputDefinitions, true, true );
// final WPSSimulationManager manager = WPSSimulationManager.getInstance();
//
// final ExecuteMediator executeMediator = new ExecuteMediator( execute );
// final WPSSimulationInfo info = manager.startSimulation( executeMediator );
//
// /* Prepare the execute response. */
// final FileObject resultDir = manager.getResultDir( info.getId() );
// resultFile = resultDir.resolveFile( "executeResponse.xml" );
// final String statusLocation = WPSUtilities.convertInternalToClient( resultFile.getURL().toExternalForm() );
// final StatusType status = WPS040ObjectFactoryUtilities.buildStatusType( "Process accepted.", true );
// executeResponse = WPS040ObjectFactoryUtilities.buildExecuteResponseType( simulationIdentifier, status, m_dataInputs,
    // m_outputDefinitions, null, statusLocation, WPSUtilities.WPS_VERSION.V040.toString() );
// }
// catch( final IOException e )
// {
// throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
// }
// catch( final SimulationException e )
// {
// throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
// }
// catch( final OWSException e )
// {
// throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
// }
// finally
// {
// if( resultFile != null )
// try
// {
// resultFile.close();
// }
// catch( final FileSystemException e )
// {
// // gobble
// }
// }
// }
// else
// {
    m_executionResponse = WPSUtilities.callExecute( m_serviceEndpoint, m_identifier, dataInputs, m_outputDefinitions );

    // TODO: check status, should now at least be 'accepted'

    // }
// }
// catch( final CoreException e )
// {
// return e.getStatus();
// }

    // TODO: move outside
// final StatusType status = executeResponse.getStatus();
// final ProcessFailedType processFailed = status.getProcessFailed();
// if( processFailed != null )
// {
// final String errorString = WPSUtilities.createErrorString( processFailed.getExceptionReport() );
// return StatusUtilities.createErrorStatus( errorString );
// }

    /* If the user aborted the job. */
    ProgressUtilities.worked( monitor, 100 );
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
        throw new CoreException( StatusUtilities.createErrorStatus( "The data input %s is mandatory. Check your input data.", inputId ) );
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
// final SupportedCRSsType boundingBoxInput = inputDescription.getBoundingBoxData();

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
        encoding = "UTF-8";
        schema = schemaLocationString;

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
      GmlSerializer.serializeWorkspace( stringWriter, gmlWorkspace, "UTF-8", true );
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

  public String getStatusLocation( )
  {
    return m_statusLocation;
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSProcess#getExecuteResponse()
   */
  public synchronized ExecuteResponseType getExecuteResponse( ) throws CoreException
  {
    switch( getProcessStatus() )
    {
      case NONE:
        return null;

      case SUCEEDED:
      case FAILED:
        return m_executionResponse;

      default:
        final String statusLocation = m_executionResponse.getStatusLocation();
        m_executionResponse = WPSUtilities.readExecutionResponse( m_manager, statusLocation );
        m_output = null;
        return m_executionResponse;
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSProcess#getResult(java.lang.String)
   */
  @Override
  public synchronized Object[] getResult( final String id )
  {
    if( m_executionResponse == null )
      return null;

    if( m_output == null )
    {
      // TODO: decide if to recollect
      if( m_executionResponse != null )
        m_output = collectOutput( m_executionResponse.getProcessOutputs() );
    }

    // TODO Auto-generated method stub
    return m_output.get( id );
  }

  /**
   * Collects the process output.<br>
   * <br>
   * <ol>
   * <li>All files (ComplexValueReference) will be collected with their id.</li>
   * <li>All literals (LiteralValueType) will be collected with their id.</li>
   * <li>All bounding boxes (BoundingBoxType) will be collected with their id.</li>
   * <li>All complex datas (ComplexDataType) will be collected with their id.</li>
   * </ol>
   * 
   * @param processOutputs
   *          The process outputs contains the info of the results, which are to be collected.
   */
  private static Map<String, Object[]> collectOutput( final ExecuteResponseType.ProcessOutputs processOutputs )
  {
    // TODO: maybe check, if all desired outputs have been created??

    final Map<String, Object[]> result = new HashMap<String, Object[]>();

    /* Collect all data for the client. */
    final List<IOValueType> ioValues = processOutputs.getOutput();
    for( final IOValueType ioValue : ioValues )
    {
      final String id = ioValue.getIdentifier().getValue();

      final ComplexValueReference complexValueReference = ioValue.getComplexValueReference();
      final LiteralValueType literalValue = ioValue.getLiteralValue();
      final BoundingBoxType boundingBox = ioValue.getBoundingBoxValue();
      final ComplexValueType complexValue = ioValue.getComplexValue();

      if( complexValueReference != null )
        addItem( result, id, complexValueReference );
      else if( literalValue != null )
      {
        final String value = literalValue.getValue();
        final String dataType = literalValue.getDataType();

        final Object parsedValue = parseValue( value, dataType );

        addItem( result, id, parsedValue );
      }
      else if( boundingBox != null )
      {
        addItem( result, id, boundingBox );
      }
      else if( complexValue != null )
        addItem( result, id, complexValue );
      else
        throw new IllegalStateException();
    }

    return result;
  }

  private static Object parseValue( final String value, final String dataType )
  {
    if( "string".equals( dataType ) )
      return DatatypeConverter.parseString( value );

    if( "int".equals( dataType ) )
      return DatatypeConverter.parseInt( value );

    if( "double".equals( dataType ) )
      return DatatypeConverter.parseDouble( value );

    if( "boolean".equals( dataType ) )
      return DatatypeConverter.parseBoolean( value );

    throw new NotImplementedException( "Unknown result type: " + dataType );
  }

  private static void addItem( final Map<String, Object[]> map, final String id, final Object value )
  {
    final Object[] objects = map.get( id );
    map.put( id, ArrayUtils.add( objects, value ) );
  }

}