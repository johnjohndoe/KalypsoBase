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
package org.kalypso.grid.areas;

import org.kalypso.grid.GeoGridCell;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.jts.JTSUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Abstract implementation of the geo grid area.<br/>
 * <br/>
 * The {@link #contains(int, int, Coordinate)} function does a simple envelope check. It should be overriden for a more
 * specific examination. The overriding function should still call the parent function, before its own code, to exclude
 * the most cells on a very perfomant way.<br/>
 * <br/>
 * The {@link #overlaps(int, int, Coordinate)} function does check for overlapping. It should not be overriden.
 *
 * @author Holger Albert
 */
public abstract class AbstractGeoGridArea implements IGeoGridArea
{
  /**
   * The grid.
   */
  private IGeoGrid m_grid;

  /**
   * The area geometry.
   */
  private Geometry m_geometry;

  /**
   * The start x coordinate.
   */
  private int m_xStart;

  /**
   * The end x coordinate.
   */
  private int m_xEnd;

  /**
   * The start y coordinate.
   */
  private int m_yStart;

  /**
   * The end y coordinate.
   */
  private int m_yEnd;

  /**
   * True, if the geo grid area was initialized.
   */
  private boolean m_hasInit;

  /**
   * The constructor.
   *
   * @param grid
   *          The grid.
   * @param geometry
   *          The area geometry.
   */
  public AbstractGeoGridArea( IGeoGrid grid, Geometry geometry )
  {
    m_grid = grid;
    m_geometry = geometry;
    m_xStart = 0;
    m_xEnd = 0;
    m_yStart = 0;
    m_yEnd = 0;
    m_hasInit = false;
  }

  /**
   * This function initializes the geo grid area.
   */
  private void init( ) throws GeoGridException
  {
    /* Was it already initialized? */
    if( m_hasInit )
      return;

    /* Get the envelope of the geometry. */
    Envelope envelope = m_geometry.getEnvelopeInternal();

    /* Determine the cells at the edges of the envelope. */
    GeoGridCell minMinCell = GeoGridUtilities.cellFromPosition( m_grid, new Coordinate( envelope.getMinX(), envelope.getMinY() ) );
    GeoGridCell maxMaxCell = GeoGridUtilities.cellFromPosition( m_grid, new Coordinate( envelope.getMaxX(), envelope.getMaxY() ) );

    /* Calculate the start and end x coordinates. */
    m_xStart = Math.max( 0, Math.min( minMinCell.x, maxMaxCell.x ) );
    m_xEnd = Math.min( m_grid.getSizeX(), Math.max( minMinCell.x, maxMaxCell.x ) + 1 );

    /* Calculate the start and end y coordinates. */
    m_yStart = Math.max( 0, Math.min( minMinCell.y, maxMaxCell.y ) );
    m_yEnd = Math.min( m_grid.getSizeY(), Math.max( minMinCell.y, maxMaxCell.y ) + 1 );

    /* Now, everything was initialized. */
    m_hasInit = true;
  }

  /**
   * Subclasses should call the super function, if they want a basic envelope check.
   *
   * @see org.kalypso.grid.areas.IGeoGridArea#contains(int, int, com.vividsolutions.jts.geom.Coordinate)
   */
  @Override
  public boolean contains( int x, int y, Coordinate coordinate ) throws GeoGridException
  {
    /* Initialize. */
    init();

    if( x < m_xStart || x > m_xEnd )
      return false;

    if( y < m_yStart || y > m_yEnd )
      return false;

    return true;
  }

  /**
   * @see org.kalypso.grid.areas.IGeoGridArea#overlaps(int, int, com.vividsolutions.jts.geom.Coordinate)
   */
  @Override
  public final double overlaps( int x, int y, Coordinate coordinate ) throws GeoGridException
  {
    /* Initialize. */
    init();

    if( x < m_xStart || x > m_xEnd )
      return 0.0;

    if( y < m_yStart || y > m_yEnd )
      return 0.0;

    /* Create the cells polygon. */
    Polygon cell = GeoGridUtilities.createCellPolygon( m_grid, x, y );

    /* The fraction of the cell. */
    return JTSUtilities.getAreaFraction( m_geometry, cell );
  }
}