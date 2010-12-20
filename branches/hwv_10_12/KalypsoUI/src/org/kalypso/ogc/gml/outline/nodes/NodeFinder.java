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
package org.kalypso.ogc.gml.outline.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gernot Belger
 */
public class NodeFinder
{
  private final IThemeNode m_rootNode;

  public NodeFinder( final IThemeNode rootNode )
  {
    m_rootNode = rootNode;
  }

  public IThemeNode[] find( final Object[] elements )
  {
    final List<IThemeNode> nodes = new ArrayList<IThemeNode>();

    for( final Object element : elements )
    {
      final IThemeNode foundNode = findNode( m_rootNode, element );
      if( foundNode != null )
        nodes.add( foundNode );
    }

    return nodes.toArray( new IThemeNode[nodes.size()] );
  }

  public IThemeNode find( final Object element )
  {
    return findNode( m_rootNode, element );
  }

  private static IThemeNode findNode( final IThemeNode node, final Object element )
  {
    if( node.getElement().equals( element ) )
      return node;

    final IThemeNode[] children = node.getChildren();
    for( final IThemeNode childNode : children )
    {
      final IThemeNode foundNode = findNode( childNode, element );
      if( foundNode != null )
        return foundNode;
    }

    return null;
  }


}
