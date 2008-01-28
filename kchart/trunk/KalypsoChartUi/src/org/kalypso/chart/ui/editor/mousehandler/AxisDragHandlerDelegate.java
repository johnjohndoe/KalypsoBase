/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.view.AxisCanvas;
import org.kalypso.chart.framework.view.ChartComposite;

/**
 * handler to manage zooming all axes of the same orientation
 * 
 * @author burtscher1
 */
public class AxisDragHandlerDelegate implements KeyListener
{

  /**
   * drag end in screen koordinates
   */
  private final int m_mouseDragEnd = -1;

  private final ChartComposite m_chartComposite;

  private final boolean m_applyOnAllAxes = false;

  private IAxisDragHandler m_activeHandler = null;

  private enum DRAG_ACTION
  {
    PAN,
    ZOOM_IN,
    ZOOM_OUT
  };

  private final Map<DRAG_ACTION, IAxisDragHandler> m_handlerMap = new HashMap<DRAG_ACTION, IAxisDragHandler>();

  private final Map<AxisCanvas, IAxis> m_axes = new HashMap<AxisCanvas, IAxis>();

  public AxisDragHandlerDelegate( ChartComposite chartComposite )
  {
    m_chartComposite = chartComposite;

    // zugehörige Achsen rausfinden
    final IMapperRegistry reg = m_chartComposite.getModel().getMapperRegistry();
    final IAxis< ? >[] axes = reg.getAxes();
    for( final IAxis< ? > axis : axes )
    {
      final IAxisComponent component = reg.getComponent( axis );
      if( component != null )
      {
        final AxisCanvas ac = (AxisCanvas) component;
        m_axes.put( ac, axis );
        // selbst als Key-Listener anmelden (zum Umschalten der Aktionen)
        ac.addKeyListener( this );
      }
    }

    // Handler-Map initialisieren
    m_handlerMap.put( DRAG_ACTION.PAN, new AxisDragPanHandler( m_chartComposite ) );
    m_handlerMap.put( DRAG_ACTION.ZOOM_IN, new AxisDragZoomInHandler( m_chartComposite ) );
    m_handlerMap.put( DRAG_ACTION.ZOOM_OUT, new AxisDragZoomOutHandler( m_chartComposite ) );

    // ersten aktiven Handler setzen
    setActiveHandler( m_handlerMap.get( DRAG_ACTION.ZOOM_IN ) );

  }

  public void setActiveHandler( IAxisDragHandler handler )
  {
    for( AxisCanvas ac : m_axes.keySet() )
    {
      if( m_activeHandler != null )
      {
        ac.removeMouseListener( m_activeHandler );
        ac.removeMouseMoveListener( m_activeHandler );
        ac.removeKeyListener( m_activeHandler );
      }

      ac.addKeyListener( handler );
      ac.addMouseListener( handler );
      ac.addMouseMoveListener( handler );
    }

    m_activeHandler = handler;
  }

  public void dispose( )
  {
    for( final AxisCanvas ac : m_axes.keySet() )
    {
      if( !ac.isDisposed() )
      {
        ac.removeMouseListener( m_activeHandler );
        ac.removeMouseMoveListener( m_activeHandler );
        ac.removeKeyListener( m_activeHandler );

      }
    }
    m_chartComposite.removeKeyListener( this );
  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  public Cursor getCursor( )
  {
    return Display.getDefault().getSystemCursor( SWT.CURSOR_CROSS );
  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
   */
  public void keyPressed( KeyEvent e )
  {
    if( e.keyCode == SWT.CTRL )
      setActiveHandler( m_handlerMap.get( DRAG_ACTION.PAN ) );
    else if( e.keyCode == SWT.SHIFT )
      setActiveHandler( m_handlerMap.get( DRAG_ACTION.ZOOM_OUT ) );
  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
   */
  public void keyReleased( KeyEvent e )
  {
    if( e.keyCode == SWT.CTRL )
      setActiveHandler( m_handlerMap.get( DRAG_ACTION.ZOOM_IN ) );
    else if( e.keyCode == SWT.SHIFT )
      setActiveHandler( m_handlerMap.get( DRAG_ACTION.ZOOM_IN ) );

  }

}
