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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.IAction;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.ModuleExtensions;
import org.kalypso.module.project.local.AbstractProjectHandle;
import org.kalypso.project.database.client.core.base.actions.EmptyProjectAction;
import org.kalypso.project.database.client.core.base.actions.ProjectDownloadAction;
import org.kalypso.project.database.client.core.base.actions.RemoteInfoAction;
import org.kalypso.project.database.client.core.model.projects.IRemoteProject;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class RemoteProjectHandler extends AbstractProjectHandle implements IRemoteProject
{
  private final KalypsoProjectBean m_bean;

  private final IKalypsoModule m_module;

  public RemoteProjectHandler( final KalypsoProjectBean bean )
  {
    m_bean = bean;
    m_module = findModule( bean );
  }

  private IKalypsoModule findModule( final KalypsoProjectBean bean )
  {
    String identifier = bean.getModuleIdentifier();
    if( Strings.isEmpty( identifier ) ) // reverse combability to existing planer client project databases
      identifier = bean.getProjectType();

    final IKalypsoModule[] modules = ModuleExtensions.getKalypsoModules();
    for( final IKalypsoModule module : modules )
    {

      if( identifier.equals( module.getId() ) )
        return module;

      // @backward compatibility for old plc projects
      if( StringUtils.equalsIgnoreCase( identifier, "PlanerClientProject" ) ) //$NON-NLS-1$
      {
        if( StringUtils.equalsIgnoreCase( identifier, "PlanerClientProject" ) ) //$NON-NLS-1$
          return module;
      }
    }

    return null;
  }

  @Override
  public String getName( )
  {
    return m_bean.getName();
  }

  @Override
  public String getUniqueName( )
  {
    return m_bean.getUnixName();
  }

  @Override
  public KalypsoProjectBean getBean( )
  {
    return m_bean;
  }

  @Override
  public String getDescription( )
  {
    return m_bean.getDescription();
  }

  @Override
  public IAction[] getProjectActions( )
  {
    final IAction[] actions = new IAction[5];
    actions[0] = new RemoteInfoAction( this );
    actions[1] = new EmptyProjectAction(); // no delete
    actions[2] = new EmptyProjectAction(); // no export
    actions[3] = new EmptyProjectAction(); // no edit
    actions[4] = new ProjectDownloadAction( m_module, this );
    return actions;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    // FIXME
// if( adapter == IProjectOpenAction.class )
// return new ListRemoteProjectAction( this );

    return super.getAdapter( adapter );
  }

  @Override
  public String toString( )
  {
    return String.format( "Remote Project: %s", getName() ); //$NON-NLS-1$
  }

  @Override
  public String getModuleIdentifier( )
  {
    final String id = m_bean.getModuleIdentifier();
    if( Strings.isNotEmpty( id ) )
      return id;

    /** bad hack for backward compability of kalypso planer client! */
    final String projectType = m_bean.getProjectType();
    if( "PlanerClientProject".equals( projectType ) ) //$NON-NLS-1$
      return "PlanerClientModule"; //$NON-NLS-1$
    else if( "PlanerClientManagerProject".equals( projectType ) || "PlanerClientProjectTemplate".equals( projectType ) ) //$NON-NLS-1$ //$NON-NLS-2$
      return "PlanerClientManagerModule"; //$NON-NLS-1$
    else if( "KalypsRrmModel".equals( projectType ) ) //$NON-NLS-1$
      return "KalypsRrmModel"; //$NON-NLS-1$
    else if( "KalypsoWspmModel".equals( projectType ) ) //$NON-NLS-1$
      return "KalypsoWspmModel"; //$NON-NLS-1$
    else if( "KalypsoFloodModelType".equals( projectType ) ) //$NON-NLS-1$
      return "KalypsoFloodModelType"; //$NON-NLS-1$
    else if( "KalypsoRiskModel".equals( projectType ) ) //$NON-NLS-1$
      return "KalypsoRiskModel"; //$NON-NLS-1$

    throw new UnsupportedOperationException();
  }
}
