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
package org.kalypso.jts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public class JTSConverter
{

  public static Coordinate[] toCoordinates( final Geometry geometry )
  {
    return geometry.getCoordinates();
  }

  public static Coordinate[] toCoordinates( final GM_Position[] positions )
  {
    final List<Coordinate> coordinates = new ArrayList<Coordinate>();
    for( final GM_Position position : positions )
    {
      coordinates.add( JTSAdapter.export( position ) );
    }

    return coordinates.toArray( new Coordinate[] {} );
  }

  public static Point[] toPoints( final Coordinate[] coordinates )
  {
    final GeometryFactory factory = new GeometryFactory();

    final List<Point> points = new ArrayList<Point>();
    for( final Coordinate coordinate : coordinates )
    {
      points.add( factory.createPoint( coordinate ) );

    }
    return points.toArray( new Point[] {} );
  }

  public static Point[] toPoints( final GM_Position[] positions )
  {
    final Coordinate[] coordinates = toCoordinates( positions );

    return toPoints( coordinates );
  }

  public static GM_Position[] toPositions( final Coordinate[] coordinates )
  {
    final List<GM_Position> positions = new ArrayList<GM_Position>();
    for( final Coordinate coordinate : coordinates )
    {
      positions.add( JTSAdapter.wrap( coordinate ) );
    }

    return positions.toArray( new GM_Position[] {} );
  }

  public static GM_Curve[] toCurves( final LineString[] profiles, final String crs ) throws GM_Exception
  {
    final Set<GM_Curve> curves = new HashSet<GM_Curve>();

    for( final LineString profile : profiles )
    {
      curves.add( (GM_Curve) JTSAdapter.wrap( profile, crs ) );
    }

    return curves.toArray( new GM_Curve[] {} );
  }

  public static GM_Triangle toGMTriangle( final LinearRing ring, final String crs ) throws GM_Exception
  {
    final Coordinate[] coordinates = ring.getCoordinates();
    if( coordinates.length != 4 )
      throw new UnsupportedOperationException();

    return toGMTriangle( new Coordinate[] { coordinates[0], coordinates[1], coordinates[2] }, crs );
  }

  private static GM_Triangle toGMTriangle( final Coordinate[] coordinates, final String crs ) throws GM_Exception
  {
    return org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Triangle( toPositions( coordinates ), crs );
  }

  public static GM_Point[] toGMPoints( final Point[] points, final String crs ) throws GM_Exception
  {
    final List<GM_Point> gmpoints = new ArrayList<GM_Point>();

    for( final Point point : points )
    {
      gmpoints.add( (GM_Point) JTSAdapter.wrap( point, crs ) );
    }

    return gmpoints.toArray( new GM_Point[] {} );
  }

  public static Point toPoint( final Coordinate coordinate )
  {
    return new GeometryFactory().createPoint( coordinate );
  }
}
