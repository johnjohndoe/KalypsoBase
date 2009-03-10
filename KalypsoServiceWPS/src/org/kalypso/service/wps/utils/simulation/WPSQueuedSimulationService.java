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
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;

import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

import org.apache.commons.vfs.FileObject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.java.net.IUrlCatalog;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.internal.queued.ISimulationFactory;

/**
 * A straight forward {@link org.kalypso.services.calculation.service.ICalculationService}-Implementation. All jobs go
 * in one fifo-queue. Support parallel processing of jobs.
 * 
 * @author Gernot Belger (original), Holger Albert (changes for WPS)
 */
@SuppressWarnings("restriction")
public class WPSQueuedSimulationService
{
  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger( WPSQueuedSimulationService.class.getName() );

  /**
   * Debug on?
   */
  private final static boolean DO_DEBUG_TRACE = Boolean.valueOf( Platform.getDebugOption( "org.kalypso.simulation.core/debug/simulation/service" ) );

  static
  {
    if( !DO_DEBUG_TRACE )
      LOGGER.setUseParentHandlers( false );
  }

  /** Vector of {@link CalcJobThread}s */
  private final Map<String, WPSSimulationThread> m_threads = new HashMap<String, WPSSimulationThread>();

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
   * Stores the process description of each simulation.
   */
  private final Map<String, ProcessDescriptionType> m_descriptions = new HashMap<String, ProcessDescriptionType>();

  /**
   * The URL catalog.
   */
  private final IUrlCatalog m_catalog;

  /**
   * The temporary directory will be used by the jobs to copy their running data.
   */
  private final File m_tmpDir;

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
  public WPSQueuedSimulationService( final ISimulationFactory factory, final IUrlCatalog catalog, final int maxThreads, final long schedulingPeriod, final File tmpDir )
  {
    m_calcJobFactory = factory;
    m_catalog = catalog;
    m_maxThreads = maxThreads;
    m_schedulingPeriod = schedulingPeriod;
    m_tmpDir = tmpDir;

    m_resultSpace = FrameworkProperties.getProperty( "org.kalypso.service.wps.results" );
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
      LOGGER.info( "Start scheduling with period: " + m_schedulingPeriod + "ms" );

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

      LOGGER.info( "Stopped scheduling" );
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
      throw new SimulationException( "Cannot dispose a running job! Cancel it first.", null );

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
        throw new SimulationException( "Job not found: " + jobID, null );

      return thread;
    }
  }

  public WPSSimulationInfo startJob( final String typeID, final String description, final Execute execute, final ProcessDescriptionType processDescription ) throws SimulationException
  {
    WPSSimulationThread cjt = null;
    synchronized( m_threads )
    {
      /* Find an unused id. */
      int id = -1;
      int cnt = 0;
      while( true )
      {
        /* Check, if this particular id is free. */
        if( !m_threads.containsKey( "" + cnt ) )
        {
          /* If yes, take it. */
          id = cnt;
          break;
        }

        /* Otherwise increase the cnt and try again. */
        cnt++;
      }

      final ISimulation job = m_calcJobFactory.createJob( typeID );
      // TODO: should this temp dir not be inside the general wps tmp-dir?
      final File tmpdir = FileUtilities.createNewTempDir( "CalcJob-" + id + "-", m_tmpDir );
      tmpdir.deleteOnExit();

      cjt = new WPSSimulationThread( "" + id, description, typeID, job, execute, processDescription, tmpdir, m_resultSpace );
      m_threads.put( "" + id, cjt );

      /* Need the description and the result directory. */
      m_descriptions.put( typeID, processDescription );

      LOGGER.info( "Job waiting for scheduling: " + id );
    }

    startScheduling();

    return cjt == null ? null : cjt.getJobInfo();
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

      LOGGER.info( "Scheduler: Running jobs: " + runningCount );
      LOGGER.info( "Scheduler: Waiting jobs: " + waitingCount );

      if( waitingCount == 0 )
      {
        stopScheduling();
        return;
      }

      // Maximal einen Job auf einmal starten
      if( runningCount >= m_maxThreads )
      {
        LOGGER.info( "Scheduler: Maximum reached" );
        return;
      }

      // start one waiting job, if maximum is not reached
      for( final WPSSimulationThread cjt : m_threads.values() )
      {
        final WPSSimulationInfo jobInfo = cjt.getJobInfo();
        if( jobInfo.getState() == ISimulationConstants.STATE.WAITING )
        {
          LOGGER.info( "Scheduler: Starting job: " + jobInfo.getId() );
          cjt.start();
          return;
        }
      }
    }
  }

  /**
   * Falls dieses Objekt wirklich mal zerstört wird und wir es mitkriegen, dann alle restlichen Jobs zerstören und
   * insbesondere alle Dateien löschen
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

  private ProcessDescriptionType getProcessDescription( final String typeID )
  {
    final ProcessDescriptionType processDescription = m_descriptions.get( typeID );
    return processDescription;
  }

  public ProcessOutputs getRequiredInput( final String typeID )
  {
    return getProcessDescription( typeID ).getProcessOutputs();
  }

  public DataInputs getDeliveringResults( final String typeID )
  {
    return getProcessDescription( typeID ).getDataInputs();
  }

  public DataHandler getSchema( final String namespace )
  {
    final URL url = m_catalog.getURL( namespace );
    if( url == null )
      return null;

    return new DataHandler( new URLDataSource( url ) );
  }

  public long getSchemaValidity( final String namespace ) throws SimulationException
  {
    try
    {
      final URL url = m_catalog.getURL( namespace );
      if( url == null )
        throw new SimulationException( "Unknown schema namespace: " + namespace, null );

      final URLConnection connection = url.openConnection();
      return connection.getLastModified();
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      throw new SimulationException( "Unknown schema namespace: " + namespace, e );
    }
  }

  public String[] getSupportedSchemata( )
  {
    final Map<String, URL> catalog = m_catalog.getCatalog();
    final String[] namespaces = new String[catalog.size()];
    int count = 0;
    for( final Iterator<String> mapIt = catalog.keySet().iterator(); mapIt.hasNext(); )
      namespaces[count++] = mapIt.next();

    return namespaces;
  }

  public FileObject getResultDir( final String jobID ) throws SimulationException
  {
    return findJobThread( jobID ).getResultDir();
  }
}