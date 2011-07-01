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
package org.kalypso.core.projecthandle.local;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.projecthandle.IProjectOpenAction;
import org.kalypso.core.status.StatusDialog;

/**
 * @author Dirk Kuch
 */
public class ProjectOpenAction extends Action implements IProjectOpenAction
{
  protected static final ImageDescriptor IMG_PROJECT_LOCAL = ImageDescriptor.createFromURL( ProjectOpenAction.class.getResource( "images/project_local.gif" ) ); //$NON-NLS-1$

  private final ILocalProjectHandle m_item;

  public ProjectOpenAction( final ILocalProjectHandle item )
  {
    m_item = item;

    setText( item.getName() );
    setImageDescriptor( IMG_PROJECT_LOCAL );
    setToolTipText( Messages.getString( "org.kalypso.core.projecthandle.local.ProjectOpenAction.10", m_item.getName() ) ); //$NON-NLS-1$
  }


  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.widget.getDisplay().getActiveShell();

    final IStatus status = checkProject();
    if( status.matches( IStatus.CANCEL ) )
      return;

    if( !status.isOK() )
    {
      new StatusDialog( shell, status, getText() ).open();
      return;
    }

    final IStatus openStatus = doOpenProject();
    if( status.isOK() || status.matches( IStatus.CANCEL ) )
      return;

    new StatusDialog( shell, openStatus, getText() ).open();
  }

  private IStatus checkProject( )
  {
    /* Some common checks, common to all the open actions */
    final IProject project = m_item.getProject();

    /* Validate parameters */
    if( !project.exists() )
    {
      final String message = String.format( "Unable to open '%s'. The project does not exist.", project.getName() );
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), message );
    }

    if( !project.isOpen() )
    {
      // TODO: instead: we should ask the user if we should open the project.
      final String message = String.format( "Unable to open '%s'. The project is closed. Please open the project first.", project.getName() );
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), message );
    }

    // Actually: we do not know ourselfs how to open a project, This depends on the real implementations.

    return Status.OK_STATUS;
  }

  protected IStatus doOpenProject( )
  {
    return Status.OK_STATUS;
  }

}
