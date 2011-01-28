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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;

/**
 * A progress monitor, which can log messages to a log.
 * 
 * @author Holger Albert
 */
public class LogProgressMonitor implements IProgressMonitor
{
  /**
   * All requests will be relayed to this progress monitor. It is not allowed to be null, because this monitor only logs
   * messages and does not implement any other functionality concerning progress monitors.
   */
  private IProgressMonitor m_monitor;

  /**
   * The log, where the messages are logged to. It is not allowed to be null.
   */
  private ILog m_log;

  /**
   * The constructor.
   * 
   * @param monitor
   *          All requests will be relayed to this progress monitor. It is not allowed to be null, because this monitor
   *          only logs messages and does not implement any other functionality concerning progress monitors.
   * @param log
   *          The log, where the messages are logged to. It is not allowed to be null.
   */
  public LogProgressMonitor( IProgressMonitor monitor, ILog log )
  {
    /* The progress monitor and the log are not allowed to be null. */
    Assert.isNotNull( monitor );
    Assert.isNotNull( log );

    /* Initialize the members. */
    m_monitor = monitor;
    m_log = log;
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
   */
  @Override
  public void beginTask( String name, int totalWork )
  {
    /* Log. */
    m_log.log( new Status( IStatus.INFO, EclipseRCPContributionsPlugin.ID, name ) );

    /* Delegate to the progress monitor. */
    m_monitor.beginTask( name, totalWork );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#done()
   */
  @Override
  public void done( )
  {
    /* Log. */
    m_log.log( new Status( IStatus.INFO, EclipseRCPContributionsPlugin.ID, "Done" ) );

    /* Delegate to the progress monitor. */
    m_monitor.done();
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
   */
  @Override
  public void internalWorked( double work )
  {
    /* Delegate to the progress monitor. */
    m_monitor.internalWorked( work );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
   */
  @Override
  public boolean isCanceled( )
  {
    /* Delegate to the progress monitor. */
    return m_monitor.isCanceled();
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
   */
  @Override
  public void setCanceled( boolean value )
  {
    /* Log. */
    if( value )
      m_log.log( new Status( IStatus.CANCEL, EclipseRCPContributionsPlugin.ID, "Canceled..." ) );

    /* Delegate to the progress monitor. */
    m_monitor.setCanceled( value );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
   */
  @Override
  public void setTaskName( String name )
  {
    /* Log. */
    m_log.log( new Status( IStatus.INFO, EclipseRCPContributionsPlugin.ID, name ) );

    /* Delegate to the progress monitor. */
    m_monitor.setTaskName( name );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
   */
  @Override
  public void subTask( String name )
  {
    /* Delegate to the progress monitor. */
    m_monitor.subTask( name );

    /* Log. */
    m_log.log( new Status( IStatus.INFO, EclipseRCPContributionsPlugin.ID, String.format( "  %s", name ) ) );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
   */
  @Override
  public void worked( int work )
  {
    /* Delegate to the progress monitor. */
    m_monitor.worked( work );
  }
}