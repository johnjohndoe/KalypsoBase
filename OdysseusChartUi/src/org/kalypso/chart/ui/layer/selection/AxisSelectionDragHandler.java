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
package org.kalypso.chart.ui.layer.selection;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author kuch
 */
public class AxisSelectionDragHandler extends AbstractChartDragHandler implements MouseListener
{
  private final AxisSelectionLayer m_layer;

  public AxisSelectionDragHandler( final IChartComposite chart, final AxisSelectionLayer layer )
  {
    super( chart );

    m_layer = layer;
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseMoveAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseMoveAction( final Point point, final EditInfo editInfo )
  {
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#doMouseUpAction(org.eclipse.swt.graphics.Point,
   *      de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseUpAction( final Point end, final EditInfo editInfo )
  {

  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    m_layer.setMousePosition( new Point( e.x, e.y ) );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartDragHandler#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
    final Point point = new Point( e.x, e.y );

    final IAxis[] axes = m_layer.getAxes();
    for( final IAxis axis : axes )
    {
      axis.setSelection( point );
    }
  }

}
