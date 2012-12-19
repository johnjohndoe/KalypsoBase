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
package org.kalypso.mt.input;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Calendar;

/**
 * @author Dirk Kuch
 *
 */
public final  class MouseEvents
{
  private MouseEvents()
  {
  }

  /**
   * FIXME - generated mouse events are correct? 
   */
  public static MouseEvent toMouseEvent(  Point point )
  {

    Long id = Calendar.getInstance().getTime().getTime();
    
    int modifiers = 0;
    int x = point.x;
    int y = point.y;
    int xAbs = x;
    int yAbs = y;
    int clickCount = 1;
    boolean popupTrigger = false;
    int button = MouseEvent.BUTTON1;
    
    return new MouseEvent( null, id.intValue(), id, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, button );
  }
}
