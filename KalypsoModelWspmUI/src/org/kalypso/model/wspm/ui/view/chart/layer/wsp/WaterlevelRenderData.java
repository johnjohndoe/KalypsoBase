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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * Data corresponding to a water level within a profile, ready to be rendered. Holds the water level segments and some
 * metadata like name of the waterlevel.
 * 
 * @author Gernot Belger
 */
public class WaterlevelRenderData
{
  private static final int GRAB_DISTANCE_PIXELS = 5;

  private final String m_label;

  private final double m_value;

  private final WaterlevelRenderSegment[] m_segments;

  public WaterlevelRenderData( final String label, final double value, final WaterlevelRenderSegment[] segments )
  {
    m_label = label;
    m_value = value;
    m_segments = segments;
  }

  public WaterlevelRenderSegment[] getSegments( )
  {
    return m_segments;
  }

  public double getValue( )
  {
    return m_value;
  }

  public String getLabel( )
  {
    return m_label;
  }

  // FIXME: check, before, it was like this
// private String findActiveLabel( final Object activeElement )
// {
// Assert.isNotNull( activeElement );
//
// if( activeElement instanceof IWspLayerDataElement )
// return ((IWspLayerDataElement) activeElement).getLabel();
//
// return activeElement.toString();
// }

  /**
   * Paints the water level segments on the given gc.
   */
  public void paint( final GC gc, final ILineStyle lineStyle, final IAreaStyle areaStyle, final ICoordinateMapper coordinateMapper )
  {
    for( final WaterlevelRenderSegment segment : m_segments )
    {
      if( areaStyle != null )
      {
        final PolygonFigure areaFigure = segment.getAreaFigure( coordinateMapper );
        areaFigure.setStyle( areaStyle );
        areaFigure.paint( gc );
      }

      if( lineStyle != null )
      {
        final PolylineFigure lineFigure = segment.getLineFigure( coordinateMapper );
        lineFigure.setStyle( lineStyle );
        lineFigure.paint( gc );
      }
    }
  }

  public WaterlevelRenderSegment findSegment( final Coordinate mousePos, final ICoordinateMapper coordinateMapper )
  {
    for( final WaterlevelRenderSegment segment : m_segments )
    {
      // REMARK: using line figure here, in order to calculate distance in pixels
      final Point[] screenLine = segment.getScreenLine( coordinateMapper );
      final Point startPoint = screenLine[0];
      final Point endPoint = screenLine[1];
      final LineSegment line = new LineSegment( new Coordinate( startPoint.x, startPoint.y ), new Coordinate( endPoint.x, endPoint.y ) );

      final double distance = line.distance( mousePos );
      if( distance < GRAB_DISTANCE_PIXELS )
        return segment;
    }

    return null;
  }
}