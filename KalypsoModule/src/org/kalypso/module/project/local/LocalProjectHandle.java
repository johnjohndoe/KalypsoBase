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
package org.kalypso.module.project.local;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.nature.ModuleNature;
import org.kalypso.module.project.local.actions.ProjectDeleteAction;
import org.kalypso.module.project.local.actions.ProjectExportAction;
import org.kalypso.module.project.local.actions.ProjectInfoAction;
import org.kalypso.module.project.local.actions.ProjectOpenAction;

/**
 * @author Dirk Kuch
 */
public class LocalProjectHandle extends AbstractProjectHandle implements ILocalProjectHandle
{
  protected final IProject m_project;

  public LocalProjectHandle( final IProject project )
  {
    m_project = project;
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
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

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

  @Override
  public IProject getProject( )
  {
    return m_project;
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
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex ) );
    }

    return null;
  }

  @Override
  public IAction[] getProjectActions( )
  {
    final IAction[] actions = new IAction[3];
    actions[0] = new ProjectInfoAction( this );
    actions[1] = new ProjectDeleteAction( this );
    actions[2] = new ProjectExportAction( this );
    return actions;
  }

  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    /* Allow others to override own behavior (via extension.point) */
    final Object adapted = super.getAdapter( adapter );
    if( adapted != null )
      return adapted;

    if( adapter == IProjectOpenAction.class )
      return new ProjectOpenAction( this );

    return adapted;
  }

  @Override
  public String getModuleIdentifier( )
  {
    final IKalypsoModule module = ModuleNature.findModule( m_project );
    if( Objects.isNotNull( module ) )
      return module.getId();

    throw new UnsupportedOperationException();
  }
}
