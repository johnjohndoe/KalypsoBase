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
package org.kalypso.contribs.eclipse.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * Default implementation of {@link IStatusCollector}.
 * 
 * @author Gernot Belger
 */
public class StatusCollector implements IStatusCollector
{
  private final Collection<IStatus> m_stati = new ArrayList<IStatus>();

  private final String m_pluginID;

  private IStatus m_status;

  /**
   * @param pluginID
   *          All convenience methods (like {@link #addError(String, String)}) will use this plug-in id to create a
   *          {@link IStatus}.
   */
  public StatusCollector( final String pluginID )
  {
    m_pluginID = pluginID;
  }

  @Override
  public IStatus add( final int severity, final String msg )
  {
    return add( severity, msg, null );
  }

  @Override
  public IStatus add( final int severity, final String msgFormat, final Throwable exception, final Object... formatParameters )
  {
    final String msg = String.format( msgFormat, formatParameters );
    final IStatus status = new Status( severity, m_pluginID, msg, exception );
    internalAddStatus( status );
    return status;
  }

  private boolean internalAddStatus( final IStatus status )
  {
    final IStatus tweakedStatus = tweakStatusBeforeAdd( status );
    return m_stati.add( tweakedStatus );
  }

  /**
   * Allows implementors to override how stati are internally added<br/>
   * By default,. the original status is returned.
   */
  protected IStatus tweakStatusBeforeAdd( final IStatus status )
  {
    return status;
  }

  @Override
  public IStatus add( final int severity, final String msg, final Throwable exception )
  {
    return add( severity, msg, exception, new Object[0] );
  }

  @Override
  public IStatus[] getAllStati( )
  {
    return m_stati.toArray( new IStatus[m_stati.size()] );
  }

  @Override
  public MultiStatus asMultiStatus( final String msg )
  {
    final IStatus[] children = getAllStati();
    return createMultiStatus( m_pluginID, IStatus.OK, children, msg, null );
  }

  protected MultiStatus createMultiStatus( final String pluginID, final int code, final IStatus[] children, final String msg, final Throwable exception )
  {
    return new MultiStatus( pluginID, code, children, msg, exception );
  }

  @Override
  public IStatus asMultiStatusOrOK( final String msg )
  {
    for( final IStatus status : m_stati )
    {
      if( !status.isOK() )
        return asMultiStatus( msg );
    }

    return Status.OK_STATUS;
  }

  @Override
  public IStatus asMultiStatusOrOK( final String msg, final String okMessage )
  {
    for( final IStatus status : m_stati )
    {
      if( !status.isOK() )
        return asMultiStatus( msg );
    }

    return new Status( IStatus.OK, m_pluginID, okMessage );
  }

  @Override
  public int size( )
  {
    return m_stati.size();
  }

  @Override
  public boolean isEmpty( )
  {
    return m_stati.isEmpty();
  }

  @Override
  public boolean contains( final Object o )
  {
    return m_stati.contains( o );
  }

  @Override
  public Iterator<IStatus> iterator( )
  {
    return m_stati.iterator();
  }

  @Override
  public Object[] toArray( )
  {
    return m_stati.toArray();
  }

  @Override
  public <T> T[] toArray( final T[] a )
  {
    return m_stati.toArray( a );
  }

  @Override
  public boolean add( final IStatus e )
  {
    return internalAddStatus( e );
  }

  @Override
  public boolean remove( final Object o )
  {
    return m_stati.remove( o );
  }

  @Override
  public boolean containsAll( final Collection< ? > c )
  {
    return m_stati.containsAll( c );
  }

  @Override
  public boolean addAll( final Collection< ? extends IStatus> c )
  {
    boolean changed = false;
    for( final IStatus status : c )
      changed |= internalAddStatus( status );

    return changed;
  }

  @Override
  public boolean removeAll( final Collection< ? > c )
  {
    return m_stati.removeAll( c );
  }

  @Override
  public boolean retainAll( final Collection< ? > c )
  {
    return m_stati.retainAll( c );
  }

  @Override
  public void clear( )
  {
    m_stati.clear();
  }

  @Override
  public boolean equals( final Object o )
  {
    return m_stati.equals( o );
  }

  @Override
  public int hashCode( )
  {
    return m_stati.hashCode();
  }

  @Override
  public boolean isOK( )
  {
    final IStatus[] stati = getAllStati();
    for( final IStatus status : stati )
    {
      if( !status.isOK() )
        return false;
    }

    return true;
  }
}