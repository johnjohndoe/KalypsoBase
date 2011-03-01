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
package org.kalypso.zml.ui.table.provider.strategy.editing;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.VALUE_STATUS;
import org.kalypso.zml.core.table.model.ZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlDataValueReference;
import org.kalypso.zml.core.table.model.references.ZmlValueReferenceFactory;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class SumValueEditingStrategy extends AbstractEditingStrategy
{
  private final ZmlLabelProvider m_labelProvider;

  public SumValueEditingStrategy( final ExtendedZmlTableColumn column, final ZmlLabelProvider labelProvider )
  {
    super( column );
    m_labelProvider = labelProvider;
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy#getValue(java.lang.Object)
   */
  @Override
  public String getValue( final IZmlModelRow row )
  {
    try
    {
      final Object plain = m_labelProvider.getPlainValue( row );
      if( Objects.isNull( plain ) )
        return null;

      final double value = NumberUtils.parseDouble( plain.toString() );

      final CellStyle style = getStyle();
      return String.format( style.getTextFormat() == null ? "%s" : style.getTextFormat(), value );
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }

    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy#setValue(org.kalypso.zml.ui.table.model.IZmlModelRow,
   *      java.lang.String)
   */
  @Override
  public void setValue( final IZmlModelRow row, final String value )
  {
    try
    {
      final ExtendedZmlTableColumn column = getColumn();
      final IZmlTableCell cell = column.findCell( row );

      final Number targetValue = getTargetValue( value );

      final IZmlTableCell previousCell = cell.findPreviousCell();

      if( cell.getTable().getResolution() == 0 )
      {
        updateOriginValue( cell.getValueReference(), targetValue );
      }
      else if( previousCell == null )
      {
        /* get first invisible value (first value will is not part of the table!) */
        final IZmlModel model = row.getModel();
        final IZmlModelRow baseRow = model.getRowAt( 0 );
        final IZmlValueReference previousReference = baseRow.get( column.getModelColumn() );

        updateAggregatedValue( previousReference, cell.getValueReference(), targetValue );
      }
      else
      {
        updateAggregatedValue( getStartReference( previousCell ), cell.getValueReference(), targetValue );
      }
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private IZmlValueReference getStartReference( final IZmlTableCell previousCell )
  {
    final ZmlDataValueReference reference = (ZmlDataValueReference) previousCell.getValueReference();

    final ZmlValueReferenceFactory factory = ZmlValueReferenceFactory.getInstance();
    return factory.createReference( (ZmlModelRow) reference.getRow(), reference.getColumn(), reference.getModelIndex() + 1 );
  }

  private void updateAggregatedValue( final IZmlValueReference start, final IZmlValueReference end, final Number targetValue ) throws SensorException
  {
    final Integer startIndex = start.getModelIndex();
    final Integer endIndex = end.getModelIndex();
    final int steps = endIndex - startIndex + 1;

    final double stepping = targetValue.doubleValue() / steps;

    final IZmlModelColumn modelColumn = getColumn().getModelColumn();

    for( int index = startIndex; index <= endIndex; index++ )
    {
      modelColumn.update( index, stepping, VALUE_STATUS.eManual );
    }
  }

  private void updateOriginValue( final IZmlValueReference reference, final Number targetValue ) throws SensorException
  {
    reference.update( targetValue );
  }
}
