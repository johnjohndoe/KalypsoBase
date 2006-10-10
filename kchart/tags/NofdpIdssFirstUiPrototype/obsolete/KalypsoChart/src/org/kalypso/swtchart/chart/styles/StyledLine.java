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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.ChartUtilities;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_LINESTYLE;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher
 */
public class StyledLine implements IStyledElement
{
  ArrayList<Point> m_path;

  private int m_width = 0;

  private SE_LINESTYLE m_style;

  private int m_swtStyle;

  private RGB m_lineColor;

  private int m_alpha;

  public StyledLine( int width, RGB lineColor, SE_LINESTYLE style, int alpha )
  {
    m_width = width;
    m_path = new ArrayList<Point>();
    m_lineColor = lineColor;
    m_style = style;
    m_swtStyle = StyleHelper.linestyleToSWT( m_style );
    m_alpha = alpha;
  }

  /**
   * @see org.kalypso.swtchart.chart.styles.IStyledElement#setPath(java.util.ArrayList)
   */
  public void setPath( ArrayList<Point> path )
  {
    m_path = path;
  }

  /**
   * @see org.kalypso.swtchart.styles.IStyledElement#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gcw, Device dev )
  {
    ChartUtilities.resetGC( gcw.m_gc, dev );
    gcw.setAlpha( m_alpha );
    Color lineColor = new Color( dev, m_lineColor );

    int[] intPath = StyleHelper.pointListToIntArray( m_path );
    gcw.setForeground( lineColor );
    gcw.setLineWidth( m_width );
    gcw.setLineStyle( m_swtStyle );
    gcw.drawPolyline( intPath );

    lineColor.dispose();

  }

  /**
   * @see org.kalypso.swtchart.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.LINE;
  }

}
