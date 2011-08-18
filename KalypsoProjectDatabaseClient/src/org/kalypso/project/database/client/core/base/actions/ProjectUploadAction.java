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
package org.kalypso.project.database.client.core.base.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.project.local.ILocalProject;
import org.kalypso.project.database.client.core.ProjectDataBaseController;
import org.kalypso.project.database.client.i18n.Messages;

/**
 * @author Dirk Kuch
 */
public class ProjectUploadAction extends Action
{
  private static final ImageDescriptor IMG_UPLOAD = ImageDescriptor.createFromURL( ProjectUploadAction.class.getResource( "images/action_upload.gif" ) ); //$NON-NLS-1$

  protected final ILocalProject m_handler;

  protected final IKalypsoModule m_module;

  public ProjectUploadAction( final ILocalProject handler, final IKalypsoModule module )
  {
    setToolTipText( Messages.getString( "org.kalypso.project.database.client.core.base.actions.ProjectUploadAction.1" ) ); //$NON-NLS-1$
    setImageDescriptor( IMG_UPLOAD );

    m_handler = handler;
    m_module = module;
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    // TODO handling when database is offline
    final IStatus status = ProjectDataBaseController.createRemoteProject( m_module.getId(), m_handler );

    final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
    if( shell != null && !shell.isDisposed() )
    {
      ErrorDialog.openError( shell, Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.LocalProjectRowBuilder.4" ), Messages.getString( "org.kalypso.project.database.client.ui.project.database.internal.LocalProjectRowBuilder.5" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

}
