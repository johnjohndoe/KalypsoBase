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
package org.kalypso.core.util.pool;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.core.KalypsoCoreDebug;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.loader.ILoader;
import org.kalypso.loader.ISaveUrnLoader;
import org.kalypso.loader.LoaderException;

public final class KeyInfo extends Job
{
  private final Collection<IPoolListener> m_listeners = Collections.synchronizedSet( new HashSet<IPoolListener>() );

  private Object m_object = null;

  private final ILoader m_loader;

  private final IPoolableObjectType m_key;

  /** Flag, indicating if the associated object needs saving. */
  private boolean m_isDirty = false;

  private final Map<IResource, Boolean> m_resources = new HashMap<IResource, Boolean>();

  /**
   * This flag becomes true, if the saving process is started and the resources are locked. It will be resetted after
   * the first reloading was blocked.
   */
  private boolean m_hasNothingBlocked = false;

  private final Map<IPoolListener, Exception> m_addListenerTraces = Collections.synchronizedMap( new HashMap<IPoolListener, Exception>() );

  private IStatus m_result;

  public KeyInfo( final IPoolableObjectType key, final ILoader loader )
  {
    super( Messages.getString( "org.kalypso.util.pool.KeyInfo.1", key.getLocation() ) ); //$NON-NLS-1$

    m_key = key;
    m_loader = loader;

    try
    {
      final IResource[] resources = m_loader.getResources( m_key );
      for( final IResource resource : resources )
        m_resources.put( resource, Boolean.FALSE );
    }
    catch( final MalformedURLException e )
    {
      // Save to just ignore it; later we will get the same exception when invoking load on the loader
      e.printStackTrace();
    }

    setPriority( Job.LONG );
    setProperty( IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE );
  }

  public void dispose( )
  {
    KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( Messages.getString("KeyInfo.0"), m_key ); //$NON-NLS-1$

    m_listeners.clear();

    cancel();

    releaseObject();
  }

  public void addListener( final IPoolListener l )
  {
    KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( "Adding Pool-Listener to key: %s%n", m_key ); //$NON-NLS-1$
    final int state = getState();
    KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( "Current Pool-Job state: %d%n", state ); //$NON-NLS-1$

    final Object o;

    synchronized( this )
    {
      m_listeners.add( l );
      m_addListenerTraces.put( l, new Exception() );

      o = m_object;
    }

    if( o != null )
    {
      l.objectLoaded( m_key, o, Status.OK_STATUS );
    }
    else if( state == Job.NONE )
    {
      KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( "Scheduling Pool-Info for key: %s%n", m_key ); //$NON-NLS-1$
      schedule();
    }
  }

  public boolean removeListener( final IPoolListener l )
  {
    m_addListenerTraces.remove( l );
    return m_listeners.remove( l );
  }

  private void reloadInternal( )
  {
    // check, if any of our registered resources is locked for load
    if( isLocked() )
      return;

    KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( "Reloading '%s'...%n", m_key ); //$NON-NLS-1$

    synchronized( this )
    {
      final int state = getState();
      KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( Messages.getString("KeyInfo.1"), state ); //$NON-NLS-1$

      cancel();
      schedule();
    }
  }

  private boolean isLocked( )
  {
    if( m_hasNothingBlocked )
    {
      //System.out.println( "Locked due to reloading event state ..." ); //$NON-NLS-1$

      /* The first call on this function after locking the resources will reset the state of the first reloading event. */
      m_hasNothingBlocked = false;

      /* This time, it counts still as locked. */
      return true;
    }

    for( final Entry<IResource, Boolean> entry : m_resources.entrySet() )
    {
      if( entry.getValue() )
      {
        //System.out.println( "Locked due to resource state ..." ); //$NON-NLS-1$
        return true;
      }
    }

    return false;
  }

  private void fireObjectLoaded( final Object o, final IStatus status )
  {
    // TRICKY: objectLoaded may add a new PoolListener for this key,
    // so we cannot iterate over m_listeners
    final IPoolListener[] ls = m_listeners.toArray( new IPoolListener[m_listeners.size()] );
    for( final IPoolListener element : ls )
      element.objectLoaded( m_key, o, status );
  }

