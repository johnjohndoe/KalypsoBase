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
package org.kalypso.shape.tools;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.ISHPMultiPoint;
import org.kalypso.shape.geometry.ISHPPoint;
import org.kalypso.shape.geometry.SHPEnvelope;
import org.kalypso.shape.geometry.SHPGeometryUtils;
import org.kalypso.shape.geometry.SHPMultiPoint;
import org.kalypso.shape.geometry.SHPMultiPointm;
import org.kalypso.shape.geometry.SHPMultiPointz;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.geometry.SHPPolyLinem;
import org.kalypso.shape.geometry.SHPPolyLinez;
import org.kalypso.shape.geometry.SHPPolygon;
import org.kalypso.shape.geometry.SHPPolygonm;
import org.kalypso.shape.geometry.SHPPolygonz;
import org.kalypso.shape.geometry.SHPRange;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Converts jts geometries into shape geometries.
 * 
 * @author Gernot Belger
 */
public final class JTS2SHP
{
  public static int[] toParts( final Coordinate[][] curves )
  {
    final int numParts = curves.length;

    final int[] parts = new int[numParts];

    int partIndex = 0;
    for( int i = 0; i < numParts; i++ )
    {
      final Coordinate[] ls = curves[i];

      parts[i] = partIndex;

      partIndex += ls.length;
    }

    return parts;
  }

  public static ISHPPoint[] toPoints( final Coordinate[][] curves )
  {
    int numPoints = 0;
    for( final Coordinate[] coordinates : curves )
      numPoints += coordinates.length;

    final ISHPPoint[] points = new ISHPPoint[numPoints];

    int pointIndex = 0;
    for( final Coordinate[] part : curves )
    {
      for( final Coordinate crd : part )
      {
        final double z = crd.z;

        if( Double.isNaN( z ) )
          points[pointIndex++] = new SHPPoint( crd.x, crd.y );
        else
          points[pointIndex++] = new SHPPointz( crd.x, crd.y, crd.z, Double.NaN );
      }
    }

    return points;
  }

  public static SHPPolyLine toPolyline( final Coordinate[][] curves )
  {
    final int[] parts = toParts( curves );
    final ISHPPoint[] points = toPoints( curves );

    final SHPEnvelope box = SHPGeometryUtils.createEnvelope( points );

    final ISHPMultiPoint multiPoint = new SHPMultiPoint( box, points );
    return new SHPPolyLine( multiPoint, parts );
  }

  public static SHPPolyLinez toPolylineZ( final Coordinate[][] curves )
  {
    final int[] parts = toParts( curves );
    final ISHPPoint[] points = toPoints( curves );

    final SHPEnvelope box = SHPGeometryUtils.createEnvelope( points );

    final SHPRange zrange = SHPGeometryUtils.createZRange( points );
    final SHPRange mrange = SHPGeometryUtils.createMRange( points );

    final ISHPMultiPoint multiPoint = new SHPMultiPointz( box, points, zrange, mrange );

    return new SHPPolyLinez( multiPoint, parts );
  }

  /**
   * Important: the outer/innter rings of the polygon must already be in the correct orientation for shape file.
   */
  public static ISHPGeometry toPolygon( final Coordinate[][] curves )
  {
    return new SHPPolygon( JTS2SHP.toPolyline( curves ) );
  }

  public static ISHPGeometry toPolygonZ( final Coordinate[][] curves )
  {
    return new SHPPolygonz( JTS2SHP.toPolylineZ( curves ) );
  }

  public static ISHPGeometry toShape( final Geometry geometry )
  {
    if( geometry instanceof Polygon )
    {
      final Polygon polygon = (Polygon)geometry;
      return toPolygon( polygon );
    }

    throw new UnsupportedOperationException();
  }

  private static ISHPGeometry toPolygon( final Polygon polygon )
  {
    return toPolygon( new Polygon[] { polygon } );
  }

  private static ISHPGeometry toPolygon( final Polygon[] polygons )
  {
    final List<Coordinate[]> parts = new LinkedList<>();

    for( final Polygon polygon : polygons )
    {
      final LineString exteriorRing = polygon.getExteriorRing();

      final Coordinate[] exteriorCrds = exteriorRing.getCoordinates();

      if( CGAlgorithms.isCCW( exteriorCrds ) )
        ArrayUtils.reverse( exteriorCrds );

      parts.add( exteriorCrds );

      final int numInteriorRing = polygon.getNumInteriorRing();
      for( int i = 0; i < numInteriorRing; i++ )
      {
        final LineString interiorRing = polygon.getInteriorRingN( i );

        final Coordinate[] interiorCrds = interiorRing.getCoordinates();

        if( !CGAlgorithms.isCCW( interiorCrds ) )
          ArrayUtils.reverse( interiorCrds );

        parts.add( interiorCrds );
      }
    }

    final Coordinate[][] crds = parts.toArray( new Coordinate[parts.size()][] );
    return toPolygon( crds );
  }

  public static SHPPolyLinem toPolylineM( final Coordinate[][] curves )
  {
    final int[] parts = toParts( curves );
    final ISHPPoint[] points = toPoints( curves );

    final SHPEnvelope box = SHPGeometryUtils.createEnvelope( points );

    final SHPRange mrange = SHPGeometryUtils.createMRange( points );

    final ISHPMultiPoint multiPoint = new SHPMultiPointm( box, points, mrange );

    return new SHPPolyLinem( multiPoint, parts );
  }

  public static ISHPGeometry toPolygonM( final Coordinate[][] curves )
  {
    return new SHPPolygonm( JTS2SHP.toPolylineM( curves ) );
  }
}