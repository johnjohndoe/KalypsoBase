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
package org.kalypso.utils.log;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.gml.binding.commons.IGeoStatus;
import org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection;

/**
 * @author Gernot Belger
 */
public class LoadStatusLogJob extends Job
{
  private final IFile m_statusLogFile;

  private final String m_statuslabel;

  private IStatus m_statusLog;

  public LoadStatusLogJob( final IFile statusLogFile, final String statusLabel )
  {
    super( String.format( "Loading log file '%s'", statusLogFile.getName() ) );

    m_statusLogFile = statusLogFile;
    m_statuslabel = statusLabel == null ? "Status log" : statusLabel;

    setUser( false );
    setPriority( Job.LONG );
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    m_statusLog = loadStatusLog();

    // REMARK: we do not use status log as result, else we get big output on console
    return Status.OK_STATUS;
  }

  public IStatus getStatusLog( )
  {
    return m_statusLog;
  }

  private IStatus loadStatusLog( )
  {
    try
    {
      /* Was the status log file provided? */
      if( m_statusLogFile == null )
        throw new IllegalArgumentException( "No log file given..." );

      /* Get the status log file. */
      final File statusLogFile = m_statusLogFile.getLocation().toFile();

      /* Check if the status log file exists. */
      if( !statusLogFile.exists() )
        return new Status( IStatus.INFO, KalypsoCorePlugin.getID(), String.format( "The log file '%s' does not exist...", statusLogFile.getName() ) );

      /* Read the status log. */
      final IStatus statusLog = readStatusLog();

      /* If only one simulation was calculated, descend on level. */
      if( statusLog.isMultiStatus() )
      {
        final IStatus[] children = statusLog.getChildren();
        if( children.length == 1 )
          return children[0];
      }

      return statusLog;
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), "Failed to read the log file.", ex );
    }
  }

  /**
   * This function reads the status log and returns it.
   * 
   * @return The status log.
   */
  private IStatus readStatusLog( ) throws Exception
  {
    final IStatusCollector results = new StatusCollector( KalypsoCorePlugin.getID() );

    final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( m_statusLogFile );
    final Feature rootFeature = workspace.getRootFeature();
    final IStatusCollection stati = (IStatusCollection) rootFeature.getAdapter( IStatusCollection.class );
    for( final IGeoStatus geoStatus : stati )
      results.add( geoStatus );

    return results.asMultiStatus( m_statuslabel );
  }

  public IFile getStatusLogFile( )
  {
    return m_statusLogFile;
  }
}