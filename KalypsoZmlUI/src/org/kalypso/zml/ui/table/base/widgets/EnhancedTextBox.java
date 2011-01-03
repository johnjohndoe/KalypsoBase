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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.zml.ui.table.base.widgets.rules.ITextWidgetRule;

/**
 * @author Dirk Kuch
 */
public class EnhancedTextBox<T> extends AbstractEnhancedWidget<T>
{
  protected Text m_text;

  protected T m_value;

  private final Set<IEnhancedTextBoxListener<T>> m_listeners = new LinkedHashSet<IEnhancedTextBoxListener<T>>();

  public EnhancedTextBox( final Composite parent, final FormToolkit toolkit, final ITextWidgetRule<T> rule )
  {
    super( parent, toolkit, rule );

    toolkit.adapt( this );
  }

  @Override
  protected void initWidget( final FormToolkit toolkit )
  {
    m_text = toolkit.createText( this, "", SWT.RIGHT | SWT.BORDER );
    m_text.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.AbstractEnhancedWidget#getRule()
   */
  @Override
  protected ITextWidgetRule<T> getRule( )
  {
    return (ITextWidgetRule<T>) super.getRule();
  }

  public void setText( final T value )
  {
    m_text.addModifyListener( new TextModifyListener( getValidationIcon(), getRule(), m_text, getValidationIcon() ) );
    m_text.setText( getRule().getFormatedString( value ) );
    m_text.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        m_value = getRule().parseValue( m_text.getText() );
      }
    } );

    m_text.addFocusListener( new FocusAdapter()
    {
      /**
       * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
       */
      @Override
      public void focusLost( final FocusEvent e )
      {
        fireValueChanged();
      }
    } );
  }

  public T getValue( )
  {
    return m_value;
  }

  protected void fireValueChanged( )
  {
    @SuppressWarnings("unchecked")
    final IEnhancedTextBoxListener<T>[] listeners = m_listeners.toArray( new IEnhancedTextBoxListener[] {} );
    for( final IEnhancedTextBoxListener<T> listener : listeners )
    {
      listener.valueChanged( getValue() );
    }
  }

  public void addListener( final IEnhancedTextBoxListener<T> listener )
  {
    m_listeners.add( listener );
  }
}
