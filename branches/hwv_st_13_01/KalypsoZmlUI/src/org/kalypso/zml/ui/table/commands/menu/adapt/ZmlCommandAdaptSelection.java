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

import org.apache.commons.lang.ArrayUtils;
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
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

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
      final IZmlTableSelection selection = table.getSelection();
      final IZmlModelValueCell focus = selection.getFocusCell();
      final IZmlModelColumn column = focus.getColumn();
      final IZmlModelValueCell[] selected = selection.getSelectedCells( column );
      if( ArrayUtils.getLength( selected ) < 2 )
        throw new ExecutionException( "Anschmiegen fehlgeschlagen - selektieren Sie eine zweite Zelle!" );

      final IZmlModelValueCell base = selected[0];

      final Date begin = base.getIndexValue();
      final Date end = selected[selected.length - 1].getIndexValue();
      final DateRange range = new DateRange( begin, end );

      final IAxis axis = findTransformAxisType( column );

      final IObservation transformed = transform( table.getModelViewport(), column, selected, range, axis );

      final DateRange dateRange = new DateRange( begin, end );

      final AdaptValuesVisitor visitor = new AdaptValuesVisitor( axis.getType() );
      transformed.accept( visitor, new ObservationRequest( dateRange ), 1 );
      column.accept( visitor );

      visitor.doFinish();
    }
    catch( final SensorException e )
    {
      throw new ExecutionException( "Anschmiegen fehlgeschlagen.", e );
    }

    return Status.OK_STATUS;

  }

  private IObservation transform( final ZmlModelViewport model, final IZmlModelColumn column, final IZmlModelCell[] selected, final DateRange range, final IAxis axis ) throws SensorException
  {
    final IObservation observation = column.getObservation();

    final double difference = getDifference( model, selected, axis );

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
      final IAxis destAxis = wqObservation.getTargetAxes().getValueAxis();
      final IAxis srcAxis = wqObservation.getSourceAxes().getValueAxis();

      if( valueAxis == destAxis )
        return destAxis;

      if( valueAxis == srcAxis )
        return srcAxis;
    }

    return null;
  }

  private double getDifference( final ZmlModelViewport model, final IZmlModelCell[] cells, final IAxis axis ) throws SensorException
  {
    final IZmlModelValueCell base = (IZmlModelValueCell) cells[0];
    final IZmlModelValueCell prev = model.findPreviousCell( base );

    final ITupleModel tupleModel1 = base.getColumn().getTupleModel();
    final ITupleModel tupleModel2 = prev.getColumn().getTupleModel();

    final int index1 = base.getModelIndex();
    final int index2 = prev.getModelIndex();

    final double value1 = ((Number) tupleModel1.get( index1, axis )).doubleValue();
    final double value2 = ((Number) tupleModel2.get( index2, axis )).doubleValue();

    return value2 - value1;
  }
}