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
package org.kalypso.shape.deegree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.ISHPMultiPoint;
import org.kalypso.shape.geometry.ISHPParts;
import org.kalypso.shape.geometry.ISHPPoint;
import org.kalypso.shape.geometry.ISHPPolyLine;
import org.kalypso.shape.geometry.ISHPPolygon;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_CurveSegment;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SimplePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * the class SHP2WKS transforms a polygon structure read from a shape-file <BR>
 * into a WKSLinearPolygon specified by the sf-specifications <BR>
 * <P>
 * <B>Last changes <B>: <BR>
 * 14.12.1999 ap: import clauses added <BR>
 * 08.02.2000 ap: method transformPoint(..) declared and implemented <BR>
 * 21.03.2000 ap: method: transformMultiPoint(..) declared and implemented <BR>
 * 21.03.2000 ap: method: transformPolyLine(..) declared and implemented <BR>
 * <!------------------------------------------------------------------------>
 * 
 * @version 21.03.2000
 * @author Andreas Poth
 */
public final class SHP2GM_Object
{
  private final static com.vividsolutions.jts.geom.GeometryFactory GF = new com.vividsolutions.jts.geom.GeometryFactory();

  private SHP2GM_Object( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /**
   * method: GM_Point transformPointz(CS_CoordinateSystem srs, <BR>
   * SHPPointz shppointz)) <BR>
   * transforms a SHPPointz to a WKSGeometry <BR>
   * gets a pointz that should be transformed <BR>
   */
  public static GM_Point transformPoint( final String crs, final ISHPPoint shppointz )
  {
    return GeometryFactory.createGM_Point( shppointz.getX(), shppointz.getY(), shppointz.getZ(), crs );
  }

  /**
   * method: GM_Point[] transformMultiPoint(CS_CoordinateSystem srs, <BR>
   * SHPMultiPoint shpmultipoint)) <BR>
   * transforms a SHPMultiPoint to a WKSGeometry <BR>
   * gets a multipoint that should be transformed <BR>
   */
  public static GM_Point[] transformMultiPoint( final String crs, final ISHPMultiPoint shpmultipoint )
  {
    final ISHPPoint[] points = shpmultipoint.getPoints();
    final GM_Point[] gm_points = new GM_Point[points.length];

    for( int i = 0; i < points.length; i++ )
      gm_points[i] = transformPoint( crs, points[i] );

    return gm_points;
  }

  public static GM_Position[][] transformParts( final ISHPParts partGeometry )
  {
    final int[] parts = partGeometry.getParts();
    final ISHPPoint[] points = partGeometry.getPoints();

    final GM_Position[][] sequences = new GM_Position[parts.length][];

    for( int i = 0; i < parts.length; i++ )
    {
      final int start = parts[i];
      final int end = i == parts.length - 1 ? points.length : parts[i + 1];

      sequences[i] = new GM_Position[end - start];

      for( int p = start; p < end; p++ )
      {
        final ISHPPoint point = points[p];
        sequences[i][p - start] = GeometryFactory.createGM_Position( point.getX(), point.getY(), point.getZ() );
      }
    }

    return sequences;
  }

  /**
   * Transforms a SHPPolyLinez to a WKSGeometry
   */
  public static GM_Curve[] transformPolyLine( final String crs, final ISHPParts shpPolyLine )
  {
    final GM_Position[][] parts = transformParts( shpPolyLine );

    final GM_Curve[] curve = new GM_Curve[parts.length];

    try
    {
      for( int i = 0; i < parts.length; i++ )
      {
        final GM_Position[] gm_points = parts[i];

        final GM_CurveSegment cs = GeometryFactory.createGM_CurveSegment( gm_points, crs );
        curve[i] = GeometryFactory.createGM_Curve( cs );
        curve[i].setCoordinateSystem( crs );
      }
    }
    catch( final GM_Exception e )
    {
      System.out.println( "SHP2WKS::" + e );
    }

    return curve;
  }

  /**
   * Transforms the SHPPolygon to a WKSGeometry
   */
  public static GM_Polygon[] transformPolygon( final String crs, final ISHPParts shppolygon )
  {
    final GM_Position[][] parts = transformParts( shppolygon );

    final List<GM_Position[]> outerRings = new ArrayList<>( parts.length );
    final List<GM_Position[]> innerRings = new ArrayList<>( parts.length );

    for( final GM_Position[] part : parts )
    {
      final GM_Position[] ring = part;

      // note: esris (unmathemathic) definition of positive area is clockwise => outer ring, negative => inner ring
      final double esriArea = -GeometryUtilities.calcSignedAreaOfRing( ring );
      if( esriArea >= 0 )
        outerRings.add( ring );
      else
        innerRings.add( ring );
    }

    final List<GM_Polygon> wkslp = new ArrayList<>();

    for( final GM_Position[] out_ring : outerRings )
    {
      final List<GM_Position[]> innerOfOuter = new ArrayList<>( innerRings.size() );
      PointInRing pir = null; // lazy create (performance!)

      for( final Iterator<GM_Position[]> innerIt = innerRings.iterator(); innerIt.hasNext(); )
      {
        final GM_Position[] in_ring = innerIt.next();

        if( pir == null )
        {
          final Coordinate[] coordinates = new Coordinate[out_ring.length];
          for( int i = 0; i < coordinates.length; i++ )
            coordinates[i] = new Coordinate( out_ring[i].getX(), out_ring[i].getY() );

          final LinearRing lr = GF.createLinearRing( coordinates );
          // Check, if SimplePointInRing is always good;
          // even for a polgone shape with quite comlex outer rings, simple was still considerably faster
          // pir = new SIRtreePointInRing( lr );
          pir = new SimplePointInRing( lr );
          // pir = new MCPointInRing( lr );
        }

        if( isInside( pir, in_ring ) )
        {
          innerOfOuter.add( in_ring );
          innerIt.remove();
        }
      }

      try
      {
        final GM_Position[][] inrings = innerOfOuter.toArray( new GM_Position[innerOfOuter.size()][] );
        final GM_Polygon sur = GeometryFactory.createGM_Surface( out_ring, inrings, crs );
        wkslp.add( sur );
      }
      catch( final GM_Exception e )
      {
        System.out.println( "SHP2WKS:: transformPolygonz\n" + e );
      }
    }

    return toArray( wkslp );
  }

  private static GM_Polygon[] toArray( final List<GM_Polygon> wkslp )
  {
    return wkslp.toArray( new GM_Polygon[wkslp.size()] );
  }

  private static boolean isInside( final PointInRing pir, final GM_Position[] inRing )
  {
    // Per esri white paper: two consecutive cannot lie on the boundary;
    // so if two consecutive points are inside the pir, we can be sure that the whole inRing
    // lies inside
    // One single check is not enough, as a inner ring may touch an outer outer ring of an alien patch
    final Coordinate c0 = new Coordinate( inRing[0].getX(), inRing[0].getY() );
    if( !pir.isInside( c0 ) )
      return false;

    // check first point
    final Coordinate c1 = new Coordinate( inRing[1].getX(), inRing[1].getY() );
    if( !pir.isInside( c1 ) )
      return false;

    return true;
  }

  public static GM_Object transform( final String crs, final ISHPGeometry shpGeom )
  {
    if( shpGeom instanceof SHPNullShape )
      return null;

    if( shpGeom instanceof ISHPPoint )
      return SHP2GM_Object.transformPoint( crs, (ISHPPoint)shpGeom );

    if( shpGeom instanceof ISHPMultiPoint )
    {
      final GM_Point[] points = SHP2GM_Object.transformMultiPoint( crs, (ISHPMultiPoint)shpGeom );
      if( points == null )
        return null;

      return GeometryFactory.createGM_MultiPoint( points, crs );
    }

    if( shpGeom instanceof ISHPPolyLine )
    {
      final GM_Curve[] curves = SHP2GM_Object.transformPolyLine( crs, (ISHPPolyLine)shpGeom );
      if( curves == null )
        return null;

      return GeometryFactory.createGM_MultiCurve( curves, crs );
    }

    if( shpGeom instanceof ISHPPolygon )
    {
      final GM_Polygon[] polygons = SHP2GM_Object.transformPolygon( crs, (ISHPPolygon)shpGeom );
      if( polygons == null || polygons.length <= 0 )
        return null;

      return GeometryFactory.createGM_MultiSurface( polygons, crs );
    }

    throw new UnsupportedOperationException( "Unknown shpe class: " + shpGeom );
  }
}
