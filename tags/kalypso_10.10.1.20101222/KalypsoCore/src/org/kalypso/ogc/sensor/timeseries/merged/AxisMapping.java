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
package org.kalypso.ogc.sensor.timeseries.merged;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * @author Dirk Kuch
 */
public class AxisMapping
{
  /** map<baseAxis, axis> */
  private final Map<IAxis, IAxis> m_mapping = new HashMap<IAxis, IAxis>();

  private final IAxis[] m_destinationAxes;

  /**
   * maps axes to base result model axes
   * 
   * @param destinationAxes
   *          are the axes of the destination tuple model
   * @param axes
   *          map axes to result model baseAxes
   */
  public AxisMapping( final IAxis[] destinationAxes, final IAxis[] axes )
  {
    m_destinationAxes = destinationAxes;
    for( final IAxis base : destinationAxes )
    {
      final IAxis fitting = find( base, axes );
      if( fitting == null )
        continue;

      m_mapping.put( base, fitting );
    }
  }

  /**
   * find fitting axis
   */
  private IAxis find( final IAxis base, final IAxis[] axes )
  {
    for( final IAxis axis : axes )
    {
      if( !base.getType().equals( axis.getType() ) )
        continue;
      else if( !base.getUnit().equals( axis.getUnit() ) )
        continue;
      else if( !base.getName().equals( axis.getName() ) )
        continue;

      return axis;
    }

    return null;
  }

  public IAxis[] getSourceAxes( )
  {
    return m_mapping.values().toArray( new IAxis[] {} );
  }

  public int getDestinationIndex( final IAxis srcAxis )
  {
    // FIXME: why do we use a set, if we make a linear search later? Use a specialized comparator on that set and use
    // .contains()!
    final Set<Entry<IAxis, IAxis>> entries = m_mapping.entrySet();
    for( final Entry<IAxis, IAxis> entry : entries )
    {
      if( entry.getValue().equals( srcAxis ) )
        return ArrayUtils.indexOf( m_destinationAxes, entry.getKey() );
    }

    return ArrayUtils.indexOf( m_destinationAxes, srcAxis );
  }

  public IAxis getDestinationAxis( final IAxis srcAxis )
  {
    final Set<Entry<IAxis, IAxis>> entries = m_mapping.entrySet();
    for( final Entry<IAxis, IAxis> entry : entries )
    {
      final IAxis destAxis = entry.getValue();
      if( destAxis.equals( srcAxis ) )
        return destAxis;
    }

    return null;
  }

  public IAxis getDataSourceAxis( )
  {
    return AxisUtils.findDataSourceAxis( m_destinationAxes );
  }

}
