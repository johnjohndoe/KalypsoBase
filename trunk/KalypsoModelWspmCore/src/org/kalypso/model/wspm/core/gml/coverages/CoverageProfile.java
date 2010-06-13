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
package org.kalypso.model.wspm.core.gml.coverages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.GeoGridUtilities.Interpolation;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IllegalProfileOperationException;
import org.kalypso.model.wspm.core.profil.ProfilFactory;
import org.kalypso.model.wspm.core.profil.util.DouglasPeuckerHelper;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
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
 * TODO: split this one up into two helpers: 1) extract points from grids 2) create profile from a coordinate array.<br>
 * This class should help handling coverages and profiles.
 * 
 * @author Holger Albert
 * @author kimwerner
 */
public class CoverageProfile
{
  private final Map<ICoverage, IGeoGrid> m_grids = new HashMap<ICoverage, IGeoGrid>();

  private final String m_profileType;

  private final ICoverageCollection m_coverages;

  /**
   * @param profileType
   *          The profile type, which should be created.
   */
  public CoverageProfile( final ICoverageCollection coverages, final String profileType )
  {
    m_coverages = coverages;
    m_profileType = profileType;
  }

  public void dispose( )
  {
    final Collection<IGeoGrid> values = m_grids.values();
    for( final IGeoGrid grid : values )
      grid.dispose();
  }

  /**
   * This function creates a new profile.<br>
   * <br>
   * The following steps are performed:<br>
   * <ol>
   * <li>the coverage is not set (=null) OR the line does not intersect with any data of the coverage (all line points
   * have resulting NaN-values):
   * <ol>
   * <li>the line is converted into a profile with '0.0' as elevation
   * </ol>
   * <li>coverage is set:
   * <ol>
   * <li>Adds points to the geometry, depending on the size of the grid cell (1/8 grid cell size).</li>
   * <li>Computes the width and height for each point.</li>
   * <li>Create a profile with each point and its width and height (only points with elevation are being considered!)</li>
   * <li>The new profile is simplified by Douglas Peucker.</li>
   * </ol>
   * <br>
   * If you want to simplify the profile, use the result and the function {@link #thinProfile(IProfil)}.
   * 
   * @param curve
   *          The curve, which represents the geometry on the map of the profile.
   * @return The new profile.
   */
  public IProfil createProfile( final GM_Curve curve ) throws Exception
  {
    /* STEP 1: Extract point from the line gridSize / 8 */
    final Coordinate[] pointsZ = extractPoints( curve );

    if( pointsZ == null )
      return ProfilUtil.convertLinestringToEmptyProfile( curve, m_profileType );

    /* STEP 2: Compute the width and height for each point of the new line. */
    /* STEP 3: Create the new profile. */
    final IProfil profile = calculatePointsAndCreateProfile( pointsZ, curve.getCoordinateSystem() );
    if( profile.getPoints().length == 0 )
      return ProfilUtil.convertLinestringToEmptyProfile( curve, m_profileType );

    /* STEP 4: Thin the profile. */
    simplifyProfile( profile, 0.10 );

    return profile;
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

  private double findValue( final Coordinate gridCoordinate ) throws GeoGridException
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
    if( !m_grids.containsKey( coverage ) )
      m_grids.put( coverage, GeoGridUtilities.toGrid( coverage ) );

    return m_grids.get( coverage );
  }

  /**
   * This function calculates the points for the profile and creates the new profile.
   * 
   * @param points
   *          All points of the new geo line.
   * @param csOfPoints
   *          The coordinate system of the points.
   * @return The new profile.
   */
  private IProfil calculatePointsAndCreateProfile( final Coordinate[] points, final String crsOfPoints ) throws Exception
  {
    /* Create the new profile. */
    final IProfil profile = ProfilFactory.createProfil( m_profileType );
    profile.setProperty( IWspmConstants.PROFIL_PROPERTY_CRS, crsOfPoints );

    // TODO: check: we calculate the 'breite' by just adding up the distances between the points, is this always OK?
    double breite = 0.0;
    Coordinate lastCrd = null;

    for( final Coordinate coordinate : points )
    {
      final IRecord profilePoint = createPoint( profile, coordinate, breite );

      /* Add the new point to the profile. */
      profile.addPoint( profilePoint );

      if( lastCrd != null )
        breite += coordinate.distance( lastCrd );

      lastCrd = coordinate;
    }

    return profile;
  }

