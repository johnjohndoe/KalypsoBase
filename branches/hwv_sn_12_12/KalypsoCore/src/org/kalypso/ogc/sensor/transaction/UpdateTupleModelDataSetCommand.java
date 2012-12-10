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
package org.kalypso.ogc.sensor.transaction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author Dirk Kuch
 */
public class UpdateTupleModelDataSetCommand implements ITupleModelCommand
{
  private final int m_index;

  private final TupleModelDataSet m_dataset;

  private final boolean m_forceUpdate;

  /**
   * @param forceUpdate
   *          == false - only changed values will be updated
   */
  public UpdateTupleModelDataSetCommand( final int index, final TupleModelDataSet dataset, final boolean forceUpdate )
  {
    m_index = index;
    m_dataset = dataset;
    m_forceUpdate = forceUpdate;
  }

  @Override
  public IStatus execute( final ITupleModel model, final MetadataList metadata )
  {
    try
    {
      if( !doUpdate( model ) )
        return Status.OK_STATUS;

      final IAxis[] axes = model.getAxes();

      model.set( m_index, m_dataset.getValueAxis(), m_dataset.getValue() );

      final IAxis statusAxis = AxisUtils.findStatusAxis( axes, m_dataset.getValueAxis() );
      if( Objects.allNotNull( m_dataset.getStatus(), statusAxis ) )
        model.set( m_index, statusAxis, m_dataset.getStatus() );

      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( axes, m_dataset.getValueAxis() );
      if( Objects.allNotNull( metadata, m_dataset.getSource(), dataSourceAxis ) )
      {
        final String source = m_dataset.getSource();
        final DataSourceHandler handler = new DataSourceHandler( metadata );
        final int srxIndex = handler.addDataSource( source, source );

        model.set( m_index, dataSourceAxis, srxIndex );
      }
    }
    catch( final SensorException e )
    {
      e.printStackTrace();

      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), String.format( "updating tuple model dataset %d failed", m_index ) );
    }

    return new Status( IStatus.OK, KalypsoCorePlugin.getID(), String.format( "updated tuple model dataset %d", m_index ) );
  }

  private boolean doUpdate( final ITupleModel model ) throws SensorException
  {
    if( m_forceUpdate )
      return true;

    final Object value = model.get( m_index, m_dataset.getValueAxis() );
    return Objects.notEqual( value, m_dataset.getValue() );
  }
}
