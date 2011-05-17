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

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.ISHPParts;
import org.kalypso.shape.geometry.ISHPPoint;
import org.kalypso.shape.geometry.SHPMultiPoint;
import org.kalypso.shape.geometry.SHPMultiPointz;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.geometry.SHPPolyLinez;
import org.kalypso.shape.geometry.SHPPolygon;
import org.kalypso.shape.geometry.SHPPolygonz;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_CurveSegment;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
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
   * method: GM_Point transformPoint(CS_CoordinateSystem srs, <BR>
   * SHPPoint shppoint)) <BR>
   * transforms a SHPPoint to a WKSGeometry <BR>
   * gets a point that should be transformed <BR>
   */
  public static GM_Point transformPoint( final String crs, final SHPPoint shppoint )
  {
    return GeometryFactory.createGM_Point( shppoint.getX(), shppoint.getY(), crs );
  }

  /**
   * method: GM_Point transformPointz(CS_CoordinateSystem srs, <BR>
   * SHPPointz shppointz)) <BR>
   * transforms a SHPPointz to a WKSGeometry <BR>
   * gets a pointz that should be transformed <BR>
   */
  public static GM_Point transformPointz( final String crs, final SHPPointz shppointz )
  {
    return GeometryFactory.createGM_Point( shppointz.getX(), shppointz.getY(), shppointz.getZ(), crs );
  }

  /**
   * method: GM_Point[] transformMultiPoint(CS_CoordinateSystem srs, <BR>
   * SHPMultiPoint shpmultipoint)) <BR>
   * transforms a SHPMultiPoint to a WKSGeometry <BR>
   * gets a multipoint that should be transformed <BR>
   */
  public static GM_Point[] transformMultiPoint( final String crs, final SHPMultiPoint shpmultipoint )
  {
    final SHPPoint[] points = shpmultipoint.getPoints();
    final GM_Point[] gm_points = new GM_Point[points.length];

    for( int i = 0; i < points.length; i++ )
      gm_points[i] = transformPoint( crs, points[i] );

    return gm_points;
  }

  /**
   * method: GM_Point[] transformMultiPointz(CS_CoordinateSystem srs, <BR>
   * SHPMultiPointz shpmultipointz)) <BR>
   * transforms a SHPMultiPointz to a WKSGeometry <BR>
   * gets a multipointz that should be transformed <BR>
   */
  public static GM_Point[] transformMultiPointz( final String crs, final SHPMultiPointz shpmultipointz )
  {
    final SHPPointz[] points = shpmultipointz.getPoints();
    final GM_Point[] gm_points = new GM_Point[points.length];

    for( int i = 0; i < points.length; i++ )
      gm_points[i] = transformPointz( crs, points[i] );

    return gm_points;
  }

  /**
   * method: GM_Point[][] transformPolyLinez(CS_CoordinateSystem srs, <BR>
   * SHPPolyLinez shppolylinez)) <BR>
   * transforms a SHPPolyLinez to a WKSGeometry <BR>
   * gets a polylinez that should be transformed <BR>
   */
  public static GM_Curve[] transformPolyLine( final String crs, final ISHPParts shpPolyLine )
  {
    final GM_Curve[] curve = new GM_Curve[shpPolyLine.getNumParts()];

    try
    {
      for( int j = 0; j < shpPolyLine.getNumParts(); j++ )
      {
        final ISHPPoint[][] pointsz = shpPolyLine.getPoints();
        final GM_Position[] gm_points = new GM_Position[pointsz[j].length];

        for( int i = 0; i < pointsz[j].length; i++ )
          gm_points[i] = GeometryFactory.createGM_Position( pointsz[j][i].getX(), pointsz[j][i].getY(), pointsz[j][i].getZ() );

        final GM_CurveSegment cs = GeometryFactory.createGM_CurveSegment( gm_points, crs );
        curve[j] = GeometryFactory.createGM_Curve( cs );
        curve[j].setCoordinateSystem( crs );
      }
    }
    catch( final Exception e )
    {
      System.out.println( "SHP2WKS::" + e );
    }

    return curve;
  }

  /**
   * transforms the SHPPolygon to a WKSGeometry <BR>
   * gets the polygon that should be transformed <BR>
   */
  public static GM_Surface<GM_SurfacePatch>[] transformPolygon( final String crs, final ISHPParts shppolygon )
  {
    final List<GM_Position[]> outerRings = new ArrayList<GM_Position[]>( shppolygon.getNumParts() );
    final List<GM_Position[]> innerRings = new ArrayList<GM_Position[]>( shppolygon.getNumParts() );

    for( int i = 0; i < shppolygon.getNumParts(); i++ )
    {
      final ISHPPoint[][] pointsz = shppolygon.getPoints();

      final GM_Position[] ring = new GM_Position[pointsz[i].length];

      for( int k = 0; k < pointsz[i].length; k++ )
        ring[k] = GeometryFactory.createGM_Position( pointsz[i][k].getX(), pointsz[i][k].getY(), pointsz[i][k].getZ() );

      // note: esris (unmathemathic) definition of positive area is clockwise => outer ring, negative => inner ring
      final double esriArea = -GeometryUtilities.calcSignedAreaOfRing( ring );
      if( esriArea >= 0 )
        outerRings.add( ring );
      else
        innerRings.add( ring );
    }

    final List<GM_Surface< ? extends GM_SurfacePatch>> wkslp = new ArrayList<GM_Surface< ? extends GM_SurfacePatch>>();

    for( final GM_Position[] out_ring : outerRings )
    {
      final List<GM_Position[]> innerOfOuter = new ArrayList<GM_Position[]>( innerRings.size() );
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
        final GM_Surface< ? extends GM_SurfacePatch> sur = GeometryFactory.createGM_Surface( out_ring, inrings, crs );
        wkslp.add( sur );
      }
      catch( final GM_Exception e )
      {
        System.out.println( "SHP2WKS:: transformPolygonz\n" + e );
      }
    }

    return toArray( wkslp );
  }

  @SuppressWarnings("unchecked")
  private static GM_Surface<GM_SurfacePatch>[] toArray( final List<GM_Surface< ? extends GM_SurfacePatch>> wkslp )
  {
    return wkslp.toArray( new GM_Surface[wkslp.size()] );
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
    if( shpGeom instanceof SHPPoint )
      return SHP2GM_Object.transformPoint( crs, (SHPPoint) shpGeom );

    if( shpGeom instanceof SHPMultiPoint )
    {
      final GM_Point[] points = SHP2GM_Object.transformMultiPoint( crs, (SHPMultiPoint) shpGeom );
      if( points == null )
        return null;

      return GeometryFactory.createGM_MultiPoint( points, crs );
    }

    if( shpGeom instanceof SHPPolyLine )
    {
      final GM_Curve[] curves = SHP2GM_Object.transformPolyLine( crs, (SHPPolyLine) shpGeom );
      if( curves == null )
        return null;

      return GeometryFactory.createGM_MultiCurve( curves, crs );
    }

    if( shpGeom instanceof SHPPolygon )
    {
      final GM_Surface<GM_SurfacePatch>[] polygons = SHP2GM_Object.transformPolygon( crs, (SHPPolygon) shpGeom );
      if( polygons == null || polygons.length <= 0 )
        return null;

      return GeometryFactory.createGM_MultiSurface( polygons, crs );
    }

    if( shpGeom instanceof SHPPointz )
      return SHP2GM_Object.transformPointz( crs, (SHPPointz) shpGeom );

    if( shpGeom instanceof SHPPolyLinez )
    {
      final GM_Curve[] curves = SHP2GM_Object.transformPolyLine( crs, (SHPPolyLinez) shpGeom );
      if( curves == null )
        return null;

      return GeometryFactory.createGM_MultiCurve( curves, crs );
    }

    if( shpGeom instanceof SHPPolygonz )
    {
      final GM_Surface<GM_SurfacePatch>[] polygonsz = SHP2GM_Object.transformPolygon( crs, (SHPPolygonz) shpGeom );
      if( polygonsz != null )
        return GeometryFactory.createGM_MultiSurface( polygonsz, crs );

      return null;
    }

    throw new NotImplementedException( "Unknown shpe class: " + shpGeom );
  }
}
