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
package org.kalypso.service.wps.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;

import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.ows.ExceptionType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.DescribeProcess;
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
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.Activator;
import org.kalypso.service.wps.client.exceptions.WPSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.VFSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;

/**
 * This class manages the connect between the client and the server.<br>
 * It polls regularly and checks the status of the calculation, that can be retrieved from it then.<br>
 * Furthermore it has the ability to be canceled.<br>
 * 
 * @author Holger Albert
 */
public class WPSRequest
{
  /**
   * Commonly used system property for the location of the WPS endpoint. Not every WPS client might use this one.
   */
  public static final String SYSTEM_PROP_WPS_ENDPOINT = "org.kalypso.service.wps.service";

  /**
   * The amount from the max monitor value, which is reserved for the server side task.
   */
  private static final int MONITOR_SERVER_VALUE = 500;

  /**
   * The identifier of the service to be called.
   */
  private final String m_identifier;

  /**
   * The address of the service.
   */
  private final String m_serviceEndpoint;

  /**
   * After this period of time, the job gives up, waiting for a result of the service.
   */
  private final long m_timeout;

  /**
   * The process description or null, if the {@link #init(IProgressMonitor)} function was not called or unsuccessfull.
   */
  private ProcessDescriptionType m_processDescription;

  /**
   * Necessary for the monitor update functionality. Stores the last updated value, to prevent, that the monitor is
   * updated to often.
   */
  private int m_alreadyWorked = 0;

  /**
   * The result for the requested references.
   */
  private Map<String, ComplexValueReference> m_references;

  /**
   * The result for the requested literals.
   */
  private Map<String, Object> m_literals;

  /**
   * The result for the requested bounding boxes.
   */
  private Map<String, BoundingBoxType> m_boundingBoxes;

  /**
   * The result for the requested complex values.
   */
  private Map<String, ComplexValueType> m_complexValues;

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
  public WPSRequest( final String identifier, final String serviceEndpoint, final long timeout )
  {
    /* Initializing of the given variables. */
    m_identifier = identifier;
    m_serviceEndpoint = serviceEndpoint;
    m_timeout = timeout;

    /* Initializing the other variables. */
    m_processDescription = null;

    /* No results so far. */
    m_references = null;
    m_literals = null;
    m_boundingBoxes = null;
    m_complexValues = null;
  }

  /**
   * This function initialises the request.<br>
   * TODO: let it throw an CoreException instead of returning the status; makes error handling much easier.
   * 
   * @param monitor
   *          A progress monitor.
   * @return A status, indicating the success of the function.
   */
  public IStatus init( IProgressMonitor monitor )
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( "Initialisiere ...", 500 );
      Debug.println( "Initializing ..." );

      /* Build the describe process request. */
      final List<CodeType> identifier = new LinkedList<CodeType>();
      identifier.add( OGCUtilities.buildCodeType( "", m_identifier ) );
      final DescribeProcess describeProcess = OGCUtilities.buildDescribeProcess( identifier );

      /* Monitor. */
      monitor.worked( 100 );
      monitor.setTaskName( "Frage nach der Prozess-Beschreibung ..." );
      Debug.println( "Asking for a process description ..." );

      /* Send the request. */
      final String describeProcessResponse = WPSUtilities.send( MarshallUtilities.marshall( describeProcess ), m_serviceEndpoint );

      // TODO: error handling: our WPS service throws other types of exceptions
      // (http://www.opengis.net/ows}ExceptionReport) than expected
      // here {http://www.opengeospatial.net/ows}ExceptionReport. We need to fix that... find out what is correct in
      // respect to WPS specification

      /* Handle the response. */
      Debug.println( "Response:\n" + describeProcessResponse );

      /* Monitor. */
      monitor.worked( 200 );
      monitor.setTaskName( "Prüfe die Prozess-Beschreibung ..." );
      Debug.println( "Check the process description ..." );

