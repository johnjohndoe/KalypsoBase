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
package org.kalypso.contribs.eclipse.swt.awt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Helper class for SWT-AWT wrapping.
 * 
 * @author Gernot Belger
 */
public class SWT_AWT_Utilities
{
  /**
   * Calls {@link MessageDialog#openConfirm(Shell, String, String)} on the currently active shell.<br>
   * This code can be called even outside a SWT thread.
   * 
   * @return The result of the call to {@link MessageDialog#openConfirm(Shell, String, String)}
   */
  public static boolean showSwtMessageBoxConfirm( final String title, final String message )
  {
    final IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getService( IHandlerService.class );
    final Shell shell = (Shell) service.getCurrentState().getVariable( ISources.ACTIVE_SHELL_NAME );
    // Force it into SWT-thread
    final boolean[] result = new boolean[1];
    shell.getDisplay().syncExec( new Runnable()
    {
      public void run( )
      {
        result[0] = MessageDialog.openConfirm( shell, title, message );
      }
    } );

    return result[0];
  }

  public static boolean showSwtMessageBoxQuestion( final String title, final String message )
  {
    final IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getService( IHandlerService.class );
    final Shell shell = (Shell) service.getCurrentState().getVariable( ISources.ACTIVE_SHELL_NAME );
    // Force it into SWT-thread
    final boolean[] result = new boolean[1];
    shell.getDisplay().syncExec( new Runnable()
    {
      public void run( )
      {
        result[0] = MessageDialog.openQuestion( shell, title, message );
      }
    } );

    return result[0];
  }

  /**
   * Calls {@link MessageDialog#openInformation(Shell, String, String)} on the currently active shell.<br>
   * This code can be called even outside a SWT thread.
   * 
   * @return The result of the call to {@link MessageDialog#openInformation(Shell, String, String)}
   */
  public static void showSwtMessageBoxInformation( final String title, final String message )
  {
    final IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getService( IHandlerService.class );
    final Shell shell = (Shell) service.getCurrentState().getVariable( ISources.ACTIVE_SHELL_NAME );
    // Force it into swt
    shell.getDisplay().syncExec( new Runnable()
    {
      public void run( )
      {
        MessageDialog.openInformation( shell, title, message );
      }
    } );
  }

  public static Color getSWTFromAWT( final java.awt.Color awtColor, final Display display )
  {
    return new Color( display, awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue() );
  }
}
