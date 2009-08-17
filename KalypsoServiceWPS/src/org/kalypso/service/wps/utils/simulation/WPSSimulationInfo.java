/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.service.wps.utils.simulation;

import java.io.Serializable;
import java.util.List;

import net.opengeospatial.wps.IOValueType;

import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.SimulationInfo;

/**
 * Contains the the actual data of a simulation.
 * 
 * @author Gernot Belger (original), Holger Albert (changes)
 */
public class WPSSimulationInfo extends SimulationInfo implements Serializable, ISimulationMonitor
{
  /**
   * The result eater that knows about the current results
   */
  private final WPSSimulationResultEater m_resultEeater;

  private final long m_threadId;

  /**
   * The construtor.
   */
  public WPSSimulationInfo( )
  {
    // nur für wscompile
    super();
    m_resultEeater = null;
    m_threadId = -1;
  }

  /**
   * The constructor.
   */
  public WPSSimulationInfo( final long threadId, final String type, final String description, final ISimulationConstants.STATE state, final int progress, final WPSSimulationResultEater eater )
  {
    super( "use threadId instead", description, type, state, progress, "not finished yet" );
    m_threadId = threadId;
    m_resultEeater = eater;
  }

  @Override
  public String getId( )
  {
    return Long.toString( m_threadId );
  }

  public final List<IOValueType> getCurrentResults( ) throws SimulationException
  {
    return m_resultEeater.getCurrentResults();
  }
}