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
package org.kalypso.swtchart.chart.styles;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

/**
 * @author burtscher
 * static helper methods to ease the handling of Style-concerns
 */
public class StyleHelper
{

  /**
   * transforms an array of alternating x- and y- values into an array of Points
   */
  public static List<Point> intArrayToPointList( int[] path )
  {
    ArrayList<Point> newpath = new ArrayList<Point>();
    if( path != null )
    {
      for( int i = 0; i < path.length; i += 2 )
      {
        int x = path[i];
        int y = path[i + 1];
        Point p = new Point( x, y );
      }
    }

    return newpath;
  }

  /**
   * transforms an array of points into an array of alternating x- and y- values
   */
  public static int[] pointListToIntArray( List<Point> path )
  {
    int[] newpath;
    if( path != null )
    {
      newpath = new int[path.size() * 2];
      for( int i = 0; i < path.size(); i++ )
      {
        Point p = path.get( i );
        if( p != null )
        {
          newpath[2 * i] = p.x;
          newpath[2 * i + 1] = p.y;
        }
      }
    }
    else
      newpath = new int[0];
    return newpath;
  }
}
