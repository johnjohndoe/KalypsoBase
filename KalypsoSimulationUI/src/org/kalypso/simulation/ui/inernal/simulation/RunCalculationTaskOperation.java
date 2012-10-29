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
package org.kalypso.simulation.ui.inernal.simulation;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.simulation.core.refactoring.ISimulationRunner;
import org.kalypso.simulation.core.refactoring.SimulationRunnerFactory;
import org.kalypso.simulation.core.simspec.Modeldata;

/**
 * @author Gernot Belger
 */
public class RunCalculationTaskOperation
{
  private final IContainer m_simulationFolder;

  private final Modeldata m_modeldata;

  public RunCalculationTaskOperation( final IContainer simulationFolder, final Modeldata modeldata )
  {
    m_simulationFolder = simulationFolder;
    m_modeldata = modeldata;
  }

  public IStatus execute( final IProgressMonitor monitor )
  {
    try
    {
      final URL scenarioURL = ResourceUtilities.createQuietURL( m_simulationFolder );

      final Map<String, Object> inputs = SimulationRunnerFactory.resolveInputs( m_modeldata.getInput() );
      final List<String> outputs = SimulationRunnerFactory.resolveOutputs( m_modeldata.getOutput() );

      /* Create runner and look for endpoint properties, maybe a WPS should be used */
      final ISimulationRunner runner = SimulationRunnerFactory.createRunner( m_modeldata, scenarioURL, SimulationRunnerFactory.WPS_ENDPOINT_PROPERTY, SimulationRunnerFactory.WPS_USE_ENDPOINT_PROPERTY );
      return runner.run( inputs, outputs, monitor );
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }
  }
}