  private void fireObjectInvalid( final Object oldObject )
  {
    // TRICKY: objectInvalid may add/remove PoolListener for this key,
    // so we cannot iterate over m_listeners
    final IPoolListener[] ls = m_listeners.toArray( new IPoolListener[m_listeners.size()] );
    for( final IPoolListener element : ls )
      element.objectInvalid( m_key, oldObject );
  }

  private void fireDirtyChanged( final boolean isDirty )
  {
    // TRICKY: objectInvalid may add/remove PoolListener for this key,
    // so we cannot iterate over m_listeners
    final IPoolListener[] ls = m_listeners.toArray( new IPoolListener[m_listeners.size()] );
    for( final IPoolListener element : ls )
      element.dirtyChanged( m_key, isDirty );
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    final IStatus status = loadObject( monitor );
    fireObjectLoaded( m_object, status );
    return status;
  }

  protected synchronized IStatus loadObject( final IProgressMonitor monitor )
  {
    m_result = doLoadObject( monitor );
    return m_result;
  }

  public IStatus getJobResult( )
  {
    return m_result;
  }

  private synchronized IStatus doLoadObject( final IProgressMonitor monitor )
  {
    try
    {
      releaseObject();

      KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( "Loading object for key: %s%n", m_key );//$NON-NLS-1$

      m_object = m_loader.load( m_key, monitor );

      if( monitor.isCanceled() )
      {
        m_object = null;
        return Status.CANCEL_STATUS;
      }

      return m_loader.getStatus();
    }
    catch( final CoreException ce )
    {
      final IStatus status = ce.getStatus();
      if( status.matches( IStatus.CANCEL ) || m_key.isIgnoreExceptions() )
        return Status.CANCEL_STATUS;

      return status;
    }
    catch( final Throwable e )
    {
      if( m_key.isIgnoreExceptions() )
        return Status.CANCEL_STATUS;

      final Throwable cause = e.getCause();
      if( cause instanceof CoreException )
      {
        final CoreException core = (CoreException) cause;
        final IStatus status = core.getStatus();
        if( status.matches( IStatus.CANCEL ) )
          return status;
      }

      e.printStackTrace();

      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), e.getLocalizedMessage(), e );
    }
  }

  private synchronized void releaseObject( )
  {
    m_isDirty = false;
    if( m_object == null )
      return;

    KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( Messages.getString("KeyInfo.2"), m_key ); //$NON-NLS-1$

    m_loader.release( m_object );
    m_object = null;
  }

  public boolean isEmpty( )
  {
    return m_listeners.isEmpty();
  }

  public void saveObject( final IProgressMonitor monitor ) throws LoaderException
  {
    synchronized( this )
    {
      try
      {
        // Lock next load
        // REMARK/TRICKY: we assume, that all resource-change-events will be
        // sent in this very thread. So we can savely lock/unlock resource
        // loading here. If resource change events will be sent in another thread,
        // we may trigger a reload here... (how to avoid this?)
        for( final Entry<IResource, Boolean> entry : m_resources.entrySet() )
          entry.setValue( Boolean.TRUE );

        /* The first reloading event should always be blocked, regardless the locking state. */
        m_hasNothingBlocked = true;

        /* Save. */
        m_loader.save( m_key, monitor, m_object );
      }
      finally
      {
        /* The state of the first reloading event is not resetted here, but on the first reloading event. */
        /* This makes sure, that at least one reloading process was blocked. */
        /* This makes sense, if the resource change event, which initiates the reloading process, */
        /* comes, after the resources were already unlocked. */

        /* Unlock the next load. */
        for( final Entry<IResource, Boolean> entry : m_resources.entrySet() )
          entry.setValue( Boolean.FALSE );
      }
    }

    setDirty( false );
  }

  @Override
  public String toString( )
  {
    final StringBuffer b = new StringBuffer();
    b.append( Messages.getString( "org.kalypso.util.pool.KeyInfo.6" ) ); //$NON-NLS-1$
    if( m_object != null )
    {
      b.append( Messages.getString( "org.kalypso.util.pool.KeyInfo.7" ) + m_object.getClass().getName() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else
    {
      b.append( Messages.getString( "org.kalypso.util.pool.KeyInfo.9" ) ); //$NON-NLS-1$
    }
    b.append( Messages.getString( "org.kalypso.util.pool.KeyInfo.10" ) + m_loader.getClass().getName() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    b.append( Messages.getString( "org.kalypso.util.pool.KeyInfo.12" ) + m_key + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    b.append( Messages.getString( "org.kalypso.util.pool.KeyInfo.14" ) + m_listeners.size() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    return b.toString();
  }

  public IPoolableObjectType getKey( )
  {
    return m_key;
  }

  public IPoolListener[] getPoolListeners( )
  {
    return m_listeners.toArray( new IPoolListener[m_listeners.size()] );
  }

  public Exception getAddTrace( final IPoolListener l )
  {
    return m_addListenerTraces.get( l );
  }

  public Object getObject( )
  {
    return m_object;
  }

  public boolean isDirty( )
  {
    return m_isDirty;
  }

  public void setDirty( final boolean isDirty )
  {
    synchronized( this )
    {
      if( m_isDirty == isDirty )
        return;
      m_isDirty = isDirty;
    }

    fireDirtyChanged( isDirty );
  }

  /**
   * Reloads the pool object if it is dirty.
   */
  public void reload( )
  {
    reload( false );
  }

  /**
   * Reloads the pool object.
   *
   * @param force
   *          If <code>false</code>, the object only is reloaded if it is dirty.
   */
  public void reload( final boolean force )
  {
    if( !force && !isDirty() )
      return;

    try
    {
      if( force )
        m_hasNothingBlocked = false;

      reloadInternal();
      setDirty( false );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  public void handleResourceChanged( final IResourceDelta delta )
  {
    final List<IResourceDelta> deltas = new ArrayList<IResourceDelta>( m_resources.size() );

    for( final IResource resource : m_resources.keySet() )
    {
      if( resource != null )
      {
        final IResourceDelta resourceDelta = delta.findMember( resource.getFullPath() );
        if( resourceDelta != null )
          deltas.add( resourceDelta );
      }
    }

    if( deltas.size() == 0 )
      return;

    for( final IResourceDelta resourceDelta : deltas )
    {
      // TODO: handle case for more than one resource
      checkDelta( resourceDelta );
    }
  }

  /**
   * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
   */
  private void checkDelta( final IResourceDelta delta )
  {
    final int flags = delta.getFlags();
    final int kind = delta.getKind();

    // System.out.println( String.format( "Resource change (kind): %d", kind ) );
    // System.out.println( String.format( "Resource change (flags): %d", flags ) );

    // TRICKY: if (exactly) only markers have changed, do nothing
    if( flags == IResourceDelta.MARKERS || flags == IResourceDelta.SYNC || flags == (IResourceDelta.MARKERS | IResourceDelta.SYNC) )
    {
      // System.out.println( "Resource change (action): Ignored" );
      return;
    }

    // System.out.println( "Resource change (action): Handled" );
    switch( kind )
    {
      case IResourceDelta.REMOVED:
        final Object oldObject = m_object;
        if( oldObject != null )
        {
          KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( Messages.getString("KeyInfo.3"), m_key ); //$NON-NLS-1$
          KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( Messages.getString("KeyInfo.4"), m_key ); //$NON-NLS-1$
          m_loader.release( m_object );
          m_object = null;
          fireObjectInvalid( oldObject );
        }
        return;

      case IResourceDelta.ADDED:
      case IResourceDelta.CHANGED:
      {
        KalypsoCoreDebug.RESOURCE_POOL_KEYS.printf( Messages.getString("KeyInfo.5"), m_key ); //$NON-NLS-1$
        reloadInternal();
        return;
      }
      default:
        return;
    }
  }

  public ILoader getLoader( )
  {
    return m_loader;
  }

  public boolean isSaveable( )
  {
    try
    {
      final ILoader loader = getLoader();
      final IResource[] resources = loader.getResources( getKey() );
      if( resources.length == 0 )
        return isSaveUrnSupported();
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
    }
    return true;
  }

  private boolean isSaveUrnSupported( )
  {
    final ILoader loader = getLoader();
    if( loader instanceof ISaveUrnLoader )
    {
      final String location = m_key.getLocation();
      return CatalogUtilities.isCatalogResource( location );
    }

    return false;
  }
}