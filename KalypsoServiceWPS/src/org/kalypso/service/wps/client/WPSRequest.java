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

import java.math.BigInteger;
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
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;

import org.apache.commons.vfs.FileObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
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
   * The amount from the max monitor value, which is reserved for the server side task.
   */
  private static int MONITOR_SERVER_VALUE = 500;

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
   * The worker, which provides this client with the required data.
   */
  private IWPSDelegate m_delegate;

  /**
   * After this period of time, the job gives up, waiting for a result of the service.
   */
  private long m_timeout;

  /**
   * Necessary for the monitor update functinality. Stores the last updated value, to prevent, that the monitor is
   * updated to often.
   */
  private Integer m_alreadyWorked;

  /**
   * The constructor.
   * 
   * @param delegate
   *            This delegate provides the data for the WPS client.
   * @param timeout
   *            After this period of time, the job gives up, waiting for a result of the service.
   */
  public WPSRequest( IWPSDelegate delegate, long timeout )
  {
    /* No results so far. */
    m_references = null;
    m_literals = null;
    m_boundingBoxes = null;
    m_complexValues = null;

    /* Initializing of the given variables. */
    m_delegate = delegate;
    m_timeout = timeout;

    /* Initializing the other variables. */
    m_alreadyWorked = new Integer( 0 );
  }

  /**
   * Starts the simulation.
   * 
   * @param monitor
   *            The progress monitor.
   */
  @SuppressWarnings("unchecked")
  public IStatus run( IProgressMonitor monitor )
  {
    if( monitor == null )
      monitor = new NullProgressMonitor();

    monitor.beginTask( "Berechnung vorbereiten ...", 1000 );

    try
    {
      /* Init the necessary components. */
      init();

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Build the describe process request. */
      List<CodeType> identifier = new LinkedList<CodeType>();
      identifier.add( OGCUtilities.buildCodeType( "", m_delegate.getIdentifier() ) );
      DescribeProcess describeProcess = OGCUtilities.buildDescribeProcess( identifier );

      /* Send the request. */
      monitor.setTaskName( "Frage nach der Prozess-Beschreibung ..." );
      Debug.println( "Asking for a process describtion ..." );
      String describeProcessResponse = WPSUtilities.send( MarshallUtilities.marshall( describeProcess ), m_delegate.getServiceURL() );

      // TODO: error handling: our WPS service throws other types of exceptions
      // (http://www.opengis.net/ows}ExceptionReport) than excpected
      // here {http://www.opengeospatial.net/ows}ExceptionReport. We need to fix that... find out what is correct in
      // respect to WPS specification

      /* Handle the response. */
      Debug.println( "Response:\n" + describeProcessResponse );

      /* Try to unmarshall. */
      Object describeProcessObject = MarshallUtilities.unmarshall( describeProcessResponse );
      if( describeProcessObject instanceof ExceptionReport )
      {
        ExceptionReport report = (ExceptionReport) describeProcessObject;
        return StatusUtilities.createErrorStatus( createErrorString( report ) );
      }

      /* Use the process description for building the DataInputs and the OutputDefinitions. */
      ProcessDescriptions processDescriptions = (ProcessDescriptions) describeProcessObject;

      /* The descriptions of all processes from the process descriptions response. */
      List<ProcessDescriptionType> processDescription = processDescriptions.getProcessDescription();

      /* There could be only one description, because we have asked for a special one. */
      ProcessDescriptionType description = processDescription.get( 0 );

      /* Copy the data to the server. */
      monitor.setTaskName( "Kopiere auf den Server ..." );
      Debug.println( "Copy to the server ..." );

      /* Copy ... */
      m_delegate.copyInputFiles( description );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Get the input data. */
      DataInputsType dataInputs = m_delegate.getDataInputs( description );

      /* Get the output data. */
      OutputDefinitionsType outputDefinitions = m_delegate.getOutputDefinitions( description );

      /* Build the execute request. */
      Execute execute = OGCUtilities.buildExecute( OGCUtilities.buildCodeType( "", m_delegate.getIdentifier() ), dataInputs, outputDefinitions, true, true );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Send the request. */
      monitor.setTaskName( "Starte die Simulation ..." );
      Debug.println( "Start the simulation ..." );
      String executeResponse = WPSUtilities.send( MarshallUtilities.marshall( execute ), m_delegate.getServiceURL() );

      /* Handle the execute response. */
      Debug.println( "Response:\n" + executeResponse );

      if( executeResponse == null || executeResponse.length() == 0 )
        return StatusUtilities.createErrorStatus( "Got an empty response ..." );

      Object response = MarshallUtilities.unmarshall( executeResponse );
      if( response instanceof ExceptionReport )
        return StatusUtilities.createErrorStatus( createErrorString( (ExceptionReport) response ) );

      JAXBElement<ExecuteResponseType> exResponse = (JAXBElement<ExecuteResponseType>) response;
      ExecuteResponseType value = exResponse.getValue();
      StatusType status = value.getStatus();

      ProcessFailedType processFailed = status.getProcessFailed();
      if( processFailed != null )
        return StatusUtilities.createErrorStatus( createErrorString( processFailed.getExceptionReport() ) );

      /* If the user aborted the job. */
      monitor.worked( 100 );
      if( monitor.isCanceled() )
        return canceled();

      /* Retrieve the path to the status file of the process. */
      String statusLocation = value.getStatusLocation();
      if( statusLocation == null || statusLocation.length() == 0 )
        return StatusUtilities.createErrorStatus( "The server responded without a status-location." );

      /* Poll all few seconds to update the status. */
      boolean run = true;
      long executed = 0;
      ExecuteResponseType exState = null;

      /* Loop, until an result is available, a timeout is reached or the user has canceled the job. */
      monitor.setTaskName( "Warte auf Status ..." );
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
          return canceled();

        Debug.println( "Checking state file of the server ..." );
        FileObject statusFile = VFSUtilities.checkProxyFor( statusLocation );

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
              String xml = MarshallUtilities.fromInputStream( statusFile.getContent().getInputStream() );
              Object object = MarshallUtilities.unmarshall( xml );
              executeState = (JAXBElement<ExecuteResponseType>) object;

              success = true;
            }
            catch( Exception e )
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
          StatusType state = exState.getStatus();

          if( state.getProcessAccepted() != null )
          {
            /* Do nothing, but waiting of an other response. */
            continue;
          }
          else if( state.getProcessFailed() != null )
          {
            String messages = createErrorString( state.getProcessFailed().getExceptionReport() );
            return StatusUtilities.createErrorStatus( messages );
          }
          else if( state.getProcessStarted() != null )
          {
            ProcessStartedType processStarted = state.getProcessStarted();
            String descriptionValue = processStarted.getValue();
            BigInteger percent = processStarted.getPercentCompleted();
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
      monitor.setTaskName( "Prüfe Ergebnisse ..." );
      Debug.println( "Check the results ..." );

      /* Get the process outputs. */
      net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs processOutputs = exState.getProcessOutputs();
      if( processOutputs == null )
        return StatusUtilities.createErrorStatus( "The server responded without any outputs ..." );

      /* Copy all process output to their destination. */
      monitor.setTaskName( "Kopiere Ergebnisse zu Client ..." );
      collectOutput( processOutputs );
      m_delegate.copyResults( m_references );

      /* Finish. */
      monitor.worked( 100 );
      finish();
    }
    catch( Exception e )
    {
      Debug.println( "Error occured with the message: " + e.getLocalizedMessage() );
      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      monitor.done();
    }

    return Status.OK_STATUS;
  }

  /**
   * This function initializes everything.
   */
  private void init( ) throws WPSException
  {
    /* Init the operation. */
    m_delegate.init();
  }

  /**
   * This function will create an error string, containing all error messages.
   * 
   * @param exceptionReport
   *            The exception report.
   * @return The error messages as one string.
   */
  private String createErrorString( ExceptionReport exceptionReport )
  {
    List<ExceptionType> exceptions = exceptionReport.getException();
    String messages = "";
    for( ExceptionType exception : exceptions )
      messages = messages + "Code: " + exception.getExceptionCode() + "\nMessage: " + exception.getExceptionText() + "\nLocator: " + exception.getLocator();

    return messages;
  }

  /**
   * This function refreshs the monitor.
   * 
   * @param percentCompleted
   *            The amount of work done, reaching from 0 to 100.
   * @param description
   *            The description to set.
   * @param monitor
   *            The progressmonitor to be updated.
   */
  private void updateMonitor( Integer percentCompleted, String description, IProgressMonitor monitor )
  {
    /* If the already updated value is not equal to the new value. Update the monitor values. */
    if( !percentCompleted.equals( m_alreadyWorked ) )
    {
      /* Check, what is already updated here, and update the rest. */
      Integer percentWorked = percentCompleted - m_alreadyWorked;

      /* Project percentWorked to the left monitor value. */
      Integer realValue = MONITOR_SERVER_VALUE * percentWorked / 100;
      monitor.worked( realValue );

      m_alreadyWorked = percentCompleted;
    }

    /* Set the message. */
    monitor.setTaskName( description );
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
   *            The process outputs contains the info of the results, which are to be collected.
   */
  private void collectOutput( ExecuteResponseType.ProcessOutputs processOutputs )
  {
    /* Collect all data for the client. */
    List<IOValueType> ioValues = processOutputs.getOutput();
    for( IOValueType ioValue : ioValues )
    {
      /* Complex value reference. */
      ComplexValueReference complexValueReference = ioValue.getComplexValueReference();
      if( complexValueReference != null )
      {
        if( m_references == null )
          m_references = new LinkedHashMap<String, ComplexValueReference>();

        m_references.put( ioValue.getIdentifier().getValue(), complexValueReference );

        continue;
      }

      /* Literal value type. */
      LiteralValueType literalValue = ioValue.getLiteralValue();
      if( literalValue != null )
      {
        String value = literalValue.getValue();
        String dataType = literalValue.getDataType();

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
      BoundingBoxType boundingBox = ioValue.getBoundingBoxValue();
      if( boundingBox != null )
      {
        if( m_boundingBoxes == null )
          m_boundingBoxes = new LinkedHashMap<String, BoundingBoxType>();

        m_boundingBoxes.put( ioValue.getIdentifier().getValue(), boundingBox );

        continue;
      }

      /* Complex value type. */
      ComplexValueType complexValue = ioValue.getComplexValue();
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
   * This function will finish the operations on this simulation.
   * <ol>
   * <li>All input files will be deleted.</li>
   * <li>Other things can be disposed here.</li>
   * </ol>
   */
  private void finish( )
  {
    /* Finish the oepration. */
    m_delegate.finish();
  }

  /**
   * This function is called, when the job is canceled by the user.<br>
   * A cancel request has to be send to the server, so that he can stop executing the simulation.
   */
  private IStatus canceled( )
  {
    /* Cancel the operation. */
    m_delegate.canceled();

    return Status.CANCEL_STATUS;
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
   * This function returns the results for the complex values or null, if none.
   * 
   * @return The results for the complex values with their identifier as key.
   */
  public Map<String, ComplexValueType> getComplexValues( )
  {
    return m_complexValues;
  }
}