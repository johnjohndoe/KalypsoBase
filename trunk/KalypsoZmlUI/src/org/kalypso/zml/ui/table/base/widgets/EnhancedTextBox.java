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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Dirk Kuch
 */
public class EnhancedTextBox extends AbstractEnhancedWidget
{
  protected Text m_text;

  protected String m_value;

  public EnhancedTextBox( final Composite parent, final FormToolkit toolkit, final IWidgetRule rule )
  {
    super( parent, toolkit, rule );
    initWidget( toolkit );

    toolkit.adapt( this );
  }

  @Override
  protected void initWidget( final FormToolkit toolkit )
  {
    m_text = toolkit.createText( this, "", SWT.RIGHT | SWT.BORDER );
    m_text.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
  }

  public void setText( final Object value )
  {
    m_text.addModifyListener( new TextModifyListener( getValidationIcon(), new IWidgetRule[] { getRule() }, m_text, getValidationIcon() ) );
    m_text.setText( getRule().getFormatedString( value ) );
    m_text.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        m_value = m_text.getText();
      }
    } );
  }

  public Object getValue( )
  {
    return getRule().getValue( m_value );
  }
}
