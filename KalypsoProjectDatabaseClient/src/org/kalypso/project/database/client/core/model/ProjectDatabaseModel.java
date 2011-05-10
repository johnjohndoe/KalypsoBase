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
package org.kalypso.project.database.client.core.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.project.database.client.core.model.interfaces.ILocalWorkspaceModel;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.core.model.interfaces.IRemoteWorkspaceModel;
import org.kalypso.project.database.client.core.model.local.ILocalWorkspaceListener;
import org.kalypso.project.database.client.core.model.local.LocalWorkspaceModel;
import org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener;
import org.kalypso.project.database.client.core.model.remote.RemoteWorkspaceModel;
import org.kalypso.project.database.client.core.utils.ProjectDatabaseServerUtils;
import org.kalypso.project.database.client.extension.database.IProjectDatabaseFilter;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;
import org.kalypso.project.database.client.extension.database.handlers.IProjectHandler;
import org.kalypso.project.database.client.extension.database.handlers.IRemoteProject;
import org.kalypso.project.database.client.extension.database.handlers.implementation.RemoteProjectHandler;
import org.kalypso.project.database.client.extension.database.handlers.implementation.TranscendenceProjectHandler;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.common.interfaces.IProjectDatabaseListener;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class ProjectDatabaseModel implements IProjectDatabaseModel, ILocalWorkspaceListener, IRemoteProjectsListener
{
  private LocalWorkspaceModel m_local;

  private RemoteWorkspaceModel m_remote = null;

  private final Set<IProjectHandler> m_projects = new TreeSet<IProjectHandler>( IProjectHandler.COMPARATOR );

  private final Set<IProjectDatabaseListener> m_listener = new LinkedHashSet<IProjectDatabaseListener>();

  /**
   * @param localIds
   *          handle project with nature id [x, y, z]
   */
  public ProjectDatabaseModel( )
  {
    init();
  }

  private void init( )
  {
    m_local = new LocalWorkspaceModel();
    m_local.addListener( this );

    if( ProjectDatabaseServerUtils.handleRemoteProject() )
    {
      m_remote = new RemoteWorkspaceModel();
      m_remote.addListener( this );
    }

  }

  public void dispose( )
  {
    m_local.dispose();

    if( m_remote != null )
    {
      m_remote.dispose();
    }
  }

  synchronized private void buildProjectList( )
  {
    m_projects.clear();

    final ILocalProject[] localProjects = m_local.getProjects();
    IRemoteProject[] remoteProjects = new IRemoteProject[] {};

    if( m_remote != null )
    {
      final Set<IRemoteProject> handler = new HashSet<IRemoteProject>();

      final KalypsoProjectBean[] beans = m_remote.getBeans();
      for( final KalypsoProjectBean bean : beans )
      {
        handler.add( new RemoteProjectHandler( bean ) );
      }

      remoteProjects = handler.toArray( new IRemoteProject[] {} );
    }

    for( final ILocalProject handler : localProjects )
    {
      final IRemoteProject remote = findRemoteProject( remoteProjects, handler );
      if( remote != null )
      {
        remoteProjects = (IRemoteProject[]) ArrayUtils.removeElement( remoteProjects, remote );
        final TranscendenceProjectHandler project = new TranscendenceProjectHandler( handler, remote );

        m_projects.add( project );
      }
      else
      {
        m_projects.add( handler );
      }
    }

    for( final IRemoteProject r : remoteProjects )
    {
      m_projects.add( r );
    }
  }

  private IRemoteProject findRemoteProject( final IRemoteProject[] remoteProjects, final ILocalProject handler )
  {
    final String uniqueName = handler.getUniqueName();
    for( final IRemoteProject remote : remoteProjects )
    {
      if( remote.getUniqueName().equals( uniqueName ) )
        return remote;
    }

    return null;
  }

  @Override
  public synchronized IProjectHandler[] getProjects( )
  {
    if( m_projects.isEmpty() )
    {
      buildProjectList();
    }

    return m_projects.toArray( new IProjectHandler[] {} );
  }

  /**
   * @see org.kalypso.project.database.client.core.model.local.ILocalWorkspaceListener#localWorkspaceChanged()
   */
  @Override
  public void localWorkspaceChanged( )
  {
    buildProjectList();

    for( final IProjectDatabaseListener listener : m_listener )
    {
      listener.projectModelChanged();
    }
  }

  /**
   * @see org.kalypso.project.database.client.core.model.remote.IRemoteWorkspaceListener#remoteWorkspaceChanged()
   */
  @Override
  public void remoteWorkspaceChanged( )
  {
    buildProjectList();

    for( final IProjectDatabaseListener listener : m_listener )
    {
      listener.projectModelChanged();
    }
  }

  @Override
  public void addListener( final IProjectDatabaseListener listener )
  {
    m_listener.add( listener );
  }

  @Override
  public void addRemoteListener( final IRemoteProjectsListener listener )
  {
    if( m_remote != null )
    {
      m_remote.addListener( listener );
    }
  }

  @Override
  public void removeRemoteListener( final IRemoteProjectsListener listener )
  {
    if( m_remote != null )
    {
      m_remote.removeListener( listener );
    }
  }

  @Override
  public void removeListener( final IProjectDatabaseListener listener )
  {
    m_listener.remove( listener );
  }

  @Override
  public IProjectHandler[] getProjects( final IProjectDatabaseFilter filter )
  {
    final Set<IProjectHandler> myProjects = new HashSet<IProjectHandler>();

    final IProjectHandler[] projects = getProjects();
    for( final IProjectHandler handler : projects )
    {
      if( filter.select( handler ) )
      {
        myProjects.add( handler );
      }
    }

    return myProjects.toArray( new IProjectHandler[] {} );
  }

  @Override
  public void setRemoteProjectsDirty( )
  {
    if( m_remote != null )
    {
      m_remote.setDirty();
    }
  }

  @Override
  public IStatus getRemoteConnectionState( )
  {
    if( m_remote != null )
      return m_remote.getRemoteConnectionState();

    return StatusUtilities.createWarningStatus( Messages.getString( "org.kalypso.project.database.client.core.model.ProjectDatabaseModel.0" ) ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.project.database.client.core.model.remote.IRemoteProjectsListener#remoteConnectionChanged()
   */
  @Override
  public void remoteConnectionChanged( final IStatus connectionState )
  {
    buildProjectList();

    for( final IProjectDatabaseListener listener : m_listener )
    {
      listener.projectModelChanged();
    }
  }

  /**
   * @see org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel#findProject(org.eclipse.core.resources.IProject)
   */
  @Override
  public IProjectHandler findProject( final IProject project )
  {
    final IProjectHandler[] projects = getProjects();
    for( final IProjectHandler handler : projects )
    {
      if( handler.equals( project ) )
        return handler;
    }

    return null;
  }

  /**
   * @see org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel#getLocalWorkspaceModel()
   */
  @Override
  public ILocalWorkspaceModel getLocalWorkspaceModel( )
  {
    return m_local;
  }

  /**
   * @see org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel#getRemoteWorkspaceModel()
   */
  @Override
  public IRemoteWorkspaceModel getRemoteWorkspaceModel( )
  {
    return m_remote;
  }

  /**
   * @see org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel#getProject(java.lang.String)
   */
  @Override
  public IProjectHandler getProject( String unique )
  {
    if( unique.startsWith( "/" ) ) //$NON-NLS-1$
    {
      unique = unique.substring( 1 );
    }

    final IProjectHandler[] projects = getProjects();
    for( final IProjectHandler project : projects )
    {
      if( project.getUniqueName().equals( unique ) )
        return project;
    }

    return null;
  }

  public void stop( )
  {
    if( Objects.isNotNull( m_remote ) )
      m_remote.stop();
  }
}
