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
package org.kalypso.simulation.ui.calccase.simulation;

import java.util.List;
import java.util.Map;

import net.opengeospatial.wps.ProcessDescriptionType;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.service.wps.client.WPSRequest;
import org.kalypso.service.wps.client.simulation.SimulationDelegate;
import org.kalypso.simulation.core.calccase.SimulationUtils;
import org.kalypso.simulation.core.simspec.Modeldata;

/**
 * @author Gernot Belger
 *
 */
public class WpsSimulationRunner implements ISimulationRunner
{
  private final IContainer m_calcCaseFolder;

  private final Modeldata m_modelspec;

  private final String m_serviceEndpoint;

  public WpsSimulationRunner( final IContainer calcCaseFolder, final Modeldata modelspec, final String serviceEndpoint )
  {
    m_calcCaseFolder = calcCaseFolder;
    m_modelspec = modelspec;
    m_serviceEndpoint = serviceEndpoint;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, 1000 );

    final String typeID = m_modelspec.getTypeID();
    final WPSRequest simulationJob = new WPSRequest( typeID, m_serviceEndpoint, 1000 * 60 * 60 );

    final SimulationDelegate delegate = new SimulationDelegate( typeID, m_calcCaseFolder, m_modelspec );
    delegate.init();

    final ProcessDescriptionType processDescription = simulationJob.getProcessDescription( progress.newChild( 100, SubMonitor.SUPPRESS_NONE ) );
    final Map<String, Object> inputs = delegate.createInputs( processDescription, progress.newChild( 100, SubMonitor.SUPPRESS_NONE ) );
    final List<String> outputs = delegate.createOutputs();

    final IStatus status = simulationJob.run( inputs, outputs, progress.newChild( 800, SubMonitor.SUPPRESS_NONE ) );
    if( !status.isOK() )
      return status;

    monitor.subTask( "Altergebnisse aus dem Arbeitsbereich löschen..." );
    SimulationUtils.clearResultsAfterCalculation( m_modelspec, m_calcCaseFolder, progress.newChild( 100, SubMonitor.SUPPRESS_NONE ) );

    monitor.subTask( "Ergebnisse in den Arbeitsbereich kopieren..." );
    delegate.copyResults( simulationJob.getReferences() );
    delegate.finish();

    return status;
  }

}
