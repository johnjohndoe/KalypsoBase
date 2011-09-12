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
 * A progress monitor which logs monitor messages to System.out.
 * 
 * @author Holger Albert
 */
public class SystemOutProgressMonitor implements IProgressMonitor
{
  /**
   * This is the delegate monitor.
   */
  private IProgressMonitor m_monitor;

  /**
   * The total work.
   */
  private int m_totalWork;

  /**
   * Done work.
   */
  private int m_worked;

  /**
   * The start time in millies.
   */
  private long m_started;

  /**
   * The checkpoint time in millies.
   */
  private long m_checkpoint;

  /**
   * The end time in millies.
   */
  private long m_ended;

  /**
   * The constructor.
   * 
   * @param monitor
   *          A progress monitor.
   */
  public SystemOutProgressMonitor( IProgressMonitor monitor )
  {
    m_monitor = monitor;
    m_totalWork = 0;
    m_worked = 0;
    m_started = 0;
    m_checkpoint = 0;
    m_ended = 0;
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
   */
  @Override
  public void beginTask( String name, int totalWork )
  {
    /* Memorize the total work. */
    m_totalWork = totalWork;

    /* Time. */
    m_started = System.currentTimeMillis();
    m_checkpoint = m_started;

    /* Log the message to System.out. */
    System.out.println( String.format( "Begin task (%d): %s", totalWork, name ) );
    System.out.println( String.format( "Started at: %d", m_started ) );

    m_monitor.beginTask( name, totalWork );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#done()
   */
  @Override
  public void done( )
  {
    /* Time. */
    m_checkpoint = System.currentTimeMillis();
    m_ended = m_checkpoint;

    /* Log the message to System.out. */
    System.out.println( "Done ..." );
    System.out.println( String.format( "Ended at: %d", m_ended ) );
    System.out.println( String.format( "Time elapsed: %d seconds", (m_ended - m_started) / 1000 ) );

    m_monitor.done();
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
   */
  @Override
  public void internalWorked( double work )
  {
    m_monitor.internalWorked( work );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
   */
  @Override
  public boolean isCanceled( )
  {
    return m_monitor.isCanceled();
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
   */
  @Override
  public void setCanceled( boolean value )
  {
    /* Time. */
    long checkpoint = System.currentTimeMillis();

    /* Log the message to System.out. */
    System.out.println( "Should be canceled ..." );
    System.out.println( String.format( "Checkpoint at: %d", checkpoint ) );
    System.out.println( String.format( "Time elapsed: %d seconds", (checkpoint - m_checkpoint) / 1000 ) );

    /* Time. */
    m_checkpoint = checkpoint;

    m_monitor.setCanceled( value );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
   */
  @Override
  public void setTaskName( String name )
  {
    /* Time. */
    long checkpoint = System.currentTimeMillis();

    /* Log the message to System.out. */
    System.out.println( String.format( "Task name: %s", name ) );
    System.out.println( String.format( "Checkpoint at: %d", checkpoint ) );
    System.out.println( String.format( "Time elapsed: %d seconds", (checkpoint - m_checkpoint) / 1000 ) );

    /* Time. */
    m_checkpoint = checkpoint;

    m_monitor.setTaskName( name );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
   */
  @Override
  public void subTask( String name )
  {
    /* Time. */
    long checkpoint = System.currentTimeMillis();

    /* Log the message to System.out. */
    System.out.println( String.format( "Sub task: %s", name ) );
    System.out.println( String.format( "Checkpoint at: %d", checkpoint ) );
    System.out.println( String.format( "Time elapsed: %d seconds", (checkpoint - m_checkpoint) / 1000 ) );

    /* Time. */
    m_checkpoint = checkpoint;

    m_monitor.subTask( name );
  }

  /**
   * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
   */
  @Override
  public void worked( int work )
  {
    /* Time. */
    long checkpoint = System.currentTimeMillis();

    /* Increase the work done. */
    m_worked = m_worked + work;

    /* Log the message to System.out. */
    System.out.println( String.format( "Worked (%d / %d): %d", m_worked, m_totalWork, work ) );
    System.out.println( String.format( "Checkpoint at: %d", checkpoint ) );
    System.out.println( String.format( "Time elapsed: %d seconds", (checkpoint - m_checkpoint) / 1000 ) );

    /* Time. */
    m_checkpoint = checkpoint;

    m_monitor.worked( work );
  }
}