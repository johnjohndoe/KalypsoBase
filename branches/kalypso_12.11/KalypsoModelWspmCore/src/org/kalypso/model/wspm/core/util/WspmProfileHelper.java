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
package org.kalypso.model.wspm.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.commons.math.geom.PolyLine;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.ProfileFactory;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.transformation.transformer.GeoTransformerException;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * TODO: merge / check this class with {@link ProfilUtil}
 * 
 * @author Holger Albert, Thomas Jung , Kim Werner
 */
public final class WspmProfileHelper
{
  private WspmProfileHelper( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * This function returns the width position of a geo point projected on a profile.
   * <p>
   * It works with the following steps:<br />
   * <ol>
   * <li>The profile points with a geo reference are stored. All points without a geo reference are ignored.</li>
   * <li>With these points, single line segments are build (using Rechtswert and Hochwert).</li>
   * <li>The geo point is transformed into the coordinate system of the profiles.</li>
   * <li>It is checked for each segment, which distance the geo point has to them.</li>
   * <li>The points of the segment with the lowest distance will be used for projection.</li>
   * </ol>
   * </p>
   * 
   * @param point
   *          The geo point, must be in the same coordinate system as the profile is. It does not have to lie on the
   *          profile.
   * @param profile
   *          The profile
   * @return The width (X-Direction) of the geo point projected on the profile.
   */
  public static Double getWidthPosition( final Point point, final IProfile profile ) throws GM_Exception, GeoTransformerException
  {
    final String srs = WspmProfileHelper.getCoordinateSystem( profile );

    final GM_Point p = (GM_Point)JTSAdapter.wrap( point, srs );

    return getWidthPosition( p, profile, srs );
  }

  /**
   * Same as {@link #getWidthPosition(Point, IProfil)}, but uses a {@link GM_Point} instead.<br>
   * The point is automatically transformed to the right coordinate system.
   */
  public static Double getWidthPosition( final GM_Point point, final IProfile profile ) throws GeoTransformerException, GM_Exception
  {
    final String srs = WspmProfileHelper.getCoordinateSystem( profile );

    final GM_Point pointInProfileCrs = (GM_Point)point.transform( srs );
    return getWidthPosition( pointInProfileCrs, profile, srs );
  }

  /**
   * Returns the coordinate system of the profile.
   * 
   * @deprecated Every IProfile shoul have its own srs
   */
  @Deprecated
  private static String getCoordinateSystem( final IProfile profile )
  {
    final String crs = profile.getSrsName();
    if( Strings.isEmpty( crs ) )
      return KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    return crs;
  }

  /**
   * @deprecated Use {@link #getWidthPosition(Point, IProfil)} instead.
   */
  @Deprecated
  public static Double getWidthPosition( final Point point, final IProfile profile, final String srsName ) throws Exception
  {
    final GM_Point p = (GM_Point)JTSAdapter.wrap( point, srsName );

    return getWidthPosition( p, profile, srsName );
  }

  /**
   * This function returns the width position of a geo point projected on a profile.
   * <p>
   * It works with the following steps:<br />
   * <ol>
   * <li>The profile points with a geo reference are stored. All points without a geo reference are ignored.</li>
   * <li>With these points, single line segments are build (using Rechtswert and Hochwert).</li>
   * <li>The geo point is transformed into the coordinate system of the profiles.</li>
   * <li>It is checked for each segment, which distance the geo point has to them.</li>
   * <li>The points of the segment with the lowest distance will be used for projection.</li>
   * </ol>
   * </p>
   * 
   * @param geoPoint
   *          The geo point. It does not have to lie on the profile.
   * @param profile
   *          The profile.
   * @param srsName
   *          The coordinate system, in which the profile lies (or null, but this can behave strange, since it assumes
   *          one).
   * @return The width (X-Direction) of the geo point projected on the profile.
   * @deprecated Use {@link #getWidthPosition(Point, IProfil)} instead.
   */
  @Deprecated
  public static Double getWidthPosition( final GM_Point geoPoint, final IProfile profile, final String srsName ) throws GeoTransformerException, GM_Exception
  {
    /* List for storing points of the profile, which have a geo reference. */
    final LinkedList<IRecord> geoReferencedPoints = new LinkedList<>();

    final IRecord[] points = profile.getPoints();
    final int iRechtswert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );
    final int iHochwert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );
    final int iBreite = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );

    for( final IRecord point : points )
    {
      final Object valueRechtswert = point.getValue( iRechtswert );
      final Object valueHochwert = point.getValue( iHochwert );
      if( valueRechtswert == null || valueHochwert == null )
      {
        continue;
      }

      final double rechtsWert = (Double)valueRechtswert;
      final double hochWert = (Double)valueHochwert;

      if( rechtsWert > 0.0 || hochWert > 0.0 )
      {
        /* Memorize the point, because it has a geo reference. */
        geoReferencedPoints.add( point );
      }
    }

    /* If no or only one geo referenced points are found, return. */
    if( geoReferencedPoints.size() <= 1 )
      return null;

    // END OF FINDING GEOREFERENCED POINTS

    /* It is assumed that all points and values share the same coordinate system. */
    final String crs;
    if( srsName == null )
    {
      crs = TimeseriesUtils.getCoordinateSystemNameForGkr( Double.toString( (Double)geoReferencedPoints.get( 0 ).getValue( iRechtswert ) ) );
    }
    else
    {
      crs = srsName;
    }

    final String kalypsoCrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    /* Transform geo point into the coord-system of the line. */
    final GM_Point transformedGeoPoint = (GM_Point)geoPoint.transform( kalypsoCrs );
    final Geometry comparePoint = JTSAdapter.export( transformedGeoPoint );

    double distance = Double.MAX_VALUE;
    IRecord pointOne = null;
    IRecord pointTwo = null;
    LineSegment segment = null;

    /* Now we have a list with fully geo referenced points of a profile. */
    for( int i = 0; i < geoReferencedPoints.size() - 1; i++ )
    {
      /* We need a line string of the two neighbouring points. */
      final IRecord tempPointOne = geoReferencedPoints.get( i );
      final double rechtsWertOne = (Double)tempPointOne.getValue( iRechtswert );
      final double hochWertOne = (Double)tempPointOne.getValue( iHochwert );

      final IRecord tempPointTwo = geoReferencedPoints.get( i + 1 );
      final double rechtsWertTwo = (Double)tempPointTwo.getValue( iRechtswert );
      final double hochWertTwo = (Double)tempPointTwo.getValue( iHochwert );

      /* Create the gm points. */
      final GM_Point geoPointOne = GeometryFactory.createGM_Point( rechtsWertOne, hochWertOne, crs );
      final GM_Point geoPointTwo = GeometryFactory.createGM_Point( rechtsWertTwo, hochWertTwo, crs );

      /* Build the line segment. */
      final Coordinate geoCoordOne = JTSAdapter.export( geoPointOne.transform( kalypsoCrs ).getCentroid().getPosition() );
      final Coordinate geoCoordTwo = JTSAdapter.export( geoPointTwo.transform( kalypsoCrs ).getCentroid().getPosition() );
      final LineSegment geoSegment = new LineSegment( geoCoordOne, geoCoordTwo );

      /* Calculate the distance of the geo point to the line. */
      final double tempDistance = geoSegment.distance( comparePoint.getCoordinate() );

      /* If it is shorter than the last distance, remember it and the distance. */
      if( tempDistance <= distance )
      {
        distance = tempDistance;
        pointOne = tempPointOne;
        pointTwo = tempPointTwo;
        segment = geoSegment;
      }
    }

    /* Now we have a segment and a distance. The two points of the segment are used to interpolate. */

    /* The point on the geo segment, which is closest to the comparePoint (originally geoPoint). */
    final Coordinate geoCoordinate = segment.closestPoint( comparePoint.getCoordinate() );

    final double geoSegmentLength = segment.getLength();
    final double toGeoPointLength = JTSUtilities.getLengthBetweenPoints( segment.getCoordinate( 0 ), geoCoordinate );

    /* Using Breite to build. */
    final double breiteOne = (Double)pointOne.getValue( iBreite );
    final double breiteTwo = (Double)pointTwo.getValue( iBreite );

    /* Important: The interpolation is done here :). */
    final double toProfilePointLength = toGeoPointLength / geoSegmentLength * (breiteTwo - breiteOne);

    return breiteOne + toProfilePointLength;
  }

  /**
   * Returns the geographic coordinates (x, y, z) for a given width coordinate as GM_Point.
   * 
   * @param width
   *          width coordinate
   * @param profile
   *          profile
   * @return Geo position as GM_Point (transformed to the Kalypso coordinate system).
   */
  public static GM_Point getGeoPositionKalypso( final double width, final IProfile profile ) throws GeoTransformerException
  {
    final GM_Point gmPoint = getGeoPosition( width, profile );
    if( gmPoint == null )
      return null;

    if( gmPoint.getCoordinateSystem() == null || gmPoint.getCoordinateSystem().isEmpty() )
      return gmPoint;

    return WspmGeometryUtilities.GEO_TRANSFORMER.transform( gmPoint );
  }

  /**
   * This function returns the geographic coordinates (x, y, z) for a given width coordinate as GM_Point.
   * 
   * @param width
   *          The width coordinate.
   * @param profile
   *          The profile.
   * @return Geo position as GM_Point (untransformed in teh coordinate system of the profile).
   */
  public static GM_Point getGeoPosition( final double width, final IProfile profile )
  {
    /* If no or only one geo referenced points are found, return. */
    final IRecord[] geoReferencedPoints = ProfileUtil.getGeoreferencedPoints( profile );
    if( geoReferencedPoints.length <= 1 )
      return null;

    // END OF FINDING GEOREFERENCED POINTS

    /* Now we have a list with fully geo referenced points of a profile. */
    final String srsName = profile.getSrsName();
    final int iRechtswert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );
    final int iHochwert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );
    final int iBreite = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );
    final int iHoehe = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_HOEHE );
    for( int i = 0; i < geoReferencedPoints.length - 1; i++ )
    {
      /* We need a line string of the two neighbouring points. */
      final IRecord tempPointOne = geoReferencedPoints[i];
      final Double widthValueOne = (Double)tempPointOne.getValue( iBreite );
      final Double heightValueOne = (Double)tempPointOne.getValue( iHoehe );
      final double heightOne = heightValueOne == null ? Double.NaN : heightValueOne;

      final Double rechtsWertOne = (Double)tempPointOne.getValue( iRechtswert );
      final Double hochWertOne = (Double)tempPointOne.getValue( iHochwert );

      final IRecord tempPointTwo = geoReferencedPoints[i + 1];
      final Double widthValueTwo = (Double)tempPointTwo.getValue( iBreite );
      final Double heightValueTwo = (Double)tempPointTwo.getValue( iHoehe );
      final double heightTwo = heightValueTwo == null ? Double.NaN : heightValueTwo;

      final Double rechtsWertTwo = (Double)tempPointTwo.getValue( iRechtswert );
      final Double hochWertTwo = (Double)tempPointTwo.getValue( iHochwert );

      /* Find the right segment with the neighbouring points. */
      if( widthValueOne < width && widthValueTwo > width )
      {
        /* Calculate the geo reference. */
        final double deltaOne = width - widthValueOne;
        final double delta = widthValueTwo - widthValueOne;
        final double x = deltaOne * (rechtsWertTwo - rechtsWertOne) / delta + rechtsWertOne;
        final double y = deltaOne * (hochWertTwo - hochWertOne) / delta + hochWertOne;

        final double z = deltaOne * (heightTwo - heightOne) / delta + heightOne;

        return org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Point( x, y, z, srsName );
      }

      /* If the point is lying on the start point of the segment. */
      else if( widthValueOne == width )
        return org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Point( rechtsWertOne, hochWertOne, heightOne, srsName );
      else if( widthValueTwo == width )
        return org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Point( rechtsWertTwo, hochWertTwo, heightTwo, srsName );
    }

    return null;
  }

  /**
   * Returns the corresponding height for an given width coordinate. if the width is outside of the profile points, the
   * first / last point height is returned. Else the height is obtained by linear interpolation between the adjacent
   * profile points.
   * 
   * @param width
   *          width coordinate
   * @param profile
   *          profile
   * @return The height
   */
  public static Double getHeightByWidth( final double width, final IProfile profile )
  {
    return interpolateValue( profile, width, IWspmPointProperties.POINT_PROPERTY_HOEHE );
  }

  /**
   * Returns the corresponding value for an given width coordinate. if the width is outside the valid range of profile
   * points, the first / last value is returned. Else the value is obtained by linear interpolation between the adjacent
   * profile points.
   * 
   * @param width
   *          width coordinate
   * @param profile
   *          profile
   * @return The height
   */
  public static Double interpolateValue( final IProfile profile, final double width, final String valueComponent )
  {
    final int indexValueComponent = profile.indexOfProperty( valueComponent );
    return interpolateValue( profile, width, indexValueComponent );
  }

  /**
   * Same as {@link #interpolateValue(IProfil, double, String)} but for several values at once.
   */
  public static Double[] interpolateValues( final IProfile profile, final Double[] widths, final String valueComponent )
  {
    final int indexValueComponent = profile.indexOfProperty( valueComponent );
    return interpolateValues( profile, widths, indexValueComponent );
  }

  /**
   * Same as {@link #interpolateValue(IProfil, double, int)} but for several values at once.
   */
  public static Double[] interpolateValues( final IProfile profile, final Double[] widths, final int indexValueComponent )
  {
    final Double[] values = new Double[widths.length];
    for( int i = 0; i < values.length; i++ )
    {
      values[i] = interpolateValue( profile, widths[i], indexValueComponent );
    }

    return values;
  }

  /**
   * Same as {@link #interpolateValue(IProfil, double, String)} but used the component index.
   */
  public static Double interpolateValue( final IProfile profile, final double width, final int indexValueComponent )
  {
    final IRecord[] points = profile.getPoints();
    if( points.length < 1 )
      return null;

    final int iBreite = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );

    Number lastValidWidth = null;
    Number lastValidValue = null;

    for( final IRecord record : points )
    {
      final Object currentWidth = record.getValue( iBreite );
      final Object currentValue = record.getValue( indexValueComponent );

      if( currentWidth instanceof Number && currentValue instanceof Number )
      {
        if( lastValidWidth != null && lastValidValue != null )
        {
          /* We have two adjacent valid widths/values */
          final double widthOne = lastValidWidth.doubleValue();
          final double widthTwo = ((Number)currentWidth).doubleValue();

          if( widthOne <= width & width <= widthTwo )
          {
            /* The width we are looking fore lies between the two adjacent widths -> interpolate */
            final double valueOne = lastValidValue.doubleValue();
            final double valueTwo = ((Number)currentValue).doubleValue();
            return (width - widthOne) * (valueTwo - valueOne) / (widthTwo - widthOne) + valueOne;
          }
        }

        lastValidWidth = (Number)currentWidth;
        lastValidValue = (Number)currentValue;
      }
    }

    if( width < (Double)points[0].getValue( iBreite ) )
      return (Double)points[0].getValue( indexValueComponent );

    if( width > (Double)points[points.length - 1].getValue( iBreite ) )
      return (Double)points[points.length - 1].getValue( indexValueComponent );

    return null;
  }

  /**
   * gets the geo-points of the intersect between profile and water level
   * 
   * @param profil
   *          input profile
   * @param wspHoehe
   *          water level
   */
  public static GM_Point[] calculateWspPoints( final IProfile profil, final double wspHoehe )
  {
    final Double[] intersections = calculateWspIntersections( profil, wspHoehe );

    final IComponent cBreite = profil.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );
    final IComponent cHochwert = profil.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );
    final IComponent cRechtswert = profil.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );

    final String crs = profil.getSrsName();

    /* ignore profile without geo-coordinates */
    if( cHochwert == null || cRechtswert == null )
      return new GM_Point[] {};

    /* Same for RW and HW, but filter 0-values */
    final PolyLine rwLine = createPolyline( profil, cBreite, cRechtswert );
    final PolyLine hwLine = createPolyline( profil, cBreite, cHochwert );
    if( rwLine.length() < 2 || hwLine.length() < 2 )
      return new GM_Point[] {};

    final GM_Point[] poses = new GM_Point[intersections.length];
    int count = 0;
    for( final Double x : intersections )
    {
      final double rw = rwLine.getYFor( x, false );
      final double hw = hwLine.getYFor( x, false );

      final GM_Point point = org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Point( rw, hw, wspHoehe, crs );

      poses[count++] = point;
    }

    return poses;
  }

  /**
   * calculates the water level segments as pairs of x-coordinates.
   */
  public static Double[] calculateWspIntersections( final IProfile profil, final double wspHoehe )
  {
    /* use worker to calculate intersections */
    final WaterlevelIntersectionWorker worker = new WaterlevelIntersectionWorker( profil, wspHoehe );
    worker.execute();
    final LineSegment[] segments = worker.getSegments();

    final Collection<Double> intersections = new ArrayList<>( segments.length * 2 );

    for( final LineSegment lineSegment : segments )
    {
      intersections.add( lineSegment.p0.x );
      intersections.add( lineSegment.p1.x );
    }

    return intersections.toArray( new Double[intersections.size()] );
  }

  private static PolyLine createPolyline( final IProfile profil, final IComponent xProperty, final IComponent yProperty )
  {
    final IRecord[] points = profil.getPoints();

    final double[] xValues = new double[points.length];
    final double[] yValues = new double[points.length];
    final double dy = yProperty.getPrecision();
    final int xIndex = profil.indexOfProperty( xProperty );
    final int yIndex = profil.indexOfProperty( yProperty );
    int count = 0;
    for( final IRecord point : points )
    {
      final Object valueX = point.getValue( xIndex );
      final Object valueY = point.getValue( yIndex );

      if( valueX instanceof Number && valueY instanceof Number )
      {
        final double x = ((Number)valueX).doubleValue();
        final double y = ((Number)valueY).doubleValue();
        if( !Double.isNaN( x ) && !Double.isNaN( y ) )
        {
          if( Math.abs( y ) > dy )
          {
            xValues[count] = x;
            yValues[count] = y;
            count++;
          }
        }
      }
    }

    final double[] xFiltered = new double[count];
    final double[] yFiltered = new double[count];

    System.arraycopy( xValues, 0, xFiltered, 0, count );
    System.arraycopy( yValues, 0, yFiltered, 0, count );

    return new PolyLine( xFiltered, yFiltered, 0.0001 );
  }

  public static GM_Curve cutProfileAtWaterlevel( final double waterlevel, final IProfile profil ) throws Exception
  {
    final GM_Point[] points = WspmProfileHelper.calculateWspPoints( profil, waterlevel );
    IProfile cutProfile = null;

    if( points != null )
    {
      if( points.length > 1 )
      {
        cutProfile = WspmProfileHelper.cutProfile( profil, points[0], points[points.length - 1] );
      }
    }

    return ProfileUtil.getLine( cutProfile );
  }

  /**
   * cuts an IProfil at defined geo-points, that have to lie on the profile-line.
   * 
   * @param profile
   *          the profile
   * @param firstPoint
   *          first geo point
   * @param lastPoint
   *          last geo point
   */
  public static IProfile cutProfile( final IProfile profile, final GM_Point firstPoint, final GM_Point lastPoint ) throws Exception
  {
    final double width1 = WspmProfileHelper.getWidthPosition( firstPoint, profile );
    final double width2 = WspmProfileHelper.getWidthPosition( lastPoint, profile );

    final IProfile orgIProfil = profile;

    final double startWidth;
    final double endWidth;
    final GM_Point geoPoint1;
    final GM_Point geoPoint2;

    if( width1 > width2 )
    {
      startWidth = width2;
      endWidth = width1;
      geoPoint1 = lastPoint;
      geoPoint2 = firstPoint;
    }
    else
    {
      startWidth = width1;
      endWidth = width2;
      geoPoint1 = firstPoint;
      geoPoint2 = lastPoint;
    }

    // calculate elevations
    final double heigth1 = WspmProfileHelper.getHeightByWidth( startWidth, orgIProfil );
    final double heigth2 = WspmProfileHelper.getHeightByWidth( endWidth, orgIProfil );

    final IProfileRecord[] profilPointList = profile.getPoints();
    final IProfile tmpProfil = ProfileFactory.createProfil( profile.getType(), null );

    /* set the coordinate system */
    tmpProfil.setSrsName( profile.getSrsName() );

    final IComponent cBreite = tmpProfil.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_BREITE );
    final IComponent cHoehe = tmpProfil.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_HOEHE );
    final IComponent cHochwert = tmpProfil.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );
    final IComponent cRechtswert = tmpProfil.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );

    if( !tmpProfil.hasPointProperty( cBreite ) )
    {
      tmpProfil.addPointProperty( cBreite );
    }
    if( !tmpProfil.hasPointProperty( cHoehe ) )
    {
      tmpProfil.addPointProperty( cHoehe );
    }
    if( !tmpProfil.hasPointProperty( cHochwert ) )
    {
      tmpProfil.addPointProperty( cHochwert );
    }
    if( !tmpProfil.hasPointProperty( cRechtswert ) )
    {
      tmpProfil.addPointProperty( cRechtswert );
    }

    final int iBreite = tmpProfil.indexOfProperty( cBreite );
    final int iHoehe = tmpProfil.indexOfProperty( cHoehe );
    final int iRechtswert = tmpProfil.indexOfProperty( cRechtswert );
    final int iHochwert = tmpProfil.indexOfProperty( cHochwert );

    final IProfileRecord point1 = tmpProfil.createProfilPoint();
    final IProfileRecord point2 = tmpProfil.createProfilPoint();

    /* calculate the width of the intersected profile */
    // sort intersection points by width
    point1.setValue( iBreite, startWidth );
    point1.setValue( iHoehe, heigth1 );
    point1.setValue( iHochwert, geoPoint1.getY() );
    point1.setValue( iRechtswert, geoPoint1.getX() );

    point2.setValue( iBreite, endWidth );
    point2.setValue( iHoehe, heigth2 );
    point2.setValue( iHochwert, geoPoint2.getY() );
    point2.setValue( iRechtswert, geoPoint2.getX() );

    tmpProfil.addPoint( point1 );

    for( final IProfileRecord point : profilPointList )
    {
      final double currentWidth = (Double)point.getValue( iBreite );
      if( currentWidth > startWidth & currentWidth < endWidth )
      {
        final IProfileRecord pt = tmpProfil.createProfilPoint();

        final IComponent[] properties = orgIProfil.getPointProperties();
        for( final IComponent property : properties )
        {
          final int iProp = tmpProfil.indexOfProperty( property );
          if( iProp != -1 )
          {
            final Object value = point.getValue( iProp );
            pt.setValue( iProp, value );
          }
        }
        tmpProfil.addPoint( pt );
      }
    }

    tmpProfil.addPoint( point2 );

    tmpProfil.setStation( orgIProfil.getStation() );

    return tmpProfil;
  }

  /**
   * Creates a new profile point from a location at a given width.
   */
  public static IProfileRecord createRecord( final IProfile profile, final Coordinate coordinate, final double breite )
  {
    /* The needed components. */
    final IComponent cRechtswert = profile.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );
    final IComponent cHochwert = profile.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );
    final IComponent cBreite = profile.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_BREITE );
    final IComponent cHoehe = profile.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_HOEHE );
    final IComponent cRauheit = profile.getPointPropertyFor( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS );

    /* Add components if necessary. */
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

    /* Get index for component. */
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
    final IProfileRecord profilePoint = profile.createProfilPoint();

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
   * @param newPoints
   *          The location to be inserted as new points into the profile. Must be in the same coordinate system as the profile.
   * @param coordinatesCRS
   *          The coordinate system of the new points.
   */
  public static void insertPoints( final IProfile profile, final int insertSign, final Coordinate[] newPoints )
  {
    Assert.isTrue( insertSign == 1 || insertSign == -1 );

    final int insertPositition = insertSign < 0 ? 0 : profile.getPoints().length - 1;
    final IRecord point = profile.getPoint( insertPositition );
    final int iBreite = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );
    double breite = (Double)point.getValue( iBreite );

    final List<IProfileRecord> newRecords = new ArrayList<>( newPoints.length );

    Coordinate lastCrd = null;
    for( final Coordinate coordinate : newPoints )
    {
      /* Transform the coordinate into the coordinate system of the profile. */
      if( lastCrd != null )
      {
        final double distance = lastCrd.distance( coordinate );
        if( insertSign < 0 )
          breite -= distance;
        else
          breite += distance;

        // REMARK: using record here because we later directly insert into the TupleResult
        final IProfileRecord newPoint = createRecord( profile, coordinate, breite );
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

  public static IProfile convertLinestringToEmptyProfile( final GM_Curve curve, final String profileType ) throws GM_Exception
  {
    final LineString jtsCurve = (LineString)JTSAdapter.export( curve );
    return convertLinestringToEmptyProfile( jtsCurve, profileType );
  }

  /**
   * creates a profile from {@link LineString} with '0.0' as z-values.
   */
  public static IProfile convertLinestringToEmptyProfile( final LineString jtsCurve, final String type )
  {
    if( jtsCurve == null )
      return null;

    /* Create the new profile. */
    final IProfile profile = ProfileFactory.createProfil( type, null );

    double breite = 0.0;

    for( int i = 0; i < jtsCurve.getNumPoints(); i++ )
    {
      final Coordinate coordinate = jtsCurve.getCoordinateN( i );

      final IProfileRecord newPoint = createRecord( profile, coordinate, breite );

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
  public static IProfile convertLinestringToEmptyProfile( final Coordinate[] points, final String type )
  {
    /* Create the new profile. */
    final IProfile profile = ProfileFactory.createProfil( type, null );

    double breite = 0.0;

    for( int i = 0; i < points.length; i++ )
    {
      final Coordinate coordinate = points[i];

      final IProfileRecord newPoint = createRecord( profile, coordinate, breite );

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
}