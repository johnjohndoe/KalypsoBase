/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IllegalProfileOperationException;
import org.kalypso.model.wspm.core.profil.ProfilFactory;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
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
public final class CoverageProfile
{
  private CoverageProfile( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
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
  public static IProfil createProfile( final String profileType, final Coordinate[] pointsZ, final String crsOfCrds, final double simplifyDistance ) throws Exception
  {
    /* STEP 2: Compute the width and height for each point of the new line. */
    /* STEP 3: Create the new profile. */
    final IProfil profile = calculatePointsAndCreateProfile( profileType, pointsZ, crsOfCrds );
    final int length = profile.getPoints().length;
    if( length == 0 )
      return profile;

    /* STEP 4: Simplify the profile. */
    ProfilUtil.simplifyProfile( profile, simplifyDistance );

    return profile;
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
  private static IProfil calculatePointsAndCreateProfile( final String profileType, final Coordinate[] points, final String crsOfPoints ) throws Exception
  {
    /* Create the new profile. */
    final IProfil profile = ProfilFactory.createProfil( profileType );
    profile.setProperty( IWspmConstants.PROFIL_PROPERTY_CRS, crsOfPoints );

    // TODO: check: we calculate the 'breite' by just adding up the distances between the points, is this always OK?
    // FIXME: no! this at least depends on the coordinate system....!
    double breite = 0.0;
    Coordinate lastCrd = null;

    for( final Coordinate coordinate : points )
    {
      final IRecord profilePoint = createPoint( profile, coordinate, breite );

      /* Add the new point to the profile. */
      profile.addPoint( profilePoint );

      if( lastCrd != null )
      {
        // FIXME: this at least depends on the coordinate system. In WGS84 we get deegrees, but we always need [m]!
        breite += coordinate.distance( lastCrd );
      }

      lastCrd = coordinate;
    }

    return profile;
  }

  private static IRecord createPoint( final IProfil profile, final Coordinate coordinate, final double breite )
  {
    /* The needed components. */
    final IComponent cRechtswert = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_RECHTSWERT );
    final IComponent cHochwert = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final IComponent cBreite = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_BREITE );
    final IComponent cHoehe = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IComponent cRauheit = profile.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_RAUHEIT_KS );

    /* add components if necessary */
    if( !profile.hasPointProperty( cRechtswert ) )
    {
      profile.addPointProperty( cRechtswert );
    }
    if( !profile.hasPointProperty( cHochwert ) )
    {
      profile.addPointProperty( cHochwert );
    }
    if( !profile.hasPointProperty( cBreite ) )
    {
      profile.addPointProperty( cBreite );
    }
    if( !profile.hasPointProperty( cHoehe ) )
    {
      profile.addPointProperty( cHoehe );
    }
    if( !profile.hasPointProperty( cRauheit ) )
    {
      profile.addPointProperty( cRauheit );
    }

    /* get index for component */
    final int iRechtswert = profile.indexOfProperty( cRechtswert );
    final int iHochwert = profile.indexOfProperty( cHochwert );
    final int iBreite = profile.indexOfProperty( cBreite );
    final int iHoehe = profile.indexOfProperty( cHoehe );
    final int iRauheit = profile.indexOfProperty( cRauheit );

    /* All necessary values. */
    final Double rechtswert = coordinate.x;
    final Double hochwert = coordinate.y;
    final Double hoehe = Double.isNaN( coordinate.z ) ? 0.0 : coordinate.z;
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
   * Inserts new points into an existing profile.<br/>
   * 
   * @param insertSign
   *          If -1, new points are inserted at the beginning of the profile, 'width' goes into negative direction.
   *          Else, points are inserted at the end of the profile with ascending width.
   */
  public static void insertPoints( final IProfil profile, final int insertSign, final Coordinate[] newPoints )
  {
    Assert.isTrue( insertSign == 1 || insertSign == -1 );

    final int insertPositition = insertSign < 0 ? 0 : profile.getPoints().length - 1;

    final IRecord point = profile.getPoint( insertPositition );
    final int iBreite = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );
    double breite = (Double) point.getValue( iBreite );

    final List<IRecord> newRecords = new ArrayList<IRecord>( newPoints.length );

    Coordinate lastCrd = null;
    for( final Coordinate coordinate : newPoints )
    {
      if( lastCrd != null )
      {
        final double distance = lastCrd.distance( coordinate );
        if( insertSign < 0 )
        {
          breite -= distance;
        }
        else
        {
          breite += distance;
        }

        final IRecord newPoint = createPoint( profile, coordinate, breite );
        newRecords.add( newPoint );
      }

      lastCrd = coordinate;
    }

    final TupleResult result = profile.getResult();
    if( insertSign < 0 )
    {
      Collections.reverse( newRecords );
      result.addAll( 0, newRecords );
    }
    else
    {
      result.addAll( newRecords );
    }
  }

  public static IProfil convertLinestringToEmptyProfile( final GM_Curve curve, final String profileType ) throws GM_Exception
  {
    final LineString jtsCurve = (LineString) JTSAdapter.export( curve );
    return convertLinestringToEmptyProfile( jtsCurve, profileType );
  }

  /**
   * creates a profile from {@link LineString} with '0.0' as z-values.
   */
  public static IProfil convertLinestringToEmptyProfile( final LineString jtsCurve, final String type )
  {
    if( jtsCurve == null )
      return null;

    /* Create the new profile. */
    final IProfil profile = ProfilFactory.createProfil( type );

    double breite = 0.0;

    for( int i = 0; i < jtsCurve.getNumPoints(); i++ )
    {
      final Coordinate coordinate = jtsCurve.getCoordinateN( i );

      final IRecord newPoint = createPoint( profile, coordinate, breite );

      /* calculate breite */
      if( i > 0 )
      {
        final double distance = coordinate.distance( jtsCurve.getCoordinateN( i - 1 ) );
        breite += distance;
      }

      profile.addPoint( newPoint );
    }

    return profile;
  }

  /**
   * creates a profile from an array of Coordinate's with '0.0' as z-values.
   */
  public static IProfil convertLinestringToEmptyProfile( final Coordinate[] points, final String type )
  {
    /* Create the new profile. */
    final IProfil profile = ProfilFactory.createProfil( type );

    double breite = 0.0;

    for( int i = 0; i < points.length; i++ )
    {
      final Coordinate coordinate = points[i];

      final IRecord newPoint = createPoint( profile, coordinate, breite );

      /* calculate breite */
      if( i > 0 )
      {
        final double distance = coordinate.distance( points[i - 1] );
        breite += distance;
      }

      profile.addPoint( newPoint );
    }

    return profile;
  }

  /**
   * @param insertSign
   *          -1 points will be insert before, ...
   */
  public static void extendPoints( final IProfil profil, final int insertSign, final Coordinate[] simplifiedCords, final double simplifyDistance ) throws IllegalProfileOperationException
  {
    if( ArrayUtils.isEmpty( simplifiedCords ) )
      return;

    CoverageProfile.insertPoints( profil, insertSign, simplifiedCords );

    final int length = profil.getPoints().length;
    final int start = insertSign == -1 ? 0 : length - simplifiedCords.length;
    final int end = insertSign == -1 ? simplifiedCords.length : length;

    ProfilUtil.simplifyProfile( profil, simplifyDistance, start, end - 1 );
  }
}