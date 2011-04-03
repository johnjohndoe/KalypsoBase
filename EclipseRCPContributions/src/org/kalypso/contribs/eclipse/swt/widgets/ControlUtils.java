/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Common helper code for {@link org.eclipse.swt.widgets.Control}.
 * 
 * @author Gernot Belger
 */
public final class ControlUtils
{
  private ControlUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate." );
  }

  public static void asyncExec( final Control control, final Runnable operation )
  {
    if( control == null )
      return;

    if( control.isDisposed() )
      return;

    final Display display = control.getDisplay();
    if( display.isDisposed() )
      return;

    display.asyncExec( operation );
  }

  public static void syncExec( final Control control, final Runnable operation )
  {
    if( control == null )
      return;

    if( control.isDisposed() )
      return;

    final Display display = control.getDisplay();
    if( display.isDisposed() )
      return;

    display.syncExec( operation );
  }

  /**
   * Disposes all children of a composite.
   */
  public static void disposeChildren( final Composite parent )
  {
    if( parent == null || parent.isDisposed() )
      return;

    final Control[] children = parent.getChildren();
    for( final Control control : children )
      control.dispose();
  }

  /**
   * Adapts a {@link Control} to a {@link FormToolkit}, but handles the case that the toolkit is <code>null</code>.
   */
  public static void adapt( final Control control, final FormToolkit toolkit )
  {
    if( toolkit == null )
      return;

    if( control instanceof Composite )
      toolkit.adapt( (Composite) control );
    else
      toolkit.adapt( control, true, true );
  }

  public static Event copyEvent( final Event event )
  {
    final Event cEvent = new Event();
    cEvent.button = event.button;
    cEvent.character = event.character;
    cEvent.count = event.count;
    cEvent.data = event.data;
    cEvent.detail = event.detail;
    cEvent.display = event.display;
    cEvent.doit = event.doit;
    cEvent.end = event.end;
    cEvent.gc = event.gc;
    cEvent.height = event.height;
    cEvent.index = event.index;
    cEvent.item = event.item;
    cEvent.keyCode = event.keyCode;
    cEvent.keyLocation = event.keyLocation;
    cEvent.start = event.start;
    cEvent.stateMask = event.stateMask;
    cEvent.text = event.text;
    cEvent.time = event.time;
    cEvent.type = event.type;
    cEvent.widget = event.widget;
    cEvent.width = event.width;
    cEvent.x = event.x;
    cEvent.y = event.y;

    return cEvent;
  }

  public static Event toEvent( final KeyEvent e, final int type )
  {
    Assert.isTrue( type == SWT.KeyDown || type == SWT.KeyUp );

    final Event event = new Event();

    event.button = 0;
    event.character = e.character;
    event.count = -1;
    event.data = e.data;
    event.detail = -1;
    event.display = e.display;
    event.doit = e.doit;
    event.end = -1;
    event.gc = null;
    event.height = -1;
    event.index = -1;
    event.item = null;
    event.keyCode = e.keyCode;
    event.keyLocation = e.keyLocation;
    event.start = -1;
    event.stateMask = e.stateMask;
    event.text = null;
    event.time = e.time;
    event.type = type;
    event.widget = e.widget;
    event.width = -1;
    event.x = -1;
    event.y = -1;

    return event;
  }

}
