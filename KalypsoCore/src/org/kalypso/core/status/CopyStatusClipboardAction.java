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
package org.kalypso.core.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCoreImages;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class CopyStatusClipboardAction extends Action
{
  private final String m_data;

  public CopyStatusClipboardAction( final IStatus status )
  {
    m_data = createClipboardString( status );

    setToolTipText( Messages.getString( "CopyStatusClipboardAction_0" ) ); //$NON-NLS-1$

    final ImageDescriptor image = KalypsoCorePlugin.getImageProvider().getImageDescriptor( KalypsoCoreImages.DESCRIPTORS.STATUS_COPY_CLIPBOARD );
    setImageDescriptor( image );

    setEnabled( !StringUtils.isEmpty( m_data ) );
  }

  static String createClipboardString( final IStatus status )
  {
    if( status == null )
      return null;

    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter( sw );

    printStatus( pw, status, 0 );

    pw.flush();
    return sw.toString();
  }

  private static void printStatus( final PrintWriter pw, final IStatus status, final int level )
  {
    final String severity = StatusUtilities.getLocalizedSeverity( status );
    final String message = status.getMessage();

    final String indent = StringUtils.repeat( " ", level * 4 ); //$NON-NLS-1$

    pw.format( "%s%s: %s%n", indent, severity, message ); //$NON-NLS-1$

    final Throwable exception = status.getException();
    if( exception != null )
    {
      pw.println();
      exception.printStackTrace( pw );
    }

    pw.println();

    final IStatus[] children = status.getChildren();
    for( final IStatus child : children )
    {
      printStatus( pw, child, level + 1 );
      pw.println();
    }

    pw.println();
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Display display = event.widget.getDisplay();
    pasteToClipboard( display );
  }

  private void pasteToClipboard( final Display display )
  {
    if( StringUtils.isEmpty( m_data ) )
      return;

    final Clipboard clipboard = new Clipboard( display );

    try
    {
      final TextTransfer textTransfer = TextTransfer.getInstance();
      final Transfer[] transfers = new Transfer[] { textTransfer };
      final Object[] data = new Object[] { m_data };
      clipboard.setContents( data, transfers );
    }
    finally
    {
      clipboard.dispose();
    }
  }
}