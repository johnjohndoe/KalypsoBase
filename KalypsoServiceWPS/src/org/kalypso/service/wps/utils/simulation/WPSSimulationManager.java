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

import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ProcessDescriptionType;

import org.apache.commons.vfs.FileObject;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.Activator;
import org.kalypso.service.wps.server.operations.DescribeProcessOperation;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.ogc.ExecuteMediator;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.internal.local.LocalSimulationFactory;
import org.kalypso.simulation.core.internal.local.LocalURLCatalog;

/**
 * Manages the simulations of the WPS.
 * 
 * @author Holger Albert
 */
public class WPSSimulationManager
{
  private static final String WPS_MAX_NUM_THREADS = "org.kalypso.service.wps.maxNumThreads";

  /**
   * The simulations will be handled here.
   */
  private WPSQueuedSimulationService m_service = null;

  /**
   * The instance of this class.
   */
  private static WPSSimulationManager INSTANCE = null;

  /**
   * The constructor.
   */
  private WPSSimulationManager( )
  {
    int maxNumThreads = 1;
    final String property = System.getProperty( WPS_MAX_NUM_THREADS );
    if( !"".equals( property ) )
    {
      try
      {
        maxNumThreads = Integer.parseInt( property );
      }
      catch( final NumberFormatException e )
      {
        Activator.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e, "%s does not specify a valid maximum number of threads: %s.", WPS_MAX_NUM_THREADS, property ) );
      }
    }
    Debug.println( "Setting maximum number of threads to " + maxNumThreads );
    m_service = new WPSQueuedSimulationService( new LocalSimulationFactory(), new LocalURLCatalog(), maxNumThreads, 2000 );
  }

  /**
   * Returns the instance of this manager. If none exists, it will be created. There can be only one instance at a time.
   * 
   * @return The instance of this manager.
   */
  public static WPSSimulationManager getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new WPSSimulationManager();

    return INSTANCE;
  }

  /**
   * This function starts a simulation.
   * 
   * @param typeID
   *          The ID of the simulation.
   * @param description
   *          A human readable description of this process.
   * @param execute
   *          The execute request of the WPS.
   */
  public WPSSimulationInfo startSimulation( final ExecuteMediator executeMediator ) throws OWSException
  {
    try
    {
      /* Get the process description. */
      final String typeID = executeMediator.getProcessId();
      final ProcessDescriptionType processDescription = DescribeProcessOperation.buildProcessDescriptionType( typeID );

      /* Start the job. */
      //TODO version 1.0
      final Execute executeV04 = executeMediator.getV04();
      final WPSSimulationInfo info = m_service.startJob( executeV04, processDescription );

      /* This thread will check for the status of the other one. */
      final WPSSimulationHandler handler = new WPSSimulationHandler( m_service, executeV04, info.getId() );
      handler.start();

      return info;
    }
    catch( final SimulationException e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "Could not start process." );
    }
  }

  /**
   * This function returns the file object, containing the path to the results for the given job.
   * 
   * @param jobID
   *          The id of the current running job.
   * @return The FileObject containing the path to the result space of this job.
   */
  public FileObject getResultDir( String jobID ) throws SimulationException
  {
    return m_service.getResultDir( jobID );
  }
}