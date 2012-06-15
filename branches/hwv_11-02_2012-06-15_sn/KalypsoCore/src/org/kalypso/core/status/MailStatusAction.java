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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.kalypso.core.KalypsoCoreImages;
import org.kalypso.core.KalypsoCorePlugin;

/**
 * @author Gernot Belger
 */
public class MailStatusAction extends Action
{
  private final String m_data;

  public MailStatusAction( final IStatus status )
  {
    m_data = CopyStatusClipboardAction.createClipboardString( status );

    setToolTipText( "Send message via email" );

    final ImageDescriptor image = KalypsoCorePlugin.getImageProvider().getImageDescriptor( KalypsoCoreImages.DESCRIPTORS.STATUS_EMAIL );
    setImageDescriptor( image );

    setEnabled( !StringUtils.isEmpty( m_data ) );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Display display = event.widget.getDisplay();
    doMail( display );
  }

  private void doMail( final Display display )
  {
    if( StringUtils.isEmpty( m_data ) )
      return;

    MessageDialog.openWarning( display.getActiveShell(), "Send Mail", "Sorry, not yet implemented." );

    // open mail program with status text; how is this possible on all platforms?

    // Outlook: \outlook.exe /c ipm.note /m enmail@sender.com /a C:\attachedFile.txt

    // start mailto:<E-Mail An:>?cc=<E-Mail Cc:>&bcc=<E-Mail Bcc:>&subject=<Betreff>&body=<Nachricht>
    // see http://www.ianr.unl.edu/internet/mailto.html
  }
}