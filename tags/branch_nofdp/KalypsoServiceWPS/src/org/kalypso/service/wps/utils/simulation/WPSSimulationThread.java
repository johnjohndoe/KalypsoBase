/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.service.wps.utils.simulation;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;

import org.apache.commons.vfs.FileObject;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationException;

/**
 * This thread executes a simulation in the WPS service.
 * 
 * @author Gernot Belger (original), Holger Albert (changes)
 */
public class WPSSimulationThread extends Thread
{
  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger( WPSSimulationThread.class.getName() );

  /**
   * Debug on?
   */
  private final static boolean DO_DEBUG_TRACE = Boolean.valueOf( Platform.getDebugOption( "org.kalypso.simulation.core/debug/simulation/service" ) );

  static
  {
    if( !DO_DEBUG_TRACE )
      LOGGER.setUseParentHandlers( false );
  }

  /**
   * The simulation.
   */
  private final ISimulation m_job;

  /**
   * The info about the running simulation.
   */
  private final WPSSimulationInfo m_jobInfo;

  /**
   * The input data provider.
   */
  private final ISimulationDataProvider m_inputData;

  /**
   * The results will go in here.
   */
  private final WPSSimulationResultEater m_resultEater;

  /**
   * The temporary directory.
   */
  private final File m_tmpDir;

  /**
   * The constructor.
   * 
   * @param id
   *            The id of the thread.
   * @param description
   *            A description.
   * @param typeID
   *            The id of the simulation.
   * @param job
   *            The simulation.
   * @param execute
   *            The execute request.
   * @param processDescription
   *            The process description.
   * @param tmpDir
   *            The temporary directory.
   * @param resultDir
   *            The job can put his results here.
   */
  public WPSSimulationThread( final String id, final String description, final String typeID, final ISimulation job, Execute execute, ProcessDescriptionType processDescription, final File tmpDir, final FileObject resultDir ) throws SimulationException
  {
    m_job = job;
    m_tmpDir = tmpDir;

    m_jobInfo = new WPSSimulationInfo( "" + id, description, typeID, ISimulationConstants.STATE.WAITING, -1, "" );
    m_inputData = new WPSSimulationDataProvider( execute );
    m_resultEater = new WPSSimulationResultEater( processDescription, execute, tmpDir, resultDir );

    /* Check, if the required input is available. */
    checkInput( execute, processDescription );
  }

  /**
   * This function checks the input, if every needed input is available.
   * 
   * @param execute
   *            The execute request containing the input data.<br>
   *            (Ok, it contains also info about the expected output data, but that is not needed now.)
   * @param processDescription
   *            The processDescription containing the output data.<br>
   *            (Ok, it contains also info about the output data, but that is not needed now.)
   */
  private void checkInput( Execute execute, ProcessDescriptionType processDescription ) throws SimulationException
  {
    DataInputsType dataInputsExecute = execute.getDataInputs();
    DataInputs dataInputsProcessDescription = processDescription.getDataInputs();

    List<IOValueType> ioValues = dataInputsExecute.getInput();
    List<InputDescriptionType> inputDescriptions = dataInputsProcessDescription.getInput();

    for( int i = 0; i < inputDescriptions.size(); i++ )
    {
      InputDescriptionType inputDescription = inputDescriptions.get( i );

      IOValueType ioValue = findInput( inputDescription, ioValues );
      if( ioValue == null )
      {
        /* Ooops, one input is missing, hopefully it was an optional one. */
        if( inputDescription.getMinimumOccurs().intValue() == 1 )
        {
          /* No, it was mandatory. */
          throw new SimulationException( "Missing input for ID: " + inputDescription.getIdentifier().getValue(), null );
        }

        /* It was an optional one. */
        continue;
      }

      /* Input is here, everything is allright. */
    }
  }

  /**
   * Tries to find a input in this list.
   * 
   * @param inputDescription
   *            The description of one input.
   * @param ioValues
   *            The input list.
   * @return The input in the input list of the execute request. If it is not available it returns null.
   */
  private IOValueType findInput( InputDescriptionType inputDescription, List<IOValueType> ioValues )
  {
    String value = inputDescription.getIdentifier().getValue();
    for( IOValueType ioValue : ioValues )
    {
      if( value.equals( ioValue.getIdentifier().getValue() ) )
        return ioValue;
    }

    return null;
  }

  /**
   * Disposes everything.
   */
  public void dispose( )
  {
    if( Debug.doNotDeleteTmpFiles() )
    {
      /* Debug-Information. */
      Debug.println( "The tmp files in directory '" + m_tmpDir.getAbsolutePath() + "' will remain untouched ..." );
    }
    else
    {
      /* Debug-Information. */
      Debug.println( "Deleting tmp files in directory '" + m_tmpDir.getAbsolutePath() + "' ..." );

      /* Delete the tmp-data. */
      FileUtilities.deleteRecursive( m_tmpDir );
    }

    m_inputData.dispose();
    m_resultEater.dispose();
  }

  public ISimulation getJob( )
  {
    return m_job;
  }

  public WPSSimulationInfo getJobInfo( )
  {
    m_jobInfo.setCurrentResults( m_resultEater.getCurrentResults() );

    return m_jobInfo;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run( )
  {
    m_jobInfo.setState( ISimulationConstants.STATE.RUNNING );

    final String jobID = m_jobInfo.getId();
    try
    {
      LOGGER.info( "Calling run for ID: " + jobID );

      m_job.run( m_tmpDir, m_inputData, m_resultEater, m_jobInfo );

      LOGGER.info( "Run finished for ID: " + jobID );

      if( m_jobInfo.isCanceled() )
        LOGGER.info( "JOB exited because it was canceled: " + jobID );
      else
      {
        m_jobInfo.setState( ISimulationConstants.STATE.FINISHED );
        LOGGER.info( "JOB exited normally: " + jobID );
      }
    }
    catch( final Throwable t )
    {
      LOGGER.warning( "JOB exited with exception: " + jobID );
      t.printStackTrace();

      m_jobInfo.setFinishText( t.getLocalizedMessage() );
      m_jobInfo.setState( ISimulationConstants.STATE.ERROR );
    }
  }

  public List<IOValueType> copyCurrentResults( ) throws SimulationException
  {
    return m_resultEater.copyCurrentResults();
  }

  public String[] getCurrentResults( )
  {
    return m_resultEater.getCurrentResults();
  }

  /**
   * This function returns the result dir of its result eater.
   * 
   * @return The result dir.
   */
  public FileObject getResultDir( )
  {
    return m_resultEater.getResultDir();
  }
}