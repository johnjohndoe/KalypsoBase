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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.util.pool.IPoolListener;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyComparator;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * @author Gernot Belger
 */
public class PoolGmlWorkspaceProvider extends AbstractGmlWorkspaceProvider implements ILoadStartable
{
  public static final IStatus LOADING_STATUS = new Status( IStatus.INFO, KalypsoCorePlugin.getID(), Messages.getString("PoolGmlWorkspaceProvider.0") ); //$NON-NLS-1$

  private final IPoolListener m_poolListener = new IPoolListener()
  {
    @Override
    public void objectLoaded( final IPoolableObjectType key, final Object newValue, final IStatus status )
    {
      handleObjectLoaded( key, newValue, status );
    }

    @Override
    public void objectInvalid( final IPoolableObjectType key, final Object oldValue )
    {
      handleObjectInvalid( key );
    }

    @Override
    public boolean isDisposed( )
    {
      return false;
    }

    @Override
    public void dirtyChanged( final IPoolableObjectType key, final boolean isDirty )
    {
      // ignored
    }
  };

  private final IPoolableObjectType m_poolKey;

  public PoolGmlWorkspaceProvider( final IPoolableObjectType poolKey )
  {
    m_poolKey = poolKey;
  }

  @Override
  public void dispose( )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    pool.removePoolListener( m_poolListener );

    super.dispose();
  }

  @Override
  public void startLoading( )
  {
    setWorkspace( null, LOADING_STATUS );

    try
    {
      final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
      pool.addPoolListener( m_poolListener, m_poolKey );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final String msg = Messages.getString( "org.kalypso.ogc.gml.PoolGmlWorkspaceProvider.4", m_poolKey.getLocation() ); //$NON-NLS-1$
      setWorkspace( null, new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg, e ) );
    }
  }

  void handleObjectLoaded( final IPoolableObjectType key, final Object newValue, final IStatus status )
  {
    if( KeyComparator.getInstance().compare( key, m_poolKey ) != 0 )
      return;

    final CommandableWorkspace commandableWorkspace = (CommandableWorkspace) newValue;
    setWorkspace( commandableWorkspace, status );
  }

  void handleObjectInvalid( final IPoolableObjectType key )
  {
    if( KeyComparator.getInstance().compare( key, m_poolKey ) != 0 )
      return;

    // clear the theme
    setWorkspace( null, new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), "" ) ); //$NON-NLS-1$
  }

  public IPoolableObjectType getPoolKey( )
  {
    return m_poolKey;
  }

  private KeyInfo getInfo( )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo info = pool.getInfoForKey( m_poolKey );
    return info;
  }

  @Override
  public void save( final IProgressMonitor monitor ) throws CoreException
  {
    final KeyInfo info = getInfo();
     if( info == null )
      return;
      
    if( info.isDirty() )
      info.saveObject( monitor );
  }

  public boolean isDirty( )
  {
    final KeyInfo info = getInfo();
    if( info == null )
      return false;

    return info.isDirty();
  }

  public void reload( final boolean evenIfDirty )
  {
    final KeyInfo info = getInfo();
    info.reload( evenIfDirty );
  }
}