      Object describeProcessObject = null;
      try
      {
        /* Try to unmarshall. */
        describeProcessObject = MarshallUtilities.unmarshall( describeProcessResponse );
        if( describeProcessObject instanceof ExceptionReport )
        {
          final ExceptionReport report = (ExceptionReport) describeProcessObject;
          return StatusUtilities.createErrorStatus( createErrorString( report ) );
        }

        /* Use the process description for building the DataInputs and the OutputDefinitions. */
        final ProcessDescriptions m_processDescriptions = (ProcessDescriptions) describeProcessObject;

        /* The descriptions of all processes from the process descriptions response. */
        final List<ProcessDescriptionType> processDescription = m_processDescriptions.getProcessDescription();

        /* We will always take the first one. */
        m_processDescription = processDescription.get( 0 );

        /* Monitor. */
        monitor.worked( 200 );

        return Status.OK_STATUS;
      }
      catch( final Exception e )
      {
        // We always will reach this point in case of an exception, as the kalypso
        // WPS implementation return the wrong kind of exception (opengeo in contrast to opengeospatial)
        final IStatus status = StatusUtilities.createErrorStatus( describeProcessResponse );
        Activator.getDefault().getLog().log( status );
        return status;
      }
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      Activator.getDefault().getLog().log( status );
      return status;
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  /**
   * Starts the simulation.
   * 
   * @param monitor
   *          The progress monitor.
   */
  @SuppressWarnings("unchecked")
  public IStatus run( final Map<String, Object> inputs, final List<String> outputs, IProgressMonitor monitor )
  {
    if( monitor == null )
      monitor = new NullProgressMonitor();

    // TODO: clear old results
    try
    {
      /* Monitor. */
      monitor.beginTask( "Berechnung vorbereiten ...", 800 );
      Debug.println( "Checking for service URL ..." );

      /* Check, if we have a service endpoint. */
      if( m_serviceEndpoint == null )
      {
        Debug.println( "No URL to the service is given." );
        throw new WPSException( "No URL to the the service is given." );
      }

      /* Check describe process. */
      if( m_processDescription == null )
        return StatusUtilities.createStatus( IStatus.ERROR, "The init() method was not called before the run method.", null );

      /* Get the input data. */
      final DataInputsType dataInputs = createDataInputs( m_processDescription, inputs );

      /* Get the output data. */
      final OutputDefinitionsType outputDefinitions = createOutputDefinitions( m_processDescription, outputs );

      /* Build the execute request. */
      final Execute execute = OGCUtilities.buildExecute( OGCUtilities.buildCodeType( "", m_identifier ), dataInputs, outputDefinitions, true, true );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS; // WPS does not support cancelling, so we just return

      /* Send the request. */
      monitor.setTaskName( "Starte die Simulation ..." );
      Debug.println( "Start the simulation ..." );
      final String executeRequest = MarshallUtilities.marshall( execute );
      final String executeResponse = WPSUtilities.send( executeRequest, m_serviceEndpoint );

      /* Handle the execute response. */
      Debug.println( "Response:\n" + executeResponse );

      if( executeResponse == null || executeResponse.length() == 0 )
        return StatusUtilities.createErrorStatus( "Got an empty response ..." );

      final Object response = MarshallUtilities.unmarshall( executeResponse );
      if( response instanceof ExceptionReport )
        return StatusUtilities.createErrorStatus( createErrorString( (ExceptionReport) response ) );

      final JAXBElement<ExecuteResponseType> exResponse = (JAXBElement<ExecuteResponseType>) response;
      final ExecuteResponseType value = exResponse.getValue();
      final StatusType status = value.getStatus();

      final ProcessFailedType processFailed = status.getProcessFailed();
      if( processFailed != null )
        return StatusUtilities.createErrorStatus( createErrorString( processFailed.getExceptionReport() ) );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      /* Retrieve the path to the status file of the process. */
      final String statusLocation = value.getStatusLocation();
      if( statusLocation == null || statusLocation.length() == 0 )
        return StatusUtilities.createErrorStatus( "The server responded without a status-location." );

      /* Poll all few seconds to update the status. */
      final boolean run = true;
      long executed = 0;
      ExecuteResponseType exState = null;

      /* Loop, until an result is available, a timeout is reached or the user has cancelled the job. */
      monitor.setTaskName( "Warte auf Prozess '" + m_processDescription.getTitle() + "'..." );
      while( run )
      {
        Thread.sleep( 1000 );
        executed += 1000;

        /* If the timeout is reached. */
        if( executed >= m_timeout )
        {
          Debug.println( "Timeout reached ..." );
          return StatusUtilities.createErrorStatus( "Timeout reached ..." );
        }

        /* If the user aborted the job. */
        if( monitor.isCanceled() )
          return Status.CANCEL_STATUS;

        Debug.println( "Checking state file of the server ..." );
        final FileObject statusFile = VFSUtilities.checkProxyFor( statusLocation );

        // TODO: at least put parsing of status file in a separate method to reduce the size of this one; maybe use same
        // method to handle first status?
        if( statusFile.exists() )
        {
          /* Some variables for handling the errors. */
          boolean success = false;
          int cnt = 0;

          /* Try to read the status at least 3 times, before exiting. */
          JAXBElement<ExecuteResponseType> executeState = null;
          while( success == false )
          {
            try
            {
              final String xml = MarshallUtilities.fromInputStream( statusFile.getContent().getInputStream() );
              final Object object = MarshallUtilities.unmarshall( xml );
              executeState = (JAXBElement<ExecuteResponseType>) object;

              success = true;
            }
            catch( final Exception e )
            {
              /* An error has occured while copying the file. */
              Debug.println( "An error has occured with the message: " + e.getLocalizedMessage() );

              /* If a certain amount (here 2) of retries was reached before, rethrow the error. */
              if( cnt >= 2 )
              {
                Debug.println( "The second retry has failed, rethrowing the error ..." );
                throw e;
              }

              /* Retry the copying of the file. */
              cnt++;
              Debug.println( "Retry: " + String.valueOf( cnt ) );
              success = false;

              /* Wait for some milliseconds. */
              Thread.sleep( 1000 );
            }
          }

          /* Satus was read successfull. */
          exState = executeState.getValue();
          final StatusType state = exState.getStatus();

          if( state.getProcessAccepted() != null )
          {
            /* Do nothing, but waiting of an other response. */
            continue;
          }
          else if( state.getProcessFailed() != null )
          {
            final String messages = createErrorString( state.getProcessFailed().getExceptionReport() );
            return StatusUtilities.createErrorStatus( messages );
          }
          else if( state.getProcessStarted() != null )
          {
            final ProcessStartedType processStarted = state.getProcessStarted();
            final String descriptionValue = processStarted.getValue();
            final Integer percent = processStarted.getPercentCompleted();
            int percentCompleted = 0;
            if( percent != null )
              percentCompleted = percent.intValue();

            /* Update the monitor values. */
            updateMonitor( percentCompleted, descriptionValue, monitor );
          }
          else if( state.getProcessSucceeded() != null )
          {
            Debug.println( "The simulation has finished ..." );
            break;
          }
          else
            return StatusUtilities.createErrorStatus( "The server responded with an unknown state ..." );
        }
        else
          Debug.println( "Not started yet ..." );
      }

      /* Check if the results are ready. */
      monitor.subTask( "" ); // reset subtask, was used for server-side messages
      monitor.setTaskName( "Prüfe Ergebnisse ..." );
      Debug.println( "Check the results ..." );

      /* Get the process outputs. */
      final net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs processOutputs = exState.getProcessOutputs();
      if( processOutputs == null )
        return StatusUtilities.createErrorStatus( "The server responded without any outputs ..." );

      /* Collect all process output. */
      collectOutput( processOutputs );

      /* Finish. */
      monitor.worked( 100 );
    }
    catch( final Exception e )
    {
      Debug.println( "Error occured with the message: " + e.getLocalizedMessage() );
      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }

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
      if( inputValue instanceof URL )
      {
        /* Build the complex value reference. */
        final URL url = (URL) inputValue;
        final CodeType code = OGCUtilities.buildCodeType( null, inputId );
        final ComplexValueReference valueReference = OGCUtilities.buildComplexValueReference( url.toExternalForm(), null, null, null );
        final IOValueType ioValue = OGCUtilities.buildIOValueType( code, inputDescription.getTitle(), inputDescription.getAbstract(), valueReference );

        /* Add the input. */
        return ioValue;
      }

      // REMARK: hack/convention: the input must now be the raw input for the anyType element
      final List<Object> value = new ArrayList<Object>( 1 );
      value.add( inputValue );

      /* Build the complex value reference. */
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
      return (String) literalValue;

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
  private OutputDefinitionsType createOutputDefinitions( final ProcessDescriptionType description, final List<String> outputs ) throws CoreException
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
        throw new CoreException( StatusUtilities.createErrorStatus( "The data output " + identifier.getValue() + " is missing. Check your model data." ) );
      }

