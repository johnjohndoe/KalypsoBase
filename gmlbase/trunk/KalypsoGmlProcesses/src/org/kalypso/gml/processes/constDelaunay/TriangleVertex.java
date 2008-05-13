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

import java.util.LinkedList;
import java.util.List;

import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * @author Thomas Jung
 */
public class TriangleVertex
{
  private boolean m_borderMarker;

  private GM_Position m_position;

  private int m_dimension;

  private final List<Double> m_attributes;

  public TriangleVertex( final GM_Position position, final boolean borderMarker, final List<Double> attributes )
  {
    m_position = position;
    m_borderMarker = borderMarker;
    m_attributes = attributes;
    m_dimension = 2;
  }

  public TriangleVertex( final GM_Position position, final boolean borderMarker, final double attribute )
  {
    m_position = position;
    m_borderMarker = borderMarker;
    List<Double> attributeList = new LinkedList<Double>();
    attributeList.add( attribute );
    m_attributes = attributeList;
    m_dimension = 2;
  }

  public boolean getBorderMarker( )
  {
    return m_borderMarker;
  }

  public void setBorderMarker( boolean borderMarker )
  {
    m_borderMarker = borderMarker;
  }

  public GM_Position getPosition( )
  {
    return m_position;
  }

  public void setPosition( GM_Position position )
  {
    m_position = position;
  }

  public List<Double> getAttributes( )
  {
    return m_attributes;
  }

  public int getDimension( )
  {
    return m_dimension;
  }

  public void setDimension( int dimension )
  {
    m_dimension = dimension;
  }

  public String getLine( )
  {
    String pos = m_position.getX() + " " + m_position.getY(); //$NON-NLS-1$
    String att = ""; //$NON-NLS-1$
    for( double attribute : m_attributes )
    {
      att = att + " " + attribute; //$NON-NLS-1$
    }
    return pos + att;

  }
}
