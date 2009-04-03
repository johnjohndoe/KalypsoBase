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
package org.kalypso.project.database.client.ui.management;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.core.model.interfaces.IRemoteWorkspaceModel;
import org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener;
import org.kalypso.project.database.client.core.utils.KalypsoProjectBeanHelper;
import org.kalypso.project.database.client.ui.MyColors;
import org.kalypso.project.database.client.ui.MyFonts;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class ManageRemoteProjects extends Composite implements IRemoteProjectsListener
{
  private static final Image IMG_DELETE = new Image( null, ManageRemoteProjects.class.getResourceAsStream( "icons/delete.gif" ) );

  private static final Image IMG_REMOTE_PROJECT = new Image( null, ManageRemoteProjects.class.getResourceAsStream( "icons/remote_project.gif" ) );

  private static final Image IMG_UNLOCK = new Image( null, ManageRemoteProjects.class.getResourceAsStream( "icons/unlock.gif" ) );

  private final String m_type;

  private final FormToolkit m_toolkit;

  private Composite m_body;

  public ManageRemoteProjects( final FormToolkit toolkit, final Composite parent, final String type )
  {
    super( parent, SWT.NULL );
    m_toolkit = toolkit;
    m_type = type;

    this.setLayout( new GridLayout() );

    final IProjectDatabaseModel model = KalypsoProjectDatabaseClient.getDefault().getProjectDatabaseModel();
    model.addRemoteListener( this );

    update();
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    final IProjectDatabaseModel model = KalypsoProjectDatabaseClient.getDefault().getProjectDatabaseModel();
    model.removeRemoteListener( this );

    super.dispose();
  }

  /**
   * @see org.eclipse.swt.widgets.Control#update()
   */
  @Override
  public void update( )
  {
    if( this.isDisposed() )
      return;

    if( m_body != null )
    {
      m_body.dispose();
      m_body = null;
    }

    m_body = m_toolkit.createComposite( this );
    m_body.setLayout( new GridLayout( 3, false ) );
    m_body.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    final IProjectDatabase service = KalypsoProjectDatabaseClient.getService();
    final KalypsoProjectBean[] heads = service.getProjectHeads( m_type );
    for( final KalypsoProjectBean head : heads )
    {
      final ImageHyperlink link = m_toolkit.createImageHyperlink( m_body, SWT.NULL );
      link.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      link.setText( head.getName() );
      link.setImage( IMG_REMOTE_PROJECT );
      link.setEnabled( false );
      link.setForeground( MyColors.COLOR_BLACK );
      link.setFont( MyFonts.HEADING );
      link.setUnderlined( false );

      final ImageHyperlink lnkDeleteAll = m_toolkit.createImageHyperlink( m_body, SWT.NULL );
      lnkDeleteAll.setToolTipText( "Delete whole remote project" );
      lnkDeleteAll.setImage( IMG_DELETE );

      // project locked in database?!?
      if( head.isProjectLockedForEditing() )
      {
        final ImageHyperlink lnkReleaseLock = m_toolkit.createImageHyperlink( m_body, SWT.NULL );
        lnkReleaseLock.setToolTipText( "Remove lock from remote project" );
        lnkReleaseLock.setImage( IMG_UNLOCK );

        lnkReleaseLock.addHyperlinkListener( new HyperlinkAdapter()
        {
          /**
           * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
           */
          @Override
          public void linkActivated( final HyperlinkEvent e )
          {
            final IProjectDatabaseModel model = KalypsoProjectDatabaseClient.getDefault().getProjectDatabaseModel();
            final IRemoteWorkspaceModel remoteModel = model.getRemoteWorkspaceModel();

            remoteModel.forceUnlock( head );

          }
        } );

      }
      else
      {
        m_toolkit.createImageHyperlink( m_body, SWT.NULL ); // spacer
      }

      final KalypsoProjectBean[] versions = KalypsoProjectBeanHelper.getSortedBeans( head );

      lnkDeleteAll.addHyperlinkListener( new HyperlinkAdapter()
      {
        @Override
        public void linkActivated( final HyperlinkEvent e )
        {
          for( final KalypsoProjectBean version : versions )
          {
            service.deleteProject( version );
          }
        }
      } );

      for( final KalypsoProjectBean version : versions )
      {
        final String text = String.format( "Version: %d", version.getProjectVersion() );

        final ImageHyperlink linkVersion = m_toolkit.createImageHyperlink( m_body, SWT.NULL );
        linkVersion.setLayoutData( new GridData( GridData.END, GridData.FILL, true, false, 2, 0 ) );
        linkVersion.setText( text );
        linkVersion.setEnabled( false );
        linkVersion.setUnderlined( false );

        final ImageHyperlink lnkDeleteVersion = m_toolkit.createImageHyperlink( m_body, SWT.NULL );
        lnkDeleteVersion.setToolTipText( String.format( "Delete %s", text ) );
        lnkDeleteVersion.setImage( IMG_DELETE );

        lnkDeleteVersion.addHyperlinkListener( new HyperlinkAdapter()
        {
          /**
           * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
           */
          @Override
          public void linkActivated( final HyperlinkEvent e )
          {
            final IProjectDatabaseModel model = KalypsoProjectDatabaseClient.getDefault().getProjectDatabaseModel();
            final IRemoteWorkspaceModel remoteModel = model.getRemoteWorkspaceModel();

            remoteModel.deleteBean( version );

          }
        } );
      }

      m_toolkit.createLabel( m_body, "" ).setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false, 2, 0 ) );
    }

    m_toolkit.adapt( this );
    this.layout();
  }

  /**
   * @see org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener#remoteConnectionChanged(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void remoteConnectionChanged( final IStatus connectionState )
  {

  }

  /**
   * @see org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener#remoteWorkspaceChanged()
   */
  @Override
  public void remoteWorkspaceChanged( )
  {
    new UIJob( "" )
    {

      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        update();

        return Status.OK_STATUS;
      }
    }.schedule();

  }

}
