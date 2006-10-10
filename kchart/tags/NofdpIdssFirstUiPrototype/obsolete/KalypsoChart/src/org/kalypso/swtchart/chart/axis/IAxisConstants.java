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
package org.kalypso.swtchart.chart.axis;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;

/**
 * @author schlienger
 * @author burtscher
 * 
 * some enumerations that describe axis characteristics
 */
public interface IAxisConstants
{
  /**
   * The position of the axis in the chart
   */
  public enum POSITION
  {
    TOP(ORIENTATION.HORIZONTAL),
    LEFT(ORIENTATION.VERTICAL),
    BOTTOM(ORIENTATION.HORIZONTAL),
    RIGHT(ORIENTATION.VERTICAL);

    private final ORIENTATION m_orientation;

    POSITION( ORIENTATION orientation )
    {
      m_orientation = orientation;
    }

    public ORIENTATION getOrientation( )
    {
      return m_orientation;
    }
  }

  public enum ORIENTATION implements ICoordinateSwitcher
  {
    VERTICAL(true),
    HORIZONTAL(false);

    private final boolean m_switsch;

    private ORIENTATION( final boolean switsch )
    {
      m_switsch = switsch;
    }

    public int toInt( )
    {
      return m_switsch ? -1 : 1;
    }

    public double getX( final Point2D point )
    {
      return m_switsch ? point.getY() : point.getX();
    }

    public double getY( final Point2D point )
    {
      return m_switsch ? point.getX() : point.getY();
    }

    public int getX( final Point point )
    {
      return m_switsch ? point.y : point.x;
    }

    public int getY( final Point point )
    {
      return m_switsch ? point.x : point.y;
    }
  }

  /**
   * The property of the data.
   */
  public enum PROPERTY
  {
    CONTINUOUS,
    DISCRETE;
  }

  /**
   * An axis direction can be one of:
   * <ul>
   * <li>POSITIVE: from bottom to top, or from left to right
   * <li>NEGATIVE: from top to bottom, or from right to left
   * </ul>
   * 
   * @author schlienger
   */
  public enum DIRECTION
  {
    POSITIVE,
    NEGATIVE;
  }
}
