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
package org.kalypso.simulation.core.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
    {
      FileUtilities.deleteRecursive( tmpDir );
    }
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

  public static Modeldata createModelData( final String typeID, final Map<String, String> inputs, final boolean inputsRelativeToCalcCase, final Map<String, String> outputs, final boolean outputsRelativeToCalcCase )
  {
    final Modeldata modelData = OF.createModeldata();
    modelData.setTypeID( typeID );

    final List<Input> inputList = modelData.getInput();
    for( final Entry<String, String> entry : inputs.entrySet() )
    {
      final Input input = OF.createModeldataInput();
      input.setId( entry.getKey() );
      input.setPath( entry.getValue() );
      input.setRelativeToCalcCase( inputsRelativeToCalcCase );
      inputList.add( input );
    }

    final List<Output> outputList = modelData.getOutput();
    for( final Entry<String, String> entry : outputs.entrySet() )
    {
      final Output output = OF.createModeldataOutput();
      output.setId( entry.getKey() );
      output.setPath( entry.getValue() );
      output.setRelativeToCalcCase( outputsRelativeToCalcCase );
      outputList.add( output );
    }
    return modelData;
  }

  public static Modeldata createModelData( final URL context, final String typeID, final List<Input> inputs, final List<Output> outputs ) throws MalformedURLException
  {
    final Modeldata modelData = OF.createModeldata();
    modelData.setTypeID( typeID );

    final List<Input> inputList = modelData.getInput();
    final List<Output> outputList = modelData.getOutput();

    for( final Input input : inputs )
    {
      if( !input.isRelativeToCalcCase() )
      {
        final URL url = new URL( context, input.getPath() ); 
        input.setPath( url.toExternalForm() );
      }

      inputList.add( input );
    }

    for( final Output output : outputs )
    {
      if( !output.isRelativeToCalcCase() )
      {
        final URL url = new URL( context, output.getPath() );
        output.setPath( url.toExternalForm() );
      }

      outputList.add( output );
    }

    return modelData;
  }

  public static Input createInput( final String id, final String path )
  {
    final Input input = OF.createModeldataInput();
    input.setId( id );
    input.setPath( path );
    return input;
  }

  public static Input createInput( final String id, final String path, final boolean isRelativeToCalcCase, final boolean isOptional )
  {
    final Input input = createInput( id, path );
    input.setRelativeToCalcCase( isRelativeToCalcCase );
    input.setOptional( isOptional );
    return input;
  }

  public static Output createOutput( final String id, final String path )
  {
    final Output output = OF.createModeldataOutput();
    output.setId( id );
    output.setPath( path );
    return output;
  }

  public static Output createOutput( final String id, final String path, final boolean isRelativeToCalcCase )
  {
    final Output output = createOutput( id, path );
    output.setRelativeToCalcCase( isRelativeToCalcCase );
    return output;
  }

}
