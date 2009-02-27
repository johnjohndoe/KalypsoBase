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
package org.kalypso.service.wps.utils.simulation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.ows.ExceptionType;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.io.VFSUtilities;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.ogc.OGCUtilities;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.ISimulationConstants.STATE;

/**
 * Manages the started simulation in backgrounds.
 * 
 * @author Holger Albert
 */
public class WPSSimulationHandler extends Thread
{
  /**
   * Manages ALL jobs.
   */
  private final WPSQueuedSimulationService m_service;

  /**
   * The id of the thread, that should be controlled.
   */
  private final String m_jobID;

  /**
   * The execute request.
   */
  private final Execute m_execute;

  /**
   * The constructor.
   * 
   * @param service
   *          Manages ALL jobs.
   * @param jobID
   *          The id of the thread, that should be controlled.
   * @param execute
   *          The execute request.
   */
  public WPSSimulationHandler( WPSQueuedSimulationService service, String jobID, Execute execute )
  {
    m_service = service;
    m_jobID = jobID;
    m_execute = execute;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run( )
  {
    try
    {
      WPSSimulationInfo jobInfo = m_service.getJob( m_jobID );

      boolean bEnd = false;
      while( bEnd == false )
      {
        STATE state = jobInfo.getState();

        // get current results
        final List<IOValueType> ioValues = jobInfo.getCurrentResults();

        /* Do something, according to the results. */
        switch( state )
        {
          case FINISHED:
            /* Send user a message. */
            if( jobInfo.getFinishStatus() != IStatus.ERROR )
              // false means process succeeded
              createExecuteResponse( OGCUtilities.buildStatusType( "Process ended successfully.", false ), ioValues );
            else
              createProcessFailedExecuteResponse( jobInfo.getFinishText() );

            /* End loop. */
            bEnd = true;
            break;
          case RUNNING:
            ProcessStartedType processStarted = OGCUtilities.buildProcessStartedType( jobInfo.getMessage(), jobInfo.getProgress() );
            // false is ignored
            createExecuteResponse( OGCUtilities.buildStatusType( processStarted, false ), ioValues );
            break;
          case CANCELED:
            /* Delete all files in result directory. */
            /* TODO Eventually delete all files in the result on cancel. */

            /* End loop. */
            bEnd = true;
            break;
          case WAITING:
            // do nothing?
            // true means process accepted
            createExecuteResponse( OGCUtilities.buildStatusType( "Process waiting.", true ), ioValues );
            break;
          case UNKNOWN:
          case ERROR:
            /* Send user a message. */
            createProcessFailedExecuteResponse( jobInfo.getFinishText() );
            /* End loop. */
            bEnd = true;
            break;
        }

        sleep( 2000 );
      }
    }
    catch( Exception e )
    {
      // TODO ProcessFailed...!!!
      e.printStackTrace();
    }
    finally
    {
      /* Job has finished somehow. */

      /* Delete all files of this job in the temp-directory. */
      try
      {
        m_service.disposeJob( m_jobID );
      }
      catch( final SimulationException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * This function creates the execute response for the case the process has failed.
   * 
   * @param message
   *          The error message.
   */
  private void createProcessFailedExecuteResponse( String message ) throws Exception
  {
    List<String> list = new ArrayList<String>();
    list.add( message );

    ExceptionType exception = OGCUtilities.buildExceptionType( list, "NO_APPLICABLE_CODE", "" );
    List<ExceptionType> exceptions = new ArrayList<ExceptionType>();
    exceptions.add( exception );

    ExceptionReport exceptionReport = OGCUtilities.buildExceptionReport( exceptions, OGCUtilities.VERSION, null );
    ProcessFailedType processFailed = OGCUtilities.buildProcessFailedType( exceptionReport );

    createExecuteResponse( OGCUtilities.buildStatusType( processFailed, false ), null );
  }

  /**
   * This function creates the execute response in the location for the given thread.
   * 
   * @param status
   *          The status of the process.
   * @param ioValues
   *          The ioValues for creating the process outputs, if any are here. Otherwise leave it null.
   */
  private synchronized void createExecuteResponse( StatusType status, List<IOValueType> ioValues ) throws Exception
  {
    /* Prepare the execute response. */
    FileObject resultFile = m_service.getResultDir( m_jobID ).resolveFile( "executeResponse.xml" );
    String statusLocation = WPSUtilities.convertInternalToClient( resultFile.getURL().toExternalForm() );

    ProcessOutputs processOutputs = null;
    if( ioValues != null )
      processOutputs = OGCUtilities.buildExecuteResponseTypeProcessOutputs( ioValues );

    ExecuteResponseType value = OGCUtilities.buildExecuteResponseType( m_execute.getIdentifier(), status, m_execute.getDataInputs(), m_execute.getOutputDefinitions(), processOutputs, statusLocation, OGCUtilities.VERSION );
    JAXBElement<ExecuteResponseType> executeResponse = OGCUtilities.buildExecuteResponse( value );

    /* Marshall it into one XML string. */
    String xml = MarshallUtilities.marshall( executeResponse );

    /* Copy the execute response to this url. */
    VFSUtilities.copyStringToFileObject( xml, resultFile );
  }
}