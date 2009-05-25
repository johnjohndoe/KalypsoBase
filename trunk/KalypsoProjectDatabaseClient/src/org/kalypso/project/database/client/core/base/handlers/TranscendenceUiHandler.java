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
package org.kalypso.project.database.client.core.base.handlers;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.base.actions.EmptyProjectAction;
import org.kalypso.project.database.client.core.base.actions.IProjectAction;
import org.kalypso.project.database.client.core.base.actions.ProjectDeleteAction;
import org.kalypso.project.database.client.core.base.actions.ProjectExportAction;
import org.kalypso.project.database.client.core.base.actions.ProjectInfoAction;
import org.kalypso.project.database.client.core.base.actions.ProjectLockRemoteAction;
import org.kalypso.project.database.client.core.base.actions.ProjectOpenAction;
import org.kalypso.project.database.client.core.base.actions.ProjectUpdateChangesAction;
import org.kalypso.project.database.client.core.base.actions.ProjectUploadChangesAction;
import org.kalypso.project.database.client.core.utils.ProjectDatabaseServerUtils;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker;
import org.kalypso.project.database.client.extension.database.handlers.ITranscendenceProject;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;

/**
 * @author kuch
 *
 */
public class TranscendenceUiHandler implements IProjectUiHandler
{
  private final ITranscendenceProject m_handler;

  private final IKalypsoModule m_module;

  private final IProjectDatabaseUiLocker m_locker;

  public TranscendenceUiHandler( final ITranscendenceProject handler, final IKalypsoModule module, final IProjectDatabaseUiLocker locker )
  {
    m_handler = handler;
    m_module = module;
    m_locker = locker;
  }

  /**
   * @see org.kalypso.project.database.client.core.base.handlers.IProjectUiHandler#getDeleteAction(org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker)
   */
  @Override
  public IProjectAction getDeleteAction( )
  {
    return new ProjectDeleteAction( m_handler, m_locker ); 
  }


  /**
   * @see org.kalypso.project.database.client.core.base.handlers.IProjectUiHandler#getEditAction()
   */
  @Override
  public IProjectAction getEditAction( )
  {
    return new ProjectLockRemoteAction( m_handler, m_locker );
  }

  /**
   * @see org.kalypso.project.database.client.core.base.handlers.IProjectUiHandler#getExportAction(org.kalypso.project.database.client.extension.database.IProjectDatabaseUiLocker)
   */
  @Override
  public IProjectAction getExportAction( )
  {
    return new ProjectExportAction( m_handler, m_locker );
  }

  /**
   * @see org.kalypso.project.database.client.core.base.handlers.IProjectUiHandler#getInfoAction()
   */
  @Override
  public IProjectAction getInfoAction( )
  {
    return new ProjectInfoAction( m_handler );
  }

  /**
   * @see org.kalypso.project.database.client.core.base.handlers.IProjectUiHandler#getOpenAction(org.kalypso.project.database.client.extension.IKalypsoModule)
   */
  @Override
  public IProjectAction getOpenAction( )
  {
    return new ProjectOpenAction(m_module, m_handler);
  }

  /**
   * @see org.kalypso.project.database.client.core.base.handlers.IProjectUiHandler#getDatabaseAction()
   */
  @Override
  public IProjectAction getDatabaseAction( )
  {
    try
    {
      // TODO refactor - *brrrr....**
      final IRemoteProjectPreferences preferences = m_handler.getRemotePreferences();

      if( ProjectDatabaseServerUtils.isUpdateAvailable( m_handler ) )
        return new ProjectUpdateChangesAction( m_module, m_handler, m_locker );
      else if( preferences != null && preferences.isModified() && !preferences.getChangesCommited() )
        return new ProjectUploadChangesAction( m_module, m_handler, m_locker );
    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
    
    return new EmptyProjectAction();
  }


}
