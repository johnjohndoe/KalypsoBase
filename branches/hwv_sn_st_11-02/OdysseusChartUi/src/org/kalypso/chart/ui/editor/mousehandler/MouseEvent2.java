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
import org.eclipse.swt.widgets.Event;

/**
 * @author Gernot Belger
 */
public class MouseEvent2 extends MouseEvent
{
  public MouseEvent2( final MouseEvent e )
  {
    super( buildEvent( e ) );
  }

  private static Event buildEvent( final MouseEvent e )
  {
    final Event event = new Event();
    event.x = e.x;
    event.y = e.y;
    event.button = e.button;
    event.stateMask = e.stateMask;
    event.count = e.count;
    event.display = e.display;
    event.widget = e.widget;
    event.time = e.time;
    event.data = e.data;
    event.widget = e.widget;

    return event;
  }

  private boolean isButton( final int buttonMask )
  {
    return (this.stateMask & buttonMask) != 0;
  }

  public boolean isButton1( )
  {
    return isButton( SWT.BUTTON1 );
  }

  public boolean isButton2( )
  {
    return isButton( SWT.BUTTON2 );
  }

  public boolean isButton3( )
  {
    return isButton( SWT.BUTTON3 );
  }

  public boolean isControl( )
  {
    return isState( SWT.CONTROL );
  }

  public boolean isAlt( )
  {
    return isState( SWT.ALT );
  }

  public boolean isShift( )
  {
    return isState( SWT.SHIFT );
  }

  public boolean isCapsLock( )
  {
    return isState( SWT.CAPS_LOCK );
  }

  private boolean isState( final int mask )
  {
    return (this.stateMask & mask) != 0;
  }

  public Point getPoint( )
  {
    return new Point( x, y );
  }
}
