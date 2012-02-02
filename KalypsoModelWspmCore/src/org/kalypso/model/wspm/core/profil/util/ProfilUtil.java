/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraï¿½e 22
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
package org.kalypso.model.wspm.core.profil.util;

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.math.LinearEquation;
import org.kalypso.commons.math.LinearEquation.SameXValuesException;
import org.kalypso.contribs.java.util.Arrays;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.IllegalProfileOperationException;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author kimwerner
 */
public final class ProfilUtil
{
  private ProfilUtil( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * @return the values of each point for this pointProperty in the correct order
   */
  public static Object[] getValuesFor( final IProfil profil, final IComponent pointProperty )
  {
    return getValuesFor( profil, pointProperty, false );
  }

  /**
   * @return the values of each point for this pointProperty in the correct order
   */
  public static Object[] getValuesFor( final IProfil profil, final IComponent pointProperty, final boolean skipNullValues )
  {
    final IProfileRecord[] points = profil.getPoints();
    return getValuesFor( points, pointProperty, skipNullValues );
  }

  public static IComponent getFeatureComponent( final String propertyId )
  {
    return ComponentUtilities.getFeatureComponent( propertyId );
  }

  /**
   * Converts a double valued station into a BigDecimal with a scale of {@value #STATION_SCALE}.
   * 
   * @see #STATION_SCALE
   */
  public static BigDecimal stationToBigDecimal( final double station )
  {
    return new BigDecimal( station ).setScale( IProfileFeature.STATION_SCALE, RoundingMode.HALF_UP );
  }

  /**
   * @return the DoubleValues of each point for this pointProperty in the correct order
   */
  public static Object[] getValuesFor( final IProfileRecord[] points, final IComponent pointProperty, final boolean skipNullValues )
  {
    return getValuesFor( points, pointProperty.getId(), skipNullValues );
  }

  public static Object[] getValuesFor( final IRecord[] points, final String component, final boolean skipNullValues )
  {
    if( points == null || points.length < 1 )
      return new Object[0];

    final TupleResult owner = points[0].getOwner();
    final int iProp = owner.indexOfComponent( component );
    if( iProp < 0 )
      throw new IllegalArgumentException( String.format( "Unknown component: %s", component ) ); //$NON-NLS-1$

    return getValuesFor( points, iProp, skipNullValues );
  }

  public static Object[] getValuesFor( final IRecord[] points, final int componentIndex, final boolean skipNullValues )
  {
    final Collection<Object> values = new ArrayList<Object>( points.length );

    for( final IRecord point : points )
    {
      final Object value = point.getValue( componentIndex );
      if( value != null || !skipNullValues )
        values.add( value );
    }

    return values.toArray( new Object[values.size()] );
  }

  public static int getNextNonNull( final IRecord[] points, final int start, final int componentIndex )
  {
    for( int i = start + 1; i < points.length; i++ )
    {

      if( points[i] != null && points[i].getValue( componentIndex ) != null )
        return i;
    }
    return -1;
  }

  public static double getDoubleValueFor( final IComponent component, final IRecord point )
  {
    if( component == null )
      return Double.NaN;
    return getDoubleValueFor( component.getId(), point );
  }

  public static double getDoubleValueFor( final String componentID, final IRecord point )
  {
    final int iComponent = point == null ? -1 : point.indexOfComponent( componentID );
    if( iComponent == -1 )
      return Double.NaN;

    return getDoubleValueFor( iComponent, point );
  }

  public static double getDoubleValueFor( final int componentIndex, final IRecord point )
  {
    try
    {
      final Object oValue = point.getValue( componentIndex );
      if( oValue instanceof Number )
        return ((Number) oValue).doubleValue();
      return Double.NaN;
    }
    catch( final IndexOutOfBoundsException e )
    {
      e.printStackTrace();
      return Double.NaN;
    }
  }

  /**
   * Returns all points between the given markers (closed interval).<br/>
   * If the marker does not exist (or not enough markers of that kind), the whole profile is returned.
   */
  public static List<IRecord> getInnerPoints( final IProfil profil, final IComponent markerTyp )
  {
    final IProfilPointMarker[] markers = profil.getPointMarkerFor( markerTyp );
    final IProfileRecord[] points = profil.getPoints();

    // REMARK: check both for length > 1, a single marker does not count
    final int leftPos = markers.length > 1 ? ArrayUtils.indexOf( points, markers[0].getPoint() ) : 0;
    final int rightPos = markers.length > 1 ? ArrayUtils.indexOf( points, markers[markers.length - 1].getPoint() ) + 1 : points.length - 1;

    return leftPos < rightPos ? profil.getResult().subList( leftPos, rightPos ) : null;
  }

  /**
   * return true if all selected properties are equal
   */
  public static boolean comparePoints( final IComponent[] properties, final IRecord point1, final IRecord point2 )
  {
    for( final IComponent property : properties )
    {
      final Double x1 = getDoubleValueFor( property.getId(), point1 );
      final Double x2 = getDoubleValueFor( property.getId(), point2 );

      if( x1.isNaN() || x2.isNaN() )
      {
        final int index = point1.getOwner().indexOfComponent( property );
        final Object o1 = point1.getValue( index );
        final Object o2 = point2.getValue( index );
        if( o1 == null || o2 == null || o1.equals( o2 ) )
        {
          continue;
        }
      }

      if( Math.abs( x1 - x2 ) > property.getPrecision() )
        return false;
    }
    return true;
  }

  public static boolean compareValues( final Double x1, final Double x2, final double precision )
  {
    return Math.abs( x1 - x2 ) <= precision;
  }

  /**
   * mirror the profiles points (axis 0.0)
   */
  public static void flipProfile( final IProfil profile, final boolean fireNoEvent )
  {
    final IComponent[] components = profile.getPointProperties();
    final IProfileRecord[] records = profile.getPoints();
    final int len = records.length;
    int iBreite = -1;
    IProfileRecord lastRec = records[0];

    for( int i = 0; i < len / 2; i++ )
    {
      final IProfileRecord currentRec = records[i].cloneRecord();
      final int k = len - 1 - i;
      for( int j = 0; j < components.length; j++ )
      {
        if( iBreite < 0 && components[j].getId().equals( IWspmPointProperties.POINT_PROPERTY_BREITE ) )
        {
          iBreite = j;
        }
        if( iBreite == j )
        {
          final Double value = (Double) records[i].getValue( j ) * -1;
          records[i].setValue( j, (Double) records[k].getValue( j ) * -1, fireNoEvent );
          records[k].setValue( j, value, fireNoEvent );
        }
        else if( components[j].getId().equals( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX ) //
            || components[j].getId().equals( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY ) //
            || components[j].getId().equals( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP ) //
            || components[j].getId().equals( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS ) //
            || components[j].getId().equals( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST ) )
        {
          final Object value = lastRec.getValue( j );
          records[i].setValue( j, records[k - 1].getValue( j ), fireNoEvent );
          records[k].setValue( j, value, fireNoEvent );
        }
        else
        {
          final Object value = records[i].getValue( j );
          records[i].setValue( j, records[k].getValue( j ), fireNoEvent );
          records[k].setValue( j, value, fireNoEvent );
        }
      }
      lastRec = currentRec;
    }

    if( len / 2 * 2 < len )
    {
      final int mid = len / 2;
      final Double dBreite = (Double) records[mid].getValue( iBreite );
      records[mid].setValue( iBreite, dBreite * -1, fireNoEvent );
    }
  }

  public static IRecord splitSegment( final IProfil profile, final IProfileRecord startPoint, final IProfileRecord endPoint )
  {
    if( startPoint == null || endPoint == null )
      return null;
    final IProfileRecord point = profile.createProfilPoint();
    final IComponent[] properties = profile.getPointProperties();

    for( final IComponent property : properties )
    {
      final int index = profile.indexOfProperty( property );
      if( IWspmPointProperties.POINT_PROPERTY_BREITE.equals( property.getId() ) )
      {
        final Double b1 = startPoint.getBreite();
        final Double l = endPoint.getBreite() - b1;
        point.setValue( index, b1 + l / 2.0 );
      }
      else if( IWspmPointProperties.POINT_PROPERTY_HOEHE.equals( property.getId() ) )
      {
        final Double h1 = (Double) startPoint.getValue( index );
        final Double z = (Double) endPoint.getValue( index ) - h1;
        point.setValue( index, h1 + z / 2.0 );
      }
      else if( profile.isPointMarker( property.getId() ) )
      {
        point.setValue( index, startPoint.getValue( index ) );
      }
    }

    return point;
  }

  /**
   * findet einen Punkt in einem Profil 1.hole Punkt[index] und vergleiche Punkt.breite mit breite -> 2.suche Punkt bei
   * breite mit einer Toleranz von delta 3.kein Punkt gefunden -> (return null)
   */
  public static IRecord findPoint( final IProfil profil, final int index, final double breite, final double delta )
  {
    final IProfileRecord[] points = profil.getPoints();
    final IProfileRecord point = index >= points.length || index < 0 ? null : points[index];
    if( Objects.isNull( point ) )
      return findPoint( profil, breite, delta );

    if( point.getBreite() == breite )
      return point;

    return findPoint( profil, breite, delta );

  }

  public static IProfileRecord[] getSegment( final IProfil profile, final double breite )
  {
    final IProfileRecord[] points = profile.getPoints();
    final IProfileRecord[] segment = new IProfileRecord[] { null, null };
    for( int i = 0; i < points.length - 1; i++ )
    {
      final double segmentStartWidth = points[i].getBreite();
      final double segmentEndWidth = points[i + 1].getBreite();
      if( segmentStartWidth <= breite & segmentEndWidth >= breite )
      {
        segment[0] = points[i];
        segment[1] = points[i + 1];
      }
    }

    return segment;
  }

  public static Integer[] findNearestPointIndices( final IProfil profil, final double[] breite )
  {
    final Integer[] result = new Integer[breite.length];

    final IProfileRecord[] points = profil.getPoints();
    if( points.length == 0 )
      return result;

    final double[] bestDist = new double[result.length];
    for( int i = 0; i < bestDist.length; i++ )
    {
      bestDist[i] = Double.MAX_VALUE;
    }

    for( int i = 0; i < points.length; i++ )
    {
      final IProfileRecord currentPoint = points[i];
      final Double current = currentPoint.getBreite();
      if( Objects.isNull( current ) )
      {
        continue;
      }

      for( int k = 0; k < bestDist.length; k++ )
      {
        final double dist = Math.abs( current - breite[k] );
        if( dist < bestDist[k] )
        {
          bestDist[k] = dist;
          result[k] = i;
        }
      }
    }

    return result;
  }

  public static IProfileRecord findPoint( final IProfil profil, final double breite, final double delta )
  {
    final IProfileRecord pkt = ProfileVisitors.findNearestPoint( profil, breite );
    final double xpos = pkt.getBreite();
    return Math.abs( xpos - breite ) <= delta ? pkt : null;
  }

  public static IComponent getComponentForID( final IComponent[] components, final String propertyID )
  {
    if( components == null || components.length < 1 )
      return null;
    for( final IComponent component : components )
    {
      if( component.getId().equals( propertyID ) )
        return component;
    }
    return null;
  }

  public static Point2D getPoint2D( final IRecord p, final IComponent pointProperty )
  {
    final Double x = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, p );
    final Double y = getDoubleValueFor( pointProperty.getId(), p );
    if( x.isNaN() || y.isNaN() )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.model.wspm.core.profil.util.ProfilUtil.7", pointProperty.getName() ) ); //$NON-NLS-1$
    return new Point2D.Double( x, y );
  }

