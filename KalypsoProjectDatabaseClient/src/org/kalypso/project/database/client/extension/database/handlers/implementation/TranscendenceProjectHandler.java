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
package org.kalypso.project.database.client.extension.database.handlers.implementation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.projecthandle.AbstractProjectHandle;
import org.kalypso.core.projecthandle.IProjectOpenAction;
import org.kalypso.core.projecthandle.local.ProjectExportAction;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.nature.ModuleNature;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.base.actions.AquireProjectLockAction;
import org.kalypso.project.database.client.core.base.actions.EmptyProjectAction;
import org.kalypso.project.database.client.core.base.actions.ProjectOpenAction;
import org.kalypso.project.database.client.core.base.actions.ProjectUpdateChangesAction;
import org.kalypso.project.database.client.core.base.actions.ProjectUploadChangesAction;
import org.kalypso.project.database.client.core.base.actions.ReleaseProjectLockAction;
import org.kalypso.project.database.client.core.base.actions.RemoteInfoAction;
import org.kalypso.project.database.client.core.base.actions.TranscendenceDeleteAction;
import org.kalypso.project.database.client.core.utils.ProjectDatabaseServerUtils;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;
import org.kalypso.project.database.client.extension.database.handlers.IRemoteProject;
import org.kalypso.project.database.client.extension.database.handlers.ITranscendenceProject;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class TranscendenceProjectHandler extends AbstractProjectHandle implements ITranscendenceProject
{
  private final ILocalProject m_local;

  private final IRemoteProject m_remote;

  private final IKalypsoModule m_module;

  public TranscendenceProjectHandler( final ILocalProject local, final IRemoteProject remote )
  {
    m_local = local;
    m_remote = remote;

    final IProject project = m_local.getProject();
    m_module = ModuleNature.findModule( project );
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.ILocalProjectHandler#dispose()
   */
  @Override
  public void dispose( )
  {
    m_local.dispose();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.ILocalProjectHandler#getProject()
   */
  @Override
  public IProject getProject( )
  {
    return m_local.getProject();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.ILocalProjectHandler#getRemotePreferences()
   */
  @Override
  public IRemoteProjectPreferences getRemotePreferences( ) throws CoreException
  {
    return m_local.getRemotePreferences();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#getName()
   */
  @Override
  public String getName( )
  {
    return m_local.getName();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#getUniqueName()
   */
  @Override
  public String getUniqueName( )
  {
    return m_local.getUniqueName();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IRemoteProjectHandler#getBean()
   */
  @Override
  public KalypsoProjectBean getBean( )
  {
    return m_remote.getBean();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.IProjectHandler#isEditable()
   */
  @Override
  public boolean isEditable( )
  {
    if( getBean().hasEditLock() )
      return false;

    return true;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.ILocalProject#isLocked()
   */
  @Override
  public boolean isLocked( )
  {
    try
    {
      final IRemoteProjectPreferences preferences = getRemotePreferences();
      return preferences.isLocked();
    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return false;

  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.IProjectHandler#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return m_remote.getDescription();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "Transcendence Project: %s", getName() );
  }

  /**
   * @see org.kalypso.core.projecthandle.IProjectHandle#getProjectActions()
   */
  @Override
  public IAction[] getProjectActions( )
  {
    final IAction[] actions = new IAction[5];
    actions[0] = new RemoteInfoAction( this );
    actions[1] = new TranscendenceDeleteAction( this );
    actions[2] = new ProjectExportAction( this );
    actions[3] = createEditAction();
    actions[4] = createDatabaseAction();
    return actions;
  }

  private IAction createDatabaseAction( )
  {
    try
    {
      // TODO refactor - *brrrr....**
      final IRemoteProjectPreferences preferences = getRemotePreferences();
      if( ProjectDatabaseServerUtils.isUpdateAvailable( this ) )
        return new ProjectUpdateChangesAction( m_module, this );
      else
      {
        if( preferences == null )
          return new EmptyProjectAction();

        // FIXME: whynot always ask the remote preferences?
// if( m_module.getDatabaseSettings().hasManagedDirtyState() )
        {
          if( !preferences.isModified() || preferences.getChangesCommited() )
            return new EmptyProjectAction();
        }

        if( !preferences.isLocked() )
          return new ProjectUploadChangesAction( m_module, this );
      }
    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return new EmptyProjectAction();
  }

  private IAction createEditAction( )
  {
    try
    {
      // FIXME: whynot always ask the remote preferences?
      // if( ((IKalypsoModuleDatabaseSettings) m_module.getDatabaseSettings()).hasManagedDirtyState() )
      {
        final Boolean remoteLocked = getBean().hasEditLock();
        final boolean localLocked = isLocked();
        if( remoteLocked && !localLocked )
          return new EmptyProjectAction();

        if( getRemotePreferences().isLocked() )
          return new ReleaseProjectLockAction( this, m_module );
        else
          return new AquireProjectLockAction( this, m_module );
      }

    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return new EmptyProjectAction();
  }

  /**
   * @see org.kalypso.core.projecthandle.LocalProjectHandle#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == IProjectOpenAction.class )
      return new ProjectOpenAction( m_module, this );

    return super.getAdapter( adapter );
  }

}
