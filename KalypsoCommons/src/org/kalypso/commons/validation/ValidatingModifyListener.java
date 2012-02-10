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
package org.kalypso.commons.validation;

import java.text.ParseException;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IMessageManager;

/**
 * @author Gernot Belger
 */
public class ValidatingModifyListener implements ModifyListener
{
  private final Text m_control;

  private final IMessageManager m_messageManager;

  private IFormValidator m_validator;

  private final IParser m_parser;

  private IValueReceiver m_valueReceiver;

  public ValidatingModifyListener( final Text control, final IParser parser, final IMessageManager messageManager )
  {
    m_control = control;
    m_parser = parser;
    m_messageManager = messageManager;

    m_control.addModifyListener( this );
  }

  public void setValidator( final IFormValidator validator )
  {
    m_validator = validator;
    revalidate();
  }

  public void setValueReceiver( final IValueReceiver valueReceiver )
  {
    m_valueReceiver = valueReceiver;
    revalidate();
  }

  /**
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  @Override
  public void modifyText( final ModifyEvent e )
  {
    revalidate();
  }

  private void revalidate( )
  {
    m_messageManager.removeMessages( m_control );

    try
    {
      final String newText = m_control.getText();
      final Object parsedValue = m_parser.parse( newText );

      if( m_validator != null )
      {
        m_messageManager.setAutoUpdate( false );
        final IMessageProvider[] messages = m_validator.validate( parsedValue );
        for( final IMessageProvider message : messages )
          m_messageManager.addMessage( message, message.getMessage(), null, message.getMessageType(), m_control );
        m_messageManager.setAutoUpdate( true );
      }

      updateValue( parsedValue );
    }
    catch( final ParseException e )
    {
      m_messageManager.addMessage( this, e.getMessage(), null, IMessageProvider.ERROR, m_control );
      updateValue( null );
    }

  }

  private void updateValue( final Object object )
  {
    if( m_valueReceiver != null )
      m_valueReceiver.updateValue( object );
  }
}
