/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.ogc.sensor.timeseries.datasource;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;

/**
 * @author Dirk Kuch
 */
public class AddDataSourceModelHandler extends AbstractDataSourceModelHandler
{
  public AddDataSourceModelHandler( final ITupleModel model )
  {
    super( model );
  }

  /**
   * @return cloned observation extended by data source axis if no data source axis exists
   */
  public ITupleModel extend( ) throws SensorException
  {
    if( hasDataSouceAxis() )
      return getModel();

    final DefaultAxis dataSourceAxis = new DefaultAxis( ITimeseriesConstants.TYPE_DATA_SRC, ITimeseriesConstants.TYPE_DATA_SRC, "", Integer.class, false );

    final ITupleModel baseModel = getModel();
    IAxis[] baseAxes = baseModel.getAxes();
    baseAxes = (IAxis[]) ArrayUtils.add( baseAxes, dataSourceAxis );

    final SimpleTupleModel model = new SimpleTupleModel( baseAxes );
    final int dataSourceIndex = ArrayUtils.indexOf( baseAxes, dataSourceAxis );

    for( int i = 0; i < baseModel.size(); i++ )
    {
      final Object[] data = new Object[baseAxes.length];

      for( final IAxis axis : baseModel.getAxes() )
      {
        final Object element = baseModel.get( i, axis );
        data[model.getPosition( axis )] = element;
      }

      /**
       * it's always '0' - virtual repository items will fill this field otherwise!
       */
      data[dataSourceIndex] = 0;

      model.addTuple( data );
    }

    return model;
  }
}
