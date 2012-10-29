/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.project.database.client.core.base.worker;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.model.projects.ITranscendenceProject;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * Acquires a project lock (lock ticket) in the model base and update
 * {@link org.kalypso.project.database.common.nature.RemoteProjectNature} lock settings
 *
 * @author Dirk Kuch
 */
public class ReleaseProjectLockWorker implements ICoreRunnableWithProgress
{

  private ITranscendenceProject m_project = null;

  private final boolean m_force;

  private KalypsoProjectBean m_bean = null;

  public ReleaseProjectLockWorker( final KalypsoProjectBean bean, final boolean force )
  {
    m_bean = bean;
    m_force = force;
  }

  public ReleaseProjectLockWorker( final ITranscendenceProject project, final boolean force )
  {
    m_project = project;
    m_force = force;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    String ticket = null;

    /* remove lock from local project */
    if( m_project != null )
    {
      final IRemoteProjectPreferences preferences = m_project.getRemotePreferences();
      ticket = preferences.getEditTicket();
      Assert.isNotNull( ticket );

      /* reset edit ticket */
      preferences.setEditTicket( "" ); //$NON-NLS-1$
      Assert.isTrue( "".equals( preferences.getEditTicket().trim() ) ); //$NON-NLS-1$
    }

    final IProjectDatabase service = KalypsoProjectDatabaseClient.getService();
    if( m_force )
    {
      if( m_project != null )
        service.forceUnlock( m_project.getBean() );
      if( m_bean != null )
        service.forceUnlock( m_bean );
    }
    else
    {
      if( m_project != null )
      {
        final Boolean released = service.releaseProjectEditLock( m_project.getUniqueName(), ticket );
        Assert.isTrue( released );
      }
      else
      {
        throw new UnsupportedOperationException();
      }

    }

    return Status.OK_STATUS;
  }
}
