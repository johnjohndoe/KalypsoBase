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
package org.kalypso.project.database.client.core;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.base.worker.AcquireProjectLockWorker;
import org.kalypso.project.database.client.core.base.worker.CreateRemoteProjectWorker;
import org.kalypso.project.database.client.core.base.worker.ReleaseProjectLockWorker;
import org.kalypso.project.database.client.core.base.worker.UpdateProjectDescriptionWorker;
import org.kalypso.project.database.client.core.base.worker.UpdateProjectWorker;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.extension.database.IKalypsoModuleDatabaseSettings;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;
import org.kalypso.project.database.client.extension.database.handlers.IRemoteProject;
import org.kalypso.project.database.client.extension.database.handlers.ITranscendenceProject;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class ProjectDataBaseController
{
  protected static WorkspaceJob JOB = null;

  public static IStatus createRemoteProject( final IKalypsoModuleDatabaseSettings settings, final ILocalProject handler )
  {
    final CreateRemoteProjectWorker worker = new CreateRemoteProjectWorker( settings, handler );
    final IStatus status = ProgressUtilities.busyCursorWhile( worker );
    setDirty();

    return status;
  }

  /**
   * set project database model dirty
   */
  synchronized private static void setDirty( )
  {
    if( JOB == null )
    {
      JOB = new WorkspaceJob( "" ) //$NON-NLS-1$
      {

        @Override
        public IStatus runInWorkspace( final IProgressMonitor monitor )
        {
          final IProjectDatabaseModel model = KalypsoProjectDatabaseClient.getModel();
          model.setRemoteProjectsDirty();

          JOB = null;

          return Status.OK_STATUS;
        }
      };

      JOB.schedule( 100 );
    }
    else
    {
      JOB.schedule( 100 );
    }

  }

  public static IStatus updateProject( final ITranscendenceProject handler )
  {
    final UpdateProjectWorker worker = new UpdateProjectWorker( handler );
    final IStatus status = ProgressUtilities.busyCursorWhile( worker );
    setDirty();

    return status;
  }

  public static IStatus releaseProjectLock( final ITranscendenceProject handler )
  {
    final ReleaseProjectLockWorker worker = new ReleaseProjectLockWorker( handler, false );
    final IStatus status = ProgressUtilities.busyCursorWhile( worker );
    setDirty();

    return status;

  }

  public static IStatus releaseProjectLock( final KalypsoProjectBean bean, final boolean force )
  {
    final ReleaseProjectLockWorker worker = new ReleaseProjectLockWorker( bean, force );
    final IStatus status = ProgressUtilities.busyCursorWhile( worker );
    setDirty();

    return status;
  }

  public static IStatus acquireProjectLock( final ILocalProject handler )
  {
    final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
    if( MessageDialog.openQuestion( shell, "Projekt zur Bearbeitung sperren", "Sie sind im Begriff, dass Projekt zur Bearbeitung zu sperren. Diese Sperre wirkt sich auf alle Nutzer im System aus.\n\nM�chten Sie das Projekt wirklich sperren / editieren?" ) )
    {
      final AcquireProjectLockWorker worker = new AcquireProjectLockWorker( handler );
      final IStatus status = ProgressUtilities.busyCursorWhile( worker );
      setDirty();

      return status;
    }

    return Status.CANCEL_STATUS;

  }

  public static IStatus updateProjectDescription( final IRemoteProject handler, final String description )
  {
    final UpdateProjectDescriptionWorker worker = new UpdateProjectDescriptionWorker( handler, description );
    final IStatus status = ProgressUtilities.busyCursorWhile( worker );
    setDirty();

    return status;
  }

}
