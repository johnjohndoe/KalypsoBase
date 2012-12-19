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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.runtime.Platform;
import org.kalypso.contribs.eclipse.osgi.FrameworkUtilities;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;
import org.kalypso.service.wps.utils.WPSUtilities.WPS_VERSION;
import org.kalypso.service.wps.utils.ogc.ExecuteMediator;
import org.kalypso.service.wps.utils.ogc.ProcessDescriptionMediator;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.calccase.ISimulationFactory;

/**
 * A straight forward {@link org.kalypso.services.calculation.service.ICalculationService}-Implementation. All jobs go
 * in one fifo-queue. Support parallel processing of jobs.
 *
 * @author Gernot Belger (original), Holger Albert (changes for WPS)
 */
public class WPSQueuedSimulationService
{
  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger( WPSQueuedSimulationService.class.getName() );

  /**
   * Debug on?
   */
  private final static boolean DO_DEBUG_TRACE = Boolean.valueOf( Platform.getDebugOption( "org.kalypso.simulation.core/debug/simulation/service" ) ); //$NON-NLS-1$

  static
  {
    if( !DO_DEBUG_TRACE )
      LOGGER.setUseParentHandlers( false );
  }

  /** Map of {@link WPSSimulationThread}s */
  private final Map<String, WPSSimulationThread> m_threads = new HashMap<>();

  /**
   * Retrieves the simulation.
   */
  private final ISimulationFactory m_calcJobFactory;

  /**
   * After this timer has finished, the job is startet.
   */
  private Timer m_timer = null;

  /**
   * Maximal count of parallel running jobs.
   */
  private final int m_maxThreads;

  /**
   * Time (in ms) where the queue is searched for waiting jobs.
   */
  private final long m_schedulingPeriod;

  /**
   * URL to space, where the jobs can put their results, so that the client can read them.
   */
  private final String m_resultSpace;

  /**
   * The constructor.
   *
   * @param factory
   *          Retrieves the simulation.
   * @param catalog
   *          The URL catalog.
   * @param maxThreads
   *          Maximal count of parallel running jobs.
   * @param schedulingPeriod
   *          Time (in ms) where the queue is searched for waiting jobs.
   * @param tmpDir
   *          The temporary directory will be used by the jobs to copy their running data.
   */
  public WPSQueuedSimulationService( final ISimulationFactory factory, final int maxThreads, final long schedulingPeriod )
  {
    m_calcJobFactory = factory;
    m_maxThreads = maxThreads;
    m_schedulingPeriod = schedulingPeriod;

    m_resultSpace = FrameworkUtilities.getProperty( "org.kalypso.service.wps.results", null ); //$NON-NLS-1$
  }

  public int getServiceVersion( )
  {
    return 0;
  }

  public synchronized final String[] getJobTypes( )
  {
    return m_calcJobFactory.getSupportedTypes();
  }

  public synchronized WPSSimulationInfo[] getJobs( )
  {
    synchronized( m_threads )
    {
      final WPSSimulationInfo[] jobInfos = new WPSSimulationInfo[m_threads.size()];
      int count = 0;

      for( final Iterator<WPSSimulationThread> jIt = m_threads.values().iterator(); jIt.hasNext(); count++ )
      {
        final WPSSimulationThread cjt = jIt.next();
        jobInfos[count] = cjt.getJobInfo();
      }

      return jobInfos;
    }
  }

  public WPSSimulationInfo getJob( final String jobID ) throws SimulationException
  {
    return findJobThread( jobID ).getJobInfo();
  }

  private void startScheduling( )
  {
    if( m_timer == null )
    {
      LOGGER.info( "Start scheduling with period: " + m_schedulingPeriod + "ms" ); //$NON-NLS-1$ //$NON-NLS-2$

      m_timer = new Timer();
      final TimerTask timerTask = new TimerTask()
      {
        @Override
        public void run( )
        {
          scheduleJobs();
        }
      };
      m_timer.schedule( timerTask, m_schedulingPeriod, m_schedulingPeriod );
    }
  }

  private void stopScheduling( )
  {
    if( m_timer != null )
    {
      m_timer.cancel();
      m_timer = null;

      LOGGER.info( "Stopped scheduling" ); //$NON-NLS-1$
    }
  }

