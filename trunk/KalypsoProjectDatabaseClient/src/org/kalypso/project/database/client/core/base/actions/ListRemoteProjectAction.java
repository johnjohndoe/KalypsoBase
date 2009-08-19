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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.project.database.client.extension.database.handlers.IRemoteProject;
import org.kalypso.project.database.client.i18n.Messages;

/**
 * @author kuch
 */
public class ListRemoteProjectAction implements IProjectAction
{
  private static final Image IMG_REMOTE_PROJECT = new Image( null, ListRemoteProjectAction.class.getResourceAsStream( "images/project_remote.gif" ) ); //$NON-NLS-1$

  protected final IRemoteProject m_handler;

  public ListRemoteProjectAction( final IRemoteProject handler )
  {
    // TODO img handling if an remote project is locked
    m_handler = handler;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.actions.IProjectAction#render(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    final ImageHyperlink link = toolkit.createImageHyperlink( body, SWT.NULL );
    link.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    link.setImage( IMG_REMOTE_PROJECT );
    link.setToolTipText( String.format( Messages.getString("org.kalypso.project.database.client.core.base.actions.ListRemoteProjectAction.1"), m_handler.getName() ) ); //$NON-NLS-1$
    link.setText( m_handler.getName() );

    link.setEnabled( false );
    link.setUnderlined( false );
  }
}
