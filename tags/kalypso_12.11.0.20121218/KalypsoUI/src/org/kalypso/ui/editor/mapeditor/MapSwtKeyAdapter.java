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

import org.eclipse.swt.SWT;
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
    final java.awt.event.KeyEvent awtEvent = translateEvent( e, java.awt.event.KeyEvent.KEY_PRESSED );

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
    wm.keyReleased( translateEvent( e, java.awt.event.KeyEvent.KEY_PRESSED ) );
  }

  private java.awt.event.KeyEvent translateEvent( final KeyEvent e, final int id )
  {
    final long when = e.time & 0xFFFFFFFFL;

    final int keyCode = id == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.VK_UNDEFINED : e.keyCode;
    final int translatedKeyCode = translateKeyCode( keyCode );
    final char keyChar = translateCharacter( e.character, translatedKeyCode );

    final int modifiers = translateStateMask( e.stateMask );

    return new java.awt.event.KeyEvent( m_map, id, when, modifiers, translatedKeyCode, keyChar );
  }

  private int translateKeyCode( final int keyCode )
  {
//    if( id == java.awt.event.KeyEvent.KEY_TYPED )
//      return keyCode;

    // REMARK: SWT handles the state maks differently than awt,
    // as the modifier keys are represented in the code instead of the mask when key is pressed
    if( keyCode == SWT.ALT )
      return java.awt.event.KeyEvent.VK_ALT;

    if( keyCode == SWT.SHIFT )
      return java.awt.event.KeyEvent.VK_SHIFT;

    if( keyCode == SWT.CTRL )
      return java.awt.event.KeyEvent.VK_CONTROL;

    if( keyCode == SWT.COMMAND )
      return java.awt.event.KeyEvent.VK_META;

    // REMARK: also some code are just different...
    if( keyCode == SWT.CR )
      return java.awt.event.KeyEvent.VK_ENTER;

    if( keyCode == SWT.INSERT )
      return java.awt.event.KeyEvent.VK_INSERT;

    if( keyCode >= 'a' && keyCode <= 'z' )
      return keyCode - ('a' - 'A');

    return keyCode;
  }

  private char translateCharacter( final char character, final int keyCode )
  {
    // REMARK: in swt, enter is represented as '\r' whereas in awt we get '\n'
    if( character == SWT.CR )
      return java.awt.event.KeyEvent.VK_ENTER;

    if( keyCode == SWT.INSERT )
      return java.awt.event.KeyEvent.VK_INSERT;

    return character;
  }

  private int translateStateMask( final int stateMask )
  {
    int modifiers = 0;

    if( (stateMask & SWT.ALT) != 0 )
      modifiers |= java.awt.event.KeyEvent.ALT_DOWN_MASK;

    if( (stateMask & SWT.SHIFT) != 0 )
      modifiers |= java.awt.event.KeyEvent.SHIFT_DOWN_MASK;

    if( (stateMask & SWT.CTRL) != 0 )
      modifiers |= java.awt.event.KeyEvent.CTRL_DOWN_MASK;

    if( (stateMask & SWT.COMMAND) != 0 )
      modifiers |= java.awt.event.KeyEvent.META_DOWN_MASK;

    return modifiers;
  }
}