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

/**
 * Class for representing a segment line entry of the triangle.exe poly-file<BR>
 * <BR>
 * Spec:<BR>
 * segment#, startpoint, endpoint, [boundary marker]<BR>
 * Segments are edges whose presence in the triangulation is enforced (although each segment may be subdivided into
 * smaller edges). Each segment is specified by listing the indices of its two endpoints. This means that you must
 * include its endpoints in the vertex list. Each segment, like each vertex, may have a boundary marker.
 * 
 * @author Thomas Jung
 */
public class TriangleSegment
{
  private final int m_startId;

  private final int m_endId;

  private final boolean m_boundaryMarker;

  public TriangleSegment( final int startId, final int endId, final boolean boundaryMarker )
  {
    m_startId = startId;
    m_endId = endId;
    m_boundaryMarker = boundaryMarker;

  }

  public int getStartId( )
  {
    return m_startId;
  }

  public int getEndId( )
  {
    return m_endId;
  }

  public boolean getBoundaryMarker( )
  {
    return m_boundaryMarker;
  }

  public String getLine( )
  {
    return m_startId + " " + m_endId + " " + getboundaryId(); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public int getboundaryId( )
  {
    if( m_boundaryMarker == true )
      return 1;
    return 0;
  }
}
