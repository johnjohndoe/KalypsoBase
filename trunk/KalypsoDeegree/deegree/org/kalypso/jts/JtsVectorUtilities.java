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
import com.vividsolutions.jts.geom.GeometryFactory;
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
    final Coordinate coords = new Coordinate( start.getX() - end.getX(), start.getY() - end.getY() );
    final GeometryFactory factory = new GeometryFactory( start.getPrecisionModel(), start.getSRID() );

    return factory.createPoint( coords );
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
    final double x = vector.getX();
    final double y = vector.getY();

    /* The length of a vector is the sum of all elements with the power of two and than the square root of it. */
    final double laenge = Math.sqrt( x * x + y * y );

    final Coordinate coord = new Coordinate( x / laenge, y / laenge );
    final GeometryFactory factory = new GeometryFactory( vector.getPrecisionModel(), vector.getSRID() );
    return factory.createPoint( coord );
  }

  public static Point movePoint( final Point point, final LineString vector, final int direction, final double distance )
  {
    final Point v = getVector( vector.getStartPoint(), vector.getEndPoint() );
    final Point normalized = getNormalizedVector( v );

    double mx;
    double my;
    if( direction < 0 )
    {
      mx = point.getX() - normalized.getX() * distance;
      my = point.getY() - normalized.getY() * distance;
    }
    else
    {
      mx = point.getX() + normalized.getX() * distance;
      my = point.getY() + normalized.getY() * distance;
    }

    final double mz = point.getCoordinate().z;

    return JTSAdapter.jtsFactory.createPoint( new Coordinate( mx, my, mz ) );
  }

  /**
   * Implementation taken from the nofdp idss ProfileBuilder class
   * 
   * @return orthogonal line string to the corresponding curve line segment
   */
  public static LineString getOrthogonalVector( final LineString curve, final Point point )
  {
    if( curve.intersection( point.buffer( JTSUtilities.TOLERANCE ) ).isEmpty() )
      return null;

    final LineSegment segment = JTSUtilities.findLineSegment( curve, point );

    /* Calculate the vector of the direction. */
    final Coordinate c0 = segment.p0;
    final Coordinate c1 = segment.p1;
    final Coordinate dVector = new Coordinate( c1.x - c0.x, c1.y - c0.y );

    /* Circle it for 90 deegree by exchanging the values x and y and making one negative. */
    final Coordinate dVectorVertical = new Coordinate( -dVector.y, dVector.x );

    /* Normalize it. */
    final GeometryFactory factory = new GeometryFactory( curve.getPrecisionModel(), curve.getSRID() );
    final Point dVectorNormalized = getNormalizedVector( factory.createPoint( dVectorVertical ) );

    /* Now, the new points are calculated. */
    final double vectorExtend = 10.0;

    /* Increase its length (the normalized vector is showing into the left direction). */
    final Coordinate dVectorLeft = new Coordinate( dVectorNormalized.getX() * vectorExtend, dVectorNormalized.getY() * vectorExtend );
    final Coordinate movedPoint = new Coordinate( point.getX() + dVectorLeft.x, point.getY() + dVectorLeft.y );

    // /* Increase its length (the normalized vector is showing into the left direction, so we have to inverse it). */
    // final Coordinate dVectorRight = new Coordinate( -1 * dVectorNormalized.getX() * vectorExtend, -1 *
    // dVectorNormalized.getY() * vectorExtend );
    // Coordinate rightPoint = new Coordinate( point.getX() + dVectorRight.x, point.getY() + dVectorRight.y);

    return JTSAdapter.jtsFactory.createLineString( new Coordinate[] { point.getCoordinate(), movedPoint } );
  }
}