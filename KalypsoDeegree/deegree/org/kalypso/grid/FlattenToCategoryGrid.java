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
package org.kalypso.grid;

import java.math.BigDecimal;

import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Grid class that flattens / merges a number of grids, each classified by categories, to a single grid, that has the
 * category numbers as cell values. The categories are defined by integer values, so that an easy hierarchical structure
 * can be created. In addition a geometry can be set for which the values shall be created, in order to fasten up
 * generation of the grid. <BR>
 * the last category overwrites all others. HINT for nofdp: zuletzt ist oft
 * 
 * @author Thomas Jung
 */
public class FlattenToCategoryGrid extends AbstractGeoGrid
{
  private static final GeometryFactory GF = new GeometryFactory();

  private final Geometry m_clipGeometry;

  private final GridCategoryWrapper[] m_gridCategories;

  private final BigDecimal m_min;

  private final BigDecimal m_max;

  private final int m_sizeX;

  private final int m_sizeY;

  public FlattenToCategoryGrid( final GridCategoryWrapper[] gridCategories, final Geometry clipGeometry, final Coordinate origin, final Coordinate offsetX, final Coordinate offsetY, final String sourceCRS, final int numOfColumns, final int numOfRows )
  {
    super( origin, offsetX, offsetY, sourceCRS );

    m_gridCategories = gridCategories;
    m_clipGeometry = clipGeometry;
    m_sizeX = numOfColumns;
    m_sizeY = numOfRows;

    m_min = new BigDecimal( Double.MAX_VALUE ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    m_max = new BigDecimal( -Double.MAX_VALUE ).setScale( 2, BigDecimal.ROUND_HALF_UP );
  }

  @Override
  public double getValue( final int x, final int y ) throws GeoGridException
  {
    try
    {
      // get the value according to the grids category
      double value = Double.NaN;

      // check for all categorie's grids if there is a value for that position
      for( final GridCategoryWrapper gridCategory : m_gridCategories )
      {
        final IGeoGrid[] grids = gridCategory.getGrids();

        for( final IGeoGrid grid : grids )
        {
          final Coordinate crd = GeoGridUtilities.toCoordinate( this, x, y, null );

          if( m_clipGeometry != null )
          {
            /* Get coordinate for raster cell x/y. */
            final Point point = GF.createPoint( crd );

            // if the point is part of clip geometry points the contains method returns false, but we want that this
            // coordinate is also considered! So, we use not the grid envelope but the grid surface, which represents
            // the outer boundary of the grid cells(not the grid points!) as buffer.
            if( m_clipGeometry.contains( point ) )
            {
              // transform to grid's crs
              final GM_Position positionAt = JTSAdapter.wrap( crd );

              /* Transform query position into the crs of the current grid. */
              final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( grid.getSourceCRS() );
              final GM_Position position = geoTransformer.transform( positionAt, getSourceCRS() );

              final double gridValue = grid.getValue( JTSAdapter.export( position ) );

              // only if there is a value for a new gird, overwrite the old value
              if( !Double.isNaN( gridValue ) )
                value = gridCategory.getValue();
            }
          }
          else
          {
            final double gridValue = grid.getValue( crd );
            if( !Double.isNaN( gridValue ) )
              value = gridCategory.getValue();
          }
        }
      }
      return value;
    }
    catch( final Exception ex )
    {
      throw new GeoGridException( "error while flattening grids", ex );
    }
  }

  @Override
  public BigDecimal getMax( )
  {
    return m_max;
  }

  @Override
  public BigDecimal getMin( )
  {
    return m_min;
  }

  @Override
  public int getSizeX( )
  {
    return m_sizeX;
  }

  @Override
  public int getSizeY( )
  {
    return m_sizeY;
  }
}