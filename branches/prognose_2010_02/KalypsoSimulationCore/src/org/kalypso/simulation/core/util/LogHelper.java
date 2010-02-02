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
package org.kalypso.simulation.core.util;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.i18n.Messages;

/**
 * Helper for simulation logging.
 * 
 * @author Gernot Belger
 */
public class LogHelper
{
  public static final String MESS_BERECHNUNG_ABGEBROCHEN = Messages.getString("org.kalypso.simulation.core.util.LogHelper.0"); //$NON-NLS-1$

  private final ISimulationMonitor m_monitor;

  private final PrintWriter m_log;

  private final PrintStream m_outputConsumer;

  public LogHelper( final PrintWriter log, final ISimulationMonitor monitor, final PrintStream outputConsumer )
  {
    m_log = log;
    m_monitor = monitor;
    m_outputConsumer = outputConsumer;
  }

  public ISimulationMonitor getMonitor( )
  {
    return m_monitor;
  }

  /**
   * @param updateMonitor
   *            if true, the message is also set to the monitor
   */
  public void log( final boolean updateMonitor, final String format, final Object... args )
  {
    final String msg = String.format( format, args );
    m_log.println( msg );
    m_outputConsumer.println( msg );
    if( updateMonitor )
      m_monitor.setMessage( msg );
  }

  /**
   * If the monitor is canceled, return true and print and log error message.
   */
  public boolean checkCanceled( )
  {
    final boolean canceled = m_monitor.isCanceled();
    if( canceled )
    {
      log( true, MESS_BERECHNUNG_ABGEBROCHEN );
      m_monitor.setFinishInfo( IStatus.CANCEL, MESS_BERECHNUNG_ABGEBROCHEN );
    }

    return canceled;
  }

  public void log( final Throwable e, final String format, final Object... args )
  {
    final String msg = String.format( format, args );
    m_log.println( msg );
    m_outputConsumer.println( msg );
    e.printStackTrace( m_log );
    e.printStackTrace( System.out );
  }

  /** Logs the message and sets the finish status of the monitor. */
  public void finish( final int status, final String message )
  {
    log( false, message );
    m_monitor.setFinishInfo( status, message );
  }

}
