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

/**
 * Simulationmonitor that supports nothing but cancel
 * 
 * @author doemming
 */
public class NullSimulationMonitor implements ISimulationMonitor
{

  private boolean m_cancel = false;

  private final ISimulationMonitor m_simulationmonitor;

  public NullSimulationMonitor( )
  {
    m_simulationmonitor = null;
  }

  /**
   * @param simulationmonitor
   *          a monitor that will receive cancel information, but no progressinformation from this monitor
   */
  public NullSimulationMonitor( ISimulationMonitor simulationmonitor )
  {
    m_simulationmonitor = simulationmonitor;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setProgress(int)
   */
  @Override
  public void setProgress( int progress )
  {
    // nothing
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getProgress()
   */
  @Override
  public int getProgress( )
  {
    // nothing
    return 0;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getMessage()
   */
  @Override
  public String getMessage( )
  {
    // nothing
    return null;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setMessage(java.lang.String)
   */
  @Override
  public void setMessage( String message )
  {
    // nothing
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setFinishInfo(int, java.lang.String)
   */
  @Override
  public void setFinishInfo( int status, String text )
  {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getFinishText()
   */
  @Override
  public String getFinishText( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getFinishStatus()
   */
  @Override
  public int getFinishStatus( )
  {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @see org.kalypso.contribs.java.lang.ICancelable#cancel()
   */
  @Override
  public void cancel( )
  {
    m_cancel = true;
    if( m_simulationmonitor != null )
      m_simulationmonitor.cancel();
  }

  /**
   * @see org.kalypso.contribs.java.lang.ICancelable#isCanceled()
   */
  @Override
  public boolean isCanceled( )
  {
    if( m_simulationmonitor != null )
      return m_simulationmonitor.isCanceled() || m_cancel;
    return m_cancel;
  }

}
