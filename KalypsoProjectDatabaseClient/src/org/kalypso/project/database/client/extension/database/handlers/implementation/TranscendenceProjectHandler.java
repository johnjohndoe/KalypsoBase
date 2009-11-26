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
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.extension.database.handlers.ILocalProject;
import org.kalypso.project.database.client.extension.database.handlers.IRemoteProject;
import org.kalypso.project.database.client.extension.database.handlers.ITranscendenceProject;
import org.kalypso.project.database.common.nature.IRemoteProjectPreferences;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class TranscendenceProjectHandler extends AbstractProjectHandler implements ITranscendenceProject
{
  private final ILocalProject m_local;

  private final IRemoteProject m_remote;

  public TranscendenceProjectHandler( final ILocalProject local, final IRemoteProject remote )
  {
    m_local = local;
    m_remote = remote;
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
    return true;
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
    if( getBean().isProjectLockedForEditing() )
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
      final IRemoteProjectPreferences preferences = this.getRemotePreferences();
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
  
}
