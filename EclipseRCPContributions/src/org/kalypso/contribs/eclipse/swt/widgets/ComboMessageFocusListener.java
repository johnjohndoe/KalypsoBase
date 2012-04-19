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

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;

/**
 * similar to SWT Text Message
 * 
 * @author kimwerner
 */
public class ComboMessageFocusListener extends FocusAdapter
{
  private final String m_message;

  private final Color m_colMessage;

  private final Color m_colText;

  public ComboMessageFocusListener( final String message, final Color messageCol, final Color textCol )
  {
    super();
    m_message = message;
    m_colMessage = messageCol;
    m_colText = textCol;
  }

  public void init( final Combo combo )
  {
    if( isUnchanged( combo ) )
    {
      combo.setForeground( m_colMessage );
      combo.setText( m_message );
    }
  }

  private boolean isUnchanged( final Combo combo )
  {
    return combo.getSelectionIndex() < 0 && ("".compareTo( combo.getText()) == 0|| m_message.compareTo( combo.getText() ) == 0);
  }

  @Override
  public void focusGained( final FocusEvent e )
  {
    if( !(e.widget instanceof Combo) )
      return;

    final Combo combo = (Combo) e.widget;
    if( isUnchanged( combo ) )
    {
      combo.setForeground( m_colText );
      combo.setText( "" );
    }
  }

  @Override
  public void focusLost( FocusEvent e )
  {
    if( !(e.widget instanceof Combo) )
      return;

    final Combo combo = (Combo) e.widget;
    if( isUnchanged( combo ) )
    {
      combo.setForeground( m_colMessage );
      combo.setText( m_message );
    }
  }
}