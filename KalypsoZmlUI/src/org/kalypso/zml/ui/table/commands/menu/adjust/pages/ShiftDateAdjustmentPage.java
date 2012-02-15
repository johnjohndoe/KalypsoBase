/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.ui.table.base.widgets.EnhancedSpinner;
import org.kalypso.zml.ui.table.base.widgets.IEnhancedTextBoxListener;

/**
 * @author Dirk Kuch
 */
public class ShiftDateAdjustmentPage extends AbstractAdjustmentPage
{
  protected int m_days;

  protected int m_hours;

  protected int m_minutes;

  private Integer m_offset;

  private int m_base;

  public ShiftDateAdjustmentPage( final IAdjustmentPageProvider provider )
  {
    super( provider );
  }

  @Override
  public String getLabel( )
  {
    return "Datum verschieben (->)";
  }

  @Override
  public void render( final Composite body, final FormToolkit toolkit )
  {
    toolkit.createLabel( body, "" ); // spacer
    final Integer offset = getOffset();

    toolkit.createLabel( body, "Verschieben, um:" ).setFont( HEADING );

    final Composite control = toolkit.createComposite( body );
    control.setLayout( LayoutHelper.createGridLayout( 3 ) );
    control.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    if( offset < 60 )
      renderMinutedBased( control, toolkit );
    else
      renderHourBased( control, toolkit );
  }

  private void renderHourBased( final Composite control, final FormToolkit toolkit )
  {
    addDaySpinner( control, toolkit );
    final EnhancedSpinner hourSpinner = addHourSpinner( control, toolkit );

    hourSpinner.setSelection( 1 );
    m_hours = 1;

  }

  private void renderMinutedBased( final Composite control, final FormToolkit toolkit )
  {
    addDaySpinner( control, toolkit );
    addHourSpinner( control, toolkit );

    final Integer offset = getOffset();
    final EnhancedSpinner spinnerMinutes = addSpinner( control, toolkit, "Minuten", 60, offset );
    spinnerMinutes.addListener( new IEnhancedTextBoxListener<Integer>()
    {
      @Override
      public void valueChanged( final Integer value )
      {
        m_minutes = value;
      }
    } );

    spinnerMinutes.setSelection( offset );
    m_minutes = offset;
  }

  private EnhancedSpinner addHourSpinner( final Composite control, final FormToolkit toolkit )
  {
    final EnhancedSpinner spinnerHour = addSpinner( control, toolkit, "Stunden", 24, 1 );
    spinnerHour.addListener( new IEnhancedTextBoxListener<Integer>()
    {
      @Override
      public void valueChanged( final Integer value )
      {
        m_hours = value;
      }
    } );

    return spinnerHour;
  }

  private EnhancedSpinner addDaySpinner( final Composite control, final FormToolkit toolkit )
  {
    final EnhancedSpinner spinnerDay = addSpinner( control, toolkit, "Tage", 30, 1 );
    spinnerDay.addListener( new IEnhancedTextBoxListener<Integer>()
    {
      @Override
      public void valueChanged( final Integer value )
      {
        m_days = value;
      }
    } );

    return spinnerDay;
  }

  private EnhancedSpinner addSpinner( final Composite body, final FormToolkit toolkit, final String text, final int maximum, final int increment )
  {
    toolkit.createLabel( body, text );
    toolkit.createLabel( body, "   " ); // spacer

    final EnhancedSpinner spinner = new EnhancedSpinner( body, toolkit, new ShiftDateWidgetRule( m_base ) );
    spinner.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    spinner.setMin( -maximum );
    spinner.setMax( maximum );
    spinner.setIncrement( increment );

    return spinner;
  }

  private Integer getOffset( )
  {
    if( Objects.isNotNull( m_offset ) )
      return m_offset;

    final IZmlModelColumn column = getColumn();
    final IZmlModelValueCell[] cells = column.getCells();

    final long t1 = cells[0].getIndexValue().getTime();
    final long t2 = cells[1].getIndexValue().getTime();

    final double minutes = Long.valueOf( t2 - t1 ).doubleValue() / 1000 / 60;

    m_base = Double.valueOf( minutes ).intValue();
    m_offset = m_base;
    return m_offset;
  }

  @Override
  public void dispose( )
  {
  }

  @Override
  public ICoreRunnableWithProgress getRunnable( )
  {
    return new ShiftDateRunnable( getColumn(), getSelection().getSelectedCells( getColumn() ), getMinutes() );
  }

  @Override
  public boolean isValid( )
  {
    final int minutes = getMinutes();

    return minutes % m_base == 0;
  }

  private int getMinutes( )
  {
    int minutes = m_minutes;
    minutes += m_hours * 60;
    minutes += m_days * 60 * 24;

    return minutes;
  }

}
