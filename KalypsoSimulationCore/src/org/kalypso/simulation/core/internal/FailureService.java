/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.simulation.core.internal;

import javax.activation.DataHandler;

import org.eclipse.core.resources.IContainer;
import org.kalypso.simulation.core.ISimulationService;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationDescription;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;

/**
 * This 'failure' service does not implement anything but always returns the same (previously given) exception.
 * <p>
 * This is usefull, when the creation of the original service fails.
 * </p>
 * 
 * @author Belger
 */
public class FailureService implements ISimulationService
{
  private final SimulationException m_se;

  public FailureService( final SimulationException se )
  {
    m_se = se;
  }

  @Override
  public String[] getJobTypes( ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public SimulationDescription[] getRequiredInput( final String typeID ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public SimulationDescription[] getDeliveringResults( final String typeID ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public SimulationInfo[] getJobs( ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public SimulationInfo getJob( final String jobID ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public SimulationInfo startJob( final String typeID, final String description, final DataHandler zipHandler, final SimulationDataPath[] input, final SimulationDataPath[] output ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public void cancelJob( final String jobID ) throws SimulationException
  {
    throw m_se;

  }

  @Override
  public void transferCurrentResults( final IContainer targetFolder, final String jobID ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public String[] getCurrentResults( final String jobID ) throws SimulationException
  {
    throw m_se;
  }

  @Override
  public void disposeJob( final String jobID ) throws SimulationException
  {
    throw m_se;
  }
}
