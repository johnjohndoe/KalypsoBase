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
package org.kalypso.zml.ui.table.commands.menu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandInterpolateValues extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
// try
// {
// // final IZmlTable table = ZmlHandlerUtil.getTable( event );
// final IZmlTableSelectionHandler selection = table.getSelectionHandler();
// final IZmlTableCell active = selection.findActiveCellByPosition();
// final IZmlTableColumn column = active.getColumn();
// final IZmlTableValueCell[] selected = (IZmlTableValueCell[]) column.getSelectedCells();
// if( selected.length < 2 )
// throw new ExecutionException( "Interpolation fehlgeschlagen - selektieren Sie eine zweite Zelle!" );
//
// final IZmlTableValueCell[] intervall = ZmlCommandUtils.findIntervall( selected );
// final IZmlModelValueCell intervallStart = intervall[0].getValueReference();
// final IZmlModelValueCell intervallEnd = intervall[1].getValueReference();
//
// final int indexDifference = Math.abs( intervallEnd.getModelIndex() - intervallStart.getModelIndex() );
// final double valueDifference = getValueDifference( intervallStart, intervallEnd );
//
// final double stepValue = valueDifference / indexDifference;
//
// final int baseIndex = intervallStart.getModelIndex();
// final double baseValue = intervallStart.getValue().doubleValue();
//
// final IZmlModelColumn modelColumn = column.getModelColumn();
//
// final IZmlModel model = table.getModel().getModel();
//
// final ZmlModelTransaction transaction = new ZmlModelTransaction();
//
// for( int index = intervallStart.getModelIndex() + 1; index < intervallEnd.getModelIndex(); index++ )
// {
// final IZmlModelRow row = model.getRowAt( index );
// final IZmlModelValueCell cell = row.get( modelColumn );
//
// final int step = cell.getModelIndex() - baseIndex;
// final double value = baseValue + step * stepValue;
//
// transaction.add( cell, value, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_OK );
// }
//
// transaction.execute();
//
    return Status.OK_STATUS;
// }
// catch( final SensorException e )
// {
// throw new ExecutionException( "Interpolation fehlgeschlagen.", e );
// }
  }

  private double getValueDifference( final IZmlModelValueCell intervallStart, final IZmlModelValueCell intervallEnd ) throws SensorException
  {
    final Number valueStart = intervallStart.getValue();
    final Number valueEnd = intervallEnd.getValue();

    return valueEnd.doubleValue() - valueStart.doubleValue();
  }

}