  private IRecord createPoint( final IProfil profile, final Coordinate coordinate, final double breite )
  {
    /* The needed components. */
    final IComponent cRechtswert = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_RECHTSWERT );
    final IComponent cHochwert = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final IComponent cBreite = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_BREITE );
    final IComponent cHoehe = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IComponent cRauheit = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_RAUHEIT_KS );

    /* add components if necessary */
    if( !profile.hasPointProperty( cRechtswert ) )
      profile.addPointProperty( cRechtswert );
    if( !profile.hasPointProperty( cHochwert ) )
      profile.addPointProperty( cHochwert );
    if( !profile.hasPointProperty( cBreite ) )
      profile.addPointProperty( cBreite );
    if( !profile.hasPointProperty( cHoehe ) )
      profile.addPointProperty( cHoehe );
    if( !profile.hasPointProperty( cRauheit ) )
      profile.addPointProperty( cRauheit );

    /* get index for component */
    final int iRechtswert = profile.indexOfProperty( cRechtswert );
    final int iHochwert = profile.indexOfProperty( cHochwert );
    final int iBreite = profile.indexOfProperty( cBreite );
    final int iHoehe = profile.indexOfProperty( cHoehe );
    final int iRauheit = profile.indexOfProperty( cRauheit );

    /* All necessary values. */
    final Double rechtswert = coordinate.x;
    final Double hochwert = coordinate.y;
    final Double hoehe = coordinate.z;
    final Object rauheit = cRauheit.getDefaultValue();

    /* Create a new profile point. */
    final IRecord profilePoint = profile.createProfilPoint();

    /* Add geo values. */
    profilePoint.setValue( iRechtswert, rechtswert );
    profilePoint.setValue( iHochwert, hochwert );

    /* Add length section values. */
    profilePoint.setValue( iBreite, breite );
    profilePoint.setValue( iHoehe, hoehe );
    profilePoint.setValue( iRauheit, rauheit );

    return profilePoint;
  }

  /**
   * This function thins the profile and removes uneccessary points. It uses the Douglas Peucker algorythm.
   * 
   * @param profile
   *          The profile, which should be thinned.
   * @param allowedDistance
   *          The allowed distance [m].
   */
  private void simplifyProfile( final IProfil profile, final double allowedDistance ) throws IllegalProfileOperationException
  {
    /* Get the profile changes. */
    final IProfilChange[] removeChanges = DouglasPeuckerHelper.reduce( allowedDistance, profile.getPoints(), profile );
    if( removeChanges.length == 0 )
      return;

    /* Perform the changes. */
    for( final IProfilChange profilChange : removeChanges )
      profilChange.doChange( null );
  }

  /**
   * Inserts new points into an existing profile.<br/>
   * 
   * @param insertSign
   *          If -1, new points are inserted at the beginning of the profile, 'width' goes into negative direction.
   *          Else, points are inserted at the end of the profile with ascending width.
   */
  public void insertPoints( final IProfil profile, final int insertSign, final Coordinate[] newPoints )
  {
    Assert.isTrue( insertSign == 1 || insertSign == -1 );

    final int insertPositition = insertSign < 0 ? 0 : profile.getPoints().length - 1;

    final IRecord point = profile.getPoint( insertPositition );
    final int iBreite = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );
    double breite = (Double) point.getValue( iBreite );

    int currentPosition = insertPositition;

    Coordinate lastCrd = null;
    for( final Coordinate coordinate : newPoints )
    {
      if( insertSign >= 0 )
        currentPosition++;
      if( lastCrd != null )
      {
        final double distance = lastCrd.distance( coordinate );
        if( insertSign < 0 )
          breite -= distance;
        else
          breite += distance;

        final IRecord newPoint = createPoint( profile, coordinate, breite );
        profile.add( currentPosition, newPoint );
      }

      lastCrd = coordinate;
    }
  }
}