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
package de.openali.odysseus.chart.framework.util.img;

import org.eclipse.swt.graphics.Rectangle;

/**
 * @author kimwerner
 */
public class ChartImageInfo
{
  private Rectangle m_axisLeftRect;

  private Rectangle m_axisRightRect;

  private Rectangle m_axisTopRect;

  private Rectangle m_axisBottomRect;

  private Rectangle m_titleRect;

  private Rectangle m_legendRect;

  private Rectangle m_clientRect;

  private Rectangle m_layerRect;
  
  private Rectangle m_plotRect;

  public Rectangle getAxisBottomRect( )
  {
    return m_axisBottomRect;
  }

  public Rectangle getAxisLeftRect( )
  {
    return m_axisLeftRect;
  }

  public Rectangle getAxisRightRect( )
  {
    return m_axisRightRect;
  }

  public Rectangle getAxisTopRect( )
  {
    return m_axisTopRect;
  }

  public Rectangle getClientRect( )
  {
    return m_clientRect;
  }

  public Rectangle getLayerRect( )
  {
    return m_layerRect;
  }

  public Rectangle getLegendRect( )
  {
    return m_legendRect;
  }

  public Rectangle getPlotRect( )
  {
    return m_plotRect;
  }

  public Rectangle getTitleRect( )
  {
    return m_titleRect;
  }

  public void setAxisBottomRect( Rectangle axisBottomRect )
  {
    m_axisBottomRect = axisBottomRect;
  }

  public void setAxisLeftRect( Rectangle axisLeftRect )
  {
    m_axisLeftRect = axisLeftRect;
  }

  public void setAxisRightRect( Rectangle axisRightRect )
  {
    m_axisRightRect = axisRightRect;
  }

  public void setAxisTopRect( Rectangle axisTopRect )
  {
    m_axisTopRect = axisTopRect;
  }

  public void setClientRect( Rectangle clientRect )
  {
    m_clientRect = clientRect;
  }

  public void setLayerRect( Rectangle layerRect )
  {
    m_layerRect = layerRect;
  }

  public void setLegendRect( Rectangle legendRect )
  {
    m_legendRect = legendRect;
  }

  public void setPlotRect( Rectangle plotRect )
  {
    m_plotRect = plotRect;
  }

  public void setTitleRect( Rectangle titleRect )
  {
    m_titleRect = titleRect;
  }
}
