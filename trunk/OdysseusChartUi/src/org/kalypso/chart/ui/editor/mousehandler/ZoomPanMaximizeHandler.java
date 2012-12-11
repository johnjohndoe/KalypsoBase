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
package org.kalypso.chart.ui.editor.mousehandler;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.visitors.FindLayerTooltipVisitor;
import de.openali.odysseus.chart.framework.model.impl.visitors.PanToVisitor;
import de.openali.odysseus.chart.framework.model.impl.visitors.SetActivePointVisitor;
import de.openali.odysseus.chart.framework.model.impl.visitors.ZoomInVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZoomPanMaximizeHandler extends AbstractChartHandler
{
  public enum DIRECTION
  {
    eHorizontal,
    eVertical,
    eBoth;

    public static DIRECTION getDirection( final String parameter )
    {
      if( parameter == null )
        return eBoth;

      if( "HORIZONTAL".equals( parameter ) ) //$NON-NLS-1$
        return eHorizontal;
      else if( "VERTICAL".equals( parameter ) ) //$NON-NLS-1$
        return eVertical;

      return eBoth;
    }
  }

  private static final int m_trashold = 5;

  private final Set<IChartSelectionChangedListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IChartSelectionChangedListener>() );

  private final DIRECTION m_direction;

  private Point m_startPlot = null;

  private Point m_startPos = null;

  public ZoomPanMaximizeHandler( final IChartComposite chartComposite, final DIRECTION direction )
  {
    super( chartComposite );
    m_direction = direction;
  }

  public void addListener( final IChartSelectionChangedListener listener )
  {
    // FIXME: listener never removed
    m_listeners.add( listener );
  }

  private int cursorFromButton( final MouseEvent e )
  {
    if( EventUtils.isButton1( e ) )
      return SWT.CURSOR_CROSS;

    if( EventUtils.isButton2( e ) )
      return SWT.CURSOR_SIZEALL;

    return -1;
  }

  protected void doMouseMovePan( final Point start, final Point end )
  {
    if( DIRECTION.eBoth.equals( m_direction ) )
    {
      getChart().setPanOffset( null, end, start );
    }
    else if( DIRECTION.eHorizontal.equals( m_direction ) )
    {
      final Point otherStart = new Point( start.x, end.y );
      getChart().setPanOffset( null, end, otherStart );
    }
    else
      throw new UnsupportedOperationException();
  }

  protected void doMouseMoveSelection( final Point start, final Point end )
  {
    final Rectangle plotRect = getChart().getPlotInfo().getPlotRect();
    final int minY = plotRect.y;
    final int height = plotRect.height;

    getChart().setDragArea( new Rectangle( start.x, minY, end.x - start.x, height ) );
  }

  protected void doMouseMoveZoom( final Point start, final Point end )
  {
    getChart().setDragArea( new Rectangle( start.x, start.y, end.x - start.x, end.y - start.y ) );
  }

  private void doMouseUpAction( final MouseEvent e )
  {
    final Point currentPos = EventUtils.getPoint( e );
    // final Point currentPlot = ChartHandlerUtilities.screen2plotPoint( currentPos, getChart().getPlotRect() );

    final boolean isMoved = isMoved( currentPos );
    if( !isMoved )
    {
      if( EventUtils.isStateButton1( e ) )
        fireSelectionChanged( currentPos );
      doMouseUpClick( m_startPlot );
      return;
    }

    if( EventUtils.isStateButton1( e ) )
    {
      if( Objects.isNotNull( currentPos ) )
      {
        if( EventUtils.isShift(  e ) )
          fireSelectionChanged( m_startPlot, currentPos );
        else if( EventUtils.isControl( e ) )
          doMouseUpAxisZoom( m_startPlot, currentPos );
        else
          doMouseUpZoom( m_startPlot, currentPos ); // zoom
      }
      else
        fireSelectionChanged( m_startPlot );
    }
    else if( EventUtils.isStateButton2( e ) )
    {
      if( Objects.isNotNull( currentPos ) )
        doMouseUpPan( m_startPlot, currentPos );
    }
  }

  private void doMouseUpClick( final Point start )
  {
    final SetActivePointVisitor visitor = new SetActivePointVisitor( start, 10 );

    final IChartModel model = getChart().getChartModel();
    model.getAxisRegistry().accept( visitor );
  }

  private void doMouseUpPan( final Point start, final Point end )
  {
    getChart().setPanOffset( null, null, null );
    final PanToVisitor visitor = new PanToVisitor( end, start );

    final IChartModel model = getChart().getChartModel();
    model.getAxisRegistry().accept( visitor );
  }

  private void doMouseUpZoom( final Point start, final Point end )
  {
    if( Objects.isNull( start, end ) )
      return;

    if( end.x < start.x )
      getChart().getChartModel().autoscale();
    else
    {
      final ZoomInVisitor visitor = new ZoomInVisitor( start, end );

      final IChartModel model = getChart().getChartModel();
      model.getAxisRegistry().accept( visitor );
    }
  }

  private void doMouseUpAxisZoom( final Point start, final Point end )
  {
    if( Objects.isNull( start, end ) )
      return;

    if( end.x < start.x )
      getChart().getChartModel().autoscale();
    else
    {
      final Rectangle plotRect = getChart().getPlotInfo().getPlotRect();
      final int y1 = plotRect.y;
      final int y2 = y1 + plotRect.height;
      final ZoomInVisitor visitor = new ZoomInVisitor( new Point( start.x, y1 ), new Point( end.x, y2 ) );
      final IChartModel model = getChart().getChartModel();
      model.getAxisRegistry().accept( visitor );
    }
  }

  private void doSetTooltip( final Point point )
  {
    final FindLayerTooltipVisitor visitor = new FindLayerTooltipVisitor( getChart(), point );
    final IChartModel model = getChart().getChartModel();
    model.accept( visitor );

    setToolInfo( visitor.getEditInfo() );
  }

  private void fireSelectionChanged( final Point... points )
  {
    final IChartSelectionChangedListener[] listeners = m_listeners.toArray( new IChartSelectionChangedListener[] {} );
    for( final IChartSelectionChangedListener listener : listeners )
      listener.selctionChanged( points );
  }

  private boolean isMoved( final Point currentPos )
  {
    return Math.abs( currentPos.x - m_startPos.x ) > m_trashold || Math.abs( currentPos.y - m_startPos.y ) > 5;
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    setCursor( cursorFromButton( e ) );

    final Point currentPos = EventUtils.getPoint( e );

    m_startPos = currentPos;
    m_startPlot = currentPos;
  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );

    final Point currentPos = EventUtils.getPoint( e );
    setToolInfo( null );

    if( m_startPlot == null )
    {
      doSetTooltip( currentPos );
      return;
    }

    final boolean isMoved = isMoved( currentPos );
    if( !isMoved )
      return;

    if( EventUtils.isStateButton1( e ) )
    {
      if( EventUtils.isControl( e ) || EventUtils.isShift( e ) )
        doMouseMoveSelection( m_startPlot, currentPos );

      else
        doMouseMoveZoom( m_startPlot, currentPos );
    }
    else if( EventUtils.isStateButton2( e ) )
      doMouseMovePan( m_startPlot, currentPos );
  }

  @Override
  public void mouseUp( final MouseEvent e )
  {
    if( m_startPlot == null )
      return;

    try
    {
      doMouseUpAction( e );
    }
    finally
    {
      setCursor( SWT.CURSOR_ARROW );

      m_startPlot = null;

      final IChartComposite chart = getChart();
      chart.setDragArea( null );
    }
  }
}
