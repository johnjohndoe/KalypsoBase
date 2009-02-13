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
package org.kalypso.simulation.core.internal.queued;

import java.io.File;
import java.util.logging.Logger;

import javax.activation.DataHandler;

import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;
import org.kalypso.simulation.core.util.UnzippedJarSimulationDataProvider;

final class SimulationThread extends Thread
{
  private static final Logger LOGGER = Logger.getLogger( SimulationThread.class.getName() );

  private final static boolean DO_DEBUG_TRACE = Boolean.valueOf( Platform.getDebugOption( "org.kalypso.simulation.core/debug/simulation/service" ) );

  static
  {
    if( !DO_DEBUG_TRACE )
      LOGGER.setUseParentHandlers( false );
  }
  
  private final ISimulation m_job;

  private final SimulationInfo m_jobBean;

  private final ISimulationDataProvider m_inputData;

  private final ISimulationResultPacker m_resultPacker;

  private final File m_tmpDir;

  public SimulationThread( final String id, final String description, final String typeID, final ISimulation job, final ModelspecData modelspec, final DataHandler zipHandler, final SimulationDataPath[] input, final SimulationDataPath[] output, final File tmpDir ) throws SimulationException
  {
    m_job = job;
    m_tmpDir = tmpDir;

    m_jobBean = new SimulationInfo( "" + id, description, typeID, ISimulationConstants.STATE.WAITING, -1, "" );

    m_inputData = new UnzippedJarSimulationDataProvider( zipHandler, modelspec, input );

    m_resultPacker = new DefaultCalcResultEater( modelspec, output );

    modelspec.checkInput( m_inputData );
  }

  public void dispose( )
  {
    m_resultPacker.disposeFiles();
    FileUtilities.deleteRecursive( m_tmpDir );
    m_inputData.dispose();
  }

  public ISimulation getJob( )
  {
    return m_job;
  }

  public SimulationInfo getJobBean( )
  {
    m_jobBean.setCurrentResults( m_resultPacker.getCurrentResults() );

    return m_jobBean;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run( )
  {
    m_jobBean.setState( ISimulationConstants.STATE.RUNNING );

    final String jobID = m_jobBean.getId();
    try
    {
      LOGGER.info( "Calling run for ID: " + jobID );

      m_resultPacker.addFile( m_tmpDir );

      m_job.run( m_tmpDir, m_inputData, m_resultPacker, m_jobBean );

      LOGGER.info( "Run finished for ID: " + jobID );

      if( m_jobBean.isCanceled() )
        LOGGER.info( "JOB exited because it was canceled: " + jobID );
      else
      {
        m_jobBean.setState( ISimulationConstants.STATE.FINISHED );
        LOGGER.info( "JOB exited normally: " + jobID );
      }
    }
    catch( final Throwable t )
    {
      LOGGER.warning( "JOB exited with exception: " + jobID );
      t.printStackTrace();

      m_jobBean.setMessage( t.getLocalizedMessage() );
      m_jobBean.setState( ISimulationConstants.STATE.ERROR );
    }
  }

  public DataHandler packCurrentResults( ) throws SimulationException
  {
    return m_resultPacker.packCurrentResults();
  }

  public String[] getCurrentResults( )
  {
    return m_resultPacker.getCurrentResults();
  }
}