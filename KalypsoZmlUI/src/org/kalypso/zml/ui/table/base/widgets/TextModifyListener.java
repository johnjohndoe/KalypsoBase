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
package org.kalypso.zml.ui.table.base.widgets;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.zml.ui.table.base.widgets.rules.ITextWidgetRule;

/**
 * @author Dirk Kuch
 */
public class TextModifyListener implements ModifyListener
{
  @SuppressWarnings("rawtypes")
  private final ITextWidgetRule m_rule;

  private final Control[] m_tooltipControls;

  private final ImageHyperlink m_valid;

  public TextModifyListener( final ImageHyperlink icon, @SuppressWarnings("rawtypes") final ITextWidgetRule rule, final Control... tooltipControls )
  {
    m_tooltipControls = tooltipControls;
    m_valid = icon;
    m_rule = rule;
  }

  /**
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  @Override
  public void modifyText( final ModifyEvent e )
  {
    final String text = resolveText( e );

    if( !m_rule.isValid( text ) )
    {
      final String message = m_rule.getLastValidationMessage();
      setTooltip( message );

      m_valid.setVisible( true );

      return;
    }

    setTooltip( null );
    m_valid.setVisible( false );
  }

  private String resolveText( final ModifyEvent e )
  {
    final Object source = e.getSource();
    if( source instanceof Text )
    {
      final Text textField = (Text) source;
      return textField.getText();
    }
    else
      throw new NotImplementedException();
  }

  private void setTooltip( final String message )
  {
    for( final Control control : m_tooltipControls )
    {
      control.setToolTipText( message );
    }
  }

}
