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

import java.util.HashMap;
import java.util.Map;

import org.kalypso.simulation.core.ISimulationResultEater;

/**
 * A {@link ISimulationResultEater} implementation that does nothing but remembering he added results.
 * 
 * @author Gernot Belger
 */
public class DefaultSimulationResultEater implements ISimulationResultEater
{
  private final Map<String, Object> m_results = new HashMap<String, Object>();

  public Object getResult( final String id )
  {
    return m_results.get( id );
  }

  public boolean hasResult( final String id )
  {
    return m_results.containsKey( id );
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationResultEater#addResult(java.lang.String, java.lang.Object)
   */
  public void addResult( final String id, final Object result )
  {
    m_results.put( id, result );
  }

}