      // TODO: maybe only ask for outputs that are in the list m_outputs?

      final CodeType code = OGCUtilities.buildCodeType( null, identifier.getValue() );
      final OutputDefinitionType outputDefinition = OGCUtilities.buildOutputDefinitionType( code, outputDescription.getTitle(), outputDescription.getAbstract(), null, null, null, null );

      /* Add the output. */
      outputValues.add( outputDefinition );
    }

    return OGCUtilities.buildOutputDefinitionsType( outputValues );
  }

  /**
   * This function will create an error string, containing all error messages.
   * 
   * @param exceptionReport
   *          The exception report.
   * @return The error messages as one string.
   */
  private String createErrorString( final ExceptionReport exceptionReport )
  {
    final List<ExceptionType> exceptions = exceptionReport.getException();
    String messages = "";
    for( final ExceptionType exception : exceptions )
      messages = messages + "Code: " + exception.getExceptionCode() + "\nMessage: " + exception.getExceptionText() + "\nLocator: " + exception.getLocator();

    return messages;
  }

  /**
   * This function refreshes the monitor.
   * 
   * @param percentCompleted
   *          The amount of work done, reaching from 0 to 100.
   * @param description
   *          The description to set.
   * @param monitor
   *          The progress monitor to be updated.
   */
  private void updateMonitor( final int percentCompleted, final String description, final IProgressMonitor monitor )
  {
    /* If the already updated value is not equal to the new value. Update the monitor values. */
    if( percentCompleted != m_alreadyWorked )
    {
      /* Check, what is already updated here, and update the rest. */
      final int percentWorked = percentCompleted - m_alreadyWorked;

      /* Project percentWorked to the left monitor value. */
      final double realValue = MONITOR_SERVER_VALUE * (double) percentWorked / 100.0;
      monitor.worked( (int) realValue );

      m_alreadyWorked = percentCompleted;
    }

    /* Set the message. */
// monitor.setTaskName( description );
    monitor.subTask( description );
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
  private void collectOutput( final ExecuteResponseType.ProcessOutputs processOutputs )
  {
    // TODO: maybe check, if all desired outputs have been created??

    /* Collect all data for the client. */
    final List<IOValueType> ioValues = processOutputs.getOutput();
    for( final IOValueType ioValue : ioValues )
    {
      /* Complex value reference. */
      // TODO: dubios The hashmap should always be non-null!
      final ComplexValueReference complexValueReference = ioValue.getComplexValueReference();
      if( complexValueReference != null )
      {
        if( m_references == null )
          m_references = new LinkedHashMap<String, ComplexValueReference>();

        m_references.put( ioValue.getIdentifier().getValue(), complexValueReference );

        continue;
      }

      /* Literal value type. */
      final LiteralValueType literalValue = ioValue.getLiteralValue();
      if( literalValue != null )
      {
        final String value = literalValue.getValue();
        final String dataType = literalValue.getDataType();

        Object result = null;
        if( "string".equals( dataType ) )
          result = DatatypeConverter.parseString( value );
        else if( "int".equals( dataType ) )
          result = DatatypeConverter.parseInt( value );
        else if( "double".equals( dataType ) )
          result = DatatypeConverter.parseDouble( value );
        else if( "boolean".equals( dataType ) )
          result = DatatypeConverter.parseBoolean( value );

        if( result != null )
        {
          if( m_literals == null )
            m_literals = new LinkedHashMap<String, Object>();

          m_literals.put( ioValue.getIdentifier().getValue(), result );
        }

        continue;
      }

      /* Bounding box type. */
      final BoundingBoxType boundingBox = ioValue.getBoundingBoxValue();
      if( boundingBox != null )
      {
        if( m_boundingBoxes == null )
          m_boundingBoxes = new LinkedHashMap<String, BoundingBoxType>();

        m_boundingBoxes.put( ioValue.getIdentifier().getValue(), boundingBox );

        continue;
      }

      /* Complex value type. */
      final ComplexValueType complexValue = ioValue.getComplexValue();
      if( complexValue != null )
      {
        if( m_complexValues == null )
          m_complexValues = new LinkedHashMap<String, ComplexValueType>();

        m_complexValues.put( ioValue.getIdentifier().getValue(), complexValue );

        continue;
      }
    }
  }

  /**
   * This function returns the process description, or null, if {@link #init(IProgressMonitor)} was not called or
   * unsuccessfull.
   * 
   * @return The process description or null.
   */
  public ProcessDescriptionType getProcessDescription( )
  {
    return m_processDescription;
  }

  /**
   * This function returns the result of the references or null, if none.
   * 
   * @return The result of the references or null, if none.
   */
  public Map<String, ComplexValueReference> getReferences( )
  {
    return m_references;
  }

  /**
   * This function returns the results for the literals or null, if none.
   * 
   * @return The results for the literals with their identifier as key.
   */
  public Map<String, Object> getLiterals( )
  {
    return m_literals;
  }

  /**
   * This function returns the results for the bounding boxes or null, if none.
   * 
   * @return The results for the bounding boxes with their identifier as key.
   */
  public Map<String, BoundingBoxType> getBoundingBoxes( )
  {
    return m_boundingBoxes;
  }

  /**
   * TODO: (same for getLiterals and getReferences): dubious: why< not just a getResult( String key) method: the client
   * has to cast the result anyway. He may even not even know which one it is, so one single method (and inspection of
   * the result) should be better. This function returns the results for the complex values or null, if none.
   * 
   * @return The results for the complex values with their identifier as key.
   */
  public Map<String, ComplexValueType> getComplexValues( )
  {
    return m_complexValues;
  }
}