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

import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public final class JtsVectorUtilities
{
  private JtsVectorUtilities( )
  {
  }

  /**
   * This function returns a vector of the line between this two points as point.
   * 
   * @param start
   *          The start point of the line.
   * @param end
   *          The end point of the line.
   * @return A vector of the line between this two points as point.
   */
  public static Point getVector( final Point start, final Point end )
  {
    final Coordinate vector = getVector( start.getCoordinate(), end.getCoordinate() );

    return JTSAdapter.jtsFactory.createPoint( vector );
  }

  public static Coordinate getVector( final Coordinate start, final Coordinate end )
  {
    final Coordinate vector = new Coordinate( start.x - end.x, start.y - end.y );

    return vector;
  }

  /**
   * This function calculates a normalized vector.
   * 
   * @param vector
   *          The vector to be normalized.
   * @return The normalized vector.
   */
  public static Point getNormalizedVector( final Point vector )
  {
    final Coordinate normalized = getNormalizedVector( vector.getCoordinate() );
    return JTSAdapter.jtsFactory.createPoint( normalized );

  }

  public static Coordinate getNormalizedVector( final Coordinate vector )
  {
    /* The length of a vector is the sum of all elements with the power of two and than the square root of it. */
    final double laenge = Math.sqrt( vector.x * vector.x + vector.y * vector.y );

    final Coordinate normalized = new Coordinate( vector.x / laenge, vector.y / laenge );
    return normalized;
  }

  public static Point movePoint( final Point point, final LineString vector, final int direction, final double distance )
  {
    final Coordinate v = getVector( vector.getStartPoint().getCoordinate(), vector.getEndPoint().getCoordinate() );

    return movePoint( point, v, direction, distance );

  }

  public static Point movePoint( final Point point, final Coordinate vector, final int direction, final double distance )
  {

    final Coordinate normalized = getNormalizedVector( vector );

    double mx;
    double my;
    if( direction < 0 )
    {
      mx = point.getX() - normalized.x * distance;
      my = point.getY() - normalized.y * distance;
    }
    else
    {
      mx = point.getX() + normalized.x * distance;
      my = point.getY() + normalized.y * distance;
    }

    final double mz = point.getCoordinate().z;

    return JTSAdapter.jtsFactory.createPoint( new Coordinate( mx, my, mz ) );
  }

  public static Coordinate getOrthogonalVector( final LineSegment segment, final Point point )
  {
    /* Calculate the vector of the direction. */
    final Coordinate c0 = segment.p0;
    final Coordinate c1 = segment.p1;
    final Coordinate dVector = new Coordinate( c1.x - c0.x, c1.y - c0.y );

    /* Circle it for 90 deegree by exchanging the values x and y and making one negative. */
    final Coordinate dVectorVertical = new Coordinate( -dVector.y, dVector.x );

    /* Normalize it. */
    final Point dVectorNormalized = getNormalizedVector( JTSAdapter.jtsFactory.createPoint( dVectorVertical ) );

    /* Now, the new points are calculated. */
    final double vectorExtend = 10.0;

    /* Increase its length (the normalized vector is showing into the left direction). */
    final Coordinate dVectorLeft = new Coordinate( dVectorNormalized.getX() * vectorExtend, dVectorNormalized.getY() * vectorExtend );
    final Coordinate movedPoint = new Coordinate( point.getX() + dVectorLeft.x, point.getY() + dVectorLeft.y );

    return getVector( point.getCoordinate(), movedPoint );
  }

  /**
   * Implementation taken from the nofdp idss ProfileBuilder class
   * 
   * @return orthogonal line string to the corresponding curve line segment
   */
  public static Coordinate getOrthogonalVector( final LineString curve, final Point point )
  {
    if( curve.intersection( point.buffer( 10E-03 ) ).isEmpty() )
      return null;

    final LineSegment segment = JTSUtilities.findLineSegment( curve, point );

    return getOrthogonalVector( segment, point );
  }

  public static Coordinate getInverse( final Coordinate vector )
  {
    return new Coordinate( vector.x * -1, vector.y * -1 );
  }
}