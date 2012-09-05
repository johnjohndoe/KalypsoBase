/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.j3d.geom.TriangulationUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_MultiPrimitive;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Primitive;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;
import org.kalypsodeegree_impl.model.geometry.GM_Envelope_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.infomatiq.jsi.Rectangle;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

/**
 * @author doemming
 */
public final class GeometryUtilities
{
  private GeometryUtilities( )
  {
    throw new UnsupportedOperationException( "Do not instantiate this helper class" );
  }

  /**
   * creates a new GM_Position that is on the straight line defined with the positions and has a special distance from
   * basePoint in the direction towards the directionPoint
   */
  public static GM_Position createGM_PositionAt( final GM_Position basePoint, final GM_Position directionPoint, final double distanceFromBasePoint )
  {
    final double[] p1 = basePoint.getAsArray();
    final double distance = basePoint.getDistance( directionPoint );
    if( distance == 0 )
      return (GM_Position) basePoint.clone();
    final double[] p2 = directionPoint.getAsArray();
    final double factor = distanceFromBasePoint / distance;
    final double[] newPos = new double[p1.length];
    // for( int i = 0; i < newPos.length; i++ )
    for( int i = 0; i < 2; i++ )
      newPos[i] = p1[i] + (p2[i] - p1[i]) * factor;
    return GeometryFactory.createGM_Position( newPos );
  }

  public static GM_Position getGM_PositionBetweenAtLevel( final GM_Position p1, final GM_Position p2, final double iso )
  {
    final double dz1 = iso - p1.getZ();
    final double dz2 = p2.getZ() - p1.getZ();
    final double dx = p2.getX() - p1.getX();
    final double c = dz1 / dz2;
    // check between
    if( c < -0.01d || c > 1.01d )
      return null;
    return GeometryFactory.createGM_Position( p1.getX() + c * dx, p1.getY() + c * (p2.getY() - p1.getY()), iso );
  }

  public static GM_Position createGM_PositionAtCenter( final GM_Position p1, final GM_Position p2 )
  {
    final double[] asArray1 = p1.getAsArray();
    final double[] asArray2 = p2.getAsArray();
    final int length = Math.min( asArray1.length, asArray2.length );
    final double[] newArray = new double[length];
    for( int i = 0; i < length; i++ )
      newArray[i] = (asArray1[i] + asArray2[i]) / 2d;

    return GeometryFactory.createGM_Position( newArray );
  }

  /**
   * assuming p1 and p2 have same coordinate system
   */
  public static GM_Point createGM_PositionAtCenter( final GM_Point p1, final GM_Point p2 )
  {
    final GM_Position newPos = createGM_PositionAtCenter( p1.getPosition(), p2.getPosition() );
    return GeometryFactory.createGM_Point( newPos, p1.getCoordinateSystem() );
  }

  public static double calcAngleToSurface( final GM_Surface< ? > surface, final GM_Point point )
  {
    final double r = surface.distance( point );
    double min = r;
    double resultAngle = 0;
    final double n = 8;
    for( double angle = 0; angle < 2d * Math.PI; angle += 2d * Math.PI / n )
    {
      final GM_Point p = createPointFrom( point, angle, r / 2 );
      final double distance = surface.distance( p );
      if( distance < min )
      {
        min = distance;
        resultAngle = angle;
      }
    }
    return resultAngle;
  }

  /**
   * guess point that is on the surface
   *
   * @param surface
   *          surface that should contain the result point
   * @param pointGuess
   * @param tries
   *          numer of maximal interations
   * @return point that is somewhere on the surface (e.g. can act as label point)
   */
  public static GM_Point guessPointOnSurface( final GM_Surface< ? > surface, GM_Point pointGuess, int tries )
  {
    if( surface == null )
      return null;
    if( pointGuess == null )
      pointGuess = surface.getCentroid();
    if( tries <= 0 )
      return pointGuess;
    tries--;
    if( surface.contains( pointGuess ) )
      return pointGuess;
    //
    // pointGuess1
    // |
    // |radius1
    // |
    // --p1--- at border
    // ----------
    // ------------
    // ------------
    // --result----
    // -----------
    // ----------
    // -----------
    // -surface---
    // --p2------- at border
    // |
    // |
    // |
    // |
    // |radius2
    // |
    // |
    // |
    // |
    // pointGuess2
    //
    // 1. find point at surface on one side
    final double angle1 = calcAngleToSurface( surface, pointGuess );
    final double r1 = surface.distance( pointGuess );
    final GM_Point p1 = createPointFrom( pointGuess, angle1, r1 );
    final GM_Point p2 = calcFarestPointOnSurfaceInDirection( surface, p1, angle1, Math.sqrt( Math.pow( surface.getEnvelope().getHeight(), 2 ) * Math.pow( surface.getEnvelope().getWidth(), 2 ) ), 8 );
    return guessPointOnSurface( surface, createGM_PositionAtCenter( p1, p2 ), tries );
  }

  private static GM_Point calcFarestPointOnSurfaceInDirection( final GM_Surface< ? > surface, final GM_Point pOnSurface, final double angle, final double max, int tries )
  {
    final GM_Point point = createPointFrom( pOnSurface, angle, max );
    if( surface.contains( point ) )
      return point;
    if( tries <= 0 )
      return point;// return the best try
    tries--;
    final double distance = surface.distance( point );
    return calcFarestPointOnSurfaceInDirection( surface, pOnSurface, angle, max - distance, tries );
  }

