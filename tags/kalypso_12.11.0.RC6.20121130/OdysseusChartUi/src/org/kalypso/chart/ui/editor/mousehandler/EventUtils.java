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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Point;

/**
 * TODO: improve and move to common place.
 * 
 * @author Gernot Belger
 */
public final class EventUtils
{
  private EventUtils( )
  {
    throw new UnsupportedOperationException();
  }

  public static boolean isButton( final TypedEvent e, final int buttonMask )
  {
    final int stateMask = getStateMask( e );
    return (stateMask & buttonMask) != 0;
  }

  public static int getStateMask( final TypedEvent e )
  {
    if( e instanceof MouseEvent )
      return ((MouseEvent) e).stateMask;

    if( e instanceof KeyEvent )
      return ((KeyEvent) e).stateMask;

    return 0;
  }

  private static int getButton( final TypedEvent e )
  {
    if( e instanceof MouseEvent )
      return ((MouseEvent) e).button;

    return 0;
  }

  public static boolean isButton1( final TypedEvent e )
  {
    final int button = getButton( e );
    return button == 1;
  }

  public static boolean isButton2( final TypedEvent e )
  {
    final int button = getButton( e );
    return button == 2;
  }

  public static boolean isButton3( final TypedEvent e )
  {
    final int button = getButton( e );
    return button == 3;
  }

  public static boolean isStateButton1( final TypedEvent e )
  {
    return isButton( e, SWT.BUTTON1 );
  }

  public static boolean isStateButton2( final TypedEvent e )
  {
    return isButton( e, SWT.BUTTON2 );
  }

  public static boolean isStateButton3( final TypedEvent e )
  {
    return isButton( e, SWT.BUTTON3 );
  }

  public static boolean isControl( final TypedEvent e )
  {
    return isState( e, SWT.CONTROL );
  }

  public static boolean isAlt( final TypedEvent e )
  {
    return isState( e, SWT.ALT );
  }

  public static boolean isShift( final TypedEvent e )
  {
    return isState( e, SWT.SHIFT );
  }

  public static boolean isCapsLock( final TypedEvent e )
  {
    return isState( e, SWT.CAPS_LOCK );
  }

  public static boolean isState( final TypedEvent e, final int mask )
  {
    final int stateMask = getStateMask( e );
    return (stateMask & mask) != 0;
  }

  public static Point getPoint( final MouseEvent e )
  {
    return new Point( e.x, e.y );
  }
}
