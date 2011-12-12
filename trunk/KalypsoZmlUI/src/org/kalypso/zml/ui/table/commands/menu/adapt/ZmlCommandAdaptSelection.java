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
package org.kalypso.zml.ui.table.commands.menu.adapt;

import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.TranProLinFilterUtilities;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.timeseries.wq.WQTimeserieProxy;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandAdaptSelection extends AbstractHandler
{
  /**
   * <pre>
   * 
   *   - - s1 = = = x = = = x = = = s2 - - 
   * 
   * s1 -> start point
   * s2 -> end point
   * x  -> fix stuetzstellen
   * =  -> will be replaced by spline interpolation point
   * 
   * </pre>
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableSelectionHandler selection = table.getSelectionHandler();
      final IZmlTableColumn column = selection.findActiveColumnByPosition();
      final IZmlTableCell[] selected = column.getSelectedCells();
      if( selected.length < 2 )
        throw new ExecutionException( "Anschmiegen fehlgeschlagen - selektieren Sie eine zweite Zelle!" );

      final IZmlTableCell base = selected[0];

      final Date begin = toDate( base );
      final Date end = toDate( selected[selected.length - 1] );
      final DateRange range = new DateRange( begin, end );

      final IZmlValueReference reference = selected[0].getValueReference();
      final IZmlModelColumn tableColumn = reference.getColumn();
      final IAxis axis = findTransformAxisType( tableColumn );

      final IObservation transformed = transform( column, selected, range, axis );

      final DateRange dateRange = new DateRange( begin, end );

      final AdaptValuesVisitor visitor = new AdaptValuesVisitor( axis.getType() );
      transformed.accept( visitor, new ObservationRequest( dateRange ) );
      column.getModelColumn().accept( visitor );

      visitor.doFinish();
    }
    catch( final SensorException e )
    {
      throw new ExecutionException( "Anschmiegen fehlgeschlagen.", e );
    }

    return Status.OK_STATUS;

  }

  private IObservation transform( final IZmlTableColumn column, final IZmlTableCell[] selected, final DateRange range, final IAxis axis ) throws SensorException
  {
    final IObservation observation = column.getModelColumn().getObservation();

    final double difference = getDifference( selected, axis );

    return TranProLinFilterUtilities.transform( observation, range, difference, 0.0, "+", axis.getType() ); //$NON-NLS-1$
  }

  private IAxis findTransformAxisType( final IZmlModelColumn tableColumn )
  {
    final IAxis valueAxis = tableColumn.getValueAxis();
    if( valueAxis.isPersistable() )
      return valueAxis;

    final IObservation observation = tableColumn.getObservation();
    if( observation instanceof WQTimeserieProxy )
    {
      final WQTimeserieProxy wqObservation = (WQTimeserieProxy) observation;
      final IAxis destAxis = wqObservation.getDestAxis();
      final IAxis srcAxis = wqObservation.getSrcAxis();

      if( valueAxis == destAxis )
        return srcAxis;

      if( valueAxis == srcAxis )
        return destAxis;
    }

    return null;
  }

  private Date toDate( final IZmlTableCell cell ) throws SensorException
  {
    final IZmlValueReference reference = cell.getValueReference();
    return reference.getIndexValue();
  }

  private double getDifference( final IZmlTableCell[] cells, final IAxis axis ) throws SensorException
  {
    final IZmlTableCell base = cells[0];
    final IZmlTableCell prev = base.findPreviousCell();

    final IZmlValueReference reference1 = base.getValueReference();
    final IZmlValueReference reference2 = prev.getValueReference();

    final ITupleModel tupleModel1 = reference1.getColumn().getTupleModel();
    final ITupleModel tupleModel2 = reference2.getColumn().getTupleModel();

    final int index1 = reference1.getModelIndex();
    final int index2 = reference2.getModelIndex();

    final double value1 = ((Number) tupleModel1.get( index1, axis )).doubleValue();
    final double value2 = ((Number) tupleModel2.get( index2, axis )).doubleValue();

    return value2 - value1;
  }
}