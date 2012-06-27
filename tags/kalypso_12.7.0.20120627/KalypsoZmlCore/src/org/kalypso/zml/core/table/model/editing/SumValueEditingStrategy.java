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
package org.kalypso.zml.core.table.model.editing;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalSourceHandler;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * editing strategy for sum values like rainfall
 * 
 * @author Dirk Kuch
 */
public class SumValueEditingStrategy extends AbstractEditingStrategy
{
  public SumValueEditingStrategy( final ZmlModelViewport model )
  {
    super( model );
  }

  @Override
  public void setValue( final IZmlModelValueCell cell, final String value )
  {
    try
    {
      final Number targetValue = (Number) getTargetValue( cell, value );

      final IZmlModelValueCell previousCell = getViewport().findPreviousCell( cell );

      if( getViewport().getResolution() == 0 )
      {
        updateOriginValue( cell, targetValue );
      }
      else if( previousCell == null )
      {
        /* get first invisible value (first value will is not part of the table!) */
        final IZmlModel model = cell.getModel();
        final IZmlModelRow baseRow = model.getRowAt( 0 );
        final IZmlModelValueCell previousReference = baseRow.get( cell.getColumn() );

        updateAggregatedValue( previousReference, cell, targetValue );
      }
      else
      {
        updateAggregatedValue( getStartReference( previousCell ), cell, targetValue );
      }
    }
    catch( final SensorException e )
    {
      KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private IZmlModelValueCell getStartReference( final IZmlModelValueCell previousCell )
  {
    final Integer index = previousCell.getModelIndex();
    final IZmlModelValueCell[] cells = previousCell.getColumn().getCells();

    return cells[index + 1];
  }

  private void updateAggregatedValue( final IZmlModelValueCell start, final IZmlModelValueCell end, final Number targetValue ) throws SensorException
  {
    final Integer startIndex = start.getModelIndex();
    final Integer endIndex = end.getModelIndex();
    final int steps = endIndex - startIndex + 1;

    final double stepping = targetValue.doubleValue() / steps;

    final IZmlModelColumn modelColumn = start.getColumn();

    for( int index = startIndex; index <= endIndex; index++ )
    {
      modelColumn.doUpdate( index, stepping, IntervalSourceHandler.SOURCE_INTERVAL_FITLER, KalypsoStati.BIT_USER_MODIFIED );
    }
  }

  private void updateOriginValue( final IZmlModelValueCell reference, final Number targetValue ) throws SensorException
  {
    reference.doUpdate( targetValue, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );
  }

  @Override
  public boolean isAggregated( )
  {
    return true;
  }
}
