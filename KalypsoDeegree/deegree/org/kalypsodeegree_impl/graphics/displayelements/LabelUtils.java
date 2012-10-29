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
package org.kalypsodeegree_impl.graphics.displayelements;

import org.kalypsodeegree.graphics.displayelements.Label;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Gernot Belger
 */
public final class LabelUtils
{
  private LabelUtils( )
  {
    throw new UnsupportedOperationException();
  }

  public static int[] calcScreenCoordinates( final GeoTransform projection, final GM_Position pos )
  {
    final int[] coords = new int[2];
    coords[0] = (int) (projection.getDestX( pos.getX() ) + 0.5);
    coords[1] = (int) (projection.getDestY( pos.getY() ) + 0.5);
    return coords;
  }

  /**
   * Calculates the screen coordinates of the given <tt>GM_Curve</tt>.
   */
  public static int[][] calcScreenCoordinates( final GeoTransform projection, final GM_Curve curve ) throws GM_Exception
  {
    final GM_LineString lineString = curve.getAsLineString();
    final int count = lineString.getNumberOfPoints();

    final int[][] pos = new int[3][];
    pos[0] = new int[count];
    pos[1] = new int[count];
    pos[2] = new int[1];

    int k = 0;
    for( int i = 0; i < count; i++ )
    {

      final GM_Position position = lineString.getPositionAt( i );
      final int[] screenPos = calcScreenCoordinates( projection, position );

      final int screenX = screenPos[0];
      final int screenY = screenPos[1];
      if( i == 0 )
      {
        pos[0][k] = screenX;
        pos[1][k] = screenY;
        k++;
      }
      else
      {
        final int lastScreenX = pos[0][k - 1];
        final int lastScreenY = pos[1][k - 1];
        final double distanceToLast = getDistance( screenX, screenY, lastScreenX, lastScreenY );
        // Filter out ducplicate points (in regard to the current screen resolution)
        if( distanceToLast > 1 )
        {
          pos[0][k] = screenX;
          pos[1][k] = screenY;
          k++;
        }
      }
    }
    pos[2][0] = k;

    return pos;
  }

  /**
   * Returns the physical (screen) coordinates.
   */
  public static int[] calcScreenCoordinates( final GeoTransform projection, final GM_Object geometry )
  {
    final GM_Position pos = geometry.getCentroid().getPosition();
    return calcScreenCoordinates( projection, pos );
  }

  public static double getDistance( final double x1, final double y1, final double x2, final double y2 )
  {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    return Math.sqrt( dx * dx + dy * dy );
  }

  public static Envelope toEnvelope( final Label label )
  {
    final Geometry bounds = label.getBoundary();
    return bounds.getEnvelopeInternal();
  }
}