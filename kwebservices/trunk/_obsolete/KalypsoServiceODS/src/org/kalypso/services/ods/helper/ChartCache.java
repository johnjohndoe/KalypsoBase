/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.services.ods.helper;

import java.util.HashSet;

import org.shiftone.cache.Cache;
import org.shiftone.cache.policy.lfu.LfuCacheFactory;

/**
 * @author burtscher1
 *
 */
public class ChartCache
{
  
  /* Cache Timeout */
  private final static int TIMEOUT = Integer.MAX_VALUE;

  /* Cache Size */
  private final static int SIZE = 30;

  private Cache m_memCache=null;
  private static ChartCache m_chartCache=null;
  private HashSet<String> m_names=new HashSet<String>();
  
  private ChartCache()
  {
    /*
     *Initialisieren des Caches
     * TODO: dummerweise müss dafür KalypsoGMLSchema eingebunden werden; die Lib sollte entweder
     * ausgelagert werden oder der Cache an einer anderen Stelle eingebunden werden
     */
    m_memCache = new LfuCacheFactory().newInstance( "gml.schemas", TIMEOUT, SIZE );
  }
  
  public static ChartCache getInstance()
  {
    if (m_chartCache==null)
       m_chartCache=new ChartCache();
    
    return m_chartCache;
  }
  
  public void addObject(String id, CachedChart cc)
  {
    if (m_names.contains( id ))
      remove( id );
    m_memCache.addObject( id, cc);
    m_names.add( id );
  }
  
  public void remove(String id)
  {
    CachedChart cc=(CachedChart) m_memCache.getObject( id );
    if (cc!=null)
      cc.dispose();
    m_names.remove( id );
    m_memCache.remove( id );
  }
  
  
  public void clear()
  {
    m_memCache.clear();
  }
  
  public CachedChart getObject(String id)
  {
    if (m_names.contains( id ))
      return (CachedChart) m_memCache.getObject( id );
    else
      return null;
  }
  
  
  

}
