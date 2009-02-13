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
package org.kalypso.simulation.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class SimulationUtilitites
{
  private static final org.kalypso.simulation.core.simspec.ObjectFactory OF = new org.kalypso.simulation.core.simspec.ObjectFactory();

  // TODO: change this tracing option to be part of the core plug-in (on server side, no ui-plug-in will be available).
  public static final String DEBUG_KEEP_SIM_FILES = "org.kalypso.simulation.ui/debug/keepSimulationFiles";

  public SimulationUtilitites( )
  {
    throw new UnsupportedOperationException( "Do not instantiate helper class" );
  }

  public static File createSimulationTmpDir( final String id ) throws IOException
  {
    final String javaTmpDir = FrameworkProperties.getProperty( FileUtilities.JAVA_IO_TMPDIR );
    final String simDir = FrameworkProperties.getProperty( ISimulationConstants.SYSPROP_SIM_DIR, javaTmpDir );

    final File simTmpDir = new File( simDir );
    final File tmpDir = new File( simTmpDir, "Simulation-" + id + "-" + System.currentTimeMillis() );
    FileUtils.forceMkdir( tmpDir );
    FileUtils.forceDeleteOnExit( tmpDir );
    return tmpDir;
  }

  /**
   * Deletes the directory, if simulation files should not be kept.
   */
  public static void clearTmpDir( final File tmpDir )
  {
    if( Boolean.valueOf( Platform.getDebugOption( DEBUG_KEEP_SIM_FILES ) ) )
      return;

    if( tmpDir != null )
      FileUtilities.deleteRecursive( tmpDir );
  }

  public static Modeldata createModelData( final Map<String, String> inputs, final Collection<String> outputs )
  {
    final Modeldata modelData = OF.createModeldata();

    final List<Input> inputList = modelData.getInput();
    for( final Entry<String, String> entry : inputs.entrySet() )
    {
      final Input input = OF.createModeldataInput();
      input.setId( entry.getKey() );
      input.setPath( entry.getValue() );
      inputList.add( input );
    }

    final List<Output> outputList = modelData.getOutput();
    for( final String entry : outputs )
    {
      final Output output = OF.createModeldataOutput();
      output.setId( entry );
      outputList.add( output );
    }

    return modelData;
  }
}
