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
package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;

/**
 * Helper that creates a button that will display a menu if clicked.
 * 
 * @author Gernot Belger
 */
public final class MenuButton
{
  private MenuButton( )
  {
    throw new UnsupportedOperationException();
  }

  public static void hookMenu( final Button button, final MenuManager menuManager, final boolean autoDisposeMenu )
  {
    final Menu buttonMenu = menuManager.createContextMenu( button );

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Point displayLocation = button.toDisplay( e.x, e.y );
        buttonMenu.setLocation( displayLocation );
        buttonMenu.setVisible( true );
      }
    } );

    if( autoDisposeMenu )
    {
      button.addDisposeListener( new DisposeListener()
      {
        @Override
        public void widgetDisposed( final DisposeEvent e )
        {
          menuManager.removeAll();
          menuManager.dispose();
        }
      } );
    }
  }

}
