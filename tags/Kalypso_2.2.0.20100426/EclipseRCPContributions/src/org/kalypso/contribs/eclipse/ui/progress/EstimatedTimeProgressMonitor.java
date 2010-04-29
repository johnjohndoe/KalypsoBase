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
package org.kalypso.contribs.eclipse.ui.progress;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A progress monitor, which tracks the time.<br>
 * <br>
 * ATTENTION: The calculation of the estimated time only works correctly, if {@link #worked(int)} is called on a regular
 * basis.
 * 
 * @author Holger Albert
 */
public class EstimatedTimeProgressMonitor implements IProgressMonitor
{
  /**
   * This is the delegate monitor.
   */
  private IProgressMonitor m_monitor;

  /**
   * This object keeps track of the time.
   */
  private EstimatedTime m_estimatedTime;

  /**
   * The constructor.
   * 
   * @param monitor
   *          A progress monitor.
   */
  public EstimatedTimeProgressMonitor( IProgressMonitor monitor )
  {
    m_monitor = monitor;
    m_estimatedTime = null;
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
   */
  public void beginTask( String name, int totalWork )
  {
    m_monitor.beginTask( name, totalWork );

    m_estimatedTime = new EstimatedTime( totalWork );
    m_estimatedTime.setStart();
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#done()
   */
  public void done( )
  {
    m_monitor.done();

    m_estimatedTime.setEnd();
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
   */
  public void internalWorked( double work )
  {
    m_monitor.internalWorked( work );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
   */
  public boolean isCanceled( )
  {
    return m_monitor.isCanceled();
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
   */
  public void setCanceled( boolean value )
  {
    m_monitor.setCanceled( value );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
   */
  public void setTaskName( String name )
  {
    m_monitor.setTaskName( name );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
   */
  public void subTask( String name )
  {
    m_monitor.subTask( name );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
   */
  public void worked( int work )
  {
    m_monitor.worked( work );

    m_estimatedTime.increaseItemsDone( work );
    m_estimatedTime.setEnd();
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.progress.EstimatedTime#getEstimatedTime()
   */
  public String getEstimatedTime( )
  {
    return m_estimatedTime.getEstimatedTime();
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.progress.EstimatedTime#getExceededTime()
   */
  public String getExceededTime( )
  {
    return m_estimatedTime.getExceededTime();
  }
}