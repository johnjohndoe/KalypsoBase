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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.visitors.PanToVisitor;
import de.openali.odysseus.chart.framework.model.impl.visitors.ZoomInVisitor;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class ZoomPanMaximizeHandler extends AbstractChartDragHandler
{
  private int m_button = -1;

  public ZoomPanMaximizeHandler( final IChartComposite chartComposite )
  {
    super( chartComposite, 5, SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3, SWT.CURSOR_ARROW );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseMoveAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseMoveAction( final Point end, final EditInfo editInfo )
  {
    if( m_button == SWT.BUTTON1 )
    {
      doMouseMoveZoom( end, editInfo );
    }
    else if( m_button == SWT.BUTTON2 )
    {
      doMouseMovePan( end, editInfo );
    }

  }

  private void doMouseMovePan( final Point start, final EditInfo editInfo )
  {
    getChart().setPanOffset( null, start, editInfo.m_pos );
  }

  private void doMouseMoveZoom( final Point end, final EditInfo editInfo )
  {
    getChart().setDragArea( new Rectangle( editInfo.m_pos.x, editInfo.m_pos.y, end.x - editInfo.m_pos.x, end.y - editInfo.m_pos.y ) );
  }

  private void doMouseMaximize( )
  {
    getChart().getChartModel().autoscale();
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseUpAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseUpAction( final Point end, final EditInfo editInfo )
  {
    if( m_button == SWT.BUTTON1 )
    {
      doMouseUpZoom( end, editInfo );
    }
    else if( m_button == SWT.BUTTON2 )
    {
      doMouseUpPan( end, editInfo );
    }
    else if( m_button == SWT.BUTTON3 )
    {
      doMouseMaximize();
    }
  }

  private void doMouseUpPan( final Point start, final EditInfo editInfo )
  {
    getChart().setPanOffset( null, null, null );
    if( start != null )
    {
      final PanToVisitor visitor = new PanToVisitor( start, editInfo.m_pos );

      final IChartModel model = getChart().getChartModel();
      model.getMapperRegistry().accept( visitor );
    }

    /** update active point */
    updateSelection( editInfo );
  }

  private void doMouseUpZoom( final Point end, final EditInfo editInfo )
  {

    if( end == null )
      return;
    try
    {
      if( end.x < editInfo.m_pos.x )
        getChart().getChartModel().autoscale();
      else
      {
        final ZoomInVisitor visitor = new ZoomInVisitor( editInfo.m_pos, end );

        final IChartModel model = getChart().getChartModel();
        model.getMapperRegistry().accept( visitor );
      }
    }
    finally
    {
      getChart().setDragArea( null );
    }
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDown( final MouseEvent e )
  {
    m_button = button2Mask( e.button );
    super.mouseDown( e );
  }
}
