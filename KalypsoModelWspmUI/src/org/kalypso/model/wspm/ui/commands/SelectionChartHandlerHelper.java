/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.model.wspm.core.IWspmLayers;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.visitors.FindClosestPointVisitor;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
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
import de.openali.odysseus.chart.framework.util.resource.Pair;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author kimwerner
 */
public class SelectionChartHandlerHelper
{
  private SelectionChartHandlerHelper( )
  {
//don't instantiate
  }

  @SuppressWarnings( "rawtypes" )
  public static Point snapToScreenPoint( final IChartComposite chart, final Point screen )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( chart );
    if( theme == null )
    {
      return null;
    }
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();
    final IProfile profile = theme.getProfil();
    if( profile == null )
      return null;
    if( domainAxis == null )
      return null;
    final Double xPosition = domainAxis.screenToNumeric( screen.x );
    final FindClosestPointVisitor visitor = new FindClosestPointVisitor( xPosition );
    profile.accept( visitor, 1 );
    final IProfileRecord point = visitor.getPoint();
    final Integer snappedScreenX = domainAxis.numericToScreen( point.getBreite() );
    if( Math.abs( snappedScreenX - screen.x ) > 5 )
    {
      return null;
    }
    final Integer snappedScreenY = targetAxis.numericToScreen( point.getHoehe() );
    return new Point( snappedScreenX, snappedScreenY );

  }

  public static void paintSingleSelection( final IChartComposite chart, final int screenX, final PaintEvent e )
  {
    final ILineStyle lineStyle = new LineStyle( 3, new RGB( 0x54, 0xA7, 0xF9 ), 240, 0F, null, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final Rectangle bounds = RectangleUtils.inflateRect( chart.getPlotInfo().getPlotRect(), lineStyle.getWidth() );
    final PolylineFigure figure = new PolylineFigure();
    figure.setStyle( lineStyle );
    figure.setPoints( new Point[] { new Point( screenX, bounds.y ), new Point( screenX, bounds.y + bounds.height ) } );
    figure.paint( e.gc );
  }

  public static void paintSelection( final IChartComposite chart, final PaintEvent e, final Integer min, final Integer max )
  {
    if( min == null || max == null )
    {
      return;
    }
    if( min - max == 0 )
    {
      SelectionChartHandlerHelper.paintSingleSelection( chart, min, e );
    }
    else
    {
      SelectionChartHandlerHelper.paintRange( chart, e, min, max );
    }
  }

  public static final IProfilChartLayer findProfileTheme( final IChartComposite chart )
  {
    final IChartModel model = chart.getChartModel();
    if( model == null )
      return null;
    final FindLayerVisitor visitor = new FindLayerVisitor( IWspmLayers.THEME_GELAENDE );
    model.getLayerManager().accept( visitor );
    final IChartLayer layer = visitor.getLayer();
    return (IProfilChartLayer)layer;
  }

  public static final IRangeSelection getSelectionFromChart( final IChartComposite chart )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( chart );
    if( theme == null )
    {
      return null;
    }
    final IProfile profile = theme.getProfil();
    return profile.getSelection();
  }

  @SuppressWarnings( "rawtypes" )
  public static final Pair<Integer, Integer> selectionToScreen( final IChartComposite chart )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( chart );
    if( theme == null )
    {
      return null;
    }
    final IProfile profile = theme.getProfil();
    final IRangeSelection selection = profile.getSelection();
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domAxis = mapper.getDomainAxis();
    if( selection.getRange() != null )
    {
      return new Pair<>( domAxis.numericToScreen( selection.getRange().getMinimum() ), domAxis.numericToScreen( selection.getRange().getMaximum() ) );
    }
    return null;

  }

  @SuppressWarnings( "rawtypes" )
  public static final Double getNumericFromScreen( final IChartComposite chart, final int screen )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( chart );
    if( theme == null )
    {
      return null;
    }
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();
    final IProfile profile = theme.getProfil();
    if( profile == null )
      return null;
    if( domainAxis == null )
      return null;
    return domainAxis.screenToNumeric( screen );
  }

  @SuppressWarnings( "rawtypes" )
  public static final Integer cursorToScreen( final IChartComposite chart )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( chart );
    if( theme == null )
      return null;

    final IProfile profile = theme.getProfil();
    final IRangeSelection selection = profile.getSelection();
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domAxis = mapper.getDomainAxis();

    final Double cursor = selection.getCursor();

    if( cursor == null )
      return null;

    return domAxis.numericToScreen( cursor );
  }

  @SuppressWarnings( "rawtypes" )
  public static final void updateCursor( final IChartComposite chart, final int screenX )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( chart );
    if( theme == null )
    {
      return;
    }
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domAxis = mapper.getDomainAxis();
    final IProfile profile = theme.getProfil();
    final Double pointX = domAxis.screenToNumeric( screenX );
    final IRangeSelection selection = profile.getSelection();
    selection.setCursor( pointX );
  }

  public static void paintRange( final IChartComposite chart, final PaintEvent e, final int min, final int max )
  {
    final RGB rgb = new RGB( 0x54, 0xA7, 0xF9 );
    final ILineStyle lineStyle = new LineStyle( 3, rgb, 180, 0F, new float[] { 2, 2, 2 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final Rectangle bounds = RectangleUtils.inflateRect( chart.getPlotInfo().getPlotRect(), lineStyle.getWidth() );
    final int yMax = bounds.y + bounds.height;
    final AreaStyle areaStyle = new AreaStyle( new ColorFill( rgb ), 40, lineStyle, true );
    final PolygonFigure figure = new PolygonFigure();
    figure.setStyle( areaStyle );
    final List<Point> points = new ArrayList<>();
    points.add( new Point( min, bounds.y ) );
    points.add( new Point( min, yMax ) );
    points.add( new Point( max, yMax ) );
    points.add( new Point( max, bounds.y ) );
    figure.setPoints( points.toArray( new Point[] {} ) );
    figure.paint( e.gc );
  }

  public static void paintMouse( final IChartComposite chart, final PaintEvent e, final Integer screenX )
  {
    if( screenX == null )
    {
      return;
    }
    final Integer p1 = screenX;// domAxis.numericToScreen( selection.getCursor() );
    final RGB rgb = new RGB( 0x54, 0xA7, 0xF9 );
    final ILineStyle lineStyle = new LineStyle( 3, rgb, 180, 0F, new float[] { 2, 2, 2 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    final PolylineFigure figure = new PolylineFigure();
    figure.setStyle( lineStyle );
    final Rectangle bounds = RectangleUtils.inflateRect( chart.getPlotInfo().getPlotRect(), lineStyle.getWidth() );
    figure.setPoints( new Point[] { new Point( p1, bounds.y ), new Point( p1, bounds.y + bounds.height ) } );

    figure.paint( e.gc );
  }
}
