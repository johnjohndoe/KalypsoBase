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

import org.apache.commons.lang3.Range;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.visitors.FindClosestPointVisitor;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;

import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
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
 * @author Dirk Kuch
 */
public abstract class AbstractProfileSelectionChartHandler extends AbstractProfilePointHandler
{
  private Double m_p0 = null;

  private Double m_p1 = null;

  private final boolean m_viewMode;

  /**
   * @param viewMode
   *          don't update selection by left mouse button. only update profile cursor and display profile selection
   */
  public AbstractProfileSelectionChartHandler( final IChartComposite chart, final boolean viewMode )
  {
    super( chart );

    m_viewMode = viewMode;

    if( !m_viewMode )
      super.setCursor( SWT.CURSOR_CROSS );
  }

  @Override
  protected void doMouseMove( final AbstractProfilTheme theme, final Point position )
  {
    final double breite = snapToPoint( getProfile(), position.x );
    if( Double.isNaN( breite ) )
      return;

    final String msg = String.format( Messages.getString( "org.kalypso.model.wspm.ui.commands.AbstractProfileSelectionChartHandler.0" ), getBreite() ); //$NON-NLS-1$
    final EditInfo info = new EditInfo( theme, null, null, getBreite(), msg, position );
    setToolInfo( info );
    final IRangeSelection selection = getProfile().getSelection();
    selection.setCursor( breite );
  }

  private void doPaintCursor( final PaintEvent e, final IProfil profile )
  {
    final IRangeSelection selection = profile.getSelection();
    final Double cursor = selection.getCursor();
    if( Objects.isNull( cursor ) || Double.isNaN( cursor ) )
      return;

    final IChartComposite chart = getChart();
    final AbstractProfilTheme theme = findProfileTheme( chart );
    if( Objects.isNull( theme ) )
      return;

    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();
    final Integer x = domainAxis.numericToScreen( cursor );

    if( isOutOfRange( x ) )
      return;

    final PolylineFigure figure = getHoverFigure( x );
    figure.getStyle().setDash( 0F, new float[] { 2, 2, 2 } );

    figure.paint( e.gc );
  }

  private void doPaintRange( final Range<Double> range, final PaintEvent e )
  {
    final IChartComposite chart = getChart();
    final AbstractProfilTheme theme = findProfileTheme( chart );
    if( Objects.isNull( theme ) )
      return;

    final ICoordinateMapper mapper = theme.getCoordinateMapper();

    final Integer x0 = mapper.getDomainAxis().numericToScreen( range.getMinimum() );
    final Integer x1 = mapper.getDomainAxis().numericToScreen( range.getMaximum() );

    final IPaintable figure = getHoverFigure( x0, x1 );
    figure.paint( e.gc );
  }

  private void doPaintSelection( final PaintEvent e, final IProfil profile )
  {
    final IRangeSelection selection = profile.getSelection();

    final Range<Double> range = selection.getRange();
    if( Objects.isNull( range ) )
      return;

    if( range.getMinimum() == range.getMaximum() )
      doPaintSinglePoint( range.getMinimum(), e );
    else
      doPaintRange( range, e );
  }

  private void doPaintSinglePoint( final Double position, final PaintEvent e )
  {
    final IChartComposite chart = getChart();
    final AbstractProfilTheme theme = findProfileTheme( chart );
    if( Objects.isNull( theme ) )
      return;

    final ICoordinateMapper mapper = theme.getCoordinateMapper();

    final Integer x = mapper.getDomainAxis().numericToScreen( position );
    if( isOutOfRange( x ) )
      return;

    final IPaintable figure = getHoverFigure( x );
    figure.paint( e.gc );
  }

