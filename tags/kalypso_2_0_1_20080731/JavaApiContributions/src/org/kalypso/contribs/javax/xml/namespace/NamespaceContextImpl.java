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
package org.kalypso.contribs.javax.xml.namespace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Gernot Belger
 */
public class NamespaceContextImpl implements NamespaceContext
{
  private final Map<String, String> m_prefixMap = new HashMap<String, String>();

  private final Map<String, Set<String>> m_namespaceMap = new HashMap<String, Set<String>>();

  /**
   * Add a new prefix-namespace combination to this context.
   */
  public void put( final String prefix, final String namespace )
  {
    m_prefixMap.put( prefix, namespace );

    final Set<String> prefixSet = getPrefixSet( namespace );
    prefixSet.add( prefix );
  }

  private Set<String> getPrefixSet( final String namespace )
  {
    if( !m_namespaceMap.containsKey( namespace ) )
      m_namespaceMap.put( namespace, new HashSet<String>() );
    final Set<String> prefixSet = m_namespaceMap.get( namespace );
    return prefixSet;
  }

  /**
   * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
   */
  public String getNamespaceURI( final String prefix )
  {
    return m_prefixMap.get( prefix );
  }

  /**
   * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
   */
  public String getPrefix( final String namespaceURI )
  {
    final Set<String> prefixSet = getPrefixSet( namespaceURI );
    if( prefixSet.isEmpty() )
      return null;
    return prefixSet.iterator().next();
  }

  /**
   * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
   */
  public Iterator< ? > getPrefixes( final String namespaceURI )
  {
    return getPrefixSet( namespaceURI ).iterator();
  }

}
