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
package org.kalypso.model.wspm.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LineStringExtracter;

/**
 * Implements to core algorythm of building waterlevel segments from a waterlevel height and a profile line string.
 *
 * @author Gernot Belger
 */
public class JTSWaterlevelIntersector
{
  private final Coordinate[] m_profileCoordinates;

  public JTSWaterlevelIntersector( final Coordinate[] profileCoordinates )
  {
    m_profileCoordinates = profileCoordinates;
  }

  public LineString[] createWaterlevels( final LineString waterlevelLine )
  {
    final double maxHeight = calculateMaxHeight( waterlevelLine );

    final Polygon solidProfile = buildSolidProfile( maxHeight, waterlevelLine.getFactory() );
    if( solidProfile == null )
      return new LineString[] {};

    /* waterlevels are intersection of waterlevel line with solid profile */
    final Geometry intersection = solidProfile.intersection( waterlevelLine );

    /* Decompose segments */
    final List<LineString> lines = LineStringExtracter.getLines( intersection );
    return lines.toArray( new LineString[lines.size()] );
  }

  private double calculateMaxHeight( final LineString waterlevelLine )
  {
    final double maxProfile = calculateHeight( m_profileCoordinates );
    final double maxWaterlevel = calculateHeight( waterlevelLine.getCoordinates() );

    return Math.max( maxProfile, maxWaterlevel ) + 1.0;
  }

  private double calculateHeight( final Coordinate[] coordinates )
  {
    double maxHeight = -Double.MAX_VALUE;

    for( final Coordinate coordinate : coordinates )
      maxHeight = Math.max( maxHeight, coordinate.y );

    return maxHeight;
  }

  /**
   * Builds a closed, 'solid' profile polygon of the profile area.
   */
  private Polygon buildSolidProfile( final double top, final GeometryFactory factory )
  {
    if( m_profileCoordinates.length < 3 )
      return null;

    final List<Coordinate> negativeCoordinates = new ArrayList<>();

    final Coordinate firstCrd = m_profileCoordinates[0];
    final Coordinate lastCrd = m_profileCoordinates[m_profileCoordinates.length - 1];

    negativeCoordinates.addAll( Arrays.asList( m_profileCoordinates ) );
    // REMARK: +1/-1 handles the rare case where we have negative vertical walls at the end of the cs
    // TODO: there is still no guarantee, that we get a valid polygon here (backjumps at cs end, etc.)
    negativeCoordinates.add( new Coordinate( lastCrd.x + 1.0, top ) );
    negativeCoordinates.add( new Coordinate( firstCrd.x - 1.0, top ) );
    negativeCoordinates.add( new Coordinate( firstCrd ) );

    final LinearRing shell = factory.createLinearRing( negativeCoordinates.toArray( new Coordinate[negativeCoordinates.size()] ) );
    if( !shell.isValid() )
      return null;

    return factory.createPolygon( shell, null );
  }
}