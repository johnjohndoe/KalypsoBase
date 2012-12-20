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
package org.kalypso.ui.editor.mapeditor;

import java.awt.event.MouseWheelEvent;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.widgets.WidgetManager;

/**
 * @author Gernto Belger
 */
// REMARK/BUGFIX: the above fix gives another problem, that mouse wheel events are not handled any more, because they
// only go to the currently focused window. So we need to directly transfer the focus event from the parent
// component to the widget manager.
public class MapSwtWheelAdapter implements MouseWheelListener
{
  private final MapPanel m_map;

  public MapSwtWheelAdapter( final MapPanel map )
  {
    m_map = map;
  }

  @Override
  public void mouseScrolled( final MouseEvent e )
  {
    final long time = e.time & Long.MAX_VALUE;
    final int modifiers = 0; // TODO: translate e.stateMask
    final boolean popupTrigger = false;

    final int scrollAmount = 1;
    final int scrollType = MouseWheelEvent.WHEEL_UNIT_SCROLL;
    final int clickCount = 0;

    // REMARK: swt always multiplied by 3? At least 1 times wheeled
    final int wheelRotation = -Math.min( 1, e.count / 3 );

    final MouseWheelEvent wheelEvent = new MouseWheelEvent( m_map, MouseWheelEvent.MOUSE_WHEEL, time, modifiers, e.x, e.y, clickCount, popupTrigger, scrollType, scrollAmount, wheelRotation );

    // REMARK: directly dispatch it to the widget manager; triggering it with mappanel#dispatchEvent will lead to a endless loop
    final WidgetManager wm = (WidgetManager)m_map.getWidgetManager();
    wm.mouseWheelMoved( wheelEvent );
  }
}