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
package org.kalypso.simulation.core;

import java.io.PrintStream;

import org.eclipse.core.runtime.Status;

/**
 * A {@link ISimulationMonitor} that prints everything into the console for testing purposes.
 * 
 * @author Gernot Belger
 */
public class TestSimulationMonitor implements ISimulationMonitor
{
  private final PrintStream m_out;

  private final String m_messagePrefix;

  private boolean m_canceled;

  private int m_progress;

  private String m_message;

  private Status m_info;

  /**
   * @param out
   *          All events to this monitor are written as message into this writer.
   * @param messagePrefix
   *          Every message is prefixed with this string.
   */
  public TestSimulationMonitor( final PrintStream out, final String messagePrefix )
  {
    m_out = out;
    m_messagePrefix = messagePrefix;
  }

  private void println( final String message )
  {
    m_out.format( "%s%s%n", m_messagePrefix, message );
  }

  /**
   * @see org.kalypso.contribs.java.lang.ICancelable#cancel()
   */
  @Override
  public void cancel( )
  {
    println( "Cancel" );
    m_canceled = true;
  }

  /**
   * @see org.kalypso.contribs.java.lang.ICancelable#isCanceled()
   */
  @Override
  public boolean isCanceled( )
  {
    return m_canceled;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setProgress(int)
   */
  @Override
  public void setProgress( final int progress )
  {
    m_progress = progress;
    println( String.format( "Progress %d%%", progress ) );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getProgress()
   */
  @Override
  public int getProgress( )
  {
    return m_progress;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getMessage()
   */
  @Override
  public String getMessage( )
  {
    return m_message;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setMessage(java.lang.String)
   */
  @Override
  public void setMessage( final String message )
  {
    m_message = message;
    println( String.format( "Message '%s'", m_message ) );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setFinishInfo(int, java.lang.String)
   */
  @Override
  public void setFinishInfo( final int status, final String text )
  {
    m_info = new Status( status, KalypsoSimulationCorePlugin.getID(), text );

    println( String.format( "Finish-Info '%s'", m_info ) );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getFinishText()
   */
  @Override
  public String getFinishText( )
  {
    if( m_info == null )
      return null;

    return m_info.getMessage();
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getFinishStatus()
   */
  @Override
  public int getFinishStatus( )
  {
    if( m_info == null )
      return -1;

    return m_info.getSeverity();
  }
}
