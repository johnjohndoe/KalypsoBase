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
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
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
  private static final int DISCRETISATION_FRACTION = 8;

  private final Map<ICoverage, IGeoGrid> m_gridCache = new HashMap<ICoverage, IGeoGrid>();

  private final Map<ICoverage, IGeoTransformer> m_geoTransformer = new HashMap<ICoverage, IGeoTransformer>();

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

  public Coordinate[] extractPoints( final LineString lineString, final String crs ) throws GeoGridException
  {
    final double gridSize = getSmallestGridOffset();
    if( Double.isNaN( gridSize ) )
      throw new IllegalStateException( "No grids available" );

    /* Add every 1/8 raster size a point. */
    final Coordinate[] points = JTSUtilities.calculatePointsOnLine( lineString, gridSize / DISCRETISATION_FRACTION );

    return extractZ( points, crs );
  }

  public Coordinate[] extractPoints( final GM_Curve curve )
  {
    try
    {
      /* Convert to a JTS geometry. */
      final LineString jtsCurve = (LineString) JTSAdapter.export( curve );
      return extractPoints( jtsCurve, curve.getCoordinateSystem() );
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
      for( final Coordinate coordinate : crds )
      {
        final double value = findValue( coordinate, crsOfCrds );
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

  /**
   * gridCoordinate: Must be in the Kalypso Coordinate System
   */
  public double findValue( final Coordinate coordinate, final String crsOfCrds ) throws GeoGridException
  {
    final GM_Position pos = GeometryFactory.createGM_Position( coordinate.x, coordinate.y );
    final List<ICoverage> foundCoverages = m_coverages.query( pos );

    for( final ICoverage coverage : foundCoverages )
    {
      final IGeoGrid grid = getGrid( coverage );

      final IGeoTransformer transformer = getTransformer( coverage );
      final Coordinate gridCoordinate = transformCoordinate( transformer, coordinate, crsOfCrds );

      /* Get the interpolated value with the coordinate in the coordinate system of the grid. */
      final double value = GeoGridUtilities.getValue( grid, gridCoordinate, Interpolation.bilinear );
      if( !Double.isNaN( value ) )
        return value;
    }

    return Double.NaN;
  }

  private IGeoTransformer getTransformer( final ICoverage coverage ) throws GeoGridException
  {
    if( !m_geoTransformer.containsKey( coverage ) )
    {
      final IGeoGrid grid = getGrid( coverage );
      final String gridCrs = grid.getSourceCRS();
      final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( gridCrs );
      m_geoTransformer.put( coverage, transformer );
    }

    return m_geoTransformer.get( coverage );
  }

  private Coordinate transformCoordinate( final IGeoTransformer transformer, final Coordinate coordinate, final String crsOfCrds ) throws GeoGridException
  {
    try
    {
      final GM_Position pos = JTSAdapter.wrap( coordinate );
      final GM_Position transformedPosition = transformer.transform( pos, crsOfCrds );
      return JTSAdapter.export( transformedPosition );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new GeoGridException( "Failed to transform coordinate to grid coordinate system.", e );
    }
  }

  private double getSmallestGridOffset( ) throws GeoGridException
  {
    if( m_coverages.size() == 0 )
      return Double.NaN;

    double minOffset = Double.MAX_VALUE;

    for( final ICoverage coverage : m_coverages )
    {
      final IGeoGrid grid = getGrid( coverage );
      final double offset = grid.getOffsetX().x;
      minOffset = Math.min( minOffset, offset );
    }

    /* Now transform this length into Kalypso crs */
    final ICoverage coverage = m_coverages.get( 0 );
    final IGeoGrid grid = getGrid( coverage );
    final Coordinate origin = grid.getOrigin();
    final Coordinate origin1 = new Coordinate( origin.x + minOffset, origin.y );

    final String kalypsoCrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( kalypsoCrs );

    final Coordinate crd1 = transformCoordinate( transformer, origin, grid.getSourceCRS() );
    final Coordinate crd2 = transformCoordinate( transformer, origin1, grid.getSourceCRS() );

    return crd1.distance( crd2 );
  }

  private IGeoGrid getGrid( final ICoverage coverage )
  {
    if( !m_gridCache.containsKey( coverage ) )
      m_gridCache.put( coverage, GeoGridUtilities.toGrid( coverage ) );

    return m_gridCache.get( coverage );
  }
}