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
package org.kalypso.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kalypso.grid.GeoGridUtilities.Interpolation;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.transformation.GeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Helper class that enriches the {@link ICoverageCollection} by several utility methods.
 * 
 * @author Holger Albert
 * @author Kim Werner
 * @author Gernot Belger
 */
public class RichCoverageCollection
{
  private final Map<ICoverage, IGeoGrid> m_gridCache = new HashMap<ICoverage, IGeoGrid>();

  private final ICoverageCollection m_coverages;

  /**
   * @param profileType
   *          The profile type, which should be created.
   */
  public RichCoverageCollection( final ICoverageCollection coverages )
  {
    m_coverages = coverages;
  }

  public void dispose( )
  {
    final Collection<IGeoGrid> values = m_gridCache.values();
    for( final IGeoGrid grid : values )
      grid.dispose();
  }

  public Coordinate[] extractPoints( final GM_Curve curve )
  {
    try
    {
      /* Convert to a JTS geometry. */
      final LineString jtsCurve = (LineString) JTSAdapter.export( curve );

      final double gridSize = getGridOffset();
      if( Double.isNaN( gridSize ) )
        throw new IllegalStateException( "No grids available" );

      /* Add every 1/8 raster size a point. */
      final Coordinate[] points = JTSUtilities.calculatePointsOnLine( jtsCurve, gridSize / 8 );
      return extractZ( points, curve.getCoordinateSystem() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This function extracts z-values from a coverage collection.<br>
   * Position that are not covered by the grids are ignored.
   */
  private Coordinate[] extractZ( final Coordinate[] crds, final String crsOfCrds )
  {
    try
    {
      final Collection<Coordinate> result = new ArrayList<Coordinate>( crds.length );

      final String gridCrds = getGridCrs();
      if( gridCrds == null )
        return null;

      final GeoTransformer geoTransformer = new GeoTransformer( gridCrds );

      for( final Coordinate coordinate : crds )
      {
        final Coordinate gridCoordinate = geoTransformer.transform( coordinate, crsOfCrds );
        final double value = findValue( gridCoordinate );

        if( !Double.isNaN( value ) )
          result.add( new Coordinate( coordinate.x, coordinate.y, value ) );
      }

      return result.toArray( new Coordinate[result.size()] );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  public double findValue( final Coordinate gridCoordinate ) throws GeoGridException
  {
    final GM_Position pos = GeometryFactory.createGM_Position( gridCoordinate.x, gridCoordinate.y );
    final List<ICoverage> foundCoverages = m_coverages.query( pos );

    for( final ICoverage coverage : foundCoverages )
    {
      final IGeoGrid grid = getGrid( coverage );
      /* Get the interpolated value with the coordinate in the coordinate system of the grid. */
      final double value = GeoGridUtilities.getValue( grid, gridCoordinate, Interpolation.bilinear );
      if( !Double.isNaN( value ) )
        return value;
    }

    return Double.NaN;
  }

  // TODO: check: we take the crs of the first grid, is this always OK?
  private String getGridCrs( ) throws GeoGridException
  {
    if( m_coverages.size() == 0 )
      return null;

    final ICoverage coverage = m_coverages.get( 0 );
    final IGeoGrid grid = getGrid( coverage );
    return grid.getSourceCRS();
  }

  // TODO: check: we take the offset of the first grid, is this always OK?
  private double getGridOffset( ) throws GeoGridException
  {
    if( m_coverages.size() == 0 )
      return Double.NaN;

    final ICoverage coverage = m_coverages.get( 0 );
    final IGeoGrid grid = getGrid( coverage );
    return grid.getOffsetX().x;
  }

  private IGeoGrid getGrid( final ICoverage coverage )
  {
    if( !m_gridCache.containsKey( coverage ) )
      m_gridCache.put( coverage, GeoGridUtilities.toGrid( coverage ) );

    return m_gridCache.get( coverage );
  }
}