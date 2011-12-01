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
package org.kalypso.ogc.gml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * @author Gernot Belger
 */
public abstract class AbstractGmlWorkspaceProvider implements IGmlWorkspaceProvider
{
  private final Collection<IGmlWorkspaceProviderListener> m_listeners = Collections.synchronizedSet( new HashSet<IGmlWorkspaceProviderListener>() );

  private CommandableWorkspace m_workspace;

  private IStatus m_status;

  /**
   * @see org.kalypso.ogc.gml.IGmlWorkspaceProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    m_listeners.clear();
  }

  /**
   * @see org.kalypso.ogc.gml.IGmlWorkspaceProvider#addListener(org.kalypso.ogc.gml.IGmlWorkspaceProviderListener)
   */
  @Override
  public final void addListener( final IGmlWorkspaceProviderListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see org.kalypso.ogc.gml.IGmlWorkspaceProvider#removeListener(org.kalypso.ogc.gml.IGmlWorkspaceProviderListener)
   */
  @Override
  public final void removeListener( final IGmlWorkspaceProviderListener l )
  {
    m_listeners.remove( l );
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getWorkspace()
   */
  @Override
  public final CommandableWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  /**
   * @see org.kalypso.ogc.gml.IGmlWorkspaceProvider#getStatus()
   */
  @Override
  public final IStatus getStatus( )
  {
    return m_status;
  }

  protected final void setWorkspace( final CommandableWorkspace workspace, final IStatus status )
  {
    final CommandableWorkspace oldWorkspace = m_workspace;

    m_workspace = workspace;
    m_status = status;

    fireOnWorkspaceChanged( oldWorkspace, workspace );
  }

  protected final void fireOnWorkspaceChanged( final CommandableWorkspace oldWorkspace, final CommandableWorkspace workspace )
  {
    final IGmlWorkspaceProviderListener[] listeners = m_listeners.toArray( new IGmlWorkspaceProviderListener[m_listeners.size()] );
    for( final IGmlWorkspaceProviderListener listener : listeners )
    {
      SafeRunner.run( new SafeRunnable( "Failed to inform listener" )
      {
        @Override
        public void run( ) throws Exception
        {
          listener.workspaceChanged( oldWorkspace, workspace );
        }
      } );
    }
  }

}
