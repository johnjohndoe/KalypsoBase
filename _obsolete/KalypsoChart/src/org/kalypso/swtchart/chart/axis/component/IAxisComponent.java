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
package org.kalypso.swtchart.chart.axis.component;

import org.eclipse.swt.graphics.Rectangle;

/**
 * @author schlienger
 * @author burtscher Widget to display chart axes; it also helps to calculate screen values from normalized values
 */
public interface IAxisComponent<T>
{
  /**
   * converts a double value into the corresponding screen value
   */
  public Integer normalizedToScreen( Double d );

  /**
   * converts a screen value into a normalized value
   */
  public Double screenToNormalized( Integer value );

  /**
   * disposes the widget
   */
  public void dispose( );

  /**
   * sets the components' visibilty
   */
  public void setVisible( boolean b );

  /**
   * @return Rectangle which describes the components' dimension
   */
  public Rectangle getBounds( );

  /**
   * redraws and resizes the widget
   */
  public void layout( );

  /**
   * 
   */
  public void useMargin( boolean useIt );

}
