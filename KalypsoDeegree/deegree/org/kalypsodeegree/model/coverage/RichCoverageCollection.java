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
package org.kalypsodeegree.model.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kalypsodeegree.model.elevation.ElevationUtilities;
import org.kalypsodeegree.model.elevation.IElevationModel;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Helper class that enriches the {@link ICoverageCollection} by several utility methods.
 *
 * @author Holger Albert
 */
public class RichCoverageCollection
{
  /**
   * The coverage collection.
   */
  private ICoverageCollection m_coverageCollection;

  /**
   * This map contains the elevation model for each coverage.
   */
  private Map<ICoverage, IElevationModel> m_emCache;

  /**
   * The constructor.
   *
   * @param coverageCollection
   *          The coverage collection.
   */
  public RichCoverageCollection( final ICoverageCollection coverageCollection )
  {
    m_coverageCollection = coverageCollection;
    m_emCache = new HashMap<>();
  }

  /**
   * This function extracts the points from the given curve. First it will be densified with the given offset.
   *
   * @param curve
   *          The curve.
   * @param offset
   *          The offset with which to add points to the line.
   * @return The extracted points.
   */
  public Coordinate[] extractPoints( final GM_Curve curve, final double offset )
  {
    try
    {
      return extractPoints( (LineString) JTSAdapter.export( curve ), curve.getCoordinateSystem(), offset );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  public Coordinate[] extractPoints( final LineString lineString, final String crs, final double offset )
  {
    final Coordinate[] points = Densifier.densify( lineString, offset ).getCoordinates();

    return extractZ( points, crs );
  }

  public void dispose( )
  {
    final Collection<IElevationModel> values = m_emCache.values();
    for( final IElevationModel value : values )
      value.dispose();

    m_coverageCollection = null;
    m_emCache = null;
  }

  /**
   * This function extracts z-values from a coverage collection.<br>
   * Positions that are not covered by the elevation models are ignored.
   *
   * @param crds
   *          The coordinates.
   * @param crsOfCrds
   *          The coordinate system of the coordinates.
   * @return An new array of coordinates, having the z-values.
   */
  protected Coordinate[] extractZ( final Coordinate[] crds, final String crsOfCrds )
  {
    try
    {
      final Collection<Coordinate> result = new ArrayList<>( crds.length );
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

  protected double findValue( final Coordinate coordinate, final String crsOfCrds ) throws Exception
  {
    final GM_Point point = GeometryFactory.createGM_Point( coordinate.x, coordinate.y, crsOfCrds );
    final IFeatureBindingCollection<ICoverage> coverages = m_coverageCollection.getCoverages();
    final List<ICoverage> foundCoverages = coverages.query( point.getPosition() );
    for( final ICoverage coverage : foundCoverages )
    {
      /* Get the elevation model. */
      final IElevationModel elevationModel = getElevationModel( coverage );

      /* Get the interpolated value with the coordinate in the coordinate system of the grid. */
      final double value = elevationModel.getElevation( point );
      if( !Double.isNaN( value ) )
        return value;
    }

    return Double.NaN;
  }

  protected IElevationModel getElevationModel( final ICoverage coverage )
  {
    if( !m_emCache.containsKey( coverage ) )
      m_emCache.put( coverage, ElevationUtilities.toElevationModel( coverage ) );

    return m_emCache.get( coverage );
  }
}