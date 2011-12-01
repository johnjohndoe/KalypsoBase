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
package org.kalypso.gml.processes.constDelaunay;

import java.util.List;

/**
 * Class for representing a triangle.exe node-file<BR>
 * <BR>
 * Spec:<BR>
 * First line: <# of vertices> <dimension (must be 2)> <# of attributes> <# of boundary markers (0 or 1)> <BR>
 * Remaining lines: <vertex #> <x> <y> [attributes] [boundary marker] <BR>
 * Blank lines and comments prefixed by `#' may be placed anywhere. Vertices must be numbered consecutively, starting
 * from one or zero. <BR>
 * The attributes, which are typically floating-point values of physical quantities (such as mass or conductivity)
 * associated with the nodes of a finite element mesh, are copied unchanged to the output mesh. If -q, -a, -u, or -s is
 * selected, each new Steiner point added to the mesh will have quantities assigned to it by linear interpolation.
 * 
 * @author Thomas Jung
 */

public class TriangleNodeFile
{
  private List<TriangleVertex> m_vertexList;

  private int m_numOfAttributes;

  private boolean m_boundaryMarker;

  private int m_dimension;

  public TriangleNodeFile( final List<TriangleVertex> vertexList )
  {
    m_vertexList = vertexList;
  }

  public void addVertex( final TriangleVertex vertex )
  {
    m_vertexList.add( vertex );
  }

  public void setVertices( final List<TriangleVertex> vertexList )
  {
    m_vertexList = vertexList;
  }

  public TriangleVertex[] getVertices( )
  {
    return m_vertexList.toArray( new TriangleVertex[m_vertexList.size()] );
  }

  public int getNumOfVertices( )
  {
    return m_vertexList.size();
  }

  public int getDimension( )
  {
    return m_dimension;
  }

  public void setDimension( final int dimension )
  {
    m_dimension = dimension;
  }

  public List<TriangleVertex> getVertexList( )
  {
    return m_vertexList;
  }

  public int getNumOfAttributes( )
  {
    return m_numOfAttributes;
  }

  public boolean isBoundaryMarker( )
  {
    return m_boundaryMarker;
  }
}
