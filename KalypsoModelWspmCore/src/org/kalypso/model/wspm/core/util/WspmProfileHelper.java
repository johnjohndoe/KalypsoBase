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

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.kalypso.commons.math.geom.PolyLine;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.ProfilFactory;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Holger Albert, Thomas Jung , kimwerner TODO: merge / check this class with {@link ProfilUtil}
 */
public final class WspmProfileHelper
{
  private WspmProfileHelper( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  public static final double FUZZINESS = 0.005; // Inaccuracies profile of points

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
  public static Double getWidthPosition( final Point point, final IProfil profile ) throws Exception
  {
    final String srs = WspmProfileHelper.getCoordinateSystem( profile );

    final GM_Point p = (GM_Point) JTSAdapter.wrap( point, srs );

    return getWidthPosition( p, profile, srs );
  }

  /**
   * Same as {@link #getWidthPosition(Point, IProfil)}, but uses a {@link GM_Point} instead.<br>
   * The point is automatically transformed to the right coordinate system.
   */
  public static Double getWidthPosition( final GM_Point point, final IProfil profile ) throws Exception
  {
    final String srs = WspmProfileHelper.getCoordinateSystem( profile );

    final GM_Point pointInProfileCrs = (GM_Point) point.transform( srs );
    return getWidthPosition( pointInProfileCrs, profile, srs );
  }

  /**
   * Returns the coordinate system of the profile.
   */
  private static String getCoordinateSystem( final IProfil profile )
  {
    return ObjectUtils.toString( profile.getProperty( IWspmConstants.PROFIL_PROPERTY_CRS ) );
  }

  /**
   * @deprecated Use {@link #getWidthPosition(Point, IProfil)} instead.
   */
  @Deprecated
  public static Double getWidthPosition( final Point point, final IProfil profile, final String srsName ) throws Exception
  {
    final GM_Point p = (GM_Point) JTSAdapter.wrap( point, srsName );

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
  public static Double getWidthPosition( final GM_Point geoPoint, final IProfil profile, final String srsName ) throws Exception
  {
    /* List for storing points of the profile, which have a geo reference. */
    final LinkedList<IRecord> geoReferencedPoints = new LinkedList<IRecord>();

    final IRecord[] points = profile.getPoints();
    final int iRechtswert = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_RECHTSWERT );
    final int iHochwert = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final int iBreite = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );

    for( final IRecord point : points )
    {
      final Object valueRechtswert = point.getValue( iRechtswert );
      final Object valueHochwert = point.getValue( iHochwert );
      if( valueRechtswert == null || valueHochwert == null )
        continue;

      final double rechtsWert = (Double) valueRechtswert;
      final double hochWert = (Double) valueHochwert;

      if( rechtsWert > 0.0 || hochWert > 0.0 )
        /* Memorize the point, because it has a geo reference. */
        geoReferencedPoints.add( point );
    }

    /* If no or only one geo referenced points are found, return. */
    if( geoReferencedPoints.size() <= 1 )
      return null;

    // END OF FINDING GEOREFERENCED POINTS

    /* It is assumed that all points and values share the same coordinate system. */
    final String crs;
    if( srsName == null )
      crs = TimeseriesUtils.getCoordinateSystemNameForGkr( Double.toString( (Double) geoReferencedPoints.get( 0 ).getValue( iRechtswert ) ) );
    else
      crs = srsName;

    final String kalypsoCrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    /* Transform geo point into the coord-system of the line. */
    final GM_Point transformedGeoPoint = (GM_Point) geoPoint.transform( kalypsoCrs );
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
      final double rechtsWertOne = (Double) tempPointOne.getValue( iRechtswert );
      final double hochWertOne = (Double) tempPointOne.getValue( iHochwert );

      final IRecord tempPointTwo = geoReferencedPoints.get( i + 1 );
      final double rechtsWertTwo = (Double) tempPointTwo.getValue( iRechtswert );
      final double hochWertTwo = (Double) tempPointTwo.getValue( iHochwert );

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
    final double breiteOne = (Double) pointOne.getValue( iBreite );
    final double breiteTwo = (Double) pointTwo.getValue( iBreite );

    /* Important: The interpolation is done here :). */
    final double toProfilePointLength = toGeoPointLength / geoSegmentLength * (breiteTwo - breiteOne);

    return breiteOne + toProfilePointLength;
  }

  /**
   * returns the geographic coordinates (x, y, z) for a given width coordinate as GM_Point.
   * 
   * @param width
   *          width coordinate
   * @param profile
   *          profile
   * @return Geo position as GM_Point (untransformed).
   */
  public static GM_Point getGeoPosition( final double width, final IProfil profile ) throws Exception
  {
    final GM_Point gmPoint = getGeoPositionInternal( width, profile );
    if( gmPoint == null )
      return null;

    if( gmPoint.getCoordinateSystem() == null || gmPoint.getCoordinateSystem().isEmpty() )
      return gmPoint;

    return (GM_Point) WspmGeometryUtilities.GEO_TRANSFORMER.transform( gmPoint );
  }

