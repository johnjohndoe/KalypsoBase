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
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.ui.i18n.Messages;
import org.kalypso.zml.ui.table.base.widgets.EnhancedTextBox;
import org.kalypso.zml.ui.table.base.widgets.IEnhancedTextBoxListener;
import org.kalypso.zml.ui.table.base.widgets.rules.DoubeValueWidgetRule;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * @author Dirk Kuch
 */
public class ConstantValueAdjustmentPage extends AbstractAdjustmentPage implements IEnhancedTextBoxListener<Double>
{
  private Double m_constantValue;

  private EnhancedTextBox<Double> m_textBox;

  public ConstantValueAdjustmentPage( final IAdjustmentPageProvider provider )
  {
    super( provider, ConstantValueAdjustmentPage.class.getName() );
  }

  @Override
  public String getLabel( )
  {
    return Messages.ConstantValueAdjustmentPage_0;
  }

  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    try
    {
      toolkit.createLabel( body, "" );// spacer //$NON-NLS-1$

      toolkit.createLabel( body, Messages.ConstantValueAdjustmentPage_2 ).setFont( HEADING );
      m_textBox = new EnhancedTextBox<>( body, toolkit, new DoubeValueWidgetRule() );
      m_textBox.setText( getConstantValue() );
      m_textBox.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      m_textBox.addListener( this );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

  }

  private Double getConstantValue( ) throws SensorException
  {
    if( Objects.isNotNull( m_constantValue ) )
      return m_constantValue;

    final IZmlModelValueCell cell = getSelection().getFocusCell();
    final Number value = (Number) cell.getValue();

    m_constantValue = value.doubleValue();
    return m_constantValue;
  }

  @Override
  public void dispose( )
  {

  }

  @Override
  public void valueChanged( final Double value )
  {
    m_constantValue = value;
  }

  @Override
  public ICoreRunnableWithProgress getRunnable( )
  {
    final IZmlTableSelection selection = getSelection();
    final IZmlModelColumn column = getColumn();

    final IZmlModelValueCell[] cells = selection.getSelectedCells( column );

    return new ConstantValueRunnable( column, cells, m_constantValue );
  }

  @Override
  public boolean isValid( )
  {
    return m_textBox.isValid();
  }

}
