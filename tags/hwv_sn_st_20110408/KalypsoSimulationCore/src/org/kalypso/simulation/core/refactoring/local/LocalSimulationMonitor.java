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
package org.kalypso.simulation.core.refactoring.local;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.simulation.core.ISimulationMonitor;

/**
 * @author Dirk Kuch
 */
public class LocalSimulationMonitor implements ISimulationMonitor
{
  private final IProgressMonitor m_monitor;

  public LocalSimulationMonitor( final IProgressMonitor monitor )
  {
    m_monitor = monitor;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getFinishStatus()
   */
  @Override
  public int getFinishStatus( )
  {
    return 0;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getFinishText()
   */
  @Override
  public String getFinishText( )
  {
    return null;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getMessage()
   */
  @Override
  public String getMessage( )
  {
    return null;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#getProgress()
   */
  @Override
  public int getProgress( )
  {
    return 0;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setFinishInfo(int, java.lang.String)
   */
  @Override
  public void setFinishInfo( final int status, final String text )
  {
    m_monitor.setTaskName( text );

  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setMessage(java.lang.String)
   */
  @Override
  public void setMessage( final String message )
  {
    m_monitor.setTaskName( message );

  }

  /**
   * @see org.kalypso.simulation.core.ISimulationMonitor#setProgress(int)
   */
  @Override
  public void setProgress( final int progress )
  {
    m_monitor.worked( progress );
  }

  /**
   * @see org.kalypso.contribs.java.lang.ICancelable#cancel()
   */
  @Override
  public void cancel( )
  {
    m_monitor.setCanceled( true );
  }

  /**
   * @see org.kalypso.contribs.java.lang.ICancelable#isCanceled()
   */
  @Override
  public boolean isCanceled( )
  {
    return m_monitor.isCanceled();
  }

}
