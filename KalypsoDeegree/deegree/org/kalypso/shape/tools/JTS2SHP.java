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

import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPPoint;
import org.kalypso.shape.geometry.SHPPointz;
import org.kalypso.shape.geometry.SHPPolyLine;
import org.kalypso.shape.geometry.SHPPolyLinez;
import org.kalypso.shape.geometry.SHPPolygon;
import org.kalypso.shape.geometry.SHPPolygonz;

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
  public static SHPPoint[][] toParts( final Coordinate[][] curves )
  {
    final int numParts = curves.length;

    final SHPPoint[][] parts = new SHPPoint[numParts][];

    for( int i = 0; i < numParts; i++ )
    {
      final Coordinate[] ls = curves[i];

      parts[i] = new SHPPoint[ls.length];

      for( int j = 0; j < ls.length; j++ )
      {
        final Coordinate crd = ls[j];
        // TODO: handle coordinates with m
        parts[i][j] = new SHPPoint( crd.x, crd.y );
      }
    }

    return parts;
  }

  public static SHPPointz[][] toPartsZ( final Coordinate[][] curves )
  {
    final int numParts = curves.length;

    final SHPPointz[][] parts = new SHPPointz[numParts][];

    for( int i = 0; i < numParts; i++ )
    {
      final Coordinate[] ls = curves[i];

      parts[i] = new SHPPointz[ls.length];

      for( int j = 0; j < ls.length; j++ )
      {
        final Coordinate crd = ls[j];
        // TODO: handle coordinates with m
        parts[i][j] = new SHPPointz( crd.x, crd.y, crd.z, Double.NaN );
      }
    }

    return parts;
  }

  public static SHPPolyLine toPolyline( final Coordinate[][] curves )
  {
    final SHPPoint[][] parts = JTS2SHP.toParts( curves );
    return new SHPPolyLine( parts );
  }

  public static SHPPolyLinez toPolylineZ( final Coordinate[][] curves )
  {
    final SHPPointz[][] parts = JTS2SHP.toPartsZ( curves );
    return new SHPPolyLinez( parts );
  }

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
      final Polygon polygon = (Polygon) geometry;
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
      parts.add( exteriorRing.getCoordinates() );

      final int numInteriorRing = polygon.getNumInteriorRing();
      for( int i = 0; i < numInteriorRing; i++ )
      {
        final LineString interiorRing = polygon.getInteriorRingN( i );
        final Coordinate[] coordinates = interiorRing.getCoordinates();
        // FIXME: handle orientation?
        parts.add( coordinates );
      }
    }

    final Coordinate[][] crds = parts.toArray( new Coordinate[parts.size()][] );
    return toPolygon( crds );
  }
}