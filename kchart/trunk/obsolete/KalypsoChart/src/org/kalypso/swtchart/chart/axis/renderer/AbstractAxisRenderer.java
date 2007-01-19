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
package org.kalypso.swtchart.chart.axis.renderer;

import java.awt.Insets;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;

/**
 * @author alibu
 *
 */
public abstract class AbstractAxisRenderer<T> implements IAxisRenderer<T>
{

  protected int m_tickLength=0;
  protected int m_lineWidth=0;
  protected int m_gap=0;
  protected Insets m_labelInsets=null;
  protected Insets m_tickLabelInsets=null;
  protected FontData m_fontDataLabel=null;
  protected FontData m_fontDataTick=null;
  protected HashMap<IAxis<T>, Collection<T>> m_tickMap=new HashMap<IAxis<T>, Collection<T>>();
  protected final RGB m_rgbForeground;
  protected final RGB m_rgbBackground;



  public AbstractAxisRenderer( final RGB rgbForegroundRGB, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick)
  {
    m_rgbForeground = rgbForegroundRGB;
    m_rgbBackground = rgbBackground;
    m_tickLength = tickLength;
    m_lineWidth = lineWidth;
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_gap = gap;
    m_fontDataLabel=fdLabel;
    m_fontDataTick=fdTick;
  }




  protected abstract Point getTextExtent( GCWrapper gcw, T value, FontData fontData);


  protected Point getTextExtent( GCWrapper gc, final String value, FontData fd )
  {
    Font f = new Font( gc.getDevice(), fd );
    gc.setFont( f );
    Point point = gc.textExtent( value );
    gc.setFont( gc.getDevice().getSystemFont() );
    f.dispose();
    return point;
  }




  /**
   * draws a given text at the position using special fontData
   */
  protected void drawText( GCWrapper gc, String text, int x, int y, FontData fd )
  {
    Font f = new Font( gc.getDevice(), fd );
    gc.m_gc.setTextAntialias( SWT.ON );
    gc.setFont( f );
    gc.drawText( text, x, y );
    gc.setFont( gc.getDevice().getSystemFont() );
    f.dispose();
  }





}
