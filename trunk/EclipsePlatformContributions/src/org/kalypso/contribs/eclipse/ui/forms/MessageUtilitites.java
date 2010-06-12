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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.widgets.Form;

/**
 * Helper methods for {@link org.eclipse.ui.forms.IMessage}.
 * 
 * @author Gernot Belger
 */
public class MessageUtilitites
{
  /**
   * {@link IMessage} implementation based on an {@link IStatus}.
   */
  public static class StatusMessage implements IMessage
  {
    private final IStatus m_status;
    private final Object m_key;

    public StatusMessage( final Object key, final IStatus status )
    {
      m_key = key;
      m_status = status;
    }

    /**
     * @see org.eclipse.ui.forms.IMessage#getControl()
     */
    @Override
    public Control getControl( )
    {
      return null;
    }

    /**
     * @see org.eclipse.ui.forms.IMessage#getData()
     */
    @Override
    public Object getData( )
    {
      return m_status;
    }

    /**
     * @see org.eclipse.ui.forms.IMessage#getKey()
     */
    @Override
    public Object getKey( )
    {
      return m_key;
    }

    /**
     * @see org.eclipse.ui.forms.IMessage#getPrefix()
     */
    @Override
    public String getPrefix( )
    {
      return null;
    }

    /**
     * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
     */
    @Override
    public String getMessage( )
    {
      return m_status.getMessage();
    }

    /**
     * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
     */
    @Override
    public int getMessageType( )
    {
      return convertStatusSeverity( m_status.getSeverity() );
    }
  }

  private MessageUtilitites( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * Converts an {@link IStatus} to an {@link org.eclipse.ui.forms.IMessage} (including its children) and sets it a
   * message to the given form.
   */
  public static void setMessage( final Form form, final IStatus status )
  {
    if( form.isDisposed() )
      return;

    final IMessage msg = convertStatus( status );
    final IMessage[] msgChildren = convertStatus( status.getChildren() );
    form.setMessage( msg.getMessage(), msg.getMessageType(), msgChildren );
  }

  private static IMessage[] convertStatus( final IStatus[] children )
  {
    if( children == null )
      return null;

    final IMessage[] msgs = new IMessage[children.length];
    for( int i = 0; i < msgs.length; i++ )
      msgs[i] = convertStatus( children[i] );
    return msgs;
  }

  private static IMessage convertStatus( final IStatus status )
  {
    return new StatusMessage( status, status );
  }

  /**
   * Converts the severity of an {@link IStatus} to the mesage type of an {@link IMessage}.
   * 
   * @see IStatus#getSeverity()
   * @see IMessage#getMessageType()
   */
  public static int convertStatusSeverity( final int severity )
  {
    switch( severity )
    {
      case IStatus.OK:
        return IMessage.NONE;
      case IStatus.INFO:
        return IMessage.INFORMATION;
      case IStatus.WARNING:
        return IMessage.WARNING;
      case IStatus.ERROR:
        return IMessage.ERROR;
      case IStatus.CANCEL:
        return IMessage.INFORMATION;

      default:
        throw new IllegalArgumentException( "Unknown status severity: " + severity ); //$NON-NLS-1$
    }

  }


}
