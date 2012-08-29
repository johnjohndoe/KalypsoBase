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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author kimwerner
 */
public class ChartPlotFrameEdge
{
  private ILineStyle m_lineStyle;

  public ChartPlotFrameEdge( )
  {
    this( null );
  }

  public ChartPlotFrameEdge( final ILineStyle lineStyle )
  {
    m_lineStyle = lineStyle;

  }

  public ILineStyle getLineStyle( )
  {
    return m_lineStyle;
  }

  public int getWidth( )
  {
    if( getLineStyle() != null && getLineStyle().isVisible() )
    {
      return getLineStyle().getWidth();
    }
    return 0;

  }

  public void paint( final GC gc, final int x1, final int y1, final int x2, final int y2 )
  {
    if( getLineStyle() != null && getLineStyle().isVisible() )
    {
      final PolylineFigure lineFigure = new PolylineFigure();
      lineFigure.setStyle( getLineStyle() );
      lineFigure.setPoints( new Point[] { new Point( x1, y1 ), new Point( x2, y2 ) } );
      lineFigure.paint( gc );
    }
  }

  public void setLineStyle( final ILineStyle lineStyle )
  {
    m_lineStyle = lineStyle;
  }
}
