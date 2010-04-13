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
package org.kalypsodeegree_impl.model.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Gernot Belger
 */
public class FeatureCacheDefinition
{
  private static final QName[] EMPTY_QNAMES = new QName[0];

  private final Map<QName, QName[]> m_dirtyMap = new HashMap<QName, QName[]>();

  private final Set<QName> m_cachedProperties = new HashSet<QName>();

  public void addCachedProperty( final QName cachedProperty, final QName... dependsOn )
  {
    m_cachedProperties.add( cachedProperty );

    for( final QName dependsOnName : dependsOn )
      addToDirtyMap( dependsOnName, cachedProperty );
  }

  private void addToDirtyMap( final QName dependsOnName, final QName cachedProperty )
  {
    if( !m_dirtyMap.containsKey( dependsOnName ) )
      m_dirtyMap.put( dependsOnName, new QName[] { cachedProperty } );

    final QName[] dependencies = m_dirtyMap.get( dependsOnName );
    final QName[] newDependencies = (QName[]) ArrayUtils.add( dependencies, cachedProperty );
    m_dirtyMap.put( dependsOnName, newDependencies );
  }

  public QName[] getDirtyProperties( final QName changedProperty )
  {
    if( m_dirtyMap.containsKey( changedProperty ) )
      return m_dirtyMap.get( changedProperty );
    
    return EMPTY_QNAMES;
  }

  public boolean isCached( final QName property )
  {
    return m_cachedProperties.contains( property );
  }
}
