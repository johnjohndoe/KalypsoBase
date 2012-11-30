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

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.widgets.WidgetManager;

/**
 * @author Gernot Belger
 */
class MapSwtKeyAdapter implements KeyListener
{
  private final MapPanel m_map;

  public MapSwtKeyAdapter( final MapPanel map )
  {
    m_map = map;
  }

  @Override
  public void keyPressed( final KeyEvent e )
  {
    final java.awt.event.KeyEvent awtEvent = translateEvent( e, java.awt.event.KeyEvent.KEY_RELEASED );

    // REMARK: directly dispatch it to the widget manager; triggering it with mappanel#dispatchEvent will lead to a endless loop
    final WidgetManager wm = (WidgetManager)m_map.getWidgetManager();
    wm.keyPressed( awtEvent );
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
    // REMARK: directly dispatch it to the widget manager; triggering it with mappanel#dispatchEvent will lead to a endless loop
    final WidgetManager wm = (WidgetManager)m_map.getWidgetManager();

    wm.keyTyped( translateEvent( e, java.awt.event.KeyEvent.KEY_TYPED ) );
    wm.keyPressed( translateEvent( e, java.awt.event.KeyEvent.KEY_PRESSED ) );
  }

  private java.awt.event.KeyEvent translateEvent( final KeyEvent e, final int id )
  {
    final long when = e.time & 0xFFFFFFFFL;
    final int modifiers = e.stateMask;

    final int keyCode = id == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.VK_UNDEFINED : e.keyCode;
    final char keyChar = e.character;

    return new java.awt.event.KeyEvent( m_map, id, when, modifiers, keyCode, keyChar );
  }
}