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
package org.kalypso.ogc.sensor.timeseries.datasource;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * @author Dirk Kuch
 */
public class RemoveDataSourceModelHandler extends AbstractDataSourceModelHandler
{

  /**
   * don't use item.getAdapter(IObservation.class) -> because some implementation extends the underlying observation
   */
  public RemoveDataSourceModelHandler( final ITupleModel model )
  {
    super( model );
  }

  /**
   * @return cloned observation extended by data source axis if no data source axis exists
   */
  public ITupleModel remove( ) throws SensorException
  {
    if( !hasDataSouceAxis() )
      return getModel();

    final ITupleModel baseModel = getModel();
    final IAxis[] axes = getAxes( baseModel.getAxes() );

    final SimpleTupleModel model = new SimpleTupleModel( axes );

    for( int i = 0; i < baseModel.size(); i++ )
    {
      final Object[] data = new Object[axes.length];

      for( final IAxis axis : axes )
      {
        final Object element = baseModel.get( i, axis );
        data[model.getPosition( axis )] = element;
      }

      model.addTuple( data );
    }

    return model;
  }

  private IAxis[] getAxes( final IAxis[] axes )
  {
    final IAxis axis = AxisUtils.findDataSourceAxis( axes );
    if( axis != null )
      return (IAxis[]) ArrayUtils.removeElement( axes, axis );

    return axes;
  }
}
