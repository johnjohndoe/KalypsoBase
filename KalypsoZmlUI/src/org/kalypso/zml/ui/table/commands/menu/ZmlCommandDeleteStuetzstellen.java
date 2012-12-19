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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.interpolation.InterpolationFilter;
import org.kalypso.ogc.sensor.transaction.TupleModelTransaction;
import org.kalypso.ogc.sensor.transaction.UpdateTupleModelDataSetCommand;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.interpolation.ZmlInterpolationWorker;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlValues;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandDeleteStuetzstellen extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableSelection selection = table.getSelection();

      final IZmlModelValueCell current = selection.getFocusCell();
      if( current == null )
        return Status.CANCEL_STATUS;

      final IZmlModelColumn column = current.getColumn();

      final TupleModelTransaction transaction = new TupleModelTransaction( column.getTupleModel(), column.getMetadata() );

      final IZmlModelValueCell[] cells = selection.getSelectedCells( column );
      for( final IZmlModelValueCell cell : cells )
      {
        try
        {
          final IZmlModelValueCell reference = cell;
          if( ZmlValues.isStuetzstelle( reference ) )
          {
            final TupleModelDataSet dataset = new TupleModelDataSet( column.getValueAxis(), cell.getValue(), KalypsoStati.BIT_OK, getSource( cell ) );
            transaction.add( new UpdateTupleModelDataSetCommand( cell.getModelIndex(), dataset, true ) );
          }
        }
        catch( final Throwable t )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
        }
      }

      try
      {
        column.getTupleModel().execute( transaction );
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }

      doInterpolation( column );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return Status.OK_STATUS;

  }

  private void doInterpolation( final IZmlModelColumn column ) throws SensorException
  {
    final String type = column.getDataColumn().getValueAxis();
    if( StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, type ) )
      return;

    /**
     * re-interpolate complete observation because of table view filter (like 12h view, stueztstellen ansicht, etc)
     */
    final ZmlInterpolationWorker interpolationWorker = new ZmlInterpolationWorker( column );
    interpolationWorker.execute( new NullProgressMonitor() );
  }

  private String getSource( final IZmlModelValueCell cell )
  {
    final String identifier = cell.getColumn().getIdentifier();
    if( ITimeseriesConstants.TYPE_WECHMANN_E.equals( identifier ) )
      return IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE;
    else if( ITimeseriesConstants.TYPE_WECHMANN_SCHALTER_V.equals( identifier ) )
      return IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE;

    return String.format( "%s%s", IDataSourceItem.FILTER_SOURCE, InterpolationFilter.FILTER_ID ); //$NON-NLS-1$
  }
}
