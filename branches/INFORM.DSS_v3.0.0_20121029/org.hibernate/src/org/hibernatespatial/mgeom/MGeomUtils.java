/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hibernatespatial.mgeom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;

/**
 * @author Gernot Belger
 */
public class MGeomUtils
{
  /**
   * Adapted form jts {@link com.vividsolutions.jts.linearref.LengthIndexedLine#extractPoint(double)} for m values.<br/>
   * <br/>
   * Computes the {@link Coordinate} for the point
   * on the line at the given index.
   * If the index is out of range the first or last point on the
   * line will be returned.
   * The Z-ordinate of the computed point will be interpolated from
   * the Z-ordinates of the line segment containing it, if they exist.
   *
   * @param index
   *          the index of the desired point
   * @return the Coordinate at the given index
   */
  public static MCoordinate extractPoint( final Geometry linearGeom, final double index )
  {
    final LinearLocation loc = LengthLocationMap.getLocation( linearGeom, index );

    final LineSegment segment = loc.getSegment( linearGeom );

    final double segmentFraction = loc.getSegmentFraction();

    final Coordinate pointAlong = segment.pointAlong( segmentFraction );

    /* handle z */
    final Coordinate p0 = segment.p0;
    final Coordinate p1 = segment.p1;
    pointAlong.z = p0.z + segmentFraction * (p1.z - p0.z);

    /* handle m */
    final MCoordinate m0 = MCoordinate.convertCoordinate( p0 );
    final MCoordinate m1 = MCoordinate.convertCoordinate( p1 );

    final MCoordinate mPointAlong = MCoordinate.convertCoordinate( pointAlong );

    mPointAlong.m = m0.m + segmentFraction * (m1.m - m0.m);

    return mPointAlong;
  }
}