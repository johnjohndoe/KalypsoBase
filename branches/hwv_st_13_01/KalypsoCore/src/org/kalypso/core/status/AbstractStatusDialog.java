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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.core.KalypsoCoreImages;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;

/**
 * @author Dirk Kuch
 */
public class AbstractStatusDialog extends MessageDialog
{
  private final IStatus m_status;

  public AbstractStatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle )
  {
    this( parentShell, status, dialogTitle, new String[] { IDialogConstants.OK_LABEL }, 0 );
  }

  public AbstractStatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle, final String dialogMessage )
  {
    this( parentShell, status, dialogTitle, dialogMessage, toMessageType( status.getSeverity() ), new String[] { IDialogConstants.OK_LABEL }, 0 );
  }

  public AbstractStatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle, final String[] dialogButtonLabels, final int defaultIndex )
  {
    this( parentShell, status, dialogTitle, StringUtils.abbreviate( tweakStatus( status ).getMessage(), 512 ), toMessageType( status.getSeverity() ), dialogButtonLabels, defaultIndex );
  }

  public AbstractStatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle, final String dialogMessage, final int severity, final String[] dialogButtonLabels, final int defaultIndex )
  {
    super( parentShell, dialogTitle, null, dialogMessage, severity, dialogButtonLabels, defaultIndex );

    m_status = tweakStatus( status );
  }

  /**
   * Special handling for some special stati...
   */
  private static IStatus tweakStatus( final IStatus status )
  {
    if( status == Status.CANCEL_STATUS )
      return new Status( IStatus.CANCEL, KalypsoCorePlugin.getID(), Messages.getString( "AbstractStatusDialog_0" ) ); //$NON-NLS-1$

    return status;
  }

  @Override
  public Image getImage( )
  {
    if( m_status != null && m_status.isOK() )
      return KalypsoCorePlugin.getImageProvider().getImage( KalypsoCoreImages.DESCRIPTORS.STATUS_IMAGE_OK_32 );

    return super.getImage();
  }

  protected IStatus getStatus( )
  {
    return m_status;
  }

  private static int toMessageType( final int severity )
  {
    switch( severity )
    {
      case IStatus.OK:
        return MessageDialog.NONE;

      case IStatus.INFO:
        return MessageDialog.INFORMATION;

      case IStatus.WARNING:
        return MessageDialog.WARNING;

      case IStatus.ERROR:
        return MessageDialog.ERROR;

      case IStatus.CANCEL:
        // hm, better cancel icon?
        return MessageDialog.NONE;

      default:
        return MessageDialog.NONE;
    }
  }
}