  /**
   * This function returns the geographic coordinates (x, y, z) for a given width coordinate as GM_Point.
   * 
   * @param width
   *          The width coordinate.
   * @param profile
   *          The profile.
   * @return Geo position as GM_Point (untransformed).
   */
  private static GM_Point getGeoPositionInternal( final double width, final IProfil profile ) throws Exception
  {
    /* If no or only one geo referenced points are found, return. */
    final IRecord[] geoReferencedPoints = ProfilUtil.getGeoreferencedPoints( profile );
    if( geoReferencedPoints.length <= 1 )
      return null;

    // END OF FINDING GEOREFERENCED POINTS

    /* Now we have a list with fully geo referenced points of a profile. */
    final String srsName = (String) profile.getProperty( IWspmConstants.PROFIL_PROPERTY_CRS );
    final int iRechtswert = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_RECHTSWERT );
    final int iHochwert = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final int iBreite = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );
    final int iHoehe = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_HOEHE );
    for( int i = 0; i < geoReferencedPoints.length - 1; i++ )
    {
      /* We need a line string of the two neighbouring points. */
      final IRecord tempPointOne = geoReferencedPoints[i];
      final Double widthValueOne = (Double) tempPointOne.getValue( iBreite );
      final Double heigthValueOne = (Double) tempPointOne.getValue( iHoehe );
      final Double rechtsWertOne = (Double) tempPointOne.getValue( iRechtswert );
      final Double hochWertOne = (Double) tempPointOne.getValue( iHochwert );

      final IRecord tempPointTwo = geoReferencedPoints[i + 1];
      final Double widthValueTwo = (Double) tempPointTwo.getValue( iBreite );
      final Double heigthValueTwo = (Double) tempPointTwo.getValue( iHoehe );
      final Double rechtsWertTwo = (Double) tempPointTwo.getValue( iRechtswert );
      final Double hochWertTwo = (Double) tempPointTwo.getValue( iHochwert );

      /* Find the right segment with the neighbouring points. */
      if( widthValueOne < width && widthValueTwo > width )
      {
        /* Calculate the geo reference. */
        final double deltaOne = width - widthValueOne;
        final double delta = widthValueTwo - widthValueOne;
        final double x = deltaOne * (rechtsWertTwo - rechtsWertOne) / delta + rechtsWertOne;
        final double y = deltaOne * (hochWertTwo - hochWertOne) / delta + hochWertOne;
        final double z = deltaOne * (heigthValueTwo - heigthValueOne) / delta + heigthValueOne;

        return org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Point( x, y, z, srsName );
      }

      /* If the point is lying on the start point of the segment. */
      else if( widthValueOne == width )
        return org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Point( rechtsWertOne, hochWertOne, heigthValueOne, srsName );
      else if( widthValueTwo == width )
        return org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Point( rechtsWertTwo, hochWertTwo, heigthValueTwo, srsName );
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
  public static Double getHeightByWidth( final double width, final IProfil profile ) throws IndexOutOfBoundsException
  {
    return interpolateValue( profile, width, IWspmConstants.POINT_PROPERTY_HOEHE );
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
  public static Double interpolateValue( final IProfil profile, final double width, final String valueComponent ) throws IndexOutOfBoundsException
  {
    final int indexValueComponent = profile.indexOfProperty( valueComponent );
    return interpolateValue( profile, width, indexValueComponent );
  }

  /**
   * Same as {@link #interpolateValue(IProfil, double, String)} but for several values at once.
   */
  public static Double[] interpolateValues( final IProfil profile, final Double[] widths, final String valueComponent ) throws IndexOutOfBoundsException
  {
    final int indexValueComponent = profile.indexOfProperty( valueComponent );
    return interpolateValues( profile, widths, indexValueComponent );
  }

  /**
   * Same as {@link #interpolateValue(IProfil, double, int)} but for several values at once.
   */
  public static Double[] interpolateValues( final IProfil profile, final Double[] widths, final int indexValueComponent ) throws IndexOutOfBoundsException
  {
    final Double[] values = new Double[widths.length];
    for( int i = 0; i < values.length; i++ )
      values[i] = interpolateValue( profile, widths[i], indexValueComponent );

    return values;
  }

  /**
   * Same as {@link #interpolateValue(IProfil, double, String)} but used the component index.
   */
  public static Double interpolateValue( final IProfil profile, final double width, final int indexValueComponent ) throws IndexOutOfBoundsException
  {
    final IRecord[] points = profile.getPoints();
    if( points.length < 1 )
      return null;

    final int iBreite = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );

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
          final double widthTwo = ((Number) currentWidth).doubleValue();

          if( widthOne <= width & width <= widthTwo )
          {
            /* The width we are looking fore lies between the two adjacent widths -> interpolate */
            final double valueOne = lastValidValue.doubleValue();
            final double valueTwo = ((Number) currentValue).doubleValue();
            return (width - widthOne) * (valueTwo - valueOne) / (widthTwo - widthOne) + valueOne;
          }
        }

        lastValidWidth = (Number) currentWidth;
        lastValidValue = (Number) currentValue;
      }
    }

    if( width < (Double) points[0].getValue( iBreite ) )
      return (Double) points[0].getValue( indexValueComponent );

    if( width > (Double) points[points.length - 1].getValue( iBreite ) )
      return (Double) points[points.length - 1].getValue( indexValueComponent );

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
  public static GM_Point[] calculateWspPoints( final IProfil profil, final double wspHoehe )
  {
    final Double[] intersections = calculateWspIntersections( profil, wspHoehe );

    final IComponent cBreite = profil.hasPointProperty( IWspmConstants.POINT_PROPERTY_BREITE );
    final IComponent cHochwert = profil.hasPointProperty( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final IComponent cRechtswert = profil.hasPointProperty( IWspmConstants.POINT_PROPERTY_RECHTSWERT );

    final String crs = (String) profil.getProperty( IWspmConstants.PROFIL_PROPERTY_CRS );

    /* ignore profile without geo-coordinates */
    if( cHochwert == null || cRechtswert == null )
      return new GM_Point[] {};

    /* Same for RW and HW, but filter 0-values */
    final PolyLine rwLine = createPolyline( profil, cBreite, cRechtswert );
    final PolyLine hwLine = createPolyline( profil, cBreite, cHochwert );

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
   * calculates the waterlevel segments as pairs of x-coordinates.
   */
  public static Double[] calculateWspIntersections( final IProfil profil, final double wspHoehe )
  {
    final IComponent cHoehe = profil.hasPointProperty( IWspmConstants.POINT_PROPERTY_HOEHE );
    final int iHoehe = profil.indexOfProperty( cHoehe );
    final IComponent cBreite = profil.hasPointProperty( IWspmConstants.POINT_PROPERTY_BREITE );
    final int iBreite = profil.indexOfProperty( cBreite );

    final IRecord[] points = profil.getPoints();
    final IRecord firstPoint = points[0];
    final IRecord lastPoint = points[points.length - 1];

    final double firstX = (Double) firstPoint.getValue( iBreite );
    final double firstY = (Double) firstPoint.getValue( iHoehe );
    final double lastX = (Double) lastPoint.getValue( iBreite );
    final double lastY = (Double) lastPoint.getValue( iHoehe );

    final Double[] breiteValues = ProfilUtil.getDoubleValuesFor( profil, cBreite );

    final PolyLine wspLine = new PolyLine( new double[] { firstX, lastX }, new double[] { wspHoehe, wspHoehe }, 0.0001 );
    final PolyLine profilLine = new PolyLine( breiteValues, ProfilUtil.getDoubleValuesFor( profil, cHoehe ), 0.0001 );

    final double[] intersectionXs = profilLine.intersect( wspLine );

    final SortedSet<Double> intersections = new TreeSet<Double>();

    if( firstY < wspHoehe )
      intersections.add( new Double( firstX ) );
    for( final double d : intersectionXs )
      intersections.add( new Double( d ) );
    if( lastY < wspHoehe )
      intersections.add( new Double( lastX ) );

    return intersections.toArray( new Double[intersections.size()] );
  }

  private static PolyLine createPolyline( final IProfil profil, final IComponent xProperty, final IComponent yProperty )
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
        final double x = ((Number) valueX).doubleValue();
        final double y = ((Number) valueY).doubleValue();

        if( Math.abs( y ) > dy )
        {
          xValues[count] = x;
          yValues[count] = y;
          count++;
        }
      }
    }

    final double[] xFiltered = new double[count];
    final double[] yFiltered = new double[count];

    System.arraycopy( xValues, 0, xFiltered, 0, count );
    System.arraycopy( yValues, 0, yFiltered, 0, count );

    return new PolyLine( xFiltered, yFiltered, 0.0001 );
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
  public static IProfil cutIProfile( final IProfil profile, final GM_Point firstPoint, final GM_Point lastPoint ) throws Exception
  {
    final double width1 = WspmProfileHelper.getWidthPosition( firstPoint, profile, profile.getName() );
    final double width2 = WspmProfileHelper.getWidthPosition( lastPoint, profile, profile.getName() );

    final IProfil orgIProfil = profile;

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

    final IRecord[] profilPointList = profile.getPoints();
    final IProfil tmpProfil = ProfilFactory.createProfil( profile.getType() );

    /* set the coordinate system */
    final String crs = (String) profile.getProperty( IWspmConstants.PROFIL_PROPERTY_CRS );
    tmpProfil.setProperty( IWspmConstants.PROFIL_PROPERTY_CRS, crs );

    final IComponent cBreite = tmpProfil.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_BREITE );
    final IComponent cHoehe = tmpProfil.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IComponent cHochwert = tmpProfil.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_HOCHWERT );
    final IComponent cRechtswert = tmpProfil.getPointPropertyFor( IWspmConstants.POINT_PROPERTY_RECHTSWERT );

    if( !tmpProfil.hasPointProperty( cBreite ) )
      tmpProfil.addPointProperty( cBreite );
    if( !tmpProfil.hasPointProperty( cHoehe ) )
      tmpProfil.addPointProperty( cHoehe );
    if( !tmpProfil.hasPointProperty( cHochwert ) )
      tmpProfil.addPointProperty( cHochwert );
    if( !tmpProfil.hasPointProperty( cRechtswert ) )
      tmpProfil.addPointProperty( cRechtswert );

    final int iBreite = tmpProfil.indexOfProperty( cBreite );
    final int iHoehe = tmpProfil.indexOfProperty( cHoehe );
    final int iRechtswert = tmpProfil.indexOfProperty( cRechtswert );
    final int iHochwert = tmpProfil.indexOfProperty( cHochwert );

    final IRecord point1 = tmpProfil.createProfilPoint();
    final IRecord point2 = tmpProfil.createProfilPoint();

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

    for( final IRecord point : profilPointList )
    {
      final double currentWidth = (Double) point.getValue( iBreite );
      if( currentWidth > startWidth & currentWidth < endWidth )
      {
        final IRecord pt = tmpProfil.createProfilPoint();

        final IComponent[] properties = orgIProfil.getPointProperties();
        for( final IComponent property : properties )
        {
          final int iProp = point.getOwner().indexOfComponent( property );
          final Object value = point.getValue( iProp );
          pt.setValue( iProp, value );
        }
        tmpProfil.addPoint( pt );
      }
    }

    tmpProfil.addPoint( point2 );

    tmpProfil.setStation( orgIProfil.getStation() );
    return tmpProfil;
  }

  public static IRecord addRecordByWidth( final IProfil profile, final IRecord record, final boolean overwritePointMarkers )
  {
    final Double width = ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_BREITE, record );

    final IRecord[] records = profile.getPoints();
    final int iBreite = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );

    for( int i = 0; i < records.length; i++ )
    {
      final IRecord r = records[i];
      final Double rw = (Double) r.getValue( iBreite );

      if( Math.abs( width - rw ) < FUZZINESS )
      {
        /* record already exists - copy values */
        for( final IComponent component : profile.getPointProperties() )
        {
          // don't overwrite existing point markers!
          if( !overwritePointMarkers && profile.isPointMarker( component.getId() ) )
            continue;
          final int index = profile.indexOfProperty( component );
          r.setValue( index, record.getValue( index ) );
        }
        return r;
      }
      else if( width < rw )
      {
        // add new record
        profile.getResult().add( i, record );
        return record;
      }
      else if( width.equals( rw ) )
        throw new IllegalStateException();
    }

    profile.addPoint( record );

    return record;
  }

  /**
   * Adds a record by its width. If this record(point) already exists in the profile, the existing record will be
   * updated
   */
  public static IRecord addRecordByWidth( final IProfil profile, final IRecord record )
  {
    return addRecordByWidth( profile, record, false );
  }

  /**
   * Returns the profile point with the lowest height.
   * 
   * @return The index of the point with the smallest height value. Returns <code>-1</code> if no such point can be
   *         determined.
   */
  public static int findLowestPointIndex( final IProfil profile )
  {
    double minHeight = Double.MAX_VALUE;
    int minIndex = -1;

    final int iHeight = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_HOEHE );
    final IRecord[] records = profile.getPoints();
    for( int i = 0; i < records.length; i++ )
    {
      final IRecord point = records[i];
      final Object heightValue = point.getValue( iHeight );
      if( heightValue instanceof Number )
      {
        final double height = ((Number) heightValue).doubleValue();
        if( height < minHeight )
        {
          minHeight = height;
          minIndex = i;
        }
      }
    }

    return minIndex;
  }

  /**
   * Returns the profile point with the lowest height.
   */
  public static IRecord findLowestPoint( final IProfil profile )
  {
    final int index = findLowestPointIndex( profile );
    if( index == -1 )
      return null;

    return profile.getPoint( index );
  }
}