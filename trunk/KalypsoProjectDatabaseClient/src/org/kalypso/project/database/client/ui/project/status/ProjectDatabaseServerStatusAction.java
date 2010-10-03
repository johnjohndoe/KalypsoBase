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
package org.kalypso.project.database.client.ui.project.status;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.util.swt.StatusDialog;

/**
 * Action which displays the current project model database server state
 * 
 * @author Dirk Kuch
 */
public class ProjectDatabaseServerStatusAction extends Action implements IRemoteProjectsListener
{
  private static final ImageDescriptor IMG_SERVER_WAITING = ImageDescriptor.createFromURL( ProjectDatabaseServerStatusAction.class.getResource( "icons/server_refresh.gif" ) ); //$NON-NLS-1$

  private static final ImageDescriptor IMG_SERVER_OK = ImageDescriptor.createFromURL( ProjectDatabaseServerStatusAction.class.getResource( "icons/server_okay.gif" ) ); //$NON-NLS-1$

  private static final ImageDescriptor IMG_SERVER_ERROR = ImageDescriptor.createFromURL( ProjectDatabaseServerStatusAction.class.getResource( "icons/server_error.gif" ) ); //$NON-NLS-1$

  private IStatus m_connectionState;

  public ProjectDatabaseServerStatusAction( )
  {
    final IProjectDatabaseModel model = KalypsoProjectDatabaseClient.getModel();
    // FIXME: listener is never removed!
    model.addRemoteListener( this );

    update( model.getRemoteConnectionState() );
  }

  protected final void update( final IStatus connectionState )
  {
    m_connectionState = connectionState;

    if( m_connectionState != null && m_connectionState.isOK() )
    {
      setText( Messages.getString( "org.kalypso.project.database.client.ui.project.status.ProjectDatabaseServerStatusComposite.0" ) ); //$NON-NLS-1$
      setImageDescriptor( IMG_SERVER_OK );
    }
    else if( m_connectionState != null && m_connectionState.matches( IStatus.WARNING ) )
    {
      setText( Messages.getString( "org.kalypso.project.database.client.ui.project.status.ProjectDatabaseServerStatusComposite.4" ) ); //$NON-NLS-1$
      setImageDescriptor( IMG_SERVER_WAITING );
    }
    else
    {
      setText( Messages.getString( "org.kalypso.project.database.client.ui.project.status.ProjectDatabaseServerStatusComposite.5" ) ); //$NON-NLS-1$
      setImageDescriptor( IMG_SERVER_ERROR );
    }

    setEnabled( connectionState != null );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.widget.getDisplay().getActiveShell();

    final String title = Messages.getString( "org.kalypso.project.database.client.ui.project.status.ProjectDatabaseServerStatusComposite.6" ); //$NON-NLS-1$
    final StatusDialog dialog = new StatusDialog( shell, m_connectionState, title );
    dialog.open();
  }

  /**
   * @see org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener#remoteConnectionChanged(boolean)
   */
  @Override
  public void remoteConnectionChanged( final IStatus connectionState )
  {
    final UIJob job = new UIJob( "" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        update( connectionState );
        return Status.OK_STATUS;
      }
    };
    job.setSystem( true );
    job.schedule();
  }

  /**
   * @see org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener#remoteWorkspaceChanged()
   */
  @Override
  public void remoteWorkspaceChanged( )
  {
  }

}
