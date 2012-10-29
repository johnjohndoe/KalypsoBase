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
package org.kalypso.module.project.local;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.kalypso.module.project.IProjectHandle;

/**
 * @author Gernot Belger
 */
public abstract class AbstractProjectHandleProvider implements IProjectHandleProvider, IExecutableExtension
{
  private final Set<IProjectHandlesChangedListener> m_listeners = new HashSet<>();

  private String m_id;

  @Override
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    m_id = config.getAttribute( ProjectHandleExtensions.ATTRIBUTE_ID );
  }

  @Override
  public String getID( )
  {
    return m_id;
  }

  @Override
  public void addProviderChangedListener( final IProjectHandlesChangedListener listener )
  {
    m_listeners.add( listener );
  }

  /**
   * @see org.kalypso.module.workspace.IProjectItemProvder#removeProviderChangedListener(org.kalypso.module.workspace.IProjectItemProviderChangedListener)
   */
  @Override
  public void removeProviderChangedListener( final IProjectHandlesChangedListener listener )
  {
    m_listeners.remove( listener );
  }

  public void fireItemsChanged( )
  {
    for( final IProjectHandlesChangedListener listener : m_listeners )
      listener.itemsChanged();
  }

  @Override
  public void dispose( )
  {
    m_listeners.clear();
  }

  /**
   * @see org.kalypso.core.projecthandle.IProjectHandleProvder#findProject(java.lang.String)
   */
  @Override
  public IProjectHandle findProject( final String uniqueName )
  {
    final IProjectHandle[] projects = getProjects();
    for( final IProjectHandle handle : projects )
    {
      if( handle.getUniqueName().equals( uniqueName ) )
        return handle;
    }

    return null;
  }
}
