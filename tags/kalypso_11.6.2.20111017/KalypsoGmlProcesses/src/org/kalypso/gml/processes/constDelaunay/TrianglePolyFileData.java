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
 * Class for presenting a triangle.exe poly-file<BR>
 * <BR>
 * Spec:<BR>
 * First line: # of vertices, dimension (must be 2), # of attributes, # of boundary markers (0 or 1)<BR>
 * Following lines: vertex #, x, y, [attributes], [boundary marker] <BR>
 * <BR>
 * One line: # of segments, # of boundary markers (0 or 1) <BR>
 * Following lines: segment #,endpoint1, endpoint2, [boundary marker]<BR>
 * <BR>
 * One line: # of holes <BR>
 * Following lines: hole #, x, y,<BR>
 * <BR>
 * Optional line: # of regional attributes and/or area constraints<BR>
 * Optional following lines: region #, x, y, attribute, maximum area<BR>
 * 
 * @author Thomas Jung
 */
public class TrianglePolyFileData
{
  private List<TriangleVertex> m_nodeList;

  private List<TriangleSegment> m_segmentList;

  private List<TriangleHole> m_holeList;

  private boolean m_boundaryMarker;

  public TrianglePolyFileData( final List<TriangleVertex> nodeList, final List<TriangleSegment> segmentList, final List<TriangleHole> holeList )
  {
    m_nodeList = nodeList;
    m_segmentList = segmentList;
    m_holeList = holeList;
  }

  public List<TriangleVertex> getNodeList( )
  {
    return m_nodeList;
  }

  public List<TriangleSegment> getSegmentList( )
  {
    return m_segmentList;
  }

  public List<TriangleHole> getHoleList( )
  {
    return m_holeList;
  }

  public void setNodeList( List<TriangleVertex> nodeList )
  {
    m_nodeList = nodeList;
  }

  public void setSegmentList( List<TriangleSegment> segmentList )
  {
    m_segmentList = segmentList;
  }

  public void setHoleList( List<TriangleHole> holeList )
  {
    m_holeList = holeList;
  }

  public String getVertexHeader( )
  {
    // we take the data from the first element
    final TriangleVertex triangleVertex = m_nodeList.get( 0 );
    return m_nodeList.size() + " " + triangleVertex.getDimension() + " " + triangleVertex.getAttributes().size() + " " + getBoundaryMarker(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public int getBoundaryMarker( )
  {
    if( m_boundaryMarker == true )
      return 1;
    else
      return 0;
  }

  public void setBorderMarker( final boolean marker )
  {
    m_boundaryMarker = marker;
  }

  public String getSegmentHeader( )
  {
    // we take the data from the first element
    // One line: # of segments, # of boundary markers (0 or 1)
    // Following lines: segment #,endpoint1, endpoint2, [boundary marker]

    final TriangleSegment triangleSegment = m_segmentList.get( 0 );
    return m_segmentList.size() + " " + triangleSegment.getboundaryId(); //$NON-NLS-1$
  }

  public String getHoleHeader( )
  {
    Integer size = m_holeList.size();
    return size.toString();
  }

}
