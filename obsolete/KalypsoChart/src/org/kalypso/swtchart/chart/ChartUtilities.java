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
package org.kalypso.swtchart.chart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;

/**
 * @author schlienger
 * @author burtscher
 * 
 * some helper methods to ease your everyday life programming chart stuff
 */
public class ChartUtilities
{
  private ChartUtilities( )
  {
    // not to be instanciated
  }

  /**
   * @return true if the screen coordinates should be inverted
   */
  public static boolean isInverseScreenCoords( final IAxis< ? > axis )
  {
    final ORIENTATION ori = axis.getPosition().getOrientation();
    final DIRECTION dir = axis.getDirection();

    return ori == ORIENTATION.VERTICAL && dir == DIRECTION.POSITIVE || ori == ORIENTATION.HORIZONTAL && dir == DIRECTION.NEGATIVE;
  }

  
  /**
   * sets the given GC to an initial state - this methosd should be called before any 
   * chart painting action is processed
   */
  public static void resetGC( final GC gc, final Device dev )
  {
    gc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );
    gc.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gc.setLineWidth( 1 );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setAlpha( 255 );
  }

  /**
   * maximises the chart view - that means all the available data of all layers is shown
   */
  public static void maximize( final Chart chart )
  {
    final IAxis[] axes = chart.getAxisRegistry().getAxes();
    chart.autoscale( axes );
    chart.repaint();
  }

}
