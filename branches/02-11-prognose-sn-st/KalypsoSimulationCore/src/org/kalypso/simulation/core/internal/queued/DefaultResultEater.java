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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.simulation.core.ISimulationResultEater;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.i18n.Messages;

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
  @Override
  public void addResult( final String id, final Object result ) throws SimulationException
  {
    if( !m_modelspec.hasOutput( id ) )
      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.DefaultResultEater.0" ) + id, null ); //$NON-NLS-1$

    final SimulationDataPath clientBean = m_clientOutputMap.get( id );
    if( clientBean != null )
    {
      // Do not throw an exception if the client does not expect this result,
      // as long as the output self is defined.
//      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.DefaultResultEater.1" ) + id, null ); //$NON-NLS-1$

      // Strange: we are on the server side, but the client defines where to store the data. This does not
      // fit to the WPS philosophie...
      final String clientPath = clientBean.getPath();
      // only add results with a valid path, because null path will later lead to a NullPointerException
      if( clientPath != null )
        m_results.add( new SimulationResult( id, clientPath, (File) result ) );
    }
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

  public void transferCurrentResults( final IContainer targetFolder ) throws SimulationException
  {
    final IPath targetLocation = targetFolder.getLocation();
    final File targetDir = targetLocation == null ? null : targetLocation.toFile();
    transferCurrentResults( targetDir, targetFolder );
  }

  public void transferCurrentResults( final File targetDir ) throws SimulationException
  {
    transferCurrentResults( targetDir, null );
  }

  /**
   * Transfers the current results. For internal use only. targetDir must correspond to targetFolder
   */
  private void transferCurrentResults( final File targetDir, final IContainer targetFolder ) throws SimulationException
  {
    try
    {
      for( final SimulationResult result : m_results )
      {
        final File resultFile = result.getFile();
        final String relativeTargetPath = result.getPath();

        final File targetFile = new File( targetDir, relativeTargetPath );
        // try to move file/directory to destination
        FileUtilities.moveContents( resultFile, targetFile );

        if( targetFolder != null )
        {
          final IResource targetMember = targetFolder.findMember( new Path( relativeTargetPath ) );
          if( targetMember != null )
            targetMember.refreshLocal( IResource.DEPTH_INFINITE, new NullProgressMonitor() );
        }
      }
    }
    catch( final IOException e )
    {
      throw new SimulationException( Messages.getString( "org.kalypso.simulation.core.internal.queued.DefaultResultEater.2" ), e ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      throw new SimulationException( "Failed to refresh result files in workspace", e );
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
