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
package org.kalypso.simulation.core.refactoring.local;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.KalypsoSimulationCoreJaxb;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypso.simulation.core.internal.local.LocalSimulationFactory;
import org.kalypso.simulation.core.internal.queued.DefaultResultEater;
import org.kalypso.simulation.core.internal.queued.ModelspecData;
import org.kalypso.simulation.core.refactoring.ISimulationRunner;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Output;
import org.kalypso.simulation.core.util.SimulationUtilitites;

/**
 * @author Dirk Kuch
 */
public class LocalSimulationRunner implements ISimulationRunner
{

  private final String m_calculationTypeId;

  private final Modeldata m_modeldata;


  private final URL m_inputDir;

  public LocalSimulationRunner( final String calculationTypeId, final Modeldata modeldata, final URL inputDir )
  {
    m_calculationTypeId = calculationTypeId;
    m_modeldata = modeldata;
    m_inputDir = inputDir;
  }

  /**
   * @see org.kalypso.simulation.core.refactoring.ISimulationRunner#startProcess(java.util.Map, java.util.List,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void run( final Map<String, Object> inputs, final List<String> outputs, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final SimulationDataPath[] inputPaths = resolveInputs( inputs );
      final SimulationDataPath[] outputPaths = resolveOutputs( outputs );

      final File tmpDir = SimulationUtilitites.createSimulationTmpDir( m_calculationTypeId );

      /* FIXME - ask gernot, we really need to move inputs to simulation tempDir? */
      final LocalSimulationFactory factory = new LocalSimulationFactory();
      final ISimulation job = factory.createJob( m_calculationTypeId );
      final ModelspecData modelspec = new ModelspecData( job.getSpezifikation(), KalypsoSimulationCoreJaxb.JC.createUnmarshaller() );

      final LocalSimulationDataProvider inputProvider = new LocalSimulationDataProvider( modelspec, inputPaths, m_inputDir );
      final DefaultResultEater resultEater = new DefaultResultEater( modelspec, outputPaths );

      job.run( tmpDir, inputProvider, resultEater, new LocalSimulationMonitor( monitor ) );
      resultEater.transferCurrentResults( new File( m_inputDir.getFile() ) );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new CoreException( StatusUtilities.createErrorStatus( Messages.getString("org.kalypso.simulation.core.refactoring.local.LocalSimulationRunner.0"), e ) ); //$NON-NLS-1$
    }
    finally {
      // TODO delete tmpDir
   // tmpDir.delete();
    }
  }

  private SimulationDataPath[] resolveOutputs( final List<String> outputs )
  {
    final List<SimulationDataPath> myPaths = new ArrayList<SimulationDataPath>();
    final List<Output> modelOutputs = m_modeldata.getOutput();
    for( final Output output : modelOutputs )
    {
      if( outputs.contains( output.getId() ) )
        myPaths.add( new SimulationDataPath( output.getId(), output.getPath() ) );
    }

    if( outputs.size() != myPaths.size() )
      System.out.println( "*narf* LocalSimulationRunner.resolveOutputs() - output array sizes differ" ); //$NON-NLS-1$

    return myPaths.toArray( new SimulationDataPath[] {} );
  }

  private SimulationDataPath[] resolveInputs( final Map<String, Object> inputs )
  {
    final List<SimulationDataPath> myPaths = new ArrayList<SimulationDataPath>();

    final Set<Entry<String, Object>> entries = inputs.entrySet();
    for( final Entry<String, Object> entry : entries )
    {
      if( entry.getValue() instanceof String )
        myPaths.add( new SimulationDataPath( entry.getKey(), (String) entry.getValue() ) );
    }

    return myPaths.toArray( new SimulationDataPath[] {} );
  }

}
