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

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.visitors.PanToVisitor;
import de.openali.odysseus.chart.framework.model.impl.visitors.ZoomInVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class ZoomPanMaximizeHandler extends AbstractChartHandler
{
  private static final int m_trashHold = 5;

  public enum DIRECTION
  {
    eHorizontal,
    eVertical,
    eBoth;

    public static DIRECTION getDirection( final String parameter )
    {
      if( parameter == null )
        return eBoth;

      if( "HORIZONTAL".equals( parameter ) )
        return eHorizontal;
      else if( "VERTICAL".equals( parameter ) )
        return eVertical;

      return eBoth;
    }
  }

  private final Set<IChartSelectionChangedListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IChartSelectionChangedListener>() );

  private final DIRECTION m_direction;

  private Point m_startPlot = null;

  private Point m_startPos = null;

  public ZoomPanMaximizeHandler( final IChartComposite chartComposite, final DIRECTION direction )
  {
    super( chartComposite );
    m_direction = direction;
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    setCursor( cursorFromButton( e ) );

    final Point currentPos = EventUtils.getPoint( e );
    m_startPos = currentPos;
    m_startPlot = getChart().screen2plotPoint( currentPos );
  }

  private int cursorFromButton( final MouseEvent e )
  {
    if( EventUtils.isButton1( e ) )
      return SWT.CURSOR_CROSS;

    if( EventUtils.isButton2( e ) )
      return SWT.CURSOR_HAND;

    return -1;
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );

    if( m_startPlot == null )
      return;

    final Point currentPos = EventUtils.getPoint( e );
    final Point currentPlot = getChart().screen2plotPoint( currentPos );

    final boolean isMoved = isMoved( currentPos );
    if( !isMoved )
      return;

    if( EventUtils.isStateButton1( e ) )
    {
      if( EventUtils.isControl( e ) || EventUtils.isShift( e ) )
        doMouseMoveSelection( m_startPlot, currentPlot );
      else
        doMouseMoveZoom( m_startPlot, currentPlot );
    }
    else if( EventUtils.isStateButton2( e ) )
      doMouseMovePan( m_startPlot, currentPlot );
  }

  private boolean isMoved( final Point currentPos )
  {
    if( currentPos == null )
      return false;

    return Math.abs( currentPos.x - m_startPos.x ) > m_trashHold || Math.abs( currentPos.y - m_startPos.y ) > 5;
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
      throw new NotImplementedException();
  }

  protected void doMouseMoveSelection( final Point start, final Point end )
  {
    final Rectangle plotRect = getChart().getPlotRect();
    final int minY = plotRect.y;
    final int height = plotRect.height;

    getChart().setDragArea( new Rectangle( start.x, minY, end.x - start.x, height ) );
  }

  protected void doMouseMoveZoom( final Point start, final Point end )
  {
    getChart().setDragArea( new Rectangle( start.x, start.y, end.x - start.x, end.y - start.y ) );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
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

  private void doMouseUpAction( final MouseEvent e )
  {
    final Point currentPos = EventUtils.getPoint( e );
    final Point currentPlot = getChart().screen2plotPoint( currentPos );

    final boolean isMoved = isMoved( currentPos );
    if( !isMoved )
    {
      if( EventUtils.isStateButton1( e ) )
        fireSelectionChanged( currentPlot );
      return;
    }

    if( EventUtils.isStateButton1( e ) )
    {
      if( Objects.isNotNull( currentPlot ) )
      {
        if( EventUtils.isControl( e ) || EventUtils.isShift( e ) )
          fireSelectionChanged( m_startPlot, currentPlot );
        else
          doMouseUpZoom( m_startPlot, currentPlot ); // zoom
      }
      else
        fireSelectionChanged( m_startPlot );
    }
    else if( EventUtils.isStateButton2( e ) )
    {
      if( Objects.isNotNull( currentPlot ) )
        doMouseUpPan( m_startPlot, currentPlot );
    }
  }

  private void doMouseUpPan( final Point start, final Point end )
  {
    getChart().setPanOffset( null, null, null );
    final PanToVisitor visitor = new PanToVisitor( end, start );

    final IChartModel model = getChart().getChartModel();
    model.getMapperRegistry().accept( visitor );
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
      model.getMapperRegistry().accept( visitor );
    }
  }

  public void addListener( final IChartSelectionChangedListener listener )
  {
    // FIXME: listener never removed
    m_listeners.add( listener );
  }

  private void fireSelectionChanged( final Point... points )
  {
    final IChartSelectionChangedListener[] listeners = m_listeners.toArray( new IChartSelectionChangedListener[] {} );
    for( final IChartSelectionChangedListener listener : listeners )
      listener.selctionChanged( points );
  }
}