  public static Point2D[] getPoints2D( final IProfil profil, final String propertyID )
  {
    if( profil.hasPointProperty( propertyID ) == null )
      return new Point2D[] {};

    final IRecord[] points = profil.getPoints();
    final Point2D[] points2D = new Point2D[points.length];
    int i = 0;
    for( final IRecord p : points )
    {
      final Double x = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, p );
      final Double y = getDoubleValueFor( propertyID, p );
      if( y.isNaN() || x.isNaN() )
        return new Point2D[] {};
      points2D[i++] = new Point2D.Double( x, y );

    }
    return points2D;
  }

  @Deprecated
  // FIXME: what to use instead?!
// FIXME: warum überhaupt deprecated?
  public static Double getSectionMinValueFor( final IRecord[] section, final IComponent property )
  {
    if( section.length == 0 )
      return Double.NaN;

    final TupleResult owner = section[0].getOwner();
    final int index = owner.indexOfComponent( property );
    if( index < 0 )
      return Double.NaN;

    Number minValue = getDoubleValueFor( property, section[0] );
    if( Double.isNaN( minValue.doubleValue() ) )
      return minValue.doubleValue();
    for( final IRecord rec : section )
    {
      final Object objVal = rec.getValue( index );
      if( objVal != null && objVal instanceof Number )
      {
        minValue = Math.min( minValue.doubleValue(), ((Number) objVal).doubleValue() );
      }
    }
    return minValue.doubleValue();
  }

  @Deprecated
  public static Double getSectionMaxValueFor( final IRecord[] section, final IComponent property )
  {
    if( section.length == 0 )
      return Double.NaN;
    final TupleResult owner = section[0].getOwner();
    final int index = owner.indexOfComponent( property );
    if( index < 0 )
      return Double.NaN;
    Number maxValue = getDoubleValueFor( property, section[0] );
    if( Double.isNaN( maxValue.doubleValue() ) )
      return maxValue.doubleValue();
    for( final IRecord rec : section )
    {
      final Object value = rec.getValue( index );
      if( value instanceof Number )
      {
        maxValue = Math.max( maxValue.doubleValue(), ((Number) value).doubleValue() );
      }
    }
    return maxValue.doubleValue();
  }

  /**
   * calculates the area of a given profile.<br>
   * the area is calculated in dependence of the max heigth value. input: IProfil<br>
   * output: area <br>
   */
  public static double calcArea( final IProfil profil )
  {
    double area = 0;
    double width = 0;
    final double maxZ = calcMaxZ( profil );
    final IRecord[] points = profil.getPoints();
    for( int i = 0; i < points.length - 1; i++ )
    {
      final double z1 = maxZ - getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, points[i] );
      final double z2 = maxZ - getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, points[i + 1] );
      final double width1 = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, points[i] );
      final double width2 = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, points[i + 1] );
      width = width2 - width1;
      area = area + (z1 + z2) / 2 * width;
    }

    return area;
  }

  /**
   * gives the maximal z value of a coordinate array
   */
  private static double calcMaxZ( final IProfil profile )
  {
    final IRecord[] points = profile.getPoints();
    if( points.length < 1 )
      return -Double.MAX_VALUE;
    Double maxZ = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, points[0] );
    for( int i = 1; i < points.length; i++ )
    {
      maxZ = Math.max( maxZ, getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, points[i] ) );
    }
    return maxZ;
  }

  /**
   * This function returns the georeferenced points of a profile.
   * 
   * @param profile
   *          The input profile.
   */
  public static IProfileRecord[] getGeoreferencedPoints( final IProfil profile )
  {
    /* List for storing points of the profile, which have a geo reference. */
    final IProfileRecord[] points = profile.getPoints();
    final ArrayList<IProfileRecord> geoReferencedPoints = new ArrayList<IProfileRecord>( points.length );

    for( final IProfileRecord point : points )
    {
      final Coordinate coordinate = point.getCoordinate();
      if( Objects.isNotNull( coordinate ) )
        geoReferencedPoints.add( point );
    }

    return geoReferencedPoints.toArray( new IProfileRecord[] {} );
  }

  public static GM_Curve getLine( final IProfil profile, final String crs ) throws GM_Exception
  {
    final IRecord[] georeferencedPoints = getGeoreferencedPoints( profile );
    final GM_Position[] pos = new GM_Position[georeferencedPoints.length];

    for( int i = 0; i < georeferencedPoints.length; i++ )
    {
      final Double x = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT, georeferencedPoints[i] );
      final Double y = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOCHWERT, georeferencedPoints[i] );
      final Double z = getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, georeferencedPoints[i] );
      pos[i] = GeometryFactory.createGM_Position( x, y, z );
    }

    return GeometryFactory.createGM_Curve( pos, crs );
  }

  public static Double[] getDoubleValuesFor( final IProfil profil, final IComponent pointProperty, final boolean skipNullValues )
  {
    final List<Double> myValues = new ArrayList<Double>();

    final Object[] values = getValuesFor( profil, pointProperty );
    for( final Object object : values )
    {
      if( object instanceof Double )
        myValues.add( (Double) object );
      else if( object instanceof Number )
        myValues.add( ((Number) object).doubleValue() );
      else if( object instanceof String )
      {
        // TODO: dubious...
        myValues.add( Double.valueOf( object.toString() ) );
      }
      else if( object == null && !skipNullValues )
        myValues.add( null );
    }

    return myValues.toArray( new Double[] {} );
  }

  /**
   * Fills missing values of one property by interpolating between neighbors.
   */
  public static void interpolateProperty( final IProfil profil, final int valueCompIndex )
  {
    final int distanceCompIndex = profil.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );

    IRecord lastGood = null;
    final List<IRecord> toInterpolate = new ArrayList<IRecord>();

    for( final IRecord point : profil.getPoints() )
    {
      final Object value = point.getValue( valueCompIndex );

      if( value == null )
      {
        if( lastGood != null )
        {
          toInterpolate.add( point );
        }
      }
      else
      {
        if( !toInterpolate.isEmpty() )
        {
          final IRecord[] pointsToInterpolate = toInterpolate.toArray( new IRecord[toInterpolate.size()] );
          doInterpolate( distanceCompIndex, valueCompIndex, lastGood, point, pointsToInterpolate );
        }

        lastGood = point;
        toInterpolate.clear();
      }
    }
  }

  private static void doInterpolate( final int distanceCompIndex, final int valueCompIndex, final IRecord prevPoint, final IRecord nextPoint, final IRecord[] toInterpolate )
  {
    final Double prevDistance = prevPoint == null ? null : (Double) prevPoint.getValue( distanceCompIndex );
    final Double prevValue = prevPoint == null ? null : (Double) prevPoint.getValue( valueCompIndex );
    final Double nextDistance = nextPoint == null ? null : (Double) nextPoint.getValue( distanceCompIndex );
    final Double nextValue = nextPoint == null ? null : (Double) nextPoint.getValue( valueCompIndex );

    if( prevDistance == null || prevValue == null )
    {
      for( final IRecord point : toInterpolate )
      {
        point.setValue( valueCompIndex, nextValue );
      }
    }
    else if( nextDistance == null || nextValue == null )
    {
      for( final IRecord point : toInterpolate )
      {
        point.setValue( valueCompIndex, prevValue );
      }
    }
    else
    {
      for( final IRecord point : toInterpolate )
      {
        final Double distance = (Double) point.getValue( distanceCompIndex );
        if( distance != null )
        {
          try
          {
            final LinearEquation le = new LinearEquation( prevDistance, prevValue, nextDistance, nextValue );
            final double value = le.computeY( distance );
            point.setValue( valueCompIndex, value );
          }
          catch( final IndexOutOfBoundsException e )
          {
            e.printStackTrace();
          }
          catch( final SameXValuesException e )
          {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * Same as {@link #getValuesFor(IRecord[], String)}, but casts the result to the given type.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] getValuesFor( final IRecord[] points, final String component, final Class<T> type, final boolean skipNullValues )
  {
    final Object[] values = getValuesFor( points, component, skipNullValues );
    final T[] targetArray = (T[]) Array.newInstance( type, values.length );
    return Arrays.castArray( values, targetArray );
  }

  public static Double[] getValuesFor( final IProfil profil, final String component, final Class<Double> type )
  {
    return getValuesFor( profil.getPoints(), component, type, false );
  }

  /**
   * Gets all values of the given component and interpolates missing values by the help of the 'BREITE' component.<br>
   * Works only for components with numeric values.
   * 
   * @return All values of the profile of the given component. The length of the array is equal to the number of
   *         records.
   */
  public static Double[] getInterpolatedValues( final IProfil profil, final String component )
  {
    final Double[] widthValues = getValuesFor( profil, IWspmPointProperties.POINT_PROPERTY_BREITE, Double.class );
    final Double[] componentValues = getValuesFor( profil, component, Double.class );

    Assert.isTrue( widthValues.length == componentValues.length );

    int lastValid = -1;
    for( int i = 0; i < widthValues.length; i++ )
    {
      if( componentValues[i] != null )
      {
        if( lastValid != -1 )
        {
          try
          {
            final Double width1 = widthValues[lastValid];
            final Double value1 = componentValues[lastValid];
            final Double width2 = widthValues[i];
            final Double value2 = componentValues[i];

            if( width1 != null && width2 != null && value1 != null && value2 != null )
            {
              final LinearEquation linearEquation = new LinearEquation( width1, value1, width2, value2 );

              /* Loop over all invalid values */
              for( int j = lastValid + 1; j < i - 1; j++ )
              {
                componentValues[j] = linearEquation.computeY( widthValues[j] );
              }
            }
          }
          catch( final SameXValuesException e )
          {
            e.printStackTrace();
          }
        }

        lastValid = i;
      }
    }

    return componentValues;
  }

  /**
   * This function thins the profile and removes unnecessary points. It uses the Douglas Peucker algorithm.
   * 
   * @param profile
   *          The profile, which should be simplified.
   * @param allowedDistance
   *          The allowed distance [m].
   */
  public static void simplifyProfile( final IProfil profile, final double allowedDistance ) throws IllegalProfileOperationException
  {
    /* Get the profile changes. */
    final IProfileRecord[] pointsToSimplify = profile.getPoints();

    final IProfilChange[] removeChanges = DouglasPeuckerHelper.reduce( allowedDistance, pointsToSimplify, profile );
    for( final IProfilChange profilChange : removeChanges )
    {
      profilChange.doChange();
    }
  }

  /**
   * This function thins the profile and removes unnecessary points. It uses the Douglas Peucker algorithm.
   * 
   * @param profile
   *          The profile, which should be simplified.
   * @param allowedDistance
   *          The allowed distance [m].
   */
  public static void simplifyProfile( final IProfil profile, final double allowedDistance, final int startPoint, final int endPoint ) throws IllegalProfileOperationException
  {
    /* Get the profile changes. */
    final IProfileRecord[] pointsToSimplify = profile.getPoints( startPoint, endPoint );

    final IProfilChange[] removeChanges = DouglasPeuckerHelper.reduce( allowedDistance, pointsToSimplify, profile );
    for( final IProfilChange profilChange : removeChanges )
    {
      profilChange.doChange();
    }
  }

  /**
   * Either gets and existing component, or creates it if it doesn't exist yet.
   * 
   * @return The index of the component
   */
  public static int getOrCreateComponent( final IProfil profil, final String componentID )
  {
    final int index = profil.indexOfProperty( componentID );
    if( index != -1 )
      return index;

    profil.addPointProperty( ProfilUtil.getFeatureComponent( componentID ) );
    return profil.indexOfProperty( componentID );
  }
}