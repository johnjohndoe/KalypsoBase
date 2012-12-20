/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  DenickestraÃŸe 22
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
package org.kalypso.module.internal.projecthandle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.ModuleExtensions;
import org.kalypso.module.internal.Module;
import org.kalypso.module.nature.ModuleNature;
import org.kalypso.module.project.local.ILocalProjectHandle;
import org.kalypso.module.project.local.IProjectOpenAction;

/**
 * @author Gernot Belger
 */
public class ModuleHandlerAdapterFactory implements IAdapterFactory
{
  private static final Class< ? >[] ADAPTER = new Class[] { IKalypsoModule.class, IProjectOpenAction.class };

  @Override
  public Object getAdapter( final Object adaptableObject, final Class adapterType )
  {
    if( !(adaptableObject instanceof ILocalProjectHandle) )
      return null;

    final ILocalProjectHandle localProject = (ILocalProjectHandle) adaptableObject;

    if( adaptableObject instanceof ILocalProjectHandle )
    {
      if( adapterType == IKalypsoModule.class )
        return getModule( localProject );

      if( adapterType == IProjectOpenAction.class )
      {
        final IKalypsoModule module = getModule( localProject );
        return getOpenAction( module, localProject );
      }
    }

    return null;
  }

  private IProjectOpenAction getOpenAction( final IKalypsoModule module, final ILocalProjectHandle localProject )
  {
    return new OpenModuleProjectAction( module, localProject );
  }

  /**
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
   */
  @Override
  public Class< ? >[] getAdapterList( )
  {
    return ADAPTER;
  }

  private IKalypsoModule getModule( final ILocalProjectHandle adaptableObject )
  {
    try
    {
      final IProject project = adaptableObject.getProject();
      final ModuleNature nature = (ModuleNature) project.getNature( ModuleNature.ID );
      if( nature == null )
        return ModuleNature.findModule( project );

      final String moduleID = nature.getModule();
      return ModuleExtensions.getKalypsoModule( moduleID );
    }
    catch( final CoreException e )
    {
      Module.getDefault().getLog().log( e.getStatus() );
    }
    return null;
  }

}
