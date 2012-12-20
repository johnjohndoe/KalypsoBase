/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  DenickestraÃŸe 22
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
package org.kalypso.zml.core.table.model.interpolation;

import java.util.Date;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.interpolation.worker.IInterpolationFilter;
import org.kalypso.ogc.sensor.transaction.ITupleModelTransaction;
import org.kalypso.ogc.sensor.transaction.UpdateTupleModelDataSetCommand;

/**
 * @author Dirk Kuch
 */
public final class ZmlInterpolation
{
  private ZmlInterpolation( )
  {
  }

  public static void interpolate( final ITupleModel model, final ITupleModelTransaction transaction, final IAxis valueAxis, final Integer before, final Integer current ) throws SensorException
  {
    if( current - before == 1 )
      return;

    final IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );

    final Date baseDate = (Date) model.get( current, dateAxis );
    final Double baseValue = (Double) model.get( current, valueAxis );

    final Date beforeDate = (Date) model.get( before, dateAxis );
    final Double beforeValue = (Double) model.get( before, valueAxis );

    final long timeDiff = baseDate.getTime() - beforeDate.getTime();
    final double valueDiff = baseValue - beforeValue;

    final double diff = valueDiff / timeDiff;

    for( int index = before + 1; index < current; index++ )
    {
      final Date ptr = (Date) model.get( index, dateAxis );
      final double ptrDiff = ptr.getTime() - beforeDate.getTime();

      final double value = beforeValue + diff * ptrDiff;

      final TupleModelDataSet dataset = new TupleModelDataSet( valueAxis, value, KalypsoStati.BIT_OK, IInterpolationFilter.DATA_SOURCE );
      transaction.add( new UpdateTupleModelDataSetCommand( index, dataset, false ) );
    }
  }

  public static void fillValue( final ITupleModelTransaction transaction, final IAxis valueAxis, final int start, final int end, final Double value )
  {
    for( int index = start; index < end; index++ )
    {
      final TupleModelDataSet dataset = new TupleModelDataSet( valueAxis, value, KalypsoStati.BIT_OK, IInterpolationFilter.DATA_SOURCE );
      transaction.add( new UpdateTupleModelDataSetCommand( index, dataset, false ) );
    }
  }

  public static boolean isSetLastValidValue( final MetadataList metadata )
  {
    final Object setting = metadata.get( IInterpolationFilter.SETTING_FILL_LAST_WITH_VALID );
    if( !(setting instanceof String) )
      return false;

    return Boolean.valueOf( (String) setting );
  }

  public static Double getDefaultValue( final MetadataList metadata )
  {
    final Object setting = metadata.get( IInterpolationFilter.SETTING_DEFAULT_VALUE );
    if( !(setting instanceof String) )
      return Double.valueOf( 0.0 );

    return Double.valueOf( (String) setting );
  }
}
