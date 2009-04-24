/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.loader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.command.ICommandManager;
import org.kalypso.commons.command.ICommandManagerListener;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.loader.AbstractLoader;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * Abstract helper for all loader's that produce a {@link GMLWorkspace}.
 * @author Holger Albert
 * @author Gernot Belger
 */
public abstract class WorkspaceLoader extends AbstractLoader
{
  private CommandableWorkspace m_workspace = null;

  /** A special command listener, which sets the dirty flag on the corresponding KeyInfo for the loaded workspace. */
  private final ICommandManagerListener m_commandManagerListener = new ICommandManagerListener()
  {
    public void onCommandManagerChanged( final ICommandManager source )
    {
      handleCommandManagerChanged( source );
    }
  };

  /**
   * @see org.kalypso.loader.ILoader#load(org.kalypso.core.util.pool.IPoolableObjectType, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Object load( final IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException
  {
    final CommandableWorkspace workspace = loadIntern( key, monitor );
    workspace.addCommandManagerListener( m_commandManagerListener );
    m_workspace = workspace;
    return workspace;
  }
  
  protected abstract CommandableWorkspace loadIntern( IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException;

  /**
   * @see org.kalypso.loader.AbstractLoader#release(java.lang.Object)
   */
  @Override
  public final void release( final Object object )
  {
    final CommandableWorkspace workspace = (CommandableWorkspace) object;
    workspace.removeCommandManagerListener( m_commandManagerListener );
    workspace.dispose();
    m_workspace = null;
  }

  /*default*/ void handleCommandManagerChanged( final ICommandManager source )
  {
    if( m_workspace != null && m_workspace.getCommandManager() == source )
    {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo info = pool.getInfo( m_workspace );
    if( info != null )
      info.setDirty( source.isDirty() );
    }
  }
}