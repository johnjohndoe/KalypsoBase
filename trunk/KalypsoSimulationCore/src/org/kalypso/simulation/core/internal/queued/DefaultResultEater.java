/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.core.internal.queued;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationException;

/**
 * Transfers results locally by moving the files to the target directory
 * 
 * @author belger, kurzbach
 */
public class DefaultResultEater implements ISimulationResultEater
{
  private final Vector<File> m_files = new Vector<File>();

  private final ModelspecData m_modelspec;

  /** Should be synchronized */
  private final Vector<SimulationResult> m_results = new Vector<SimulationResult>();

  private final Map<String, SimulationDataPath> m_clientOutputMap;

  public DefaultResultEater( final ModelspecData modelspec, final SimulationDataPath[] clientOutput )
  {
    m_modelspec = modelspec;

    m_clientOutputMap = new HashMap<String, SimulationDataPath>( clientOutput.length );
    for( final SimulationDataPath bean : clientOutput )
      m_clientOutputMap.put( bean.getId(), bean );
  }

  /**
   * @param id
   *          the bean id
   * @param result
   *          this file or directory is added as a result
   * @throws SimulationException
   * @see org.kalypso.simulation.core.ISimulationResultEater#addResult(java.lang.String, java.lang.Object)
   */
  public void addResult( final String id, final Object result ) throws SimulationException
  {
    if( !m_modelspec.hasOutput( id ) )
      throw new SimulationException( "Vom Server unerwartete Ausgabe mit ID: " + id, null );

    final SimulationDataPath clientBean = m_clientOutputMap.get( id );
    if( clientBean == null )
      throw new SimulationException( "Vom Client unerwartete Ausgabe mit ID: " + id, null );

    final String clientPath = clientBean.getPath();
    // only add results with a valid path, because null path will later lead to a NullPointerException
    if( clientPath != null )
      m_results.add( new SimulationResult( id, clientPath, (File) result ) );
  }

  /**
   * Lists the available results
   */
  public String[] getCurrentResults( )
  {
    final String[] results = new String[m_results.size()];
    for( int i = 0; i < results.length; i++ )
    {
      final SimulationResult result = m_results.get( i );
      results[i] = result.getID();
    }

    return results;
  }

  /**
   * Transfers the current results
   */
  public void transferCurrentResults( final File targetFolder ) throws SimulationException
  {
    try
    {
      for( final SimulationResult result : m_results )
      {
        final File file = result.getFile();
        final String path = result.getPath();
        
        // destination file is the file relative to the target folder
        final File targetRelativeFile = new File( targetFolder, path );

        // try to move file/directory to destination
        FileUtilities.moveContents( file, targetRelativeFile );
      }
    }
    catch( final IOException e )
    {
      throw new SimulationException( "Results could not be transfered.", e );
    }
  }

  /**
   * Remember a file/directory that must be deleted later
   */
  public void addFile( final File file )
  {
    m_files.add( file );
  }

  /**
   * Deletes all remembered files/directories
   */
  public void disposeFiles( )
  {
    for( final File file : m_files )
    {
      FileUtilities.deleteRecursive( file );
    }
  }
}
