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
package org.kalypso.service.wps.utils;

import javax.xml.bind.JAXBException;

import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ProcessDescriptionType;

import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.server.operations.DescribeProcess;
import org.kalypso.service.wps.utils.simulation.WPSQueuedSimulationService;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.internal.local.LocalSimulationFactory;
import org.kalypso.simulation.core.internal.local.LocalURLCatalog;

/**
 * Manages the simulations of the WPS.
 * 
 * @author Holger Albert
 */
public class SimulationManager
{
  /**
   * The simulations will be handled here.
   */
  private WPSQueuedSimulationService m_service = null;

  /**
   * The instance of this class.
   */
  private static SimulationManager INSTANCE = null;

  /**
   * The constructor.
   */
  private SimulationManager( )
  {
    m_service = new WPSQueuedSimulationService( new LocalSimulationFactory(), new LocalURLCatalog(), 1, 2000, FileUtilities.TMP_DIR );
  }

  /**
   * Returns the instance of this manager. If none exists, it will be created. There can be only one instance at a time.
   * 
   * @return The instance of this manager.
   */
  public static SimulationManager getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new SimulationManager();

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
  public void startSimulation( String typeID, String description, Execute execute ) throws OWSException
  {
    /* Get the Simulation.. */
    ISimulation simulation = WPSUtilities.getSimulation( typeID );

    try
    {
      /* Get the process description. */
      ProcessDescriptionType processDescription = DescribeProcess.buildProcessDescriptionType( simulation, typeID );

      /* Start the job. */
      m_service.startJob( typeID, description, execute, processDescription );
    }
    catch( JAXBException e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" );
    }
    catch( SimulationException e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" );
    }
  }
}