  public void cancelJob( final String jobID ) throws SimulationException
  {
    findJobThread( jobID ).getJobInfo().cancel();
  }

  public void disposeJob( final String jobID ) throws SimulationException
  {
    final WPSSimulationThread cjt = findJobThread( jobID );

    if( cjt.isAlive() )
      throw new SimulationException( Messages.getString( "org.kalypso.service.wps.utils.simulation.WPSQueuedSimulationService.0" ), null ); //$NON-NLS-1$

    cjt.dispose();

    synchronized( m_threads )
    {
      m_threads.remove( jobID );
      if( m_threads.size() == 0 )
        stopScheduling();
    }
  }

  private WPSSimulationThread findJobThread( final String jobID ) throws SimulationException
  {
    synchronized( m_threads )
    {
      final WPSSimulationThread thread = m_threads.get( jobID );
      if( thread == null )
        throw new SimulationException( "Job not found: " + jobID, null ); //$NON-NLS-1$

      return thread;
    }
  }

  public WPSSimulationInfo startJob( final ExecuteMediator executeMediator ) throws SimulationException
  {
    final String typeID = executeMediator.getProcessId();
    if( typeID == null || typeID.length() == 0 )
    {
      KalypsoServiceWPSDebug.DEBUG.printf( "Missing parameter Identifier!\n" ); //$NON-NLS-1$
      throw new SimulationException( "Process identifier is missing!" ); //$NON-NLS-1$
    }

    final WPS_VERSION version = executeMediator.getVersion();
    final ProcessDescriptionMediator processDescriptionMediator = new ProcessDescriptionMediator( version );

    WPSSimulationThread cjt = null;
    synchronized( m_threads )
    {
      final ISimulation job = m_calcJobFactory.createJob( typeID );

      cjt = new WPSSimulationThread( job, executeMediator, processDescriptionMediator, m_resultSpace );

      final String threadId = Long.toString( cjt.getId() );
      m_threads.put( threadId, cjt );

      LOGGER.info( "Job waiting for scheduling: " + threadId ); //$NON-NLS-1$
    }

    startScheduling();

    return cjt.getJobInfo();
  }

  public void scheduleJobs( )
  {
    synchronized( m_threads )
    {
      // count running thread
      int runningCount = 0;
      int waitingCount = 0;
      for( final WPSSimulationThread cjt : m_threads.values() )
      {
        if( cjt.isAlive() )
          runningCount++;

        final WPSSimulationInfo jobInfo = cjt.getJobInfo();
        if( jobInfo.getState() == ISimulationConstants.STATE.WAITING )
          waitingCount++;
      }

      LOGGER.info( "Scheduler: Running jobs: " + runningCount ); //$NON-NLS-1$
      LOGGER.info( "Scheduler: Waiting jobs: " + waitingCount ); //$NON-NLS-1$

      if( waitingCount == 0 )
      {
        stopScheduling();
        return;
      }

      // Maximal einen Job auf einmal starten
      if( runningCount >= m_maxThreads )
      {
        LOGGER.info( "Scheduler: Maximum reached" ); //$NON-NLS-1$
        return;
      }

      // start one waiting job, if maximum is not reached
      for( final WPSSimulationThread cjt : m_threads.values() )
      {
        final WPSSimulationInfo jobInfo = cjt.getJobInfo();
        if( jobInfo.getState() == ISimulationConstants.STATE.WAITING )
        {
          LOGGER.info( "Scheduler: Starting job: " + jobInfo.getId() ); //$NON-NLS-1$
          jobInfo.setState( ISimulationConstants.STATE.RUNNING );
          cjt.start();
          return;
        }
      }
    }
  }

  /**
   * Falls dieses Objekt wirklich mal zerst�rt wird und wir es mitkriegen, dann alle restlichen Jobs zerst�ren und
   * insbesondere alle Dateien l�schen
   *
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize( ) throws Throwable
  {
    synchronized( m_threads )
    {
      for( final WPSSimulationThread cjt : m_threads.values() )
      {
        final WPSSimulationInfo jobInfo = cjt.getJobInfo();
        disposeJob( jobInfo.getId() );
      }
    }
    super.finalize();
  }

  public FileObject getResultDir( final String jobID ) throws SimulationException
  {
    return findJobThread( jobID ).getResultDir();
  }
}