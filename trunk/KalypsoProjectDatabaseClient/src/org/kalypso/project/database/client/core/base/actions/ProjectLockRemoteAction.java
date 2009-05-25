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
package org.kalypso.project.database.client.core.base.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.ProjectDataBaseController;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker;
import org.kalypso.project.database.client.extension.database.handlers.ITranscendenceProject;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;

/**
 * @author kuch
 */
public class ProjectLockRemoteAction implements IProjectAction
{
  private static final Image IMG_AQUIRE_LOCK = new Image( null, ProjectLockRemoteAction.class.getResourceAsStream( "images/action_lock_acquire.gif" ) );

  private static final Image IMG_RELEASE_LOCK = new Image( null, ProjectLockRemoteAction.class.getResourceAsStream( "images/action_lock_release.gif" ) );

  protected final ITranscendenceProject m_handler;

  protected final IProjectDatabaseUiLocker m_locker;

  protected final IKalypsoModule m_module;

  public ProjectLockRemoteAction( final ITranscendenceProject handler, final IProjectDatabaseUiLocker locker, final IKalypsoModule module )
  {
    m_handler = handler;
    m_locker = locker;
    m_module = module;
  }

  /**
   * @see org.kalypso.project.database.client.core.base.actions.IProjectAction#render(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    final ImageHyperlink link = toolkit.createImageHyperlink( body, SWT.NULL );
    link.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false ) );

    try
    {
      // TODO dialog - ask for setting and releasing of project locks

      if( m_handler.getRemotePreferences().isLocked() )
        releaseProjectLock( link );
      else
        acquireProjectLock( link );
    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  private void releaseProjectLock( final ImageHyperlink link )
  {
    link.setImage( IMG_RELEASE_LOCK );
    link.setToolTipText( "Entsperre Projekt" );

    link.addHyperlinkListener( new HyperlinkAdapter()
    {
      /**
       * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
       */
      @Override
      public void linkActivated( final HyperlinkEvent e )
      {
        try
        {
          m_locker.acquireUiUpdateLock();

          final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

          final IStatus lockStatus = ProjectDataBaseController.releaseProjectLock( m_handler );
          if( !shell.isDisposed() )
          {
            ErrorDialog.openError( shell, Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.TranscendenceProjectRowBuilder.28" ), Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.TranscendenceProjectRowBuilder.29" ), lockStatus ); //$NON-NLS-1$ //$NON-NLS-2$
          }

          try
          {
            final IRemoteProjectPreferences preferences = m_handler.getRemotePreferences();
            preferences.setChangesCommited( false );
            if( m_module.getDatabaseSettings().modifyOnLockRelease() )
              preferences.setModified( true );
          }
          catch( final CoreException e1 )
          {
            KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e1 ) );
          }
        }
        finally
        {
          m_locker.releaseUiUpdateLock();
        }
      }
    } );

  }

  private void acquireProjectLock( final ImageHyperlink link )
  {
    link.setImage( IMG_AQUIRE_LOCK );
    link.setToolTipText( "Sperre Projekt für die Bearbeitung" );

    link.addHyperlinkListener( new HyperlinkAdapter()
    {
      /**
       * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
       */
      @Override
      public void linkActivated( final HyperlinkEvent e )
      {
        try
        {
          m_locker.acquireUiUpdateLock();

          final IStatus lockStatus = ProjectDataBaseController.acquireProjectLock( m_handler );

          final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
          if( shell == null )
            return;

          if( !shell.isDisposed() )
          {
            ErrorDialog.openError( shell, Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.TranscendenceProjectRowBuilder.25" ), Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.TranscendenceProjectRowBuilder.26" ), lockStatus ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
        finally
        {
          m_locker.releaseUiUpdateLock();
        }
      }
    } );

  }
}
