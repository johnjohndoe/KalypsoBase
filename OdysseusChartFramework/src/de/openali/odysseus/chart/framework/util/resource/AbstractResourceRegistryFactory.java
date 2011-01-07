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
package de.openali.odysseus.chart.framework.util.resource;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.graphics.Device;

/**
 * Abstract implementation of a "registry factory" containing SWT resources which need to be disposed. The keys used to
 * achieve resources is also a descriptor which is used to create the resource.
 * 
 * @author burtscher1
 */
public abstract class AbstractResourceRegistryFactory<T_descriptor, T_resource>
{
  private final Map<T_descriptor, T_resource> m_map;

  /**
   * Uses a HashMap to store pairs of descriptors and resources
   */
  public AbstractResourceRegistryFactory( )
  {
    m_map = new HashMap<T_descriptor, T_resource>();
  }

  /**
   * This constructor can be used to provide a comparator for descriptors. Instead of a HashMap, a TreeMap is used
   */
  public AbstractResourceRegistryFactory( final Comparator<T_descriptor> comparator )
  {
    m_map = new TreeMap<T_descriptor, T_resource>( comparator );
  }

  public final synchronized T_resource getResource( final Device dev, final T_descriptor descriptor )
  {
    T_resource resource = m_map.get( descriptor );
    if( resource == null )
    {
      resource = createResource( dev, descriptor );
      m_map.put( descriptor, resource );
    }
    return resource;
  }

  protected abstract T_resource createResource( Device dev, T_descriptor descriptor );

  protected abstract void disposeResource( T_resource resource );

  public final synchronized void dispose( )
  {
    for( final T_resource resource : m_map.values() )
      disposeResource( resource );
    m_map.clear();
  }
}
