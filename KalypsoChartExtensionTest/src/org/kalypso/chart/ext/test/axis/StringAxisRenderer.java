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
package org.kalypso.chart.ext.test.axis;

import java.awt.Insets;
import java.text.Format;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.base.axisrenderer.AbstractAxisRenderer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu
 * 
 */
public class StringAxisRenderer extends AbstractAxisRenderer<String>
{

  public StringAxisRenderer( String id, RGB rgbForeground, RGB rgbBackground, int lineWidth, int tickLength, Insets tickLabelInsets, Insets labelInsets, int gap, FontData fdLabel, FontData fdTick )
  {
    super( id, rgbForeground, rgbBackground, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick );
  }

  /**
   * @see org.kalypso.chart.ext.base.axisrenderer.AbstractAxisRenderer#getTextExtent(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      java.lang.Object, org.eclipse.swt.graphics.FontData, java.text.Format)
   */
  @Override
  protected Point getTextExtent( GCWrapper gcw, String value, FontData fontData, Format format )
  {
    return super.getTextExtent( gcw, value, fontData );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer#getAxisWidth(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  public int getAxisWidth( IAxis axis )
  {
    return 70;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer#getGridTicks(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  public String[] getTicks( IAxis<String> axis )
  {
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.kalypso.chart.framework.model.mapper.IAxis, org.eclipse.swt.graphics.Rectangle)
   */
  public void paint( GCWrapper gc, IAxis axis, Rectangle screenArea )
  {
    // TODO Auto-generated method stub

  }

}
