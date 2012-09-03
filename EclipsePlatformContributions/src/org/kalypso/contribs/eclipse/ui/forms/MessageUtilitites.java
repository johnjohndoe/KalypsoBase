/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.ui.forms;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.widgets.Form;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;

/**
 * Helper methods for {@link org.eclipse.ui.forms.IMessage}.
 * 
 * @author Gernot Belger
 */
public final class MessageUtilitites
{
  /**
   * {@link IMessage} implementation based on an {@link IStatus}.
   */
  public static class StatusMessage implements IMessage
  {
    private final IStatus m_status;

    private final Object m_key;

    private final Control m_control;

    public StatusMessage( final Object key, final IStatus status )
    {
      this( key, status, null );
    }

    public StatusMessage( final Object key, final IStatus status, final Control control )
    {
      m_key = key;
      m_status = status;
      m_control = control;
    }

    /**
     * @see org.eclipse.ui.forms.IMessage#getControl()
     */
    @Override
    public Control getControl( )
    {
      return m_control;
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
   * message to the given form.<br/>
   * If the status isOK(), the message will be cleared.
   */
  public static void setMessage( final Form form, final IStatus status )
  {
    if( form.isDisposed() )
      return;

    if( status.isOK() )
    {
      form.setMessage( null );
      return;
    }

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

  public static IMessage convertStatus( final IStatus status )
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
        return IMessageProvider.NONE;
      case IStatus.INFO:
        return IMessageProvider.INFORMATION;
      case IStatus.WARNING:
        return IMessageProvider.WARNING;
      case IStatus.ERROR:
        return IMessageProvider.ERROR;
      case IStatus.CANCEL:
        return IMessageProvider.INFORMATION;

      default:
        throw new IllegalArgumentException( "Unknown status severity: " + severity ); //$NON-NLS-1$
    }
  }

  /**
   * Converts the type of an {@link IMessage} to the severity of an {@link IStatus}.
   * 
   * @see IStatus#getSeverity()
   * @see IMessage#getMessageType()
   */
  public static int convertMessageSeverity( final int type )
  {
    switch( type )
    {
      case IMessageProvider.NONE:
        return IStatus.OK;

      case IMessageProvider.INFORMATION:
        return IStatus.INFO;
      case IMessageProvider.WARNING:
        return IStatus.WARNING;
      case IMessageProvider.ERROR:
        return IStatus.ERROR;

      default:
        throw new IllegalArgumentException( "Unknown message type: " + type ); //$NON-NLS-1$
    }
  }

  public static IStatus convertMessage( final IMessageProvider message )
  {
    final int severity = convertMessageSeverity( message.getMessageType() );
    final String msg = message.getMessage();
    return new Status( severity, EclipsePlatformContributionsPlugin.getID(), msg );
  }
}