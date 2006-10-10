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

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher
 * 
 * an ISTyledElement is a helper for drawing geometric primitives according to an 
 * external configuration; there are several types of StyledElements: Points, Fonts, 
 * Polygons, Lines 
 */
public interface IStyledElement
{
  /**
   * sets the path used to draw the element; this can be used in different ways for the diverse
   * element types:
   * 1.) Point, Font:  the element is drawn at each point of the path
   * 2.) Line:  the path describes the line path
   * 3.) Polygon: the path describes the outline of the element 
   */
  public void setPath( ArrayList<Point> path );

  /**
   * paints the element into the given GC 
   */
  public void paint( GCWrapper gc, Device dev );

  /**
   * @return type of the element (point, line, polygon, etc.)
   */
  public SE_TYPE getType( );

}
