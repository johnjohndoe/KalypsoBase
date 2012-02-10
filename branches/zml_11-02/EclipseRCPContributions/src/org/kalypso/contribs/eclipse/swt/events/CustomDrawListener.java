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
package org.kalypso.contribs.eclipse.swt.events;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.util.Util;

/**
 * A listener for the table/tree custom draw events.
 * <p>
 * Tries to delegate all events to a {@link ICustomDrawListener} by delegating the event item data to it.
 * </p>
 * 
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class CustomDrawListener implements Listener
{
  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void handleEvent( final Event event )
  {
    final Object treeData = event.item.getData();
    final ICustomDrawHandler customDraw = (ICustomDrawHandler) Util.getAdapter( treeData, ICustomDrawHandler.class );
    if( customDraw == null )
      return;

    switch( event.type )
    {
      case SWT.MeasureItem:
        customDraw.measureItem( event );
        break;

      case SWT.EraseItem:
        customDraw.eraseItem( event );
        break;

      case SWT.PaintItem:
        customDraw.paintItem( event );
        break;

      default:
        throw new IllegalArgumentException( "Listener only suitable for custom draw events." );
    }
  }

}
