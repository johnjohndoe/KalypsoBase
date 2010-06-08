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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;

import org.eclipse.core.runtime.Platform;
import org.kalypso.contribs.java.net.IUrlCatalog;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.ISimulationService;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationDescription;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypso.simulation.core.util.SimulationUtilitites;

/**
 * A straight forward {@link org.kalypso.services.calculation.service.ICalculationService}-Implementation. All jobs go
 * in one fifo-queue. Support parallel processing of jobs.
 * 
 * @author Belger
 */
public class QueuedSimulationService implements ISimulationService
{
  private static final Logger LOGGER = Logger.getLogger( QueuedSimulationService.class.getName() );

  private final static boolean DO_DEBUG_TRACE = Boolean.valueOf( Platform.getDebugOption( "org.kalypso.simulation.core/debug/simulation/service" ) ); //$NON-NLS-1$

  static
  {
    if( !DO_DEBUG_TRACE )
    {
      LOGGER.setUseParentHandlers( false );
    }
  }

  /** Vector of {@link CalcJobThread}s */
  private final Vector<SimulationThread> m_threads = new Vector<SimulationThread>();

  private final ISimulationFactory m_calcJobFactory;

  private Timer m_timer = null;

  /** maximale Anzahl von parallel laufenden Job */
  private final int m_maxThreads;

  /** So oft (in ms) wird die queue nach wartenden Jobs durchsucht */
  private final long m_schedulingPeriod;

  private final Map<String, ModelspecData> m_modelspecMap = new HashMap<String, ModelspecData>();

  private final IUrlCatalog m_catalog;

  public QueuedSimulationService( final ISimulationFactory factory, final IUrlCatalog catalog, final int maxThreads, final long schedulingPeriod )
  {
    m_calcJobFactory = factory;
    m_catalog = catalog;
    m_maxThreads = maxThreads;
    m_schedulingPeriod = schedulingPeriod;
  }

  /**
   * @see org.kalypso.services.IKalypsoService#getServiceVersion()
   */
  public int getServiceVersion( )
  {
    return 0;
  }

  @Override
  public synchronized final String[] getJobTypes( )
  {
    return m_calcJobFactory.getSupportedTypes();
  }

