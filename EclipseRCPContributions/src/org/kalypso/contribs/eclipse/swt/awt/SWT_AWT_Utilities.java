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
package org.kalypso.contribs.eclipse.swt.awt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
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
public final class SWT_AWT_Utilities
{
  private SWT_AWT_Utilities( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls {@link MessageDialog#openConfirm(Shell, String, String)} on the currently active shell.<br>
   * This code can be called even outside a SWT thread.
   * 
   * @return The result of the call to {@link MessageDialog#openConfirm(Shell, String, String)}
   */
  public static boolean showSwtMessageBoxConfirm( final String title, final String message )
  {
    final Shell shell = findActiveShell();
    // Force it into SWT-thread
    final boolean[] result = new boolean[1];
    shell.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        result[0] = MessageDialog.openConfirm( shell, title, message );
      }
    } );

    return result[0];
  }

  public static boolean showSwtMessageBoxQuestion( final String title, final String message )
  {
    final Shell shell = findActiveShell();
    // Force it into SWT-thread
    final boolean[] result = new boolean[1];
    shell.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        result[0] = MessageDialog.openQuestion( shell, title, message );
      }
    } );

    return result[0];
  }

  /**
   * Calls {@link Dialog#open()} on the currently active shell.<br>
   * This code can be called even outside a SWT thread.
   * 
   * @return The result of the call to {@link Dialog#open()}
   */
  public static int openSwtWindow( final Window window )
  {
    final Shell shell = findActiveShell();
    // Force it into swt
    final int[] result = new int[1];
    shell.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        result[0] = window.open();
      }
    } );
    return result[0];
  }

  /**
   * Calls {@link Dialog#open()} on the currently active shell.<br>
   * This code can be called even outside a SWT thread.
   * 
   * @return The result of the call to {@link Dialog#open()}
   */
  public static int openSwtWindowAsync( final Window window )
  {
    final Shell shell = findActiveShell();
    // Force it into swt
    final int[] result = new int[1];
    shell.getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        result[0] = window.open();
      }
    } );
    return result[0];
  }

  public static Shell findActiveShell( )
  {
    final IHandlerService service = (IHandlerService)PlatformUI.getWorkbench().getService( IHandlerService.class );
    return (Shell)service.getCurrentState().getVariable( ISources.ACTIVE_SHELL_NAME );
  }

  /**
   * Calls {@link MessageDialog#openInformation(Shell, String, String)} on the currently active shell.<br>
   * This code can be called even outside a SWT thread.
   * 
   * @return The result of the call to {@link MessageDialog#openInformation(Shell, String, String)}
   */
  public static void showSwtMessageBoxInformation( final String title, final String message )
  {
    final Shell shell = findActiveShell();
    // Force it into swt
    shell.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        MessageDialog.openInformation( shell, title, message );
      }
    } );
  }

  public static void showSwtMessageBoxError( final String title, final String message )
  {
    final Shell shell = findActiveShell();
    // Force it into swt
    shell.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        MessageDialog.openError( shell, title, message );
      }
    } );
  }

  public static void showSwtMessageBoxWarning( final String title, final String message )
  {
    final Shell shell = findActiveShell();
    // Force it into swt
    shell.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        MessageDialog.openWarning( shell, title, message );
      }
    } );
  }

  public static Color getSWTFromAWT( final java.awt.Color awtColor, final Display display )
  {
    return new Color( display, awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue() );
  }

}
