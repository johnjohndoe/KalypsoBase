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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author burtscher1
 */
public class DragPanHandler extends AbstractChartDragHandler
{

  private Point m_keyUp = null;

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#keyReleased(org.eclipse.swt.events.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    try
    {
      mouseUp( m_keyUp );
    }
    finally
    {
      m_keyUp = null;
    }
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#keyPressed(org.eclipse.swt.events.KeyEvent)
   */
  @Override
  public void keyPressed( final KeyEvent e )
  {
    if( e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT )
    {
      if( m_keyUp == null )
      {
        m_keyUp = new Point( 0, 0 );
        mouseDown( m_keyUp );
      }
      if( e.keyCode == SWT.ARROW_LEFT )
        m_keyUp.x -= 10;
      else if( e.keyCode == SWT.ARROW_RIGHT )
        m_keyUp.x += 10;
      mouseMove( m_keyUp );
      getChart().invalidate();
      getChart().getPlot().update();
    }
  }

  public DragPanHandler( final IChartComposite chartComposite, final int trashold, final int observedButtonMask )
  {
    super( chartComposite, trashold, observedButtonMask, SWT.CURSOR_HAND );
  }

  public DragPanHandler( final IChartComposite chartComposite )
  {
    this( chartComposite, SWT.BUTTON_MASK, SWT.BUTTON1 );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseUpAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseUpAction( final Point start, final EditInfo editInfo )
  {
    getChart().setPanOffset( null, null, null );
    if( start != null )
      getChart().getChartModel().panTo( start, editInfo.m_pos );

  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseMoveAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseMoveAction( final Point start, final EditInfo editInfo )
  {
    getChart().setPanOffset( null, start, editInfo.m_pos );
  }

}
