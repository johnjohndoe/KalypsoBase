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
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.opengeospatial.wps.InputDescriptionType;

import org.apache.commons.vfs.FileObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.ogc.ExecuteMediator;
import org.kalypso.service.wps.utils.ogc.ProcessDescriptionMediator;
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
  private static Logger LOGGER = Logger.getLogger( WPSSimulationThread.class.getName() );

  /**
   * Debug on?
   */
  private static boolean DO_DEBUG_TRACE = Boolean.valueOf( Platform.getDebugOption( "org.kalypso.simulation.core/debug/simulation/service" ) ); //$NON-NLS-1$

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
   *          The id of the thread.
   * @param description
   *          A description.
   * @param typeID
   *          The id of the simulation.
   * @param job
   *          The simulation.
   * @param execute
   *          The execute request.
   * @param processDescription
   *          The process description.
   * @param tmpDir
   *          The temporary directory.
   * @param resultDir
   *          The job can put his results here.
   */
  public WPSSimulationThread( final ISimulation job, final ExecuteMediator executeMediator, final ProcessDescriptionMediator processDescriptionMediator, final String resultSpace ) throws SimulationException
  {
    super( "WPS-SimulationThread" ); //$NON-NLS-1$
    m_job = job;

    // TODO: should this temp dir not be inside the general wps tmp-dir?
    final long threadId = getId();
    m_tmpDir = FileUtilities.createNewTempDir( "CalcJob-" + threadId ); //$NON-NLS-1$
    m_tmpDir.deleteOnExit();

    final String typeID = executeMediator.getProcessId();
    final Map<String, Object> inputList = executeMediator.getInputList();
    Map<String, Object> inputDescriptions;
    try
    {
      inputDescriptions = processDescriptionMediator.getInputDescriptions( typeID );
    }
    catch( final CoreException e )
    {
      throw new SimulationException( "Could not get process description.", e ); //$NON-NLS-1$
    }

    /* Check, if the required input is available. */
    checkInputs( inputList, inputDescriptions );

    m_inputData = new WPSSimulationDataProvider( inputList );
    m_resultEater = new WPSSimulationResultEater( processDescriptionMediator, executeMediator, m_tmpDir, resultSpace );
    m_jobInfo = new WPSSimulationInfo( threadId, typeID, typeID, ISimulationConstants.STATE.WAITING, -1, m_resultEater );
  }

  /**
   * This function checks the input, if every needed input is available.
   */
  private void checkInputs( final Map<String, Object> inputList, final Map<String, Object> inputDescriptions ) throws SimulationException
  {
    for( final Entry<String, Object> entry : inputDescriptions.entrySet() )
    {
      final String id = entry.getKey();
      final InputDescriptionType inputDescription = (InputDescriptionType) entry.getValue();
      if( inputDescription.getMinimumOccurs().intValue() == 1 && inputList.get( id ) == null )
      {
        throw new SimulationException( Messages.getString("org.kalypso.service.wps.utils.simulation.WPSSimulationThread.0", id )); //$NON-NLS-1$
      }
    }
  }

  /**
   * Disposes everything.
   */
  public void dispose( )
  {
    if( KalypsoServiceWPSDebug.DO_NOT_DELETE_TEMP_FILES.isEnabled() )
    {
      /* Debug-Information. */

      Debug.println( "The tmp files in directory '" + m_tmpDir.getAbsolutePath() + "' will remain untouched ..." ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else
    {
      /* Debug-Information. */
      Debug.println( "Deleting tmp files in directory '" + m_tmpDir.getAbsolutePath() + "' ..." ); //$NON-NLS-1$ //$NON-NLS-2$

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
      LOGGER.info( "Calling run for ID: " + jobID ); //$NON-NLS-1$

      m_job.run( m_tmpDir, m_inputData, m_resultEater, m_jobInfo );

      LOGGER.info( "Run finished for ID: " + jobID ); //$NON-NLS-1$

      if( m_jobInfo.isCanceled() )
        LOGGER.info( "JOB exited because it was canceled: " + jobID ); //$NON-NLS-1$
      else
      {
        m_jobInfo.setState( ISimulationConstants.STATE.FINISHED );
        LOGGER.info( "JOB exited normally: " + jobID ); //$NON-NLS-1$
      }
    }
    catch( final Throwable t )
    {
      LOGGER.warning( "Simulation aborted with exception: " + jobID ); //$NON-NLS-1$
      m_jobInfo.setFinishText( "Simulation aborted with exception." ); //$NON-NLS-1$
      m_jobInfo.setFinishStatus( IStatus.ERROR );
      m_jobInfo.setException( t );
      m_jobInfo.setState( ISimulationConstants.STATE.ERROR );
    }
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