  // public static GM_Point guessPointOnSurface( final GM_Surface surface, //
  // // // GM_Point firstGuessPoint, int tries )
  // {
  // if( surface == null )
  // return null;
  // if( firstGuessPoint == null )
  // firstGuessPoint = surface.getCentroid();
  // if( tries <= 0 )
  // return firstGuessPoint;
  // tries--;
  // //
  // // guessPoint
  // // |
  // // |radius
  // // |
  // // --p1-- at border
  // // --|-----
  // // --p3----- middle of p1 and p2
  // // --|------
  // // --p2---- guesspoint mirror at p2
  // // --------
  // // ---------
  // // -surface-
  // // ---------
  // //
  // // 1. find direction to surface
  // double n = 8; // number of directions to test
  // final double r = surface.distance( firstGuessPoint );
  // double min = r;
  // double resultAngle = 0;
  // for( double angle = 0; angle < 2d * Math.PI; angle += Math.PI / n )
  // {
  // GM_Point p = createPointFrom( firstGuessPoint, angle, r / 2 );
  // double distance = surface.distance( p );
  // if( distance < min )
  // {
  // min = distance;
  // resultAngle = angle;
  // }
  // }
  // // calc point at border
  // final GM_Point p1 = createPointFrom( firstGuessPoint, resultAngle, r );
  // // mirror at p1
  // final GM_Point p2 = createPointFrom( firstGuessPoint, resultAngle, 2 * r );
  // // center of p1 and p2
  // final GM_Point p3 = createGM_PositionAtCenter( p1, p2 );
  //
  //
  // if(!surface.contains( p2 ) )
  // {
  // if( surface.contains( p3 ) )
  // return p3;
  // return p2;
  // }
  // return guessPointOnSurface( surface, p3, tries );
  // // if( surface.contains( p2 ) )
  // // {
  // // if( surface.contains( p3 ) )
  // // return p3;
  // // return p2;
  // // }
  // // return guessPointOnSurface( surface, p3, tries );
  // }

  private static GM_Point createPointFrom( final GM_Point centroid, final double angle, final double radius )
  {
    final double x = centroid.getX() + Math.cos( angle ) * radius;
    final double y = centroid.getY() + Math.sin( angle ) * radius;
    return GeometryFactory.createGM_Point( x, y, centroid.getCoordinateSystem() );
  }

  public static double calcArea( final GM_Object geom )
  {
    if( geom instanceof GM_Surface )
      return ((GM_Surface< ? >) geom).getArea();
    else if( geom instanceof GM_MultiSurface )
    {
      double area = 0;
      final GM_Surface< ? >[] allSurfaces = ((GM_MultiSurface) geom).getAllSurfaces();
      for( final GM_Surface< ? > element : allSurfaces )
        area += calcArea( element );
      return area;
    }
    else if( geom instanceof GM_MultiPrimitive )
    {
      double area = 0;
      final GM_Primitive[] allPrimitives = ((GM_MultiPrimitive) geom).getAllPrimitives();
      for( final GM_Primitive element : allPrimitives )
        area += calcArea( element );
      return area;
    }
    return 0d;
  }

  public static boolean isInside( final GM_Object a, final GM_Object b )
  {
    if( a instanceof GM_Surface && b instanceof GM_Surface )
      return a.contains( guessPointOnSurface( (GM_Surface< ? >) b, b.getCentroid(), 3 ) );
    // return a.contains(b);
    if( a instanceof GM_MultiSurface )
      return isInside( ((GM_MultiSurface) a).getAllSurfaces()[0], b );
    if( b instanceof GM_MultiSurface )
      return isInside( a, ((GM_MultiSurface) b).getAllSurfaces()[0] );
    return false;
  }

  public static double calcArea( final GM_Envelope env )
  {
    return env.getHeight() * env.getHeight();
  }

  public static GM_Position createGM_PositionAverage( final GM_Position[] positions )
  {
    double x = 0d, y = 0d;
    for( final GM_Position position : positions )
    {
      x += position.getX();
      y += position.getY();
    }
    return GeometryFactory.createGM_Position( x / positions.length, y / positions.length );
  }

