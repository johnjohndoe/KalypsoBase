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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.kalypso.model.wspm.ui.i18n.Messages;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;

import de.openali.odysseus.chart.ext.base.layer.TooltipFormatter;
import de.openali.odysseus.chart.framework.model.figure.IFigure;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.MultiFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IFill;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.AreaStyle;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.util.FigureUtilities;

/**
 * @author Gernot Belger
 */
public class WaterlevelRenderSegment
{
  private final LineSegment m_line;

  private final Polygon m_area;

  public WaterlevelRenderSegment( final LineSegment line, final Polygon area )
  {
    m_line = line;
    m_area = area;
  }

  public PolylineFigure getLineFigure( final ICoordinateMapper coordinateMapper )
  {
    final Point[] line = getScreenLine( coordinateMapper );

    final PolylineFigure lineFigure = new PolylineFigure();
    lineFigure.setPoints( line );
    return lineFigure;
  }

  Point[] getScreenLine( final ICoordinateMapper coordinateMapper )
  {
    final Coordinate[] lineCoords = new Coordinate[] { m_line.getCoordinate( 0 ), m_line.getCoordinate( 1 ) };
    return FigureUtilities.numericToScreen( coordinateMapper, lineCoords );
  }

  public PolygonFigure getAreaFigure( final ICoordinateMapper coordinateMapper )
  {
    final PolygonFigure areaFigure = new PolygonFigure();

    if( m_area != null )
    {
      final Point[] area = FigureUtilities.numericToScreen( coordinateMapper, m_area.getCoordinates() );
      areaFigure.setPoints( area );
    }

    return areaFigure;
  }

  public LineSegment getLine( )
  {
    return m_line;
  }

  public String formatTooltip( final String label )
  {
    final TooltipFormatter tooltipFormatter = new TooltipFormatter( label, new String[] { "%s", "%.2f", "%s" }, new int[] { SWT.LEFT, SWT.RIGHT, SWT.LEFT } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final double value = getHeight();
    final double width = getWidth();
    final double area = getArea();

    final String heightLabel = Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLayer.2" ); //$NON-NLS-1$
    final String widthLabel = Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLayer.3" ); //$NON-NLS-1$
    final String areaLabel = Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.WspLayer.4" ); //$NON-NLS-1$

    tooltipFormatter.addLine( heightLabel, value, "mNN" ); //$NON-NLS-1$
    tooltipFormatter.addLine( widthLabel, width, "m" ); //$NON-NLS-1$

    if( !Double.isNaN( area ) )
      tooltipFormatter.addLine( areaLabel, area, "m≤" ); //$NON-NLS-1$

    return tooltipFormatter.format();
  }

  private double getArea( )
  {
    if( m_area == null )
      return Double.NaN;

    return m_area.getArea();
  }

  private double getWidth( )
  {
    return m_line.getLength();
  }

  private double getHeight( )
  {
    return m_line.p0.y;
  }

  public IPaintable getHoverFigure( final ILineStyle hoverLineStyle, final int screenLeft, final int screenRight, final ICoordinateMapper mapper )
  {
    final Collection<IFigure< ? >> figures = new ArrayList<>();

    /* Area */
    final PolygonFigure areaFigure = getAreaFigure( mapper );
    final IFill fill = new ColorFill( hoverLineStyle.getColor() );
    final IAreaStyle areaStyle = new AreaStyle( fill, hoverLineStyle.getAlpha() / 2, hoverLineStyle, true );
    areaFigure.setStyle( areaStyle );

    figures.add( areaFigure );

    /* Horizontal line over the whole section */
    final int screenY = mapper.numericToScreen( 0.0, getHeight() ).y;

    final PolylineFigure hoverFigure = new PolylineFigure();
    hoverFigure.setStyle( hoverLineStyle );
    hoverFigure.setPoints( new Point[] { new Point( screenLeft, screenY ), new Point( screenRight, screenY ) } );
    figures.add( hoverFigure );

    return new MultiFigure( figures.toArray( new IFigure< ? >[figures.size()] ) );
  }
}