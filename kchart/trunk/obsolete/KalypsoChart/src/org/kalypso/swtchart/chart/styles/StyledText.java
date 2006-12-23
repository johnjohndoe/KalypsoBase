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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.ChartUtilities;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher
 */
public class StyledText implements IStyledElement
{

  private int m_width = 5;

  private int m_height = 7;

  private RGB m_borderColor;

  private RGB m_fillColor;

  private List<Point> m_path;

  private int m_borderWidth;

  private final int m_alpha;

  private RGB m_foregroundColor;

  private RGB m_backgroundColor;

  private String m_fontName;

  private int m_fontStyle;

  private int m_fontSize;

  private FontData m_fontData;

  private String m_text;

  public StyledText( RGB foregroundColor, RGB backgroundColor, String fontName, int fontStyle, int fontSize, int alpha )
  {
    m_alpha=alpha;
    m_path = new ArrayList<Point>();
    m_foregroundColor=foregroundColor;
    m_backgroundColor=backgroundColor;
    m_fontName=fontName;
    m_fontStyle=fontStyle;
    m_fontSize=fontSize;
    m_fontData=new FontData(fontName, fontSize, fontStyle);
  }


  /**
   * @see org.kalypso.swtchart.styles.IStyledElement#setPath(java.util.List)
   * The path is used draw a styled point at each point of the path; the Point is regarded
   * as the center position of the element
   */
  public void setPath( List<Point> path )
  {
    m_path = path;
  }

  /**
   * @see org.kalypso.swtchart.styles.IStyledElement#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc, Device dev )
  {
    ChartUtilities.resetGC( gc.m_gc, dev );
    gc.setAlpha( m_alpha );

    //Farben speichern, damit sie sp‰ter - nach dem disposen der neuen Farben - nicht leer sind
    Color oldForeground=gc.getForeground();
    Color oldBackground=gc.getBackground();

    Color foregroundColor = new Color( dev, m_foregroundColor );
    Color backgroundColor = new Color( dev, m_backgroundColor );
    gc.setForeground( foregroundColor );
    gc.setBackground( backgroundColor );
    if( m_path != null )
    {
      for( int i = 0; i < m_path.size(); i++ )
      {
        if (m_text.trim().compareTo( "" )==0)
        {
            Point point = m_path.get(i);
            gc.drawText( m_text.trim(), point.x, point.y );
        }
      }
    }
    gc.setForeground( oldForeground );
    gc.setBackground( oldBackground );

    foregroundColor.dispose();
    backgroundColor.dispose();
  }

  /**
   * @see org.kalypso.swtchart.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.TEXT;
  }

  public void setText(String text)
  {
    m_text=text;
  }

  public static StyledText getDefault()
  {
    return new StyledText( new RGB(0,0,0), new RGB(255,255,255), "Arial", SWT.NORMAL, 10, 255 );
  }

}
