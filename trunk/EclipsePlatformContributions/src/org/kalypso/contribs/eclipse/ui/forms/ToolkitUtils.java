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
package org.kalypso.contribs.eclipse.ui.forms;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Helper class for {@link org.eclipse.ui.forms.widgets.FormToolkit} related stuff.
 * 
 * @author Gernot Belger
 */
public final class ToolkitUtils
{
  private ToolkitUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /**
   * Create a {@link FormToolkit} on the display of a control.<br/>
   * The toolkit will be automatically disposed when the control is disposed.
   */
  public static FormToolkit createToolkit( final Control control )
  {
    final boolean isWindows = Platform.getOS().equals( Platform.OS_WIN32 );
    // FIXME: how to decide this?! maybe use system property?
    final boolean classicWindows = false;

    final FormToolkit toolkit = new FormToolkit( control.getDisplay() )
    {
      @Override
      public Button createButton( final Composite buttonParent, final String text, final int style )
      {
        if( !isWindows )
          return super.createButton( buttonParent, text, style );

        // Suppress flat style: this works for all windowses
        final Button button = new Button( buttonParent, style | Window.getDefaultOrientation() );
        if( text != null )
          button.setText( text );
        adapt( button, true, true );
        return button;
      }
    };

    if( classicWindows )
      toolkit.setBackground( null );

    control.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        toolkit.dispose();
      }
    } );

    return toolkit;
  }
}
