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
package org.kalypso.grid.areas;

import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.IGeoGrid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Visits all grid cells that lie inside the given geometry.
 * 
 * @author Holger Albert
 * @author Gernot Belger
 */
public class PolygonGeoGridArea extends AbstractGeoGridArea
{
  /**
   * The geometry factory.
   */
  private static GeometryFactory GF = new GeometryFactory();

  /**
   * The area geometry.
   */
  private final Geometry m_geom;

  /**
   * The constructor.
   * 
   * @param grid
   *          The grid.
   * @param geom
   *          The area geometry.
   */
  public PolygonGeoGridArea( final IGeoGrid grid, final Geometry geom )
  {
    super( grid, geom );

    m_geom = geom;
  }

  /**
   * @see org.kalypso.grid.areas.AbstractGeoGridArea#contains(int, int, com.vividsolutions.jts.geom.Coordinate)
   */
  @Override
  public boolean contains( final int x, final int y, final Coordinate coordinate ) throws GeoGridException
  {
    final boolean contains = super.contains( x, y, coordinate );
    if( contains == false )
      return false;

    return m_geom.contains( GF.createPoint( coordinate ) );
  }
}