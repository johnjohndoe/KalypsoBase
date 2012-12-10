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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.transaction.TupleModelTransaction;
import org.kalypso.ogc.sensor.transaction.UpdateTupleModelDataSetCommand;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandInterpolateValues extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableSelection selection = table.getSelection();

      final IZmlModelValueCell current = selection.getFocusCell();
      if( current == null )
        return Status.CANCEL_STATUS;

      final IZmlModelValueCell[] cells = selection.getSelectedCells( current.getColumn() );
      if( ArrayUtils.getLength( cells ) < 2 )
        throw new ExecutionException( "Interpolation fehlgeschlagen - selektieren Sie eine zweite Zelle!" );

      final IZmlModelCell[] intervall = ZmlCommandUtils.findIntervall( cells );
      final IZmlModelValueCell intervallStart = (IZmlModelValueCell) intervall[0];
      final IZmlModelValueCell intervallEnd = (IZmlModelValueCell) intervall[1];

      final int indexDifference = Math.abs( intervallEnd.getModelIndex() - intervallStart.getModelIndex() );
      final double valueDifference = getValueDifference( intervallStart, intervallEnd );

      final double stepValue = valueDifference / indexDifference;

      final int baseIndex = intervallStart.getModelIndex();
      final double baseValue = ((Number) intervallStart.getValue()).doubleValue();

      final IZmlModelColumn column = intervallStart.getColumn();
      final TupleModelTransaction transaction = new TupleModelTransaction( column.getTupleModel(), column.getMetadata() );

      final ZmlModelViewport viewModel = table.getModelViewport();
      final IZmlModel model = viewModel.getModel();

      for( int index = intervallStart.getModelIndex() + 1; index < intervallEnd.getModelIndex(); index++ )
      {
        final IZmlModelRow row = model.getRowAt( index );
        final IZmlModelValueCell cell = row.get( current.getColumn() );

        final int step = cell.getModelIndex() - baseIndex;
        final double value = baseValue + step * stepValue;

        final TupleModelDataSet dataset = new TupleModelDataSet( column.getValueAxis(), value, KalypsoStati.BIT_OK, IDataSourceItem.SOURCE_MANUAL_CHANGED );
        transaction.add( new UpdateTupleModelDataSetCommand( cell.getModelIndex(), dataset, true ) );
      }

      column.getTupleModel().execute( transaction );

      return Status.OK_STATUS;
    }
    catch( final SensorException e )
    {
      throw new ExecutionException( "Interpolation fehlgeschlagen.", e );
    }
  }

  private double getValueDifference( final IZmlModelValueCell intervallStart, final IZmlModelValueCell intervallEnd ) throws SensorException
  {
    final Number valueStart = (Number) intervallStart.getValue();
    final Number valueEnd = (Number) intervallEnd.getValue();

    return valueEnd.doubleValue() - valueStart.doubleValue();
  }

}
