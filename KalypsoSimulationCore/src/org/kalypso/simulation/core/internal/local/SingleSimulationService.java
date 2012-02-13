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
package org.kalypso.simulation.core.internal.local;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.activation.DataHandler;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.Platform;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.ISimulationService;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationDescription;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;
import org.kalypso.simulation.core.calccase.ISimulationFactory;
import org.kalypso.simulation.core.calccase.LocalSimulationFactory;
import org.kalypso.simulation.core.calccase.ModelspecData;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypso.simulation.core.internal.queued.SimulationThread;
import org.kalypso.simulation.core.util.SimulationUtilitites;

/**
 * A {@link ISimulationService} that only calculates a single simulation
 * 
 * @author kurzbach
 */
public class SingleSimulationService implements ISimulationService
{
  private static final Logger LOGGER = Logger.getLogger( SingleSimulationService.class.getName() );

  private final static boolean DO_DEBUG_TRACE = Boolean.valueOf( Platform.getDebugOption( "org.kalypso.simulation.core/debug/simulation/service" ) ); //$NON-NLS-1$

  static
  {
    if( !DO_DEBUG_TRACE )
      LOGGER.setUseParentHandlers( false );
  }

  private final ISimulationFactory m_calcJobFactory = new LocalSimulationFactory();

  private SimulationInfo m_simulationInfo;

  private SimulationThread m_simulationThread;

  private File m_tmpDir;

  public SingleSimulationService( final File tmpDir )
  {
    m_tmpDir = tmpDir;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.simulation.core.ISimulationService#getJobTypes()
   */
  @Override
  public synchronized final String[] getJobTypes( )
  {
    return m_calcJobFactory.getSupportedTypes();
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.simulation.core.ISimulationService#getJobs()
   */
  @Override
  public synchronized SimulationInfo[] getJobs( )
  {
    return new SimulationInfo[] { m_simulationInfo };
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.simulation.core.ISimulationService#getJob(java.lang.String)
   */
  @Override
  public SimulationInfo getJob( final String jobID )
  {
    return m_simulationInfo;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.simulation.core.ISimulationService#cancelJob(java.lang.String )
   */
  @Override
  public void cancelJob( final String jobID )
  {
    m_simulationInfo.cancel();
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.simulation.core.ISimulationService#disposeJob(java.lang.String )
   */
  @Override
  public void disposeJob( final String jobID )
  {
    m_simulationThread.dispose();
  }

  /**
   * @see org.kalypso.services.calculation.service.ICalculationService#startJob(java.lang.String, java.lang.String,
   *      javax.activation.DataHandler,
   *      org.kalypso.services.calculation.service.CalcJobClientBean[],org.kalypso.services.calculation.service.CalcJobClientBean[])
   */
  @Override
  public final SimulationInfo startJob( final String typeID, final String description, final DataHandler zipHandler, final SimulationDataPath[] input, final SimulationDataPath[] output ) throws SimulationException
  {
    final ModelspecData modelspec = getModelspec( typeID );

    final ISimulation job = m_calcJobFactory.createJob( typeID );

    if( m_tmpDir == null )
    {
      try
      {
        m_tmpDir = SimulationUtilitites.createSimulationTmpDir( typeID );
      }
      catch( final IOException e )
      {
        throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.local.SingleSimulationService.0" ), e ); //$NON-NLS-1$
      }
    }

    m_simulationThread = new SimulationThread( typeID, description, typeID, job, modelspec, zipHandler, input, output, m_tmpDir );

    m_simulationThread.run();

    m_simulationInfo = m_simulationThread.getJobBean();

    return m_simulationInfo;
  }

  @Override
  public void transferCurrentResults( final IContainer targetFolder, final String jobID ) throws SimulationException
  {
    m_simulationThread.transferCurrentResults( targetFolder );
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.simulation.core.ISimulationService#getCurrentResults(java .lang.String)
   */
  @Override
  public String[] getCurrentResults( final String jobID )
  {
    return m_simulationThread.getCurrentResults();
  }

  private ModelspecData getModelspec( final String typeID ) throws SimulationException
  {
    final ISimulation job = m_calcJobFactory.createJob( typeID );
    final URL modelspecURL = job.getSpezifikation();

    try
    {
      return new ModelspecData( modelspecURL );
    }
    catch( final IllegalArgumentException e )
    {
      e.printStackTrace();
      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.local.SingleSimulationService.2" ), e ); //$NON-NLS-1$
    }
  }

  @Override
  public SimulationDescription[] getRequiredInput( final String typeID ) throws SimulationException
  {
    return getModelspec( typeID ).getInput();
  }

  @Override
  public SimulationDescription[] getDeliveringResults( final String typeID ) throws SimulationException
  {
    return getModelspec( typeID ).getOutput();
  }
}