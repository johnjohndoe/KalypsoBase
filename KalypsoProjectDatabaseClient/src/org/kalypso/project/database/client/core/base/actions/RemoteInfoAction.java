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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.project.database.client.core.model.projects.IRemoteProject;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.client.ui.project.wizard.info.RemoteInfoDialog;

/**
 * @author Dirk Kuch
 */
public class RemoteInfoAction extends Action
{
  private static final ImageDescriptor IMG_INFO = ImageDescriptor.createFromURL( RemoteInfoAction.class.getResource( "images/action_info.gif" ) ); //$NON-NLS-1$

  protected final IRemoteProject m_handler;

  public RemoteInfoAction( final IRemoteProject handler )
  {
    m_handler = handler;

    setToolTipText( Messages.getString("RemoteInfoAction_0") ); //$NON-NLS-1$
    setImageDescriptor( IMG_INFO );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    // TODO if database is offline - disable action
    final Shell shell = event.widget.getDisplay().getActiveShell();
    final RemoteInfoDialog dialog = new RemoteInfoDialog( m_handler, shell, true );
    dialog.open();
  }
}