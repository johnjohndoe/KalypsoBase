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

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.repository.IRepositoryItem;

/**
 * @author Dirk Kuch
 */
public class AddDataSourceObservationHandler
{
  private final IObservation m_observation;

  private final String m_itemIdentifier;

  private final String m_repositoryId;

  public AddDataSourceObservationHandler( final IRepositoryItem item, final IObservation observation )
  {
    this( item.getIdentifier(), item.getRepository().getName(), observation );
  }

  public AddDataSourceObservationHandler( final String itemIdentifier, final String repositoryId, final IObservation observation )
  {
    m_itemIdentifier = itemIdentifier;
    m_repositoryId = repositoryId;
    m_observation = observation;
  }

  /**
   * @return cloned observation extended by data source axis if no data source axis exists
   */
  public IObservation extend( ) throws SensorException
  {
    if( hasDataSouceAxis() )
      return m_observation;

    // FIXME: this must be changed! On each access to the observation, a big request is sent. This makes
    // the timeseris brwoser not useable any more!

    final AddDataSourceModelHandler handler = new AddDataSourceModelHandler( m_observation.getValues( null ) );
    final ITupleModel model = handler.extend();

    final DataSourceHandler dataSourceHandler = new DataSourceHandler( m_observation.getMetadataList() );
    dataSourceHandler.addDataSource( m_itemIdentifier, m_repositoryId );

    return new SimpleObservation( m_observation.getHref(), m_observation.getName(), m_observation.getMetadataList(), model );
  }

  private boolean hasDataSouceAxis( )
  {
    final IAxis[] axes = m_observation.getAxisList();
    final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( axes );

    return dataSourceAxis != null;
  }

}
