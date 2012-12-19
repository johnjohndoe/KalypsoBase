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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.IKalypsoModuleProjectOpenAction;
import org.kalypso.module.project.local.ILocalProjectHandle;
import org.kalypso.module.project.local.actions.ProjectOpenAction;

/**
 * @author Gernot Belger
 */
public class OpenModuleProjectAction extends ProjectOpenAction
{
  private final IKalypsoModule m_module;

  private final ILocalProjectHandle m_item;

  public OpenModuleProjectAction( final IKalypsoModule module, final ILocalProjectHandle item )
  {
    super( item );

    m_module = module;
    m_item = item;
  }

  /**
   * @see org.kalypso.core.projecthandle.local.ProjectOpenAction#doOpenProject()
   */
  @Override
  protected IStatus doOpenProject( )
  {
    try
    {
      if( m_module == null )
        return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), "Miss-configured module" ); //$NON-NLS-1$

      final IKalypsoModuleProjectOpenAction action = m_module.getProjectOpenAction();
      final IProject project = m_item.getProject();
      return action.open( project );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return e.getStatus();
    }
    catch( final Exception e )
    {
      final String msg = String.format( "Unexpected error: %s", e.getLocalizedMessage() ); //$NON-NLS-1$
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg, e );
    }
  }
}