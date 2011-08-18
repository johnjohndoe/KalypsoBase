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
package org.kalypso.project.database.client.core.utils;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.model.projects.IRemoteProject;
import org.kalypso.project.database.client.i18n.Messages;

/**
 * @author Dirk Kuch
 */
public class RemoteProjectDownloader implements ICoreRunnableWithProgress
{
  private final File m_destination;

  private File m_projectDir;

  private final IRemoteProject m_project;

  public RemoteProjectDownloader( final IRemoteProject project, final File destination )
  {
    Assert.isNotNull( project );

    m_project = project;
    m_destination = destination;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public final IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      // unzip remote project to destination dir
      final String unixName = m_project.getUniqueName();
      m_projectDir = new File( m_destination, unixName );
      m_projectDir.mkdir();

      final URL urlBean = m_project.getBean().getUrl();
      ZipUtilities.unzip( urlBean, m_projectDir );

      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      final String msg = Messages.getString( "org.kalypso.project.database.client.core.utils.RemoteProjectDownloader.0", m_project.getUniqueName() ); //$NON-NLS-1$;
      final IStatus status = new Status( IStatus.ERROR, KalypsoProjectDatabaseClient.PLUGIN_ID, msg, e );
      throw new CoreException( status );
    }
  }

  public File getProjectDir( )
  {
    return m_projectDir;
  }

}
