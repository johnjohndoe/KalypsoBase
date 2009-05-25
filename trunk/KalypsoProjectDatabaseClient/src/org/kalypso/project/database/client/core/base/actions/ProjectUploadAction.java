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
package org.kalypso.project.database.client.core.base.actions;

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
import org.kalypso.project.database.client.core.ProjectDataBaseController;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;
import org.kalypso.project.database.client.i18n.Messages;

/**
 * @author kuch
 */
public class ProjectUploadAction implements IProjectAction
{
  private static final Image IMG_UPLOAD = new Image( null, ProjectUploadAction.class.getResourceAsStream( "images/action_upload.gif" ) );

  protected final ILocalProject m_handler;

  protected final IProjectDatabaseUiLocker m_locker;

  protected final IKalypsoModule m_module;

  public ProjectUploadAction( final ILocalProject handler, final IKalypsoModule module, final IProjectDatabaseUiLocker locker )
  {
    m_handler = handler;
    m_module = module;
    m_locker = locker;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.actions.IProjectAction#render(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    // TODO handling when database is offline
    
    
    final ImageHyperlink link = toolkit.createImageHyperlink( body, SWT.NULL );
    link.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false ) );
    link.setImage( IMG_UPLOAD );
    link.setToolTipText( "‹bertrage lokales Projekt in die Modelldatenbank" );

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

          final IStatus status = ProjectDataBaseController.createRemoteProject( m_module.getDatabaseSettings(), m_handler );

          final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
          if( shell != null && !shell.isDisposed() )
          {
            ErrorDialog.openError( shell, Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.LocalProjectRowBuilder.4" ), Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.LocalProjectRowBuilder.5" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
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
