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
package org.kalypso.jts;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A pair of coordinates.
 * 
 * @author Holger Albert
 */
public class CoordinatePair implements Comparable<CoordinatePair>
{
  /**
   * The first coordinate.
   */
  private Coordinate m_coordinate1;

  /**
   * The second coordinate.
   */
  private Coordinate m_coordinate2;

  /**
   * The distance between the two coordinates.
   */
  private double m_distance;

  /**
   * The constructor.
   * 
   * @param coordinate1
   *          The first coordinate.
   * @param coordinate2
   *          The first coordinate.
   */
  public CoordinatePair( Coordinate coordinate1, Coordinate coordinate2 )
  {
    m_coordinate1 = coordinate1;
    m_coordinate2 = coordinate2;

    m_distance = Double.NaN;
    if( coordinate1 != null && coordinate2 != null )
      m_distance = coordinate1.distance( coordinate2 );
  }

  /**
   * This function returns the first coordinate.
   * 
   * @return The first coordinate.
   */
  public Coordinate getFirstCoordinate( )
  {
    return m_coordinate1;
  }

  /**
   * This function returns the second coordinate.
   * 
   * @return The second coordinate.
   */
  public Coordinate getSecondCoordinate( )
  {
    return m_coordinate2;
  }

  /**
   * This function returns the distance between the two coordinates.
   * 
   * @return The distance between the two coordinates.
   */
  public double getDistance( )
  {
    return m_distance;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo( CoordinatePair o )
  {
    return Double.compare( getDistance(), o.getDistance() );
  }
}