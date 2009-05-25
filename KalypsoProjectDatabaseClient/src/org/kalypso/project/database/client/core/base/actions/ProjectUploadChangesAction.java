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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
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
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker;
import org.kalypso.project.database.client.extension.database.handlers.ITranscendenceProject;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.client.ui.project.wizard.commit.WizardCommitProject;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;

/**
 * @author Dirk Kuch
 */
public class ProjectUploadChangesAction implements IProjectAction
{
  private static final Image IMG_COMMIT_CHANGES = new Image( null, ProjectUploadChangesAction.class.getResourceAsStream( "images/action_changes_commit.gif" ) );
  
  protected final IKalypsoModule m_module;

  protected final ITranscendenceProject m_handler;

  protected final IProjectDatabaseUiLocker m_locker;

  public ProjectUploadChangesAction( final IKalypsoModule module, final ITranscendenceProject handler, final IProjectDatabaseUiLocker locker )
  {
    m_module = module;
    m_handler = handler;
    m_locker = locker;
  }

  /**
   * @see org.kalypso.project.database.client.core.base.actions.IProjectAction#render(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    final ImageHyperlink link = toolkit.createImageHyperlink( body, SWT.NULL );
    link.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false ) );
    link.setImage( IMG_COMMIT_CHANGES );
    link.setToolTipText( "Übermittle Änderungen in die Projektdatenbank" );

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
          
          final WizardCommitProject wizard = new WizardCommitProject( m_handler );
          final WizardDialog2 dialog = new WizardDialog2( null, wizard );
          dialog.open();

          try
          {
            final IRemoteProjectPreferences preferences = m_handler.getRemotePreferences();
            preferences.setChangesCommited( true );

          }
          catch( final CoreException e1 )
          {
            KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e1 ) );
          }

          final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
          if( shell != null && !shell.isDisposed() && Window.OK != dialog.getReturnCode() )
          {
            ErrorDialog.openError( shell, Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.TranscendenceProjectRowBuilder.21" ), Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.TranscendenceProjectRowBuilder.22" ), StatusUtilities.createErrorStatus( "" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
