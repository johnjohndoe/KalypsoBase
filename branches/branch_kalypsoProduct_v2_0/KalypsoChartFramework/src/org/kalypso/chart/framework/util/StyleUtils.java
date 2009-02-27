package org.kalypso.chart.framework.util;

import java.util.List;

import org.eclipse.swt.graphics.Point;

/**
 * @author burtscher static helper methods to ease the handling of Style-concerns
 */
public class StyleUtils
{

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
        final Point p = path.get( i );
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