  @Override
  public synchronized SimulationInfo[] getJobs( )
  {
    synchronized( m_threads )
    {
      final SimulationInfo[] jobBeans = new SimulationInfo[m_threads.size()];
      int count = 0;

      for( final Iterator<SimulationThread> jIt = m_threads.iterator(); jIt.hasNext(); count++ )
      {
        final SimulationThread cjt = jIt.next();
        jobBeans[count] = cjt.getJobBean();
      }

      return jobBeans;
    }
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.service.ICalculationService#getJob(java.lang.String)
   */
  @Override
  public SimulationInfo getJob( final String jobID ) throws SimulationException
  {
    return findJobThread( jobID ).getJobBean();
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

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#cancelJob(java.lang.String)
   */
  @Override
  public void cancelJob( final String jobID ) throws SimulationException
  {
    findJobThread( jobID ).getJobBean().cancel();
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#disposeJob(java.lang.String)
   */
  @Override
  public void disposeJob( final String jobID ) throws SimulationException
  {
    final SimulationThread cjt = findJobThread( jobID );

    if( cjt.isAlive() )
      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.QueuedSimulationService.0" ), null ); //$NON-NLS-1$

    cjt.dispose();

    synchronized( m_threads )
    {
      m_threads.remove( cjt );
      if( m_threads.size() == 0 )
      {
        stopScheduling();
      }
    }
  }

  private SimulationThread findJobThread( final String jobID ) throws SimulationException
  {
    synchronized( m_threads )
    {
      for( final Object element : m_threads )
      {
        final SimulationThread cjt = (SimulationThread) element;

        if( cjt.getJobBean().getId().equals( jobID ) )
          return cjt;
      }
    }

    throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.QueuedSimulationService.1" ) + jobID, null ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#startJob(java.lang.String, java.lang.String,
   *      javax.activation.DataHandler,
   *      org.kalypso.services.calculation.service.CalcJobClientBean[],org.kalypso.services.calculation.service.CalcJobClientBean[])
   */
  @Override
  public final SimulationInfo startJob( final String typeID, final String description, final DataHandler zipHandler, final SimulationDataPath[] input, final SimulationDataPath[] output ) throws SimulationException
  {
    /*
     * TODO @Gernot I think this is wrong, because the Vector changes the indexes of its elements on adding and removing
     * one element. Problem here is: Three threads are running, (ids 0, 1, 2) and are still in the list (indexes 0, 1,
     * 2). Adding a new one would be ok, getting index 3 and id 3. But if lets say, the thread with the id 1 finishes
     * and is removed from the list, the Thread with id 2 will get the new index 1. Now, starting a new thread will give
     * it the index 2 and the id 2. Now we have two threads with the same id. In the WPSQueuedSimulationService I tried
     * to solve this and it seems to be working. If you would like (and if you think it is the right solution) we could
     * change it here, too, if this class should be continued to be used for local calculations with perhaps more than
     * one thread running.
     */
    SimulationThread cjt = null;
    synchronized( m_threads )
    {
      // eine unbenutzte ID finden
      int id = -1;
      for( int i = 0; i < m_threads.size(); i++ )
      {
        if( m_threads.get( i ) == null )
        {
          id = i;
          break;
        }
      }
      if( id == -1 )
      {
        id = m_threads.size();
      }

      File tmpdir;
      try
      {
        tmpdir = SimulationUtilitites.createSimulationTmpDir( "" + id ); //$NON-NLS-1$
      }
      catch( final IOException e )
      {
        e.printStackTrace();
        throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.QueuedSimulationService.2" ), e ); //$NON-NLS-1$
      }

      final ModelspecData modelspec = getModelspec( typeID );

      final ISimulation job = m_calcJobFactory.createJob( typeID );

      cjt = new SimulationThread( "" + id, description, typeID, job, modelspec, zipHandler, input, output, tmpdir ); //$NON-NLS-1$

      if( id == m_threads.size() )
      {
        m_threads.add( cjt );
      }
      else
      {
        m_threads.set( id, cjt );
      }

      LOGGER.info( "Job waiting for scheduling: " + id ); //$NON-NLS-1$
    }

    startScheduling();

    return cjt == null ? null : cjt.getJobBean();
  }

  public void scheduleJobs( )
  {
    synchronized( m_threads )
    {
      // count running thread
      int runningCount = 0;
      int waitingCount = 0;
      for( final Object element : m_threads )
      {
        final SimulationThread cjt = (SimulationThread) element;
        if( cjt.isAlive() )
        {
          runningCount++;
        }

        final SimulationInfo jobBean = cjt.getJobBean();
        if( jobBean.getState() == ISimulationConstants.STATE.WAITING )
        {
          waitingCount++;
        }
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
      for( final Object element : m_threads )
      {
        final SimulationThread cjt = (SimulationThread) element;

        final SimulationInfo jobBean = cjt.getJobBean();
        if( jobBean.getState() == ISimulationConstants.STATE.WAITING )
        {
          LOGGER.info( "Scheduler: Starting job: " + jobBean.getId() ); //$NON-NLS-1$
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
      for( final Object element : m_threads )
      {
        final SimulationThread cjt = (SimulationThread) element;
        final SimulationInfo jobBean = cjt.getJobBean();
        disposeJob( jobBean.getId() );
      }

    }

    super.finalize();
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.service.ICalculationService#transferCurrentResults(java.lang.String)
   */
  @Override
  public void transferCurrentResults( final File targetFolder, final String jobID ) throws SimulationException
  {
    final SimulationThread thread = findJobThread( jobID );
    thread.transferCurrentResults( targetFolder );
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#getCurrentResults(java.lang.String)
   */
  @Override
  public String[] getCurrentResults( final String jobID ) throws SimulationException
  {
    final SimulationThread thread = findJobThread( jobID );
    return thread.getCurrentResults();
  }

  private ModelspecData getModelspec( final String typeID ) throws SimulationException
  {
    ModelspecData data = m_modelspecMap.get( typeID );
    if( data != null )
      return data;

    final ISimulation job = m_calcJobFactory.createJob( typeID );
    if( job == null )
      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.QueuedSimulationService.3", typeID ) ); //$NON-NLS-1$

    try
    {
      final URL modelspecURL = job.getSpezifikation();
      data = new ModelspecData( modelspecURL );
    }
    catch( final IllegalArgumentException e )
    {
      e.printStackTrace();
      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.QueuedSimulationService.5" ), e ); //$NON-NLS-1$
    }

    m_modelspecMap.put( typeID, data );

    return data;
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.service.ICalculationService#getRequiredInput(java.lang.String)
   */
  @Override
  public SimulationDescription[] getRequiredInput( final String typeID ) throws SimulationException
  {
    return getModelspec( typeID ).getInput();
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.service.ICalculationService#getDeliveringResults(java.lang.String)
   */
  @Override
  public SimulationDescription[] getDeliveringResults( final String typeID ) throws SimulationException
  {
    return getModelspec( typeID ).getOutput();
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#getSchema(java.lang.String)
   */
  @Override
  public DataHandler getSchema( final String namespace )
  {
    final URL url = m_catalog.getURL( namespace );
    if( url == null )
      return null;

    return new DataHandler( new URLDataSource( url ) );
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#getSchemaValidity(java.lang.String)
   */
  @Override
  public long getSchemaValidity( final String namespace ) throws SimulationException
  {
    try
    {
      final URL url = m_catalog.getURL( namespace );
      if( url == null )
        throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.QueuedSimulationService.6" ) + namespace, null ); //$NON-NLS-1$

      final URLConnection connection = url.openConnection();
      return connection.getLastModified();
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.QueuedSimulationService.7" ) + namespace, e ); //$NON-NLS-1$
    }
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#getSupportedSchemata()
   */
  @Override
  public String[] getSupportedSchemata( )
  {
    final Map<String, URL> catalog = m_catalog.getCatalog();
    final String[] namespaces = new String[catalog.size()];
    int count = 0;
    for( final String string : catalog.keySet() )
    {
      namespaces[count++] = string;
    }

    return namespaces;
  }
}