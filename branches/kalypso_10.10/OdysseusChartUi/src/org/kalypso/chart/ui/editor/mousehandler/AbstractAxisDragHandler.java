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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.view.IAxisDragHandler;
import de.openali.odysseus.chart.framework.view.impl.AxisCanvas;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author kimwerner
 */
public abstract class AbstractAxisDragHandler implements IAxisDragHandler
{

  private final ChartComposite m_chartComposite;

  private Point m_mouseDragStart = null;

  private boolean m_isDragging = false;

  private boolean m_applyOnAllAxes = false;

  private IAxis[] m_axes = new IAxis[] {};

  private final int m_trashHold;

  private Cursor m_cursor = null;

  public AbstractAxisDragHandler( final ChartComposite chartComposite )
  {
    this( chartComposite, 5 );
  }

  public AbstractAxisDragHandler( final ChartComposite chartComposite, final int trashHold )
  {
    m_chartComposite = chartComposite;
    m_trashHold = trashHold;
  }

  abstract void doMouseMoveAction( final Point start, final Point end, final IAxis[] axes );

  abstract void doMouseUpAction( final Point start, final Point end, final IAxis[] axes );

  protected final IAxis[] getInvolvedAxis( final IAxis axis )
  {
    if( !isApplyOnAllAxes() )
      return new IAxis[] { axis };
    final ORIENTATION ori = axis.getPosition().getOrientation();
    final List<IAxis> axisList = new ArrayList<IAxis>();

    for( final IAxis ax : m_chartComposite.getChartModel().getMapperRegistry().getAxes() )
      if( ax.getPosition().getOrientation().equals( ori ) )
        axisList.add( axis );

    return axisList.toArray( new IAxis[] {} );
  }

  public ChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  private final void setCursor( final MouseEvent e )
  {
    final Cursor cursor = getCursor( e );
    if( cursor == null )
      return;
    if( e.getSource() instanceof Control )
    {

      if( cursor == m_cursor )
        return;
      m_cursor = cursor;
      ((Control) e.getSource()).setCursor( cursor );
    }
  }

  private IAxis getEventSource( final MouseEvent e )
  {
    if( e.getSource() instanceof AxisCanvas )
      return ((AxisCanvas) e.getSource()).getAxis();
    return null;
  }

  public boolean isApplyOnAllAxes( )
  {
    return m_applyOnAllAxes;
  }

  @Override
  public void keyPressed( KeyEvent e )
  {
    if( e.keyCode == SWT.ALT )
    {
      m_applyOnAllAxes = true;
    }
  }

  @Override
  public void keyReleased( KeyEvent e )
  {
    if( e.keyCode == SWT.ALT )
    {
      m_applyOnAllAxes = false;
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDoubleClick( MouseEvent e )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDown( final MouseEvent e )
  {
    m_mouseDragStart = new Point( e.x, e.y );
    final IAxis axis = getEventSource( e );
    if( axis != null )
      m_axes = getInvolvedAxis( axis );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( MouseEvent e )
  {
    try
    {
      doMouseUpAction( m_mouseDragStart, new Point( e.x, e.y ), m_axes );
    }

    finally
    {
      m_mouseDragStart = null;
      m_axes = new IAxis[] {};
      m_isDragging = false;
    }

  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( MouseEvent e )
  {
    setCursor( e );
    if( m_isDragging )
      doMouseMoveAction( m_mouseDragStart, new Point( e.x, e.y ), m_axes );
    else
      m_isDragging = verifyTrashold( e.x, e.y );

  }

  private boolean verifyTrashold( int x, int y )
  {
    if( m_mouseDragStart == null )
      return false;
    return Math.abs( x - m_mouseDragStart.x ) > m_trashHold || Math.abs( y - m_mouseDragStart.y ) > m_trashHold;
  }

  protected void setApplyOnAllAxes( boolean applyOnAllAxes )
  {
    m_applyOnAllAxes = applyOnAllAxes;
  }
}
