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
package org.kalypso.shape.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.ISHPMultiPoint;
import org.kalypso.shape.geometry.ISHPParts;
import org.kalypso.shape.geometry.ISHPPoint;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.geometry.SHPPolyLinem;
import org.kalypso.shape.geometry.SHPPolyLinez;
import org.kalypso.shape.geometry.SHPPolygon;
import org.kalypso.shape.geometry.SHPPolygonm;
import org.kalypso.shape.geometry.SHPPolygonz;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SimplePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Transforms {@link ISHPGeometry}s to JTS {@link com.vividsolutions.jts.geom.Geometry}s.
 * 
 * @author Gernot Belger
 */
public final class SHP2JTS
{
  private final GeometryFactory m_factory;

  public SHP2JTS( final GeometryFactory factory )
  {
    m_factory = factory;
  }

  public Point transformPoint( final ISHPPoint shppoint )
  {
    final Coordinate crd = new Coordinate( shppoint.getX(), shppoint.getY(), shppoint.getZ() );
    return m_factory.createPoint( crd );
  }

  public Point[] transformMultiPoint( final ISHPMultiPoint shpmultipoint )
  {
    final ISHPPoint[] points = shpmultipoint.getPoints();
    final Point[] jtsPoints = new Point[points.length];

    for( int i = 0; i < points.length; i++ )
      jtsPoints[i] = transformPoint( points[i] );

    return jtsPoints;
  }

  public CoordinateSequence[] transformParts( final ISHPParts partGeometry )
  {
    final int[] parts = partGeometry.getParts();
    final ISHPPoint[] points = partGeometry.getPoints();

    final CoordinateSequence[] sequences = new CoordinateSequence[parts.length];

    for( int i = 0; i < parts.length; i++ )
    {
      final int start = parts[i];
      final int end = i == parts.length - 1 ? points.length : parts[i + 1];

      final Coordinate[] crds = new Coordinate[end - start];

      for( int p = start; p < end; p++ )
      {
        final ISHPPoint point = points[p];
        crds[p - start] = new Coordinate( point.getX(), point.getY(), point.getZ() );
      }

      sequences[i] = new CoordinateArraySequence( crds );
    }

    return sequences;
  }

  public LineString[] transformPolyLine( final ISHPParts shpPolyLine )
  {

    final CoordinateSequence[] sequences = transformParts( shpPolyLine );
    final LineString[] curves = new LineString[sequences.length];

    for( int i = 0; i < curves.length; i++ )
      curves[i] = m_factory.createLineString( sequences[i] );

    return curves;
  }

  /**
   * transforms the SHPPolygon to a WKSGeometry <BR>
   * gets the polygon that should be transformed <BR>
   */
  public Polygon[] transformPolygon( final ISHPParts shppolygon )
  {
    /* convert to jts */
    final CoordinateSequence[] sequences = transformParts( shppolygon );

    /* sort into inner and outer rings */
    final List<LinearRing> outerRings = new ArrayList<>( sequences.length );
    final List<LinearRing> innerRings = new ArrayList<>( sequences.length );

    for( final CoordinateSequence part : sequences )
    {
      final LinearRing ring = m_factory.createLinearRing( part );

      final Coordinate[] ringCrds = ring.getCoordinates();

      // note: esri's (un - mathematically) definition of positive area is clockwise => outer ring,
      // negative => inner ring
      final boolean ccw = CGAlgorithms.isCCW( ringCrds );
      if( !ccw )
        outerRings.add( ring );
      else
        innerRings.add( ring );
    }

    /* build polygons and determine which ring is inside which polyogn */
    final List<Polygon> polygons = new ArrayList<>();

    for( final LinearRing out_ring : outerRings )
    {
      final List<LinearRing> innerOfOuter = new ArrayList<>( innerRings.size() );
      PointInRing pir = null; // lazy create (performance!)

      for( final Iterator<LinearRing> innerIt = innerRings.iterator(); innerIt.hasNext(); )
      {
        final LinearRing in_ring = innerIt.next();

        if( pir == null )
        {
          // Check, if SimplePointInRing is always good;
          // even for a polgone shape with quite comlex outer rings, simple was still considerably faster
          // pir = new SIRtreePointInRing( lr );
          pir = new SimplePointInRing( out_ring );
          // pir = new MCPointInRing( lr );
        }

        if( isInside( pir, in_ring ) )
        {
          innerOfOuter.add( in_ring );
          innerIt.remove();
        }
      }

      final LinearRing[] inrings = innerOfOuter.toArray( new LinearRing[innerOfOuter.size()] );
      polygons.add( m_factory.createPolygon( out_ring, inrings ) );
    }
    
    // FIXME: check: this is against the SHP specification!
    if( outerRings.isEmpty() && innerRings.size() == 1 )
    {
      polygons.add( m_factory.createPolygon( innerRings.get( 0 ), null ) );
    }
    return polygons.toArray( new Polygon[polygons.size()] );
  }

  private boolean isInside( final PointInRing pir, final LinearRing inRing )
  {
    final Coordinate c0 = inRing.getCoordinateN( 0 );

    // Per esri white paper: two consecutive cannot lie on the boundary;
    // so if two consecutive points are inside the pir, we can be sure that the whole inRing
    // lies inside
    // One single check is not enough, as a inner ring may touch an outer outer ring of an alien patch
    if( !pir.isInside( c0 ) )
      return false;

    // check first point
    final Coordinate c1 = inRing.getCoordinateN( 0 );
    if( !pir.isInside( c1 ) )
      return false;

    return true;
  }

  public Geometry transform( final int srid, final ISHPGeometry shpGeom )
  {
    final Geometry geometry = transform( shpGeom );
    if( geometry == null )
      return null;

    if( geometry instanceof GeometryCollection )
    {
      final GeometryCollection collection = (GeometryCollection)geometry;
      final int numGeometries = collection.getNumGeometries();
      for( int i = 0; i < numGeometries; i++ )
        collection.getGeometryN( i ).setSRID( srid );
    }

    geometry.setSRID( srid );
    return geometry;
  }

  public Geometry transform( final ISHPGeometry shpGeom )
  {
    if( shpGeom instanceof SHPNullShape )
      return null;

    if( shpGeom instanceof ISHPPoint )
      return transformPoint( (ISHPPoint)shpGeom );

    if( shpGeom instanceof ISHPMultiPoint )
    {
      final Point[] points = transformMultiPoint( (ISHPMultiPoint)shpGeom );
      if( points == null )
        return null;

      return m_factory.createMultiPoint( points );
    }

    if( shpGeom instanceof SHPPolyLine || shpGeom instanceof SHPPolyLinez || shpGeom instanceof SHPPolyLinem )
    {
      final LineString[] curves = transformPolyLine( (ISHPParts)shpGeom );
      if( curves == null )
        return null;

      return m_factory.createMultiLineString( curves );
    }

    if( shpGeom instanceof SHPPolygon || shpGeom instanceof SHPPolygonz || shpGeom instanceof SHPPolygonm )
    {
      final Polygon[] polygons = transformPolygon( (ISHPParts)shpGeom );
      if( polygons == null || polygons.length <= 0 )
        return null;

      return m_factory.createMultiPolygon( polygons );
    }

    throw new UnsupportedOperationException( "Unknown shpe class: " + shpGeom );
  }
}
