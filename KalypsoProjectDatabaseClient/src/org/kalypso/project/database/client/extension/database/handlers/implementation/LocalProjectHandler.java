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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.core.model.interfaces.IRemoteWorkspaceModel;
import org.kalypso.project.database.client.core.model.local.LocalWorkspaceModel;
import org.kalypso.project.database.client.core.model.local.WorkspaceResourceManager;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;
import org.kalypso.project.database.common.nature.RemoteProjectNature;

/**
 * @author Dirk Kuch
 */
public class LocalProjectHandler extends AbstractProjectHandler implements ILocalProject, IPreferenceChangeListener
{

  protected final IProject m_project;

  private final LocalWorkspaceModel m_model;

  private IRemoteProjectPreferences m_preferences;

  public LocalProjectHandler( final IProject project, final LocalWorkspaceModel model )
  {
    m_project = project;
    m_model = model;

    final WorkspaceResourceManager manager = WorkspaceResourceManager.getInstance();
    manager.add( this );
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#getName()
   */
  @Override
  public String getName( )
  {
    try
    {
      final IProjectDescription description = getProject().getDescription();

      return description.getName();
    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return getProject().getName();
    }
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#getUniqueName()
   */
  @Override
  public String getUniqueName( )
  {
    return getProject().getName();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#isLocal()
   */
  @Override
  public boolean isLocal( )
  {
    return true;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#isRemote()
   */
  @Override
  public boolean isRemote( )
  {
    return false;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.ILocalProjectHandler#dispose()
   */
  @Override
  public void dispose( )
  {
    final WorkspaceResourceManager manager = WorkspaceResourceManager.getInstance();
    manager.remove( this );
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.ILocalProjectHandler#getProject()
   */
  @Override
  public IProject getProject( )
  {
    return m_project;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.ILocalProjectHandler#getRemotePreferences()
   */
  @Override
  public IRemoteProjectPreferences getRemotePreferences( ) throws CoreException
  {
    if( m_preferences == null )
    {
      if( !m_project.isNatureEnabled( RemoteProjectNature.NATURE_ID ) )
      {

        final WorkspaceJob job = new WorkspaceJob( Messages.getString( "org.kalypso.project.database.client.core.model.local.LocalProjectHandler.0" ) ) //$NON-NLS-1$
        {

          @Override
          public IStatus runInWorkspace( final IProgressMonitor monitor ) throws CoreException
          {
            final IProjectDescription description = m_project.getDescription();
            final String[] natureIds = description.getNatureIds();
            ArrayUtils.add( natureIds, RemoteProjectNature.NATURE_ID );

            description.setNatureIds( natureIds );
            m_project.setDescription( description, new NullProgressMonitor() );

            return Status.OK_STATUS;
          }
        };
// FIXME
// job.schedule();
        // TODO: use job.join instead!
// FIXME: sometimes called from ui thread blocking everything for some time...
        // encountered in 1df2d when switching from fenet to result map
        int count = 0;
        while( job.getState() != Job.NONE && count < 100 )
          try
          {
            Thread.sleep( 200 );
            count += 1;
          }
          catch( final InterruptedException e )
          {
            KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
          }
      }

      final RemoteProjectNature myNature = (RemoteProjectNature) m_project.getNature( RemoteProjectNature.NATURE_ID );
      if( myNature == null )
        return null;

      m_preferences = myNature.getRemotePreferences( m_project, this );
    }

    return m_preferences;
  }

  /**
   * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
   */
  @Override
  public void preferenceChange( final PreferenceChangeEvent event )
  {
    m_model.fireLocalUpdateEvent();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.IProjectHandler#isEditable()
   */
  @Override
  public boolean isEditable( )
  {
    try
    {
      // special case: when the project db is offline all projects will be treated as local projects! (but not editable)
      final IRemoteProjectPreferences preferences = getRemotePreferences();
      if( preferences.isOnServer() )
        return false;
    }
    catch( final CoreException e )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return true;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.ILocalProject#isLocked()
   */
  @Override
  public boolean isLocked( )
  {
    /**
     * special case: the project database is offline and this project exists onto the database -> the project is not
     * editable and locked!!!
     */
    final IProjectDatabaseModel model = KalypsoProjectDatabaseClient.getModel();
    final IRemoteWorkspaceModel remote = model.getRemoteWorkspaceModel();

    // FIXME: NPE below for Demo-Lokal. Check if this check and return value is ok.
    // FIXME: be sure that no throwable escapes this methods, as else the user interface
    // if completely broken
    if( remote == null )
      return false;

    if( !remote.isDatabaseOnline() )
      try
      {
        if( getRemotePreferences().isOnServer() )
          return false;
      }
      catch( final CoreException e )
      {
        KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }

    return true;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.IProjectHandler#getDescription()
   */
  @Override
  public String getDescription( )
  {
    try
    {
      return m_project.getDescription().getComment();
    }
    catch( final CoreException ex )
    {
      KalypsoProjectDatabaseClient.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex ) );
    }

    return null;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "Local Project: %s", getName() );
  }

}