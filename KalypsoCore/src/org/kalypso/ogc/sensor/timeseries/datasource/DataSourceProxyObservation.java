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
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * Data source proxy observation class. To add a data source axis to an model we have to create a new model. This is a
 * very slow operation, so we introduced this proxy class. the new model will be created by the first request.
 * 
 * @author Dirk Kuch
 */
public class DataSourceProxyObservation implements IObservation
{
  private final IObservation m_observation;

  /**
   * cached request arguments
   */
  private IRequest m_args;

  /**
   * cached model
   */
  private ITupleModel m_model;

  private final String m_itemIdentifier;

  private final String m_repositoryId;

  public DataSourceProxyObservation( final IObservation observation, final String itemIdentifier, final String repositoryId )
  {
    m_observation = observation;
    m_itemIdentifier = itemIdentifier;
    m_repositoryId = repositoryId;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getMetadataList()
   */
  @Override
  public MetadataList getMetadataList( )
  {
    final MetadataList metadata = m_observation.getMetadataList();
    final DataSourceHandler handler = new DataSourceHandler( metadata );
    if( !handler.containsDataSourceReferences() )
    {
      handler.addDataSource( m_itemIdentifier, m_repositoryId );
    }

    return metadata;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getAxisList()
   */
  @Override
  public IAxis[] getAxisList( )
  {
    final IAxis[] axes = m_observation.getAxisList();
    if( AxisUtils.findDataSourceAxis( axes ) == null )
    {
      return (IAxis[]) ArrayUtils.add( axes, new DefaultAxis( ITimeseriesConstants.TYPE_DATA_SRC, ITimeseriesConstants.TYPE_DATA_SRC, "", Integer.class, false ) );
    }

    return axes;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    if( args == null || args != m_args )
    {
      m_model = m_observation.getValues( args );
      if( !DataSourceHelper.hasDataSources( m_model ) )
      {
        // to assert a valid source reference!
        getMetadataList();

        final AddDataSourceModelHandler handler = new AddDataSourceModelHandler( m_model );
        m_model = handler.extend();
      }

      m_args = args;
    }

    return m_model;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#setValues(org.kalypso.ogc.sensor.ITupleModel)
   */
  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    m_observation.setValues( values );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#addListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void addListener( final IObservationListener listener )
  {
    m_observation.addListener( listener );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#removeListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void removeListener( final IObservationListener listener )
  {
    m_observation.removeListener( listener );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#fireChangedEvent(java.lang.Object)
   */
  @Override
  public void fireChangedEvent( final Object source )
  {
    m_observation.fireChangedEvent( source );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getName()
   */
  @Override
  public String getName( )
  {
    return m_observation.getName();
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getHref()
   */
  @Override
  public String getHref( )
  {
    return m_observation.getHref();
  }
}