  private PolylineFigure getHoverFigure( final Integer x0 )
  {
    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final PolylineFigure figure = new PolylineFigure();
    final ILineStyle lineStyle = new LineStyle( 3, new RGB( 0x8D, 0xC3, 0xFC ), 180, 0F, null, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    figure.setStyle( lineStyle );

    figure.setPoints( new Point[] { new Point( x0, 0 ), new Point( x0, bounds.y + bounds.height ) } );

    return figure;
  }

  private IPaintable getHoverFigure( final Integer x0, final Integer x1 )
  {
    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final int yMax = bounds.y + bounds.height;

    final RGB rgb = new RGB( 0x8D, 0xC3, 0xFC );

    final ILineStyle lineStyle = new LineStyle( 3, rgb, 180, 0F, new float[] { 2, 2, 2 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );

    final AreaStyle areaStyle = new AreaStyle( new ColorFill( rgb ), 60, lineStyle, true );
    final PolygonFigure figure = new PolygonFigure();
    figure.setStyle( areaStyle );

    final List<Point> points = new ArrayList<Point>();
    points.add( new Point( x0, 0 ) );
    points.add( new Point( x0, yMax ) );
    points.add( new Point( x1, yMax ) );
    points.add( new Point( x1, 0 ) );

    figure.setPoints( points.toArray( new Point[] {} ) );

    return figure;
  }

  private double getSelection( final IProfil profile, final int x )
  {
    final double snap = snapToPoint( profile, x );
    if( Double.isNaN( snap ) )
      return Double.NaN;

    if( Objects.isNotNull( ProfileVisitors.findPoint( profile, snap ) ) )
      return snap;

    return profile.findPreviousPoint( snap ).getBreite();
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    super.mouseDown( e );

    if( m_viewMode )
      return;
    if( e.button != 1 )
      return;

    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final Point position = ChartHandlerUtilities.screen2plotPoint( new Point( e.x, e.y ), bounds );
    if( !isValid( bounds, new Point( e.x, e.y )))//position ) )
    {
      return;
    }

    final IProfil profile = getProfile();

    final double breite = getSelection( profile, position.x );
    if( Double.isNaN( breite ) )
      return;

    if( (e.stateMask & SWT.SHIFT) == 0 )
    {
      m_p0 = breite;
      m_p1 = null;
    }
    else
    {
      if( m_p0 == null )
        m_p0 = breite;
      m_p1 = breite;
    }

    final IRangeSelection selection = profile.getSelection();
    selection.setRange( Range.is( snapToPoint( profile, position.x ) ) );

    selection.setCursor( getBreite() );
  }

  @Override
  public void mouseUp( final MouseEvent e )
  {
    if( m_viewMode )
      return;

    if( (e.stateMask & SWT.SHIFT) == 0 )
      return;

    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final Point position = ChartHandlerUtilities.screen2plotPoint( new Point( e.x, e.y ), bounds );
    if( !isValid( bounds,new Point( e.x, e.y )))// position ) )
    {
      return;
    }

    final AbstractProfilTheme theme = findProfileTheme( chart );
    final ICoordinateMapper mapper = theme.getCoordinateMapper();

    m_p1 = mapper.getDomainAxis().screenToNumeric( position.x ).doubleValue();

    final IProfil profile = getProfile();

    final IRangeSelection selection = profile.getSelection();
    selection.setRange( Range.between( m_p0, m_p1 ) );
  }

  /**
   * @see org.kalypso.model.wspm.ui.commands.AbstractProfilePointHandler#onNewProfileSet()
   */
  @Override
  protected void onNewProfileSet( )
  {
    super.onNewProfileSet();
    m_p0 = null;
    m_p1 = null;
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    super.paintControl( e );

    final IProfil profile = getProfile();
    if( Objects.isNull( profile ) )
      return;

    doPaintSelection( e, profile );
    doPaintCursor( e, profile );

  }

  @Override
  protected void profileChanged( final ProfilChangeHint hint )
  {
    if( !hint.isSelectionChanged() || !hint.isSelectionCursorChanged() )
      return;

    forceRedrawEvent();
  }

  private double snapToPoint( final IProfil profile, final int screenX )
  {
    if( profile == null )
      return Double.NaN;

    final AbstractProfilTheme theme = findProfileTheme( getChart() );
    if( theme == null )
      return Double.NaN;

    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domainAxis = mapper.getDomainAxis();

    final Number xPosition = domainAxis.screenToNumeric( screenX );
    final Number xMin = domainAxis.screenToNumeric( screenX - 5 );
    final Number xMax = domainAxis.screenToNumeric( screenX + 5 );

    final FindClosestPointVisitor visitor = new FindClosestPointVisitor( xPosition.doubleValue() );
    profile.accept( visitor, 1 );

    final IProfileRecord point = visitor.getPoint();
    final Range<Double> range = Range.between( xMin.doubleValue(), xMax.doubleValue() );

    if( range.contains( point.getBreite() ) )
      return point.getBreite();

    return xPosition.doubleValue();
  }
}
