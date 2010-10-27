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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.awt.Insets;

import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * Helper object to reduce the complexity of the GenericAxisRenderer constructor
 * 
 * @author burtscher1
 */
public class AxisRendererConfig
{
  /**
   * length of ticks in pixels
   */
  public int tickLength = 5;

  /**
   * insets aruond tick labels
   */
  public Insets tickLabelInsets = new Insets( 1, 1, 1, 1 );

  /**
   * insets around axis label
   */
  public Insets labelInsets = new Insets( 1, 1, 1, 1 );

  /**
   * gap between plot and axis line
   */
  public int gap = 0;

  /**
   * smallest intervall for tick label values
   */
  public Number minTickInterval = 0;

  /**
   * if set to true, a tick label which would not diplayed in full because it's cut off by the border of the axis
   * widget, will not be displayed at all
   */
  public boolean hideCut = true;

  /**
   * if set to a value greater 0, the axis will not be (re)sized according to its needs, its width will always be the
   * given value in pixels
   */
  public int fixedWidth = 0;

  /**
   * style for axis line
   */
  public ILineStyle axisLineStyle = StyleUtils.getDefaultLineStyle();

  /**
   * style for axis label text
   */
  public ITextStyle labelStyle = StyleUtils.getDefaultTextStyle();

  /**
   * style for tick line
   */
  public ILineStyle tickLineStyle = StyleUtils.getDefaultLineStyle();

  /**
   * style for tick label text
   */
  public ITextStyle tickLabelStyle = StyleUtils.getDefaultTextStyle();

  public int borderSize = 0;

  public AxisRendererConfig( )
  {
    // nothing to do
  }

}
