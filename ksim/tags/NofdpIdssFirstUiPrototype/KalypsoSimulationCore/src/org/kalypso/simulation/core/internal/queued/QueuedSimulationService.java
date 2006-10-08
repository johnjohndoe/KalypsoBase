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
import javax.xml.bind.JAXBException;

import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.java.net.IUrlCatalog;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.ISimulationService;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationDescription;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;

/**
 * A straight forward {@link org.kalypso.services.calculation.service.ICalculationService}-Implementation. All jobs go
 * in one fifo-queue. Support parallel processing of jobs.
 * 
 * @author Belger
 */
public class QueuedSimulationService implements ISimulationService
{
  private static final Logger LOGGER = Logger.getLogger( QueuedSimulationService.class.getName() );

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

  private final File m_tmpDir;

  public QueuedSimulationService( final ISimulationFactory factory, final IUrlCatalog catalog, final int maxThreads, final long schedulingPeriod, final File tmpDir )
  {
    m_calcJobFactory = factory;
    m_catalog = catalog;
    m_maxThreads = maxThreads;
    m_schedulingPeriod = schedulingPeriod;
    m_tmpDir = tmpDir;
  }

  /**
   * @see org.kalypso.services.IKalypsoService#getServiceVersion()
   */
  public int getServiceVersion( )
  {
    return 0;
  }

  public synchronized final String[] getJobTypes( )
  {
    return m_calcJobFactory.getSupportedTypes();
  }

  public synchronized SimulationInfo[] getJobs( )
  {
    synchronized( m_threads )
    {
      final SimulationInfo[] jobBeans = new SimulationInfo[m_threads.size()];
      int count = 0;

      for( final Iterator jIt = m_threads.iterator(); jIt.hasNext(); count++ )
      {
        final SimulationThread cjt = (SimulationThread) jIt.next();
        jobBeans[count] = cjt.getJobBean();
      }

      return jobBeans;
    }
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.service.ICalculationService#getJob(java.lang.String)
   */
  public SimulationInfo getJob( final String jobID ) throws SimulationException
  {
    return findJobThread( jobID ).getJobBean();
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

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#cancelJob(java.lang.String)
   */
  public void cancelJob( final String jobID ) throws SimulationException
  {
    findJobThread( jobID ).getJobBean().cancel();
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#disposeJob(java.lang.String)
   */
  public void disposeJob( final String jobID ) throws SimulationException
  {
    final SimulationThread cjt = findJobThread( jobID );

    if( cjt.isAlive() )
      throw new SimulationException( "Cannot dispose a running job! Cancel it first.", null );

    cjt.dispose();

    synchronized( m_threads )
    {
      m_threads.remove( cjt );
      if( m_threads.size() == 0 )
        stopScheduling();
    }
  }

  private SimulationThread findJobThread( final String jobID ) throws SimulationException
  {
    synchronized( m_threads )
    {
      for( final Iterator jIt = m_threads.iterator(); jIt.hasNext(); )
      {
        final SimulationThread cjt = (SimulationThread) jIt.next();

        if( cjt.getJobBean().getId().equals( jobID ) )
          return cjt;
      }
    }

    throw new SimulationException( "Job not found: " + jobID, null );
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#startJob(java.lang.String, java.lang.String,
   *      javax.activation.DataHandler,
   *      org.kalypso.services.calculation.service.CalcJobClientBean[],org.kalypso.services.calculation.service.CalcJobClientBean[])
   */
  public final SimulationInfo startJob( final String typeID, final String description, final DataHandler zipHandler, final SimulationDataPath[] input, final SimulationDataPath[] output ) throws SimulationException
  {
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
        id = m_threads.size();

      final ModelspecData modelspec = getModelspec( typeID );

      final ISimulation job = m_calcJobFactory.createJob( typeID );

      final File tmpdir = FileUtilities.createNewTempDir( "CalcJob-" + id + "-", m_tmpDir );
      tmpdir.deleteOnExit();

      cjt = new SimulationThread( "" + id, description, typeID, job, modelspec, zipHandler, input, output, tmpdir );

      if( id == m_threads.size() )
        m_threads.add( cjt );
      else
        m_threads.set( id, cjt );

      LOGGER.info( "Job waiting for scheduling: " + id );
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
      for( final Iterator jIt = m_threads.iterator(); jIt.hasNext(); )
      {
        final SimulationThread cjt = (SimulationThread) jIt.next();
        if( cjt.isAlive() )
          runningCount++;

        final SimulationInfo jobBean = cjt.getJobBean();
        if( jobBean.getState() == ISimulationConstants.STATE.WAITING )
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
      for( final Iterator jIt = m_threads.iterator(); jIt.hasNext(); )
      {
        final SimulationThread cjt = (SimulationThread) jIt.next();

        final SimulationInfo jobBean = cjt.getJobBean();
        if( jobBean.getState() == ISimulationConstants.STATE.WAITING )
        {
          LOGGER.info( "Scheduler: Starting job: " + jobBean.getId() );
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
      for( final Iterator iter = m_threads.iterator(); iter.hasNext(); )
      {
        final SimulationThread cjt = (SimulationThread) iter.next();
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
  public DataHandler transferCurrentResults( final String jobID ) throws SimulationException
  {
    final SimulationThread thread = findJobThread( jobID );
    return thread.packCurrentResults();
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#getCurrentResults(java.lang.String)
   */
  public String[] getCurrentResults( String jobID ) throws SimulationException
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
    final URL modelspecURL = job.getSpezifikation();

    try
    {
      data = new ModelspecData( modelspecURL, KalypsoSimulationCoreJaxb.JC.createUnmarshaller() );
    }
    catch( final JAXBException e )
    {
      e.printStackTrace();
      throw new SimulationException( "Unable to initialize jaxb unmarshaller", e );
    }
    catch( IllegalArgumentException e )
    {
      e.printStackTrace();
      throw new SimulationException( "Error while reading sim-service specs", e );
    }

    m_modelspecMap.put( typeID, data );

    return data;
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.service.ICalculationService#getRequiredInput(java.lang.String)
   */
  public SimulationDescription[] getRequiredInput( final String typeID ) throws SimulationException
  {
    return getModelspec( typeID ).getInput();
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.service.ICalculationService#getDeliveringResults(java.lang.String)
   */
  public SimulationDescription[] getDeliveringResults( final String typeID ) throws SimulationException
  {
    return getModelspec( typeID ).getOutput();
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#getSchema(java.lang.String)
   */
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

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#getSupportedSchemata()
   */
  public String[] getSupportedSchemata( )
  {
    final Map catalog = m_catalog.getCatalog();
    final String[] namespaces = new String[catalog.size()];
    int count = 0;
    for( final Iterator mapIt = catalog.keySet().iterator(); mapIt.hasNext(); )
      namespaces[count++] = (String) mapIt.next();

    return namespaces;
  }
}