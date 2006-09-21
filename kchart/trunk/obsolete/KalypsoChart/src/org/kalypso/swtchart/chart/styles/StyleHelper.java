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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_LINESTYLE;

/**
 * @author burtscher 
 * static helper methods to ease the handling of Style-concerns
 */
public class StyleHelper
{

  /**
   * transforms an array of alternating x- and y- values into an array of Points
   */
  public static ArrayList<Point> intArrayToPointList( int[] path )
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
  public static int[] pointListToIntArray( ArrayList<Point> path )
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

  /**
   * creates a SE_LINESTYLE enum element from a string 
   * 
   * TODO: Dieses Vorgehen ist leicht behindert: zuerst wird aus einem
   * TODO: There must be a better way: first, a String is transformed into a SE_LINESTYLE,
   * next (@see linestyleToSWT) the SE_LINESTYLE is transformed into a SWT-Linestyle; "someone"
   * should check if the separation of these 2 steps is necessary
   */
  public static SE_LINESTYLE stringToLinestyle( String ls )
  {
    if( ls.compareTo( "SOLID" ) == 0 )
      return SE_LINESTYLE.SOLID;
    else if( ls.compareTo( "DOT" ) == 0 )
      return SE_LINESTYLE.DOT;
    else if( ls.compareTo( "DASH" ) == 0 )
      return SE_LINESTYLE.DASH;
    else if( ls.compareTo( "DASHDOT" ) == 0 )
      return SE_LINESTYLE.DASHDOT;
    else if( ls.compareTo( "DASHDOTDOT" ) == 0 )
      return SE_LINESTYLE.DASHDOTDOT;
    else
      return null;
  }

  /**
   * creates an SWT line style from a SE_LINESTYLE 
   */
  public static int linestyleToSWT( SE_LINESTYLE ls )
  {
    if( ls == SE_LINESTYLE.DASH )
      return SWT.LINE_DASH;
    else if( ls == SE_LINESTYLE.DOT )
      return SWT.LINE_DOT;
    else if( ls == SE_LINESTYLE.DASHDOT )
      return SWT.LINE_DASHDOT;
    else if( ls == SE_LINESTYLE.DASHDOTDOT )
      return SWT.LINE_DASHDOTDOT;
    else
      return SWT.LINE_SOLID;
  }
}
