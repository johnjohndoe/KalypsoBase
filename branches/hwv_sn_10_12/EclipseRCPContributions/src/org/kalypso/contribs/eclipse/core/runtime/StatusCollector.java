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
  public void add( final int severity, final String msg )
  {
    add( severity, msg, null );
  }

  @Override
  public void add( final int severity, final String msgFormat, final Throwable exception, final Object... formatParameters )
  {
    final String msg = String.format( msgFormat, formatParameters );
    m_stati.add( new Status( severity, m_pluginID, msg, exception ) );
  }

  @Override
  public void add( final int severity, final String msg, final Throwable exception )
  {
    add( severity, msg, exception, new Object[0] );
  }

  /**
   * @see org.kalypso.contribs.eclipse.core.runtime.IStatusCollector#getAllStati()
   */
  @Override
  public IStatus[] getAllStati( )
  {
    return m_stati.toArray( new IStatus[m_stati.size()] );
  }

  @Override
  public MultiStatus asMultiStatus( final String msg )
  {
    final IStatus[] children = getAllStati();
    return new MultiStatus( m_pluginID, IStatus.OK, children, msg, null );
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

  /**
   * @return
   * @see java.util.Collection#size()
   */
  @Override
  public int size( )
  {
    return m_stati.size();
  }

  /**
   * @return
   * @see java.util.Collection#isEmpty()
   */
  @Override
  public boolean isEmpty( )
  {
    return m_stati.isEmpty();
  }

  /**
   * @param o
   * @return
   * @see java.util.Collection#contains(java.lang.Object)
   */
  @Override
  public boolean contains( final Object o )
  {
    return m_stati.contains( o );
  }

  /**
   * @return
   * @see java.util.Collection#iterator()
   */
  @Override
  public Iterator<IStatus> iterator( )
  {
    return m_stati.iterator();
  }

  /**
   * @return
   * @see java.util.Collection#toArray()
   */
  @Override
  public Object[] toArray( )
  {
    return m_stati.toArray();
  }

  /**
   * @param <T>
   * @param a
   * @return
   * @see java.util.Collection#toArray(T[])
   */
  @Override
  public <T> T[] toArray( final T[] a )
  {
    return m_stati.toArray( a );
  }

  /**
   * @param e
   * @return
   * @see java.util.Collection#add(java.lang.Object)
   */
  @Override
  public boolean add( final IStatus e )
  {
    return m_stati.add( e );
  }

  /**
   * @param o
   * @return
   * @see java.util.Collection#remove(java.lang.Object)
   */
  @Override
  public boolean remove( final Object o )
  {
    return m_stati.remove( o );
  }

  /**
   * @param c
   * @return
   * @see java.util.Collection#containsAll(java.util.Collection)
   */
  @Override
  public boolean containsAll( final Collection< ? > c )
  {
    return m_stati.containsAll( c );
  }

  /**
   * @param c
   * @return
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  @Override
  public boolean addAll( final Collection< ? extends IStatus> c )
  {
    return m_stati.addAll( c );
  }

  /**
   * @param c
   * @return
   * @see java.util.Collection#removeAll(java.util.Collection)
   */
  @Override
  public boolean removeAll( final Collection< ? > c )
  {
    return m_stati.removeAll( c );
  }

  /**
   * @param c
   * @return
   * @see java.util.Collection#retainAll(java.util.Collection)
   */
  @Override
  public boolean retainAll( final Collection< ? > c )
  {
    return m_stati.retainAll( c );
  }

  /**
   * @see java.util.Collection#clear()
   */
  @Override
  public void clear( )
  {
    m_stati.clear();
  }

  /**
   * @param o
   * @return
   * @see java.util.Collection#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object o )
  {
    return m_stati.equals( o );
  }

  /**
   * @return
   * @see java.util.Collection#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return m_stati.hashCode();
  }
}
