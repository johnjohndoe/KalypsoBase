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

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;

import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.commons.io.VFSUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;

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
   * This value for WPS endpoint indicates that a service call should be local (i.e. inside the same VM).
   */
  public static final String SERVICE_LOCAL = "ServiceLocal";

  /**
   * The amount from the max monitor value, which is reserved for the server side task.
   */
  private static int MONITOR_SERVER_VALUE = 500;

  /**
   * Necessary for the monitor update functionality. Stores the last updated value, to prevent, that the monitor is
   * updated to often.
   */
  private int m_alreadyWorked = 0;

  /**
   * The result for the requested references.
   */
  private final Map<String, ComplexValueReference> m_references = new LinkedHashMap<String, ComplexValueReference>();

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
   * A non blocking request. This class will use it to send a request and waits for its results afterwards.
   */
  private final NonBlockingWPSRequest wpsRequest;

  /**
   * After this period of time, the job gives up, waiting for a result of the service.
   */
  private final long m_timeout;

  public WPSRequest( final String identifier, final String serviceEndpoint, final long timeout )
  {
    wpsRequest = new NonBlockingWPSRequest( identifier, serviceEndpoint );
    m_timeout = timeout;
  }

  /**
   * @deprecated Provided for backwards compatibility. The call can savely be removed. If the process description is
   *             needed, query {@link #getProcessDescription(IProgressMonitor)} instead.
   */
  @SuppressWarnings("unused")
  @Deprecated
  public void init( final IProgressMonitor monitor )
  {
    // nothing to do
  }

  /**
   * Initializes the WPS process by getting the description.
   */
  public ProcessDescriptionType getProcessDescription( final IProgressMonitor monitor ) throws CoreException
  {
    return wpsRequest.getProcessDescription( monitor );
  }

  /**
   * Use {@link #getProcessDescription(IProgressMonitor)} instead
   */
  @Deprecated
  public ProcessDescriptionType getProcessDescription( ) throws CoreException
  {
    return wpsRequest.getProcessDescription( null );
  }

  public IStatus run( final Map<String, Object> inputs, final List<String> outputs, IProgressMonitor monitor )
  {
    monitor = SubMonitor.convert( monitor );

    IStatus status = wpsRequest.init( inputs, outputs, monitor );

    // on error return immediately
    if( !status.isOK() )
    {
      return status;
    }

    // start request, returns immediately
    status = wpsRequest.run( monitor );

    // on error return immediately
    if( !status.isOK() )
    {
      return status;
    }

    // after success, the status location will be set
    final String statusLocation = wpsRequest.getStatusLocation();
    if( statusLocation.length() == 0 )
    {
      return StatusUtilities.createErrorStatus( "The server responded without a status-location." );
    }

    StandardFileSystemManager manager = null;
    FileObject statusFile = null;
    try
    {
      Debug.println( "Checking state file of the server ..." );
      ExecuteResponseType exState = null;

      /* Poll to update the status. */
      boolean run = true;
      long executed = 0;

      /* Loop, until an result is available, a timeout is reached or the user has cancelled the job. */
      final ProcessDescriptionType processDescription = getProcessDescription( monitor );
      final String title = processDescription.getTitle();
      monitor.setTaskName( "Warte auf Prozess " + title );

      manager = VFSUtilities.getNewManager();
      // TODO: at least put parsing of status file in a separate method to reduce the size of this one;
      // maybe use same method to handle first status?
      while( run )
      {
        statusFile = VFSUtilities.checkProxyFor( statusLocation, manager );
        if( statusFile.exists() )
        {
          final JAXBElement<ExecuteResponseType> executeState = waitForExecuteResponse( statusFile );

          /* Status was read successfull. */
          exState = executeState.getValue();
          final StatusType state = exState.getStatus();

          if( state.getProcessAccepted() != null )
          {
            /* Do nothing, but wait for an other response. */
            continue;
          }
          else if( state.getProcessFailed() != null )
          {
            final String messages = WPSUtilities.createErrorString( state.getProcessFailed().getExceptionReport() );
            status = StatusUtilities.createErrorStatus( messages );
            run = false;
          }
          else if( state.getProcessStarted() != null )
          {
            final ProcessStartedType processStarted = state.getProcessStarted();
            final String descriptionValue = processStarted.getValue();
            final Integer percent = processStarted.getPercentCompleted();
            int percentCompleted = 0;
            if( percent != null )
            {
              percentCompleted = percent.intValue();
            }

            /* Update the monitor values. */
            updateMonitor( percentCompleted, descriptionValue, monitor );
          }
          else if( state.getProcessSucceeded() != null )
          {
            Debug.println( "The simulation has finished ..." );
            run = false;
          }
          else
          {
            status = StatusUtilities.createErrorStatus( "The server responded with an unknown state ..." );
            run = false;
          }

          /* If the user aborted the job. */
          if( monitor.isCanceled() )
          {
            return Status.CANCEL_STATUS;
          }

          /* Get the process outputs every time. */
          final net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs processOutputs = exState.getProcessOutputs();

          /* Collect all process output. */
          if( processOutputs != null )
          {
            collectOutput( processOutputs );
          }
        }
        try
        {
          Thread.sleep( 2000 );
          executed += 2000;
        }
        catch( final InterruptedException e )
        {
          return StatusUtilities.statusFromThrowable( e );
        }

        /* If the timeout is reached. */
        if( executed >= m_timeout )
        {
          Debug.println( "Timeout reached ..." );
          return StatusUtilities.createErrorStatus( "Timeout reached ..." );
        }
      }

      if( exState != null )
      {
        /* Check if the results are ready. */
        monitor.subTask( "" ); // reset subtask, was used for server-side messages
        monitor.setTaskName( "Prüfe Ergebnisse ..." );
        Debug.println( "Check the results ..." );

        /* Get the process outputs. */
        final net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs processOutputs = exState.getProcessOutputs();

        if( processOutputs == null )
        {
          status = StatusUtilities.createErrorStatus( "The process did not return any results." );
        }
        else
        {
          /* Collect all process output. */
          collectOutput( processOutputs );
        }
      }
    }
    catch( final Exception e )
    {
      status = StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      if( statusFile != null )
      {
        try
        {
          statusFile.close();
        }
        catch( final FileSystemException e )
        {
          // gobble
        }
      }
      if( manager != null )
        manager.close();
    }

    return status;
  }

  @SuppressWarnings("unchecked")
  private JAXBElement<ExecuteResponseType> waitForExecuteResponse( final FileObject statusFile ) throws Exception, InterruptedException
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
        if( xml != null && !"".equals( xml ) )
        {
          final Object object = MarshallUtilities.unmarshall( xml );
          executeState = (JAXBElement<ExecuteResponseType>) object;
          success = true;
        }
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
      finally
      {
        IOUtils.closeQuietly( inputStream );
        statusFile.close();
      }
    }

    return executeState;
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
      final ComplexValueReference complexValueReference = ioValue.getComplexValueReference();
      if( complexValueReference != null )
      {
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
        {
          result = DatatypeConverter.parseString( value );
        }
        else if( "int".equals( dataType ) )
        {
          result = DatatypeConverter.parseInt( value );
        }
        else if( "double".equals( dataType ) )
        {
          result = DatatypeConverter.parseDouble( value );
        }
        else if( "boolean".equals( dataType ) )
        {
          result = DatatypeConverter.parseBoolean( value );
        }

        if( result != null )
        {
          if( m_literals == null )
          {
            m_literals = new LinkedHashMap<String, Object>();
          }

          m_literals.put( ioValue.getIdentifier().getValue(), result );
        }

        continue;
      }

      /* Bounding box type. */
      final BoundingBoxType boundingBox = ioValue.getBoundingBoxValue();
      if( boundingBox != null )
      {
        if( m_boundingBoxes == null )
        {
          m_boundingBoxes = new LinkedHashMap<String, BoundingBoxType>();
        }

        m_boundingBoxes.put( ioValue.getIdentifier().getValue(), boundingBox );

        continue;
      }

      /* Complex value type. */
      final ComplexValueType complexValue = ioValue.getComplexValue();
      if( complexValue != null )
      {
        if( m_complexValues == null )
        {
          m_complexValues = new LinkedHashMap<String, ComplexValueType>();
        }

        m_complexValues.put( ioValue.getIdentifier().getValue(), complexValue );

        continue;
      }
    }
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