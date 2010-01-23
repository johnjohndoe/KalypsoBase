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
package org.kalypso.simulation.core.ant;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Delegating {@link IProgressMonitor} implementation that produces nicer messages.
 * 
 * @author Gernot Belger
 */
public class AntProgressMonitor implements IProgressMonitor
{
  private final IProgressMonitor m_delegate;

  private final String m_subTaskPrefix;

  public AntProgressMonitor( final IProgressMonitor delegate, final String subTaskPrefix )
  {
    m_delegate = delegate;
    m_subTaskPrefix = subTaskPrefix;
  }

  /**
   * @param name
   * @param totalWork
   * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
   */
  public void beginTask( final String name, final int totalWork )
  {
    m_delegate.beginTask( name, totalWork );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#done()
   */
  public void done( )
  {
    m_delegate.done();
  }

  /**
   * @param work
   * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
   */
  public void internalWorked( final double work )
  {
    m_delegate.internalWorked( work );
  }

  /**
   * @return
   * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
   */
  public boolean isCanceled( )
  {
    return m_delegate.isCanceled();
  }

  /**
   * @param value
   * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
   */
  public void setCanceled( final boolean value )
  {
    m_delegate.setCanceled( value );
  }

  /**
   * @param name
   * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
   */
  public void setTaskName( final String name )
  {
    m_delegate.setTaskName( name );
  }

  /**
   * @param name
   * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
   */
  public void subTask( final String name )
  {
    final String subTaskName = String.format( "%s - %s", m_subTaskPrefix, name );

    m_delegate.subTask( subTaskName );
  }

  /**
   * @param work
   * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
   */
  public void worked( final int work )
  {
    m_delegate.worked( work );
  }

}