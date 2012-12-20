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
package org.kalypso.zml.core.table.model.editing;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.transaction.TupleModelTransaction;
import org.kalypso.ogc.sensor.transaction.UpdateTupleModelDataSetCommand;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlValues;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * updated value will be a new stützstelle. update all values between s_new and s_next. set updated values to s_new
 * value
 * 
 * <pre>
 *                                 ( update too  )
 *         x ------------------ x --------------- x
 *      stuetz                stuetz            stuetz
 *      stelle                stelle            stelle
 *        s_1                  s_new            s_next
 * 
 * </pre>
 * 
 * @author Dirk Kuch
 */
public class ContinuedInterpolatedValueEditingStrategy extends AbstractEditingStrategy
{

  public ContinuedInterpolatedValueEditingStrategy( final ZmlModelViewport facade )
  {
    super( facade );
  }

  @Override
  public void setValue( final IZmlModelValueCell cell, final String value )
  {
    try
    {
      /** update current cell */
      final Number targetValue = (Number) getTargetValue( cell, value );

      final IZmlModelColumn column = cell.getColumn();
      final TupleModelTransaction transaction = new TupleModelTransaction( column.getTupleModel(), column.getMetadata() );

      try
      {
        final TupleModelDataSet dataset = new TupleModelDataSet( column.getValueAxis(), targetValue, KalypsoStati.BIT_USER_MODIFIED, IDataSourceItem.SOURCE_MANUAL_CHANGED );
        transaction.add( new UpdateTupleModelDataSetCommand( cell.getModelIndex(), dataset, true ) );

        IZmlModelValueCell next = getViewport().findNextCell( cell );
        while( next != null )
        {
          final IZmlModelValueCell ref = next;
          if( ZmlValues.isStuetzstelle( ref ) )
            break;

          final TupleModelDataSet dataset2 = new TupleModelDataSet( column.getValueAxis(), targetValue, KalypsoStati.BIT_OK, IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE );
          transaction.add( new UpdateTupleModelDataSetCommand( ref.getModelIndex(), dataset2, true ) );

          next = getViewport().findNextCell( next );
        }
      }
      finally
      {
        column.getTupleModel().execute( transaction );
      }

    }
    catch( final SensorException e )
    {
      KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  @Override
  public boolean isAggregated( )
  {
    return false;
  }

}
