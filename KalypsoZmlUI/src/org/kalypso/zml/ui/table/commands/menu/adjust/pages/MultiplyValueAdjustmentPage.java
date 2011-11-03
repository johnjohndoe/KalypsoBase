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
package org.kalypso.zml.ui.table.commands.menu.adjust.pages;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.zml.ui.table.base.widgets.EnhancedTextBox;
import org.kalypso.zml.ui.table.base.widgets.IEnhancedTextBoxListener;
import org.kalypso.zml.ui.table.base.widgets.rules.DoubeValueWidgetRule;

/**
 * @author Dirk Kuch
 */
public class MultiplyValueAdjustmentPage extends AbstractAdjustmentPage implements IEnhancedTextBoxListener<Double>
{
  private Double m_multiplier;

  private EnhancedTextBox<Double> m_textBox;

  public MultiplyValueAdjustmentPage( final IAdjustmentPageProvider provider )
  {
    super( provider );
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.pager.IElementPage#getLabel()
   */
  @Override
  public String getLabel( )
  {
    return "Werte multiplizieren (*)";
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.pager.IElementPage#render(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    toolkit.createLabel( body, "" );// spacer

    toolkit.createLabel( body, "Multiplikator" ).setFont( HEADING );
    m_textBox = new EnhancedTextBox<Double>( body, toolkit, new DoubeValueWidgetRule( "%.02f" ) );
    m_textBox.setText( getMultiplier() );
    m_textBox.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    m_textBox.addListener( this );
  }

  private Double getMultiplier( )
  {
    if( Objects.isNotNull( m_multiplier ) )
      return m_multiplier;

    m_multiplier = 1.0;
    return m_multiplier;
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.pager.IElementPage#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.IEnhancedTextBoxListener#valueChanged(java.lang.Object)
   */
  @Override
  public void valueChanged( final Double value )
  {
    m_multiplier = value;
  }

  /**
   * @see org.kalypso.zml.ui.table.commands.menu.adjust.pages.AbstractAdjustmentPage#getRunnable()
   */
  @Override
  public ICoreRunnableWithProgress getRunnable( )
  {
    return new MultiplyValueRunnable( getColumn().getSelectedCells(), m_multiplier );
  }

  /**
   * @see org.kalypso.zml.ui.table.commands.menu.adjust.pages.AbstractAdjustmentPage#isValid()
   */
  @Override
  public boolean isValid( )
  {
    return m_textBox.isValid();
  }
}