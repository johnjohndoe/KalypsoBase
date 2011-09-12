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
package org.kalypso.simulation.core.internal.local;

import javax.activation.DataHandler;

import org.eclipse.core.resources.IContainer;
import org.kalypso.simulation.core.ISimulationService;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationDescription;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;
import org.kalypso.simulation.core.internal.queued.QueuedSimulationService;

/**
 * This implementation of a {@link org.kalypso.simulation.core.ISimulationService} let the simulations run locally.
 * <p>
 * The information, which simulations can be run is gathered from the extension registry.
 * </p>
 * 
 * @see org.kalypso.simulation.core.internal.local.LocalSimulationFactory
 * @author Belger
 */
public class LocalSimulationService implements ISimulationService
{
  private final ISimulationService m_service;

  private final LocalSimulationFactory m_simulationFactory;

  private final LocalURLCatalog m_urlCatalog;

  public LocalSimulationService( )
  {
    m_simulationFactory = new LocalSimulationFactory();
    m_urlCatalog = new LocalURLCatalog();
    m_service = new QueuedSimulationService( m_simulationFactory, m_urlCatalog, 2, 2000 );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#cancelJob(java.lang.String)
   */
  @Override
  public void cancelJob( final String jobID ) throws SimulationException
  {
    m_service.cancelJob( jobID );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#disposeJob(java.lang.String)
   */
  @Override
  public void disposeJob( final String jobID ) throws SimulationException
  {
    m_service.disposeJob( jobID );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getCurrentResults(java.lang.String)
   */
  @Override
  public String[] getCurrentResults( final String jobID ) throws SimulationException
  {
    return m_service.getCurrentResults( jobID );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getDeliveringResults(java.lang.String)
   */
  @Override
  public SimulationDescription[] getDeliveringResults( final String typeID ) throws SimulationException
  {
    return m_service.getDeliveringResults( typeID );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getJob(java.lang.String)
   */
  @Override
  public SimulationInfo getJob( final String jobID ) throws SimulationException
  {
    return m_service.getJob( jobID );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getJobs()
   */
  @Override
  public SimulationInfo[] getJobs( ) throws SimulationException
  {
    return m_service.getJobs();
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getJobTypes()
   */
  @Override
  public String[] getJobTypes( ) throws SimulationException
  {
    return m_service.getJobTypes();
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getRequiredInput(java.lang.String)
   */
  @Override
  public SimulationDescription[] getRequiredInput( final String typeID ) throws SimulationException
  {
    return m_service.getRequiredInput( typeID );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getSchema(java.lang.String)
   */
  @Override
  public DataHandler getSchema( final String namespace ) throws SimulationException
  {
    return m_service.getSchema( namespace );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getSchemaValidity(java.lang.String)
   */
  @Override
  public long getSchemaValidity( final String namespace ) throws SimulationException
  {
    return m_service.getSchemaValidity( namespace );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#getSupportedSchemata()
   */
  @Override
  public String[] getSupportedSchemata( ) throws SimulationException
  {
    return m_service.getSupportedSchemata();
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationService#startJob(java.lang.String, java.lang.String,
   *      javax.activation.DataHandler, org.kalypso.simulation.core.SimulationDataPath[],
   *      org.kalypso.simulation.core.SimulationDataPath[])
   */
  @Override
  public SimulationInfo startJob( final String typeID, final String description, final DataHandler zipHandler, final SimulationDataPath[] input, final SimulationDataPath[] output ) throws SimulationException
  {
    return m_service.startJob( typeID, description, zipHandler, input, output );
  }

  @Override
  public void transferCurrentResults( final IContainer targetFolder, final String jobID ) throws SimulationException
  {
    m_service.transferCurrentResults( targetFolder, jobID );
  }

}
