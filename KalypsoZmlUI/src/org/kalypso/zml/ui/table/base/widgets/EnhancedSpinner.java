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
package org.kalypso.zml.ui.table.base.widgets;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.zml.ui.table.base.widgets.rules.IWidgetRule;

/**
 * @author Dirk Kuch
 */
public class EnhancedSpinner extends AbstractEnhancedWidget<Integer>
{
  private Spinner m_spinner;

  private final Set<IEnhancedTextBoxListener<Integer>> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IEnhancedTextBoxListener<Integer>>() );

  public EnhancedSpinner( final Composite parent, final FormToolkit toolkit, final IWidgetRule<Integer> rule )
  {
    super( parent, toolkit, rule );

    toolkit.adapt( this );
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.AbstractEnhancedWidget#initWidget(org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  protected void initWidget( final FormToolkit toolkit )
  {
    m_spinner = new Spinner( this, SWT.BORDER );
    m_spinner.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    m_spinner.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        fireValueChanged();
      }
    } );

  }

  protected void fireValueChanged( )
  {
    for( final IEnhancedTextBoxListener<Integer> listener : m_listeners )
    {
      listener.valueChanged( m_spinner.getSelection() );
    }
  }

  public void setIncrement( final Integer increment )
  {
    m_spinner.setIncrement( increment );
  }

  public void setSelection( final Integer selection )
  {
    m_spinner.setSelection( selection );
  }

  public void setMin( final int minimum )
  {
    m_spinner.setMinimum( minimum );
  }

  public void setMax( final int maximum )
  {
    m_spinner.setMaximum( maximum );
  }

  public void addListener( final IEnhancedTextBoxListener<Integer> listener )
  {
    m_listeners.add( listener );
  }
}
