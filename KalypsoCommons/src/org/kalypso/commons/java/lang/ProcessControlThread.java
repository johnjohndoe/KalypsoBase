/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.commons.java.lang;

/**
 * Thread, der die Ausführung des Prozesses proc nach lTimeout ms abbricht. bei lTimeout = 0 wird die Ausführung des
 * Prozesses proc nicht abgebrochen.
 * 
 * @author Thül
 */
public class ProcessControlThread extends Thread
{
  private volatile boolean m_bProcCtrlActive = false;

  private volatile boolean m_bProcDestroyed = false;

  private final long m_lTimeout;

  private final Process m_proc;

  public ProcessControlThread( final Process proc, final long lTimeout )
  {
    m_proc = proc;
    m_lTimeout = lTimeout;
  }

  @Override
  public void run( )
  {
    synchronized( this )
    {
      try
      {
        m_bProcCtrlActive = true;
        wait( m_lTimeout );
      }
      catch( final InterruptedException ex )
      {
        // sollte nicht passieren
        ex.printStackTrace();
      }
    }
    if( m_bProcCtrlActive )
    {
      // Prozess läuft nach Ablauf von m_lTimeout ms immer noch: Abbruch
      m_bProcDestroyed = true;
      m_proc.destroy();
    }
  }

  public synchronized void endProcessControl( )
  {
    // stoppt die Überwachung des Prozesses
    m_bProcCtrlActive = false;
    notifyAll();
  }

  public boolean procDestroyed( )
  {
    // wurde der Prozess durch diesen Thread abbgebrochen?
    return m_bProcDestroyed;
  }
}