  /**
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isPointGeometry( final IValuePropertyType ftp )
  {
    // remember to use the same classes as used by the marshalling type handlers !!
    return ftp.getValueClass().equals( getPointClass() );
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */
  public static boolean isPointGeometry( final Object o )
  {
    return o.getClass().equals( getPointClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isMultiPointGeometry( final IValuePropertyType ftp )
  {
    return ftp.getValueClass().equals( getMultiPointClass() );
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */
  public static boolean isMultiPointGeometry( final Object o )
  {
    return o.getClass().equals( getMultiPointClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isCurveGeometry( final IValuePropertyType ftp )
  {
    return ftp.getValueClass().equals( getCurveClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isLineStringGeometry( final IValuePropertyType ftp )
  {
    return ftp.getValueClass().equals( getLineStringClass() );
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */

  public static boolean isLineStringGeometry( final Object o )
  {
    return o.getClass().equals( getLineStringClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isMultiLineStringGeometry( final IValuePropertyType ftp )
  {
    return ftp.getValueClass().equals( getMultiLineStringClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isSurfaceGeometry( final IValuePropertyType ftp )
  {
    return getSurfaceClass().isAssignableFrom( ftp.getValueClass() );
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */
  public static boolean isMultiLineStringGeometry( final Object o )
  {
    return o.getClass().equals( getMultiLineStringClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isPolygonGeometry( final IValuePropertyType ftp )
  {
    return getPolygonClass().isAssignableFrom( ftp.getValueClass() );
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */
  public static boolean isPolygonGeometry( final Object o )
  {
    final Class< ? extends Object> class1 = o.getClass();
    return getPolygonClass().isAssignableFrom( class1 );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isMultiPolygonGeometry( final IValuePropertyType ftp )
  {
    return ftp.getValueClass().equals( getMultiPolygonClass() );
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */
  public static boolean isMultiPolygonGeometry( final Object o )
  {
    return o.getClass().equals( getMultiPolygonClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isUndefinedGeometry( final IValuePropertyType ftp )
  {
    return ftp.getValueClass().equals( getUndefinedGeometryClass() );
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */
  public static boolean isUndefinedGeometry( final Object o )
  {
    return o.getClass().equals( getUndefinedGeometryClass() );
  }

  /**
   * @param ftp
   * @return <code>true</code> if feature property type equals this type of geometry
   */
  public static boolean isAnyMultiGeometry( final IPropertyType ftp )
  {
    ftp.getClass(); // no yellow things
    return false; // not supported TODO support it
  }

  public static boolean isEnvelopeGeometry( final IValuePropertyType ftp )
  {
    return getEnvelopeClass().equals( ftp.getValueClass() );
  }

  /**
   * Classifies the property as a geometry.
   *
   * @return <code>null</code>, if the property is not a geometry property.
   */
  public static GeometryType classifyGeometry( final IPropertyType pt )
  {
    if( !isGeometry( pt ) )
      return null;

    final IValuePropertyType vpt = (IValuePropertyType) pt;
    if( isPointGeometry( vpt ) )
      return GeometryType.POINT;

    if( isCurveGeometry( vpt ) )
      return GeometryType.CURVE;

    if( isSurfaceGeometry( vpt ) )
      return GeometryType.SURFACE;

    return GeometryType.UNKNOWN;
  }

  /**
   * @param o
   * @return <code>true</code> if object type equals this type of geometry
   */
  public static boolean isEnvelopeGeometry( final Object o )
  {
    return getEnvelopeClass().equals( o.getClass() );
  }

  public static Class< ? extends Object> getEnvelopeClass( )
  {
    return GM_Envelope.class;
  }

  public static boolean isGeometry( final IPropertyType pt )
  {
    if( !(pt instanceof IValuePropertyType) )
      return false;
    final IValuePropertyType gpt = (IValuePropertyType) pt;
    return gpt.isGeometry();
  }

  public static Class< ? extends GM_Object> getPointClass( )
  {
    return GM_Point.class;
  }

  public static Class< ? extends GM_Object> getMultiPointClass( )
  {
    return GM_MultiPoint.class;
  }

  public static Class< ? extends GM_Object> getLineStringClass( )
  {
    return GM_Curve.class;
  }

  public static Class< ? extends GM_Object> getCurveClass( )
  {
    return GM_Curve.class;
  }

  public static Class< ? extends GM_Object> getMultiLineStringClass( )
  {
    return GM_MultiCurve.class;
  }

  public static Class< ? extends GM_Object> getSurfaceClass( )
  {
    return GM_Surface.class;
  }

  public static Class< ? extends GM_Object> getPolygonClass( )
  {
    return GM_Surface.class;
  }

  public static Class< ? extends GM_Object> getMultiPolygonClass( )
  {
    return GM_MultiSurface.class;
  }

  public static Class< ? extends GM_Object> getUndefinedGeometryClass( )
  {
    return GM_Object.class;
  }

  public static boolean isGeometry( final Object o )
  {
    final Class< ? extends Object> class1 = o.getClass();
    if( getUndefinedGeometryClass().isAssignableFrom( class1 ) )
      return true;
    else if( getPointClass().isAssignableFrom( class1 ) )
      return true;
    else if( getMultiPointClass().isAssignableFrom( class1 ) )
      return true;
    else if( getLineStringClass().isAssignableFrom( class1 ) )
      return true;
    else if( getMultiLineStringClass().isAssignableFrom( class1 ) )
      return true;
    else if( getPolygonClass().isAssignableFrom( class1 ) )
      return true;
    else if( getMultiPolygonClass().isAssignableFrom( class1 ) )
      return true;
    return false;
  }

  /**
   * This method ensure to return a multi polygon (GM_MultiSurface ). the geomToCheck is a polygon ( GM_Surface) the
   * polygon is wrapped to a multi polygon.
   *
   * @param geomToCheck
   *          geometry object to check
   * @return multi polygon, if geomToCheck is null, null is returned, if the geomToCheck is a multi polygon it returns
   *         itself
   * @exception a
   *              GM_Exception is thrown when a the geomToCheck can not be wrapped in a multi polygon.
   */
  public static GM_MultiSurface ensureIsMultiPolygon( final GM_Object geomToCheck ) throws GM_Exception
  {
    if( geomToCheck == null )
      return null;
    final Class< ? extends GM_Object> class1 = geomToCheck.getClass();
    if( getMultiPolygonClass().isAssignableFrom( class1 ) )
      return (GM_MultiSurface) geomToCheck;
    else if( getPolygonClass().isAssignableFrom( class1 ) )
      return GeometryFactory.createGM_MultiSurface( new GM_Surface[] { (GM_Surface< ? >) geomToCheck }, ((GM_Surface< ? >) geomToCheck).getCoordinateSystem() );
    else
      throw new GM_Exception( "This geometry can not be a MultiPolygon..." );
  }

  /**
   * @param positions
   *          array of ordered {@link GM_Position}, last must equal first one
   * @return signed area, area >= 0 means points are counter clockwise defined (mathematic positive)
   */
  public static double calcSignedAreaOfRing( final GM_Position[] positions )
  {
    if( positions.length < 4 ) // 3 points and 4. is repetition of first point
      throw new UnsupportedOperationException( "can not calculate area of < 3 points" );
    final GM_Position a = positions[0]; // base
    double area = 0;
    for( int i = 1; i < positions.length - 2; i++ )
    {
      final GM_Position b = positions[i];
      final GM_Position c = positions[i + 1];
      area += (b.getY() - a.getY()) * (a.getX() - c.getX()) // bounding rectangle
          - ((a.getX() - b.getX()) * (b.getY() - a.getY())//
              + (b.getX() - c.getX()) * (b.getY() - c.getY())//
          + (a.getX() - c.getX()) * (c.getY() - a.getY())//
          ) / 2d;
    }
    return area;
  }

  /**
   * Finds the first geometry property of the given feature type.
   *
   * @param aPreferedGeometryClass
   *          If non null, the first property of this type is returned.
   */
  public static IValuePropertyType findGeometryProperty( final IFeatureType featureType, final Class< ? > aPreferedGeometryClass )
  {
    final QName defaultGeometryPropertyQName = new QName( NS.GML3, "location" );
    final IValuePropertyType[] allGeomteryProperties = featureType.getAllGeometryProperties();

    IValuePropertyType geometryProperty = null;
    for( final IValuePropertyType property : allGeomteryProperties )
    {
      if( aPreferedGeometryClass == null || property.getValueClass().isAssignableFrom( aPreferedGeometryClass ) )
      {
        geometryProperty = property;
        if( !geometryProperty.getQName().equals( defaultGeometryPropertyQName ) )
          break;
      }
    }
    return geometryProperty;
  }

  /**
   * clones a GM_Linestring as GM_Curve and sets its z-value to a given value.
   *
   * @param newLine
   *          the input linestring
   * @param value
   *          the new z-value
   */
  public static GM_Curve setValueZ( final GM_LineString newLine, final double value ) throws GM_Exception
  {
    final GM_Position[] positions = newLine.getPositions();
    final String crs = newLine.getCoordinateSystem();
    final GM_Position[] newPositions = new GM_Position[positions.length];
    for( int i = 0; i < positions.length; i++ )
    {
      final GM_Position position = positions[i];
      newPositions[i] = GeometryFactory.createGM_Position( position.getX(), position.getY(), value );
    }
    return GeometryFactory.createGM_Curve( newPositions, crs );
  }

  /**
   * creates a new curve by simplifying a given curve by using Douglas-Peucker Algorithm.
   *
   * @param curve
   *          input curve to be simplified
   * @param epsThinning
   *          max. distance value for Douglas-Peucker-Algorithm
   */
  public static GM_Curve getThinnedCurve( final GM_Curve curve, final Double epsThinning ) throws GM_Exception
  {
    final LineString line = (LineString) JTSAdapter.export( curve );
    final LineString simplifiedLine = (LineString) DouglasPeuckerSimplifier.simplify( line, epsThinning );
    final GM_Curve thinnedCurve = (GM_Curve) JTSAdapter.wrap( simplifiedLine );

    return thinnedCurve;
  }

  public static GM_Envelope grabEnvelopeFromDistance( final GM_Point point, final double grabDistance )
  {
    final GM_Position position = point.getPosition();

    final GM_Envelope posEnvelope = GeometryFactory.createGM_Envelope( position, position, point.getCoordinateSystem() );

    final double grabDistanceHalf = grabDistance / 2;

    return posEnvelope.getBuffer( grabDistanceHalf );
  }

  public static Feature findNearestFeature( final GM_Point point, final double grabDistance, final FeatureList modelList, final QName geoQName )
  {
    final GM_Envelope reqEnvelope = GeometryUtilities.grabEnvelopeFromDistance( point, grabDistance );
    final List< ? > foundElements = modelList.query( reqEnvelope, null );

    double min = Double.MAX_VALUE;
    Feature nearest = null;

    for( final Object feature : foundElements )
    {
      final GM_Object geom = (GM_Object) ((Feature) feature).getProperty( geoQName );

      if( geom != null )
      {
        final double curDist = point.distance( geom );
        if( min > curDist && curDist <= grabDistance )
        {
          nearest = (Feature) feature;
          min = curDist;
        }
      }
    }
    return nearest;
  }

  /**
   * Same as {@link #findNearestFeature(GM_Point, double, FeatureList, QName)}, but only regards features of certain
   * qnames.
   *
   * @param allowedQNames
   *          Only features that substitute one of these qnames are considered.
   */
  public static Feature findNearestFeature( final GM_Point point, final double grabDistance, final FeatureList modelList, final GMLXPath[] geometryPathes, final QName[] allowedQNames )
  {
    if( geometryPathes == null )
      return null;

    final GM_Envelope reqEnvelope = GeometryUtilities.grabEnvelopeFromDistance( point, grabDistance );
    final List< ? > foundElements = modelList.query( reqEnvelope, null );

    double min = Double.MAX_VALUE;
    Feature nearest = null;

    final Feature parentFeature = modelList.getOwner();
    final GMLWorkspace workspace = parentFeature == null ? null : parentFeature.getWorkspace();
    for( final Object object : foundElements )
    {
      final Feature feature = FeatureHelper.getFeature( workspace, object );
      final IFeatureType featureType = feature.getFeatureType();
      if( GMLSchemaUtilities.substitutes( featureType, allowedQNames ) )
      {
        for( final GMLXPath geometryPath : geometryPathes )
        {
          try
          {
            final Object geomOrList = GMLXPathUtilities.query( geometryPath, feature );
            final GM_Object[] geometries = findGeometries( geomOrList, GM_Object.class );
            for( final GM_Object geometry : geometries )
            {
              final double curDist = point.distance( geometry );
              if( min > curDist && curDist <= grabDistance )
              {
                nearest = feature;
                min = curDist;
              }
            }
          }
          catch( final GMLXPathException e )
          {
            e.printStackTrace();
          }
        }
      }
    }
    return nearest;
  }

  /**
   * Returns either the given qnames or all geometry qname's of the given feature.
   */
  public static QName[] getGeometryQNames( final Feature feature, final QName[] geomQNames )
  {
    if( feature == null )
      return geomQNames;

    if( geomQNames == null )
    {
      final IValuePropertyType[] properties = feature.getFeatureType().getAllGeometryProperties();
      return toQNames( properties );
    }

    return geomQNames;
  }

  private static QName[] toQNames( final IValuePropertyType[] properties )
  {
    final QName[] result = new QName[properties.length];
    for( int i = 0; i < properties.length; i++ )
      result[i] = properties[i].getQName();
    return result;
  }

/**
   * Same as
   * {@link #findNearestFeature(GM_Point, double, FeatureList, QName, QName[]), but with an array of Featurelists.
   *
   * @param allowedQNames
   *            Only features that substitute one of these qnames are considered.
   */
  public static Feature findNearestFeature( final GM_Point point, final double grabDistance, final FeatureList[] modelLists, final QName geoQName, final QName[] allowedQNames )
  {
    Feature nearest = null;

    for( final FeatureList modelList : modelLists )
    {
      if( modelList == null )
        continue;

      final GM_Envelope reqEnvelope = GeometryUtilities.grabEnvelopeFromDistance( point, grabDistance );
      final List<Object> foundElements = modelList.query( reqEnvelope, null );

      double min = Double.MAX_VALUE;

      for( final Object element : foundElements )
      {
        final Feature feature = (Feature) element;
        if( GMLSchemaUtilities.substitutes( feature.getFeatureType(), allowedQNames ) )
        {
          final GM_Object geom = (GM_Object) feature.getProperty( geoQName );

          if( geom != null )
          {
            final double curDist = point.distance( geom );
            if( min > curDist && curDist <= grabDistance )
            {
              nearest = feature;
              min = curDist;
            }
          }
        }
      }
    }
    return nearest;
  }

  /**
   * Calculates the direction (in degrees) from one position to another.
   *
   * @return The angle in degree or {@link Double#NaN} if the points coincide.
   */
  public static double directionFromPositions( final GM_Position from, final GM_Position to )
  {
    final double vx = to.getX() - from.getX();
    final double vy = to.getY() - from.getY();

    return directionFromVector( vx, vy );
  }

  /**
   * Calculates the 'direction' of a vector in degrees. The degree value represents the angle between the vector and the
   * x-Axis in coordinate space.
   * <p>
   * Orientation is anti.clockwise (i.e. positive).
   * </p>
   *
   * @return The angle in degree or {@link Double#NaN} if the given vector has length 0.
   */
  public static double directionFromVector( final double vx, final double vy )
  {
    final double length = Math.sqrt( vx * vx + vy * vy );
    if( length == 0.0 ) // double comparison problems?
      return Double.NaN;

    final double alpha = Math.acos( vx / length );

    if( vy < 0 )
      return Math.toDegrees( 2 * Math.PI - alpha );

    return Math.toDegrees( alpha );
  }

  /**
   * Scales an envelope by the given factor (1 means no scaling) while maintaining the position of its center-point.
   */
  public static GM_Envelope scaleEnvelope( final GM_Envelope zoomBox, final double factor )
  {
    final GM_Position zoomMax = zoomBox.getMax();
    final GM_Position zoomMin = zoomBox.getMin();

    final double newMaxX = zoomMin.getX() + (zoomMax.getX() - zoomMin.getX()) * factor;
    final double newMinX = zoomMax.getX() - (zoomMax.getX() - zoomMin.getX()) * factor;

    final double newMaxY = zoomMin.getY() + (zoomMax.getY() - zoomMin.getY()) * factor;
    final double newMinY = zoomMax.getY() - (zoomMax.getY() - zoomMin.getY()) * factor;

    final GM_Position newMin = GeometryFactory.createGM_Position( newMinX, newMinY );
    final GM_Position newMax = GeometryFactory.createGM_Position( newMaxX, newMaxY );

    return GeometryFactory.createGM_Envelope( newMin, newMax, zoomBox.getCoordinateSystem() );
  }

  /**
   * checks, if a position lies inside or outside of an polygon defined by a position array
   *
   * @param pos
   *          position array of the polygon object
   * @param position
   *          position to be checked
   * @return 0 - if position lies outside of the polygon<BR>
   *         1 - if position lies inside of the polygon<BR>
   *         2 - if position lies on polygon's border.
   */
  public static int pointInsideOrOutside( final GM_Position[] pos, final GM_Position position )
  {
    int hits = 0;

    for( int i = 0; i < pos.length - 1; i++ )
    {
      /* check, if position lies on ring's border */
      final double sC = pos[i].getDistance( pos[i + 1] );
      final double sA = pos[i].getDistance( position );
      final double sB = pos[i + 1].getDistance( position );

      if( Math.abs( sC - sA - sB ) < 0.001 )
        return 2;

      /* calculate determinant */
      final double a00 = 2181.2838;
      final double a10 = 0.31415926; // = PI/10 (??)
      final double a01 = pos[i].getX() - pos[i + 1].getX();
      final double a11 = pos[i].getY() - pos[i + 1].getY();
      final double b0 = pos[i].getX() - position.getX();
      final double b1 = pos[i].getY() - position.getY();

      final double det = a00 * a11 - a10 * a01;
      if( det == 0.0 )
      {
        System.out.println( "Indefinite problem in pointInsideOrOutside" );
      }
      final double x0 = (a11 * b0 - a01 * b1) / det;
      final double x1 = (-a10 * b0 + a00 * b1) / det;

      if( x0 > 0. && x1 >= 0. && x1 <= 1. )
        hits++;
    }
    final int check = hits % 2;

    if( check == 1 )
      return 1;

    return 0;
  }

  /**
   * Convert the given bounding box into a {@link GM_Curve}
   */
  public static GM_Curve toGM_Curve( final GM_Envelope bBox, final String crs )
  {
    try
    {
      final GM_Position min = bBox.getMin();
      final GM_Position max = bBox.getMax();

      final double minx = min.getX();
      final double miny = min.getY();

      final double maxx = max.getX();
      final double maxy = max.getY();

      final double[] coords = new double[] { minx, miny, maxx, miny, maxx, maxy, minx, maxy, minx, miny, };
      final GM_Curve curve = GeometryFactory.createGM_Curve( coords, 2, crs );
      return curve;
    }
    catch( final Throwable e )
    {
      throw new RuntimeException( "error while creating a curve", e ); //$NON-NLS-1$
    }
  }

  /**
   * Tests whether a ring (defined by its positions) is self-intersecting.
   */
  public static boolean isSelfIntersecting( final GM_Position[] ring )
  {
    final LineString ls = JTSAdapter.exportAsLineString( ring );
    return !ls.isSimple();
  }

  protected static GM_Position[] getPolygonPositions( final GM_Curve[] curves, final boolean selfIntersected ) throws GM_Exception
  {
    /* test for self-intersection */
    final List<GM_Position> posList = new ArrayList<GM_Position>();

    /* - add first curve's positions to positions list */
    final GM_Position[] positions1 = curves[1].getAsLineString().getPositions();
    for( final GM_Position element : positions1 )
    {
      posList.add( element );
    }

    /* - add second curve's positions to positions list */
    final GM_Position[] positions2 = curves[0].getAsLineString().getPositions();

    if( !selfIntersected )
    {
      // not twisted: curves are oriented in the same direction, so we add the second curve's positions in the
      // opposite direction in order to get a non-self-intersected polygon.
      for( int i = 0; i < positions2.length; i++ )
      {
        posList.add( positions2[positions2.length - 1 - i] );
      }
    }
    else
    {
      // twisted: curves are oriented in different directions, so we add the second curve's positions
      // from start to end in order to get a non-self-intersected polygon.
      for( final GM_Position element : positions2 )
      {
        posList.add( element );
      }
    }

    /* close polygon position list */
    posList.add( positions1[0] );

    return posList.toArray( new GM_Position[posList.size()] );
  }

  /**
   * converts two given curves into a position array of a ccw oriented polygon.<br>
   * The ring is simply produced by adding all positions of the first curve and the positions of the second curve in
   * inverse order.<br>
   * Produces a closed ring.
   *
   * @param curves
   *          the curves as {@link GM_Curve}
   */
  public static GM_Position[] getPolygonfromCurves( final GM_Curve firstCurve, final GM_Curve secondCurve ) throws GM_Exception
  {
    /* get the positions of the curves */

    // as a first guess, we assume that the curves build a non-intersecting polygon
    final GM_Position[] firstPoses = firstCurve.getAsLineString().getPositions();
    final GM_Position[] secondPoses = secondCurve.getAsLineString().getPositions();
    final GM_Position[] polygonPositions = new GM_Position[firstPoses.length + secondPoses.length + 1];

    for( int i = 0; i < firstPoses.length; i++ )
      polygonPositions[i] = firstPoses[i];

    for( int i = 0; i < secondPoses.length; i++ )
      polygonPositions[i + firstPoses.length] = secondPoses[secondPoses.length - i - 1];

    polygonPositions[polygonPositions.length - 1] = polygonPositions[0];

    return orientateRing( polygonPositions );
  }

  /**
   * converts two given curves into a position array of a non-self-intersecting, ccw oriented, closed polygon
   *
   * @param curves
   *          the curves as {@link GM_Curve}
   */
  public static GM_Position[] getPolygonfromCurves( final GM_Curve[] curves ) throws GM_Exception
  {
    /* get the positions of the curves */

    // as a first guess, we assume that the curves build a non-intersecting polygon
    GM_Position[] polygonPositions = getPolygonPositions( curves, false );

    // then we check this assumption
    if( isSelfIntersecting( polygonPositions ) )
      polygonPositions = getPolygonPositions( curves, true );

    return orientateRing( polygonPositions );
  }

  /**
   * Orientates a ring counter clock wise.
   *
   * @return The inverted list of position, or the original list, if the ring was already oriented in the right way.
   */
  public static GM_Position[] orientateRing( final GM_Position[] polygonPositions )
  {
    // check orientation
    if( calcSignedAreaOfRing( polygonPositions ) < 0 )
      ArrayUtils.reverse( polygonPositions );

    return polygonPositions;
  }

  /**
   * Triangulates a closed ring (must be oriented counter-clock-wise). <br>
   * <b>It uses floats, so there can occur rounding problems!</b><br>
   * To avoid this, we substract all values with its minimum value. And add it later.
   *
   * @return An array of triangles: GM_Position[numberOfTriangles][3]
   */
  public static GM_Position[][] triangulateRing( final GM_Position[] ring )
  {
    final float[] posArray = new float[ring.length * 3];

    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double minZ = Double.MAX_VALUE;

    // find minimum values
    for( final GM_Position element : ring )
    {
      minX = Math.min( minX, element.getX() );
      minY = Math.min( minY, element.getY() );
      minZ = Math.min( minZ, element.getZ() );
    }

    // if we have no z-value we fake one.
    if( Double.isNaN( minZ ) )
    {
      for( int i = 0; i < ring.length; i++ )
      {
        posArray[i * 3] = (float) (ring[i].getX() - minX);
        posArray[i * 3 + 1] = (float) (ring[i].getY() - minY);
        posArray[i * 3 + 2] = new Float( 0.0 );
      }
    }
    else
    {
      for( int i = 0; i < ring.length; i++ )
      {
        posArray[i * 3] = (float) (ring[i].getX() - minX);
        posArray[i * 3 + 1] = (float) (ring[i].getY() - minY);
        posArray[i * 3 + 2] = (float) (ring[i].getZ() - minZ);
      }
    }

    final float[] normal = { 0, 0, 1 };
    final int[] output = new int[posArray.length];
    final int numVertices = posArray.length / 3;
    final TriangulationUtils triangulator = new TriangulationUtils();
    final int num = triangulator.triangulateConcavePolygon( posArray, 0, numVertices, output, normal );

    if( num < 0 )
      return new GM_Position[0][0];

    final GM_Position[][] triangles = new GM_Position[num][3];

    if( Double.isNaN( minZ ) )
    {
      for( int i = 0; i < num; i++ )
      {
        triangles[i] = new GM_Position[3];

        final double x1 = posArray[output[i * 3]] + minX;
        final double y1 = posArray[output[i * 3] + 1] + minY;
        final double z1 = Double.NaN;
        final double x2 = posArray[output[i * 3 + 1]] + minX;
        final double y2 = posArray[output[i * 3 + 1] + 1] + minY;
        final double z2 = Double.NaN;
        final double x3 = posArray[output[i * 3 + 2]] + minX;
        final double y3 = posArray[output[i * 3 + 2] + 1] + minY;
        final double z3 = Double.NaN;
        triangles[i][0] = GeometryFactory.createGM_Position( x1, y1, z1 );
        triangles[i][1] = GeometryFactory.createGM_Position( x2, y2, z2 );
        triangles[i][2] = GeometryFactory.createGM_Position( x3, y3, z3 );
      }
    }
    else
    {
      for( int i = 0; i < num; i++ )
      {
        triangles[i] = new GM_Position[3];

        final double x1 = posArray[output[i * 3]] + minX;
        final double y1 = posArray[output[i * 3] + 1] + minY;
        final double z1 = posArray[output[i * 3] + 2] + minZ;
        final double x2 = posArray[output[i * 3 + 1]] + minX;
        final double y2 = posArray[output[i * 3 + 1] + 1] + minY;
        final double z2 = posArray[output[i * 3 + 1] + 2] + minZ;
        final double x3 = posArray[output[i * 3 + 2]] + minX;
        final double y3 = posArray[output[i * 3 + 2] + 1] + minY;
        final double z3 = posArray[output[i * 3 + 2] + 2] + minZ;
        triangles[i][0] = GeometryFactory.createGM_Position( x1, y1, z1 );
        triangles[i][1] = GeometryFactory.createGM_Position( x2, y2, z2 );
        triangles[i][2] = GeometryFactory.createGM_Position( x3, y3, z3 );
      }
    }
    return triangles;
  }

  public static GM_Envelope envelopeFromRing( final GM_Position[] poses, final String crs )
  {
    double minX = poses[0].getX();
    double maxX = poses[0].getX();
    double minY = poses[0].getY();
    double maxY = poses[0].getY();

    for( int i = 1; i < poses.length; i++ )
    {
      minX = Math.min( minX, poses[i].getX() );
      minY = Math.min( minY, poses[i].getY() );
      maxX = Math.max( maxX, poses[i].getX() );
      maxY = Math.max( maxY, poses[i].getY() );
    }

    return new GM_Envelope_Impl( minX, minY, maxX, maxY, crs );
  }

  /**
   * calculates the centroid of a ring of points
   * <p>
   * taken from gems iv (modified)
   * <p>
   * </p>
   * this method is only valid for the two-dimensional case.
   */
  public static GM_Position centroidFromRing( final GM_Position[] ring )
  {
    double ai;
    double x;
    double y;
    double atmp = 0;
    double xtmp = 0;
    double ytmp = 0;

    // move points to the origin of the coordinate space
    // (to solve precision issues)
    final double transX = ring[0].getX();
    final double transY = ring[0].getY();

    int i;
    int j;
    for( i = ring.length - 1, j = 0; j < ring.length; i = j, j++ )
    {
      final double x1 = ring[i].getX() - transX;
      final double y1 = ring[i].getY() - transY;
      final double x2 = ring[j].getX() - transX;
      final double y2 = ring[j].getY() - transY;
      ai = x1 * y2 - x2 * y1;
      atmp += ai;
      xtmp += (x2 + x1) * ai;
      ytmp += (y2 + y1) * ai;
    }

    if( atmp != 0 )
    {
      x = xtmp / (3 * atmp) + transX;
      y = ytmp / (3 * atmp) + transY;
    }
    else
    {
      x = ring[0].getX();
      y = ring[0].getY();
    }

    return GeometryFactory.createGM_Position( x, y );
  }

  public static GM_Point centroidFromRing( final GM_Position[] poses, final String crs )
  {
    return GeometryFactory.createGM_Point( centroidFromRing( poses ), crs );
  }

  /**
   * creates {@link GM_Triangle} with from given positions with Z value from according map field on error returns null
   */
  public static GM_Triangle createTriangleForBilinearInterpolation( final Map<GM_Point, Double> mapPositionsValues )
  {
    if( mapPositionsValues.size() > 3 )
    {
      return null;
    }
    try
    {
      final List<GM_Position> lListPositionWithValues = new ArrayList<GM_Position>();
      final Set<GM_Point> lSetKeys = mapPositionsValues.keySet();
      GM_Point gmPoint = null;
      for( final Iterator<GM_Point> iterator = lSetKeys.iterator(); iterator.hasNext(); )
      {
        gmPoint = iterator.next();
        lListPositionWithValues.add( GeometryFactory.createGM_Position( gmPoint.getX(), gmPoint.getY(), mapPositionsValues.get( gmPoint ) ) );
      }
      return GeometryFactory.createGM_Triangle( lListPositionWithValues.get( 0 ), lListPositionWithValues.get( 1 ), lListPositionWithValues.get( 2 ), gmPoint.getCoordinateSystem() );
    }
    catch( final Exception e )
    {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  /**
   * Adapts a given object to one or more GM_Objects of a given type.
   */
  public static <T extends GM_Object> T[] findGeometries( final Object geomOrList, final Class<T> type )
  {
    final T[] emptyArray = (T[]) Array.newInstance( type, 0 );

    if( geomOrList == null )
      return emptyArray;

    final Class< ? extends GM_Object[]> arrayType = emptyArray.getClass();

    if( geomOrList instanceof GM_Object )
    {
      final T[] adapter = (T[]) ((GM_Object) geomOrList).getAdapter( arrayType );
      if( adapter == null )
        return emptyArray;
      return adapter;
    }

    if( geomOrList instanceof List )
      return findGeometries( (List< ? >) geomOrList, arrayType );

    return emptyArray;
  }

  @SuppressWarnings("unchecked")
  private static <T extends GM_Object> T[] findGeometries( final List< ? > geomList, final Class< ? extends GM_Object[]> arrayType )
  {
    final List<T> result = new ArrayList<T>();
    for( final Object geom : geomList )
    {
      if( geom instanceof GM_Object )
      {
        final T[] geometries = (T[]) ((GM_Object) geom).getAdapter( arrayType );
        result.addAll( Arrays.asList( geometries ) );
      }
    }

    final T[] store = (T[]) Array.newInstance( arrayType.getComponentType(), result.size() );

    return result.toArray( store );
  }

  @SuppressWarnings("unchecked")
  public static <G extends GM_Object> G[] transform( final G[] input, final IGeoTransformer transformer ) throws Exception
  {
    final G[] result = (G[]) Array.newInstance( input.getClass().getComponentType(), input.length );

    for( int i = 0; i < result.length; i++ )
    {
      if( input[i] != null )
        result[i] = (G) transformer.transform( input[i] );
    }

    return result;
  }

  public static final Rectangle toRectangle( final GM_Envelope envelope )
  {
    if( envelope == null )
      return null;

    final float x1 = (float) envelope.getMinX();
    final float y1 = (float) envelope.getMinY();
    final float x2 = (float) envelope.getMaxX();
    final float y2 = (float) envelope.getMaxY();

    return new Rectangle( x1, y1, x2, y2 );
  }

  public static GM_Envelope toEnvelope( final Rectangle bounds, final String crs )
  {
    if( bounds == null )
      return null;

    return new GM_Envelope_Impl( bounds.minX, bounds.minY, bounds.maxX, bounds.maxY, crs );
  }

  /**
   * Create the union of two or more envelopes. Handles <code>null</code> envelopes.
   *
   * @return The smallest envelope that covers all the given envelopes. <code>null</code>, if all given envelopes are
   *         <code>null</code>.
   * @see GM_Envelope#getMerged(GM_Envelope)
   */
  public static GM_Envelope mergeEnvelopes( final GM_Envelope... envelopes )
  {
    GM_Envelope union = null;

    for( final GM_Envelope envelope : envelopes )
    {
      if( union == null )
        union = envelope;
      else
        union = union.getMerged( envelope );
    }

    return union;
  }
}