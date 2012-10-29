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
package org.kalypso.service.wps.client.simulation;

import java.net.URL;
import java.util.List;
import java.util.Map;

import net.opengeospatial.wps.ProcessDescriptionType;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.service.wps.Activator;
import org.kalypso.service.wps.client.WPSRequest;
import org.kalypso.simulation.core.refactoring.ISimulationRunner;
import org.kalypso.simulation.core.refactoring.SimulationRunnerFactory;
import org.kalypso.simulation.core.simspec.Modeldata;

/**
 * @author Gernot Belger
 */
public class WpsSimulationRunner implements ISimulationRunner
{
  private Modeldata m_modelspec;

  private String m_serviceEndpoint;

  private URL m_inputDir;

  @Override
  public void init( final Modeldata modeldata, final URL inputDir )
  {
    m_modelspec = modeldata;
    m_inputDir = inputDir;
    m_serviceEndpoint = System.getProperty( SimulationRunnerFactory.WPS_ENDPOINT_PROPERTY );
  }

  @Override
  public IStatus run( final Map<String, Object> inputs, final List<String> outputs, final IProgressMonitor monitor ) throws CoreException
  {
    final IFolder calcCaseFolder = ResourceUtilities.findFolderFromURL( m_inputDir );
    if( calcCaseFolder == null )
    {
      final String messaage = String.format( "WSP Runner only works on Platform Resources: %s", m_inputDir );
      return new Status( IStatus.ERROR, Activator.PLUGIN_ID, messaage );
    }

    final SubMonitor progress = SubMonitor.convert( monitor, 1000 );

    final String typeID = m_modelspec.getTypeID();
    final WPSRequest simulationJob = new WPSRequest( typeID, m_serviceEndpoint, 1000 * 60 * 60 );

    final SimulationDelegate delegate = new SimulationDelegate( typeID, calcCaseFolder, m_modelspec );
    delegate.init();

    final ProcessDescriptionType processDescription = simulationJob.getProcessDescription( progress.newChild( 100, SubMonitor.SUPPRESS_NONE ) );

    // FIXME: check: inputs/outputs should be same as given in the signature; maybe we should use those?
    final Map<String, Object> delegateInputs = delegate.createInputs( processDescription, progress.newChild( 100, SubMonitor.SUPPRESS_NONE ) );
    final List<String> delegateOutputs = delegate.createOutputs();

    final IStatus status = simulationJob.run( delegateInputs, delegateOutputs, progress.newChild( 800, SubMonitor.SUPPRESS_NONE ) );
    if( !status.isOK() )
      return status;

    monitor.subTask( "Altergebnisse aus dem Arbeitsbereich löschen..." );
    SimulationUtils.clearResultsAfterCalculation( m_modelspec, calcCaseFolder, progress.newChild( 100, SubMonitor.SUPPRESS_NONE ) );

    monitor.subTask( "Ergebnisse in den Arbeitsbereich kopieren..." );
    delegate.copyResults( simulationJob.getReferences() );
    delegate.finish();

    return status;
  }
}