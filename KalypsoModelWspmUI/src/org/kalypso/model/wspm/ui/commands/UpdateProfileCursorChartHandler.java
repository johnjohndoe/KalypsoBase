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
package org.kalypso.model.wspm.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.model.wspm.core.IWspmLayers;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.FindLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.AreaStyle;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Kim Werner
 */
public class UpdateProfileCursorChartHandler extends AbstractChartHandler
{
  private Integer m_p1 = null;

  private Integer m_pMin = null;

  private Integer m_pMax = null;

  public UpdateProfileCursorChartHandler( final IChartComposite chart )
  {
    super( chart );

  }

  private void doPaintSelection( final PaintEvent e )
  {
    if( m_pMin == null || m_pMax == null )
    {
      return;
    }
    if( m_pMin - m_pMax == 0 )
    {
      doPaintSingleSelection( e );
    }
    else
    {
      doPaintRange( e );
    }
  }

  private void doPaintSingleSelection( final PaintEvent e )
  {

    final ILineStyle lineStyle = new LineStyle( 3, new RGB( 0x54, 0xA7, 0xF9 ), 240, 0F, null, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final IChartComposite chart = getChart();
    final Rectangle bounds = RectangleUtils.inflateRect( chart.getPlotInfo().getPlotRect(), lineStyle.getWidth() );

    final PolylineFigure figure = new PolylineFigure();
    figure.setStyle( lineStyle );

    figure.setPoints( new Point[] { new Point( m_pMin, bounds.y ), new Point( m_pMin, bounds.y + bounds.height ) } );

    figure.paint( e.gc );
  }

  private void doPaintRange( final PaintEvent e )
  {

    final RGB rgb = new RGB( 0x54, 0xA7, 0xF9 );
    final ILineStyle lineStyle = new LineStyle( 3, rgb, 180, 0F, new float[] { 2, 2, 2 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final IChartComposite chart = getChart();
    final Rectangle bounds = RectangleUtils.inflateRect( chart.getPlotInfo().getPlotRect(), lineStyle.getWidth() );

    final int yMax = bounds.y + bounds.height;

    final AreaStyle areaStyle = new AreaStyle( new ColorFill( rgb ), 40, lineStyle, true );
    final PolygonFigure figure = new PolygonFigure();
    figure.setStyle( areaStyle );

    final List<Point> points = new ArrayList<>();
    points.add( new Point( m_pMin, bounds.y ) );
    points.add( new Point( m_pMin, yMax ) );
    points.add( new Point( m_pMax, yMax ) );
    points.add( new Point( m_pMax, bounds.y ) );

    figure.setPoints( points.toArray( new Point[] {} ) );
    figure.paint( e.gc );
  }

  public static final IProfilChartLayer findProfileTheme( final IChartComposite chart )
  {
    final IChartModel model = chart.getChartModel();

    final FindLayerVisitor visitor = new FindLayerVisitor( IWspmLayers.LAYER_GELAENDE );
    model.getLayerManager().accept( visitor );

    final IChartLayer layer = visitor.getLayer();

    return (IProfilChartLayer)layer;
  }

  private void doPaintMouse( final PaintEvent e )
  {
    if( m_p1 == null )
    {
      return;
    }
//    final IProfilChartLayer theme = AbstractProfilePointHandler.findProfileTheme( getChart() );
    final IChartComposite chart = getChart();
//    final ICoordinateMapper mapper = theme.getCoordinateMapper();
//    final IAxis domAxis = mapper.getDomainAxis();
//    final IProfile profile = theme.getProfil();
////    final Double x2 = domAxis.screenToNumeric( m_p1 );
//    final IRangeSelection selection = profile.getSelection();
//    if( selection.getCursor() == null )
//    {
//      return;
//    }
//    selection.setCursor( x2 );
    final Integer p1 = m_p1;// domAxis.numericToScreen( selection.getCursor() );
    final RGB rgb = new RGB( 0x54, 0xA7, 0xF9 );
    final ILineStyle lineStyle = new LineStyle( 3, rgb, 180, 0F, new float[] { 2, 2, 2 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final PolylineFigure figure = new PolylineFigure();
    figure.setStyle( lineStyle );
    final Rectangle bounds = RectangleUtils.inflateRect( chart.getPlotInfo().getPlotRect(), lineStyle.getWidth() );
    figure.setPoints( new Point[] { new Point( p1, bounds.y ), new Point( p1, bounds.y + bounds.height ) } );

    figure.paint( e.gc );
  }

  @SuppressWarnings( "rawtypes" )
  private final void getSelectionCursor( )
  {
    final IProfilChartLayer theme = findProfileTheme( getChart() );
    if( theme == null )
    {
      return;
    }
    final IProfile profile = theme.getProfil();
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domAxis = mapper.getDomainAxis();
    if( domAxis == null )
    {
      return;
    }
    final IRangeSelection selection = profile.getSelection();
    if( selection.getCursor() != null )
    {
      m_p1 = domAxis.numericToScreen( selection.getCursor() );
    }

    if( selection.getRange() != null )
    {
      m_pMin = domAxis.numericToScreen( selection.getRange().getMinimum() );
      m_pMax = domAxis.numericToScreen( selection.getRange().getMaximum() );
    }
  }

  @Override
  public CHART_HANDLER_TYPE getType( )
  {
    return CHART_HANDLER_TYPE.eToggle;
  }

  @SuppressWarnings( { "unused", "rawtypes" } )
  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );
    if( isOutOfRange( new Point( e.x, e.y ) ) )
    {
      return;
    }
    final IProfilChartLayer theme = findProfileTheme( getChart() );
    final IChartComposite chart = getChart();
    if( theme == null )
    {
      return;
    }
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domAxis = mapper.getDomainAxis();
    final IProfile profile = theme.getProfil();
    final Double x2 = domAxis.screenToNumeric( e.x );
    final IRangeSelection selection = profile.getSelection();
    selection.setCursor( x2 );
    // m_p1 = e.x;
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    super.paintControl( e );

    getSelectionCursor();

    // doPaintCursor( e );
    doPaintMouse( e );
    doPaintSelection( e );
    m_p1 = null;
    m_pMin = null;
    m_pMax = null;